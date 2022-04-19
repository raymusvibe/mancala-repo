const url = "https://localhost";
const login_url = "https://localhost/login";

let stomp_client;
let stomp_client_heart_beat_rate = 30000

let player_triggered_connection = false;

let retry_count = 0;
let retry_limit = 10;
let retry_nominal_back_off = 500;
let retry_back_off = 2000;

var game_id;
var game;
var player_name;
var opponent_name;

function connect_to_socket() {
    if (stomp_client) {
        stomp_client.disconnect();
    }
    let socket = new SockJS(url + "/websocket");
    stomp_client = Stomp.over(socket);
    stomp_client.heartbeat.outgoing = stomp_client_heart_beat_rate;
    stomp_client.heartbeat.incoming = stomp_client_heart_beat_rate;
    //disable stomp debug logging to console
    stomp_client.debug = f => f;
    stomp_client.connect({}, function (frame) {
        stomp_client.subscribe("/topic/game-messaging." + game_id, function (response) {
            append_chat_message(JSON.parse(response.body));
        });
        stomp_client.subscribe("/topic/game-progress." + game_id, function (response) {
            game = JSON.parse(response.body);
            handle_gameplay_websocket_response();
        });
        enable_chat();
        //to notify other player to start
        if (player_triggered_connection) {
            player_triggered_connection = false;
            game_play();
        }
        //error scenario
        if (retry_count > 0) {
            game.gamePlayStatus = GameStatus.DISRUPTED;
            game_play();
            sync_board_with_ui_stones ();
            add_pot_handlers();
            update_game_parameters();
            retry_count = 0;
        }
    }, function(error) {
        error_connect_retry();
    });
    socket.onclose = function() {
        //server occasionally closes the connection abruptly
        error_connect_retry();
    };
}

function error_connect_retry() {
    if (retry_count < retry_limit && game.gamePlayStatus != GameStatus.NEW) {
        disable_chat();
        remove_pot_handlers();
        connect_retry_message();
        setTimeout(connect_to_socket, retry_nominal_back_off + retry_back_off * retry_count);
        retry_count++;
    } else {
        if (stomp_client) {
            game_error_ui_updates ();
            stomp_client.disconnect();
        }
    }
}

function create_game() {
    $.ajax({
        url: url + "/mancala/v1/start",
        type: 'GET',
        contentType: "application/json",
        success: function (data) {
            if (data.gameId) {
                game_id = data.gameId;
                game = data;
                retry_count = 0;
                connect_to_socket();
                ui_start_new_game();
                clear_chat_messages();
            } else {
                window.location.href = login_url;
            }
        },
        error: function (error) {
            if (stomp_client) {
                game_error_ui_updates();
                stomp_client.disconnect();
            }
        }
    });
}

function connect_to_specific_game() {
    let interim_game_id = document.getElementById("game_id").value;
    if (interim_game_id == null || interim_game_id === '') {
        missing_game_id_message();
        return;
    }
    $.ajax({
        url: url + "/mancala/v1/connect?gameId=" + interim_game_id,
        type: 'GET',
        contentType: "application/json",
        success: function (data) {
            if (data.gameId) {
                game_id = data.gameId;
                game = data;
                player_triggered_connection = true;
                retry_count = 0;
                connect_to_socket();
                ui_connect_to_game();
                clear_chat_messages();
            } else {
                window.location.href = login_url;
            }
        },
        error: function (error) {
            connect_to_game_error_message ();
        }
    });
}

function game_play() {
    stomp_client.send(
    "/app/gameplay." + game_id,
    {},
    JSON.stringify(
        {
            "gameId": game_id,
            "gamePlayStatus": game.gamePlayStatus,
            "selectedStoneContainerIndex": game.selectedStoneContainerIndex
        }
        )
    );
}

function send_chat_message () {
    let message = $("#chat_message").val();
    if (message == null || message === '') {
        empty_chat_text_error_message ();
        return;
    }
    stomp_client.send(
    "/app/messaging." + game_id,
    {},
    JSON.stringify(
        {
            "sender": player_name,
            "message": message
        }
        )
    );
}
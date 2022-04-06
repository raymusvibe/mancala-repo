const url = 'https://localhost:8080';

let stomp_client;
let player_triggered_connection = false;
let retry_count = 0;
let retry_limit = 10;
let retry_back_off = 2000;
let nominal_retry_back_off = 500;

function connect_to_socket() {
    let socket = new SockJS(url + "/websocket");
    stomp_client = Stomp.over(socket);
    stomp_client.connect({}, function (frame) {
        stomp_client.subscribe('/topic/game-messaging.' + game_id, function (response) {
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
    //allow game to disconnect if game status is not FINISHED and when retry limit is still to be reached
    if (game.gamePlayStatus != GameStatus.FINISHED && retry_count < retry_limit) {
        disable_chat();
        remove_pot_handlers();
        connect_retry_message();
        setTimeout(connect_to_socket, nominal_retry_back_off + retry_back_off * retry_count);
        retry_count++;
    } else {
        game_error_updates ();
        stomp_client.disconnect();
    }
}

function create_game() {
    $.ajax({
        url: url + "/mancala/v1/start",
        type: 'GET',
        contentType: "application/json",
        success: function (data) {
            game_id = data.gameId;
            game = data;            
            retry_count = 0;
            connect_to_socket();
            start_new_game();
            clear_chat_messages();
        },
        error: function (error) {
            game_error_updates();
            stomp_client.disconnect();
        }
    })
}

function connect_to_specific_game() {
    game_id = document.getElementById("game_id").value;
    if (game_id == null || game_id === '') {
        missing_game_id_message();
        return;
    }
    $.ajax({
        url: url + "/mancala/v1/connect?gameId=" + game_id,
        type: 'GET',
        contentType: "application/json",
        success: function (data) {
            game = data;        
            player_triggered_connection = true;
            retry_count = 0;
            connect_to_socket();
            connect_to_game();
            clear_chat_messages();
        },
        error: function (error) {
            alert("Connection to game failed. Please use a valid game ID or start a new game and share the game ID with a friend to play.");
        }
    })
}

function game_play() {
    stomp_client.send(
    "/app/gameplay." + game_id,
    {},
    JSON.stringify(game)
    );
}

function send_chat_message () {
    let message = $("#chat_message").val();
    if (message == null || message === '') {
        alert ("Please enter a message first to send.");
        return;
    }
    stomp_client.send(
    "/app/messaging." + game_id,
    {},
    JSON.stringify(
        {
            'sender': player_name,
            "message": message
        }
        )
    );
}
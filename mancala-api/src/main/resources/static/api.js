const url = 'http://localhost:8080';

let stomp_client;
let player_triggered_connection = false;

var game_id;
var game;
var player_name;
var opponent_name;

function connect_to_socket() {
    let socket = new SockJS(url + "/websocket");
    stomp_client = Stomp.over(socket);
    stomp_client.connect({}, function (frame) {
        enable_chat();
        console.log('Connected: ' + frame);
        stomp_client.subscribe('/topic/game-messaging.' + game_id, function (response) {
            append_chat_message(JSON.parse(response.body));
        });
        stomp_client.subscribe("/topic/game-progress." + game_id, function (response) {
            game = JSON.parse(response.body);
            action_response();
        });
        //to notify other player to start
        if (player_triggered_connection) {
            player_triggered_connection = false;
            game_play ();
        }
    }, function(error) {
        game_error_updates ();
        stomp_client.disconnect();
   });
    socket.onclose = function() {
        game_error_updates ();
        stomp_client.disconnect();
    };
}

function create_game() {
    $.ajax({
        url: url + "/mancala/v1/start",
        type: 'GET',
        contentType: "application/json",
        success: function (data) {
            game_id = data.gameId;
            game = data;
            player_name = Player.ONE;
            opponent_name = Player.TWO;
            connect_to_socket();
            initialise_new_game ();
            clear_chat_messages ();
        },
        error: function (error) {
            game_error_updates ();
            stomp_client.disconnect();
        }
    })
}

function connect_to_specific_game() {
    game_id = document.getElementById("game_id").value;
    if (game_id == null || game_id === '') {
        missing_game_id_message ();
        return;
    }
    $.ajax({
        url: url + "/mancala/v1/connect?gameId=" + game_id,
        type: 'GET',
        contentType: "application/json",
        success: function (data) {
            game_id = data.gameId;
            game = data;
            player_name = Player.TWO;
            opponent_name = Player.ONE;
            isPlayerOne = null;
            player_triggered_connection = true;
            connect_to_socket();
            initialise_new_game_connection();
            clear_chat_messages ();
        },
        error: function (error) {
            alert("Connection to game failed. Please use a valid game ID or start a new game.");
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
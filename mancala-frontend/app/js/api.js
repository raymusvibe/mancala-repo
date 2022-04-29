import $ from 'jquery';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import * as Constants from './constants.js';
import { Player, GameStatus } from './enums.js';
import { enable_chat, append_chat_message } from './presentation_library.js';
import * as utils from './game_helper.js';

'use strict';

let game_id;
let player_name;

let player_triggered_game_restart = false;
let player_triggered_game_connection = false;

let retry_count = 0;

let stomp_client;

function start_new_game () {
    $.ajax({
        url: Constants.api_url + "/mancala/v1/start",
        type: 'GET',
        contentType: "application/json",
        success: function (data) {
            if (data.gameId) {
                game_id = data.gameId
                //game creator is player one
                player_name = Player.ONE
                retry_count = 0;
                connect_to_socket();
                utils.ui_start_new_game(data);
            } else {
                window.location.href = Constants.api_url + "/login";
            }
        },
        error: function (error) {
            if (stomp_client) {
                utils.service_error();
                stomp_client.disconnect();
            }
        }
    });
}

function connect_to_specific_game () {
    let interim_game_id = document.getElementById("game_id").value;
    if (interim_game_id == null || interim_game_id === '') {
        utils.missing_game_id_message();
        return;
    }
    $.ajax({
        url: Constants.api_url + "/mancala/v1/connect?gameId=" + interim_game_id,
        type: 'GET',
        contentType: "application/json",
        success: function (data) {
            if (data.gameId) {
                game_id = data.gameId;
                player_triggered_game_connection = true;
                retry_count = 0;
                //game joiner is player two
                player_name = Player.TWO;
                connect_to_socket();
                utils.ui_connect_to_game(data);
            } else {
                window.location.href = Constants.api_url + "/login";;
            }
        },
        error: function (error) {
            utils.connect_to_game_error_message ();
        }
    });
}

function restart_game () {
    player_triggered_game_restart = true;
    utils.ui_restart_game();
    send_game_play_message (game_id, Constants.player_one_house_index, GameStatus.RESTARTING, stomp_client);
}

function send_game_play_message (g_id, selected_index, game_status, stmp_client) {
    stmp_client.send(
        "/app/gameplay." + g_id,
        {},
        JSON.stringify(
            {
                "gameId": g_id,
                "gamePlayStatus": game_status,
                "selectedStoneContainerIndex": selected_index
            }
            )
        );
}

function connect_to_socket () {
    if (stomp_client) {
        stomp_client.disconnect();
    }
    let socket = new SockJS(Constants.api_url + "/websocket");
    stomp_client = Stomp.over(socket);
    stomp_client.heartbeat.outgoing = Constants.stomp_client_heart_beat_rate;
    stomp_client.heartbeat.incoming = Constants.stomp_client_heart_beat_rate;
    stomp_client.debug = f => f;
    stomp_client.connect({}, function (frame) {
        stomp_client.subscribe("/topic/game-messaging." + game_id, function (response) {
            let data = JSON.parse(response.body);
            append_chat_message(data);
            utils.set_game_chat_message_input("");
        });
        stomp_client.subscribe("/topic/game-progress." + game_id, function (response) {
            let game = JSON.parse(response.body);
            utils.handle_gameplay_websocket_response(game, player_name, player_triggered_game_restart, stomp_client, send_game_play_message);
            if(player_triggered_game_restart) {
                player_triggered_game_restart = false;
            }
        });
        enable_chat();
        //to notify other player to start
        if (player_triggered_game_connection) {
            player_triggered_game_connection = false;
            send_game_play_message (game_id, Constants.player_one_house_index, GameStatus.IN_PROGRESS, stomp_client);
        }
        //error scenario
        if (retry_count > 0) {
            send_game_play_message (game_id, null, GameStatus.DISRUPTED, stomp_client);
            utils.connection_error (player_name, stomp_client, send_game_play_message);
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

function error_connect_retry () {
    if (retry_count < Constants.retry_limit) {
        utils.ui_error_connect_retry ();
        setTimeout(connect_to_socket, Constants.retry_nominal_back_off + Constants.retry_back_off * retry_count);
        retry_count++;
    } else {
        if (stomp_client) {
            utils.service_error();
            stomp_client.disconnect();
        }
    }
}

function send_chat_message () {
    let message = $("#chat_message").val();
    if (message == null || message === '') {
        utils.empty_chat_text_error_message ();
        return;
    }
    stomp_client.send(
    "/app/messaging." + game_id,
    {},
    JSON.stringify(
        {
            "sender": player_name,
            "textMessage": message
        }
        )
    );
}

export {
    start_new_game, 
    connect_to_specific_game, 
    restart_game,
    send_chat_message
}
import $ from 'jquery';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import * as Constants from './constants.js';
import { Player, GameStatus } from './enums.js';
import { Pot, toggle_player_buttons_selection, remove_pot_handlers, 
    enable_chat, disable_chat, append_chat_message } from './presentation_library.js';
import * as utils from './game_helper.js';
import { sow_stones, sync_board_with_ui_stones } from './sowing.js';

'use strict';

let game;
let game_id;
let player_name;
let is_player_one = true;
let player_initiated_game_restart = false;

let stomp_client;

let player_triggered_connection = false;
let retry_count = 0;

function start_new_game () {
    $.ajax({
        url: Constants.api_url + "/mancala/v1/start",
        type: 'GET',
        contentType: "application/json",
        success: function (data) {
            if (data.gameId) {
                game_id = data.gameId
                data = data;
                retry_count = 0;
                connect_to_socket();
                player_name = Player.ONE
                utils.ui_start_new_game(game_id);
            } else {
                window.location.href = Constants.api_url + "/login";
            }
        },
        error: function (error) {
            if (stomp_client) {
                utils.game_error_ui_updates();
                disable_chat();
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
                game = data;
                player_triggered_connection = true;
                retry_count = 0;
                connect_to_socket();
                is_player_one = true; 
                player_name = Player.TWO;
                utils.ui_connect_to_game();
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
    utils.ui_restart_game();
    is_player_one = false;
    game.activePlayer = Player.TWO;
    player_initiated_game_restart = true;
    game.gamePlayStatus = GameStatus.RESTARTING;
    game.selectedStoneContainerIndex = Constants.player_one_house_index;
    send_game_play_message ();
}

function send_game_play_message (g_id, selected_index, stmp_client) {
    if (g_id) {
        stmp_client.send(
            "/app/gameplay." + g_id,
            {},
            JSON.stringify(
                {
                    "gameId": g_id,
                    "gamePlayStatus": GameStatus.IN_PROGRESS,
                    "selectedStoneContainerIndex": selected_index
                }
                )
            );
    } else {
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
            let responseMessage = JSON.parse(response.body);
            append_chat_message(responseMessage);
            utils.set_game_chat_message_input("");
        });
        stomp_client.subscribe("/topic/game-progress." + game_id, function (response) {
            game = JSON.parse(response.body);
            handle_gameplay_websocket_response();
        });
        enable_chat();
        //to notify other player to start
        if (player_triggered_connection) {
            player_triggered_connection = false;
            send_game_play_message();
        }
        //error scenario
        if (retry_count > 0) {
            game.gamePlayStatus = GameStatus.DISRUPTED;
            send_game_play_message();
            sync_board_with_ui_stones (game);
            utils.add_pot_handlers(send_game_play_message, game_id, stomp_client, is_player_one, player_name);
            update_game_parameters();
            toggle_player_buttons_selection(is_player_one, player_name);
            utils.add_pot_handlers(send_game_play_message, game_id, stomp_client, is_player_one, player_name);
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
    if (retry_count < Constants.retry_limit && game.gamePlayStatus != GameStatus.NEW) {
        disable_chat();
        remove_pot_handlers();
        utils.connect_retry_message();
        setTimeout(connect_to_socket, Constants.retry_nominal_back_off + Constants.retry_back_off * retry_count);
        retry_count++;
    } else {
        if (stomp_client) {
            utils.game_error_ui_updates ();
            disable_chat();
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
  
function handle_gameplay_websocket_response () {
    let player_one_house_count = game.mancalaBoard[Constants.player_one_house_index].stones;
    let player_two_house_count = game.mancalaBoard[Constants.player_two_house_index].stones;
    if (game.gamePlayStatus === GameStatus.FINISHED) {
        utils.display_game_outcome(game, player_name);
        utils.game_over_ui_updates(player_one_house_count, player_two_house_count);
        remove_pot_handlers();
        return;
    }
    if (game.gamePlayStatus === GameStatus.RESTARTING && !player_initiated_game_restart) {
        game.gamePlayStatus = GameStatus.IN_PROGRESS;
        is_player_one = false;
        game.activePlayer = Player.TWO;
        utils.handle_game_restart_request ();
        //restart sets player two as active
        if (player_name === Player.ONE) {
            utils.player_one_restart_message ();
            return;
        }
    } else {
      if (game.gamePlayStatus != GameStatus.IN_PROGRESS) {
            game.gamePlayStatus = GameStatus.IN_PROGRESS;
            player_initiated_game_restart = false;
      }
    }
    let src_pot = new Pot(Constants.map_board_to_pots[game.selectedStoneContainerIndex]);
    const number_of_children = src_pot.$().children().length;
    sow_stones (src_pot, null, is_player_one, game);
    setTimeout(update_game_parameters, Constants.sowing_interval * (number_of_children + 1));
  }

function update_game_parameters () {
    if(game.gamePlayStatus != GameStatus.FINISHED) {
        let message;
    
        if (game.activePlayer === Player.ONE) {
            is_player_one = true;
            message = (player_name === Player.ONE)? Constants.player_one_turn_message_string : Constants.opponent_turn_message_string;
            utils.set_game_status_message(message);
        } else {
            is_player_one = false;
            message = (player_name == Player.TWO)? Constants.player_two_turn_message_string : Constants.opponent_turn_message_string;
            utils.set_game_status_message(message);
        }
        toggle_player_buttons_selection(is_player_one, player_name);
        utils.add_pot_handlers(send_game_play_message, game_id, stomp_client, is_player_one, player_name);
        let player_one_house_count = game.mancalaBoard[Constants.player_one_house_index].stones;
        let player_two_house_count = game.mancalaBoard[Constants.player_two_house_index].stones;
        utils.update_house_counters(player_one_house_count, player_two_house_count);
    }
}

export {
    start_new_game, 
    connect_to_specific_game, 
    restart_game,
    send_chat_message
}
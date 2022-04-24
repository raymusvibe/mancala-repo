import $ from 'jquery';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import * as Constants from './constants.js';
import { Player, GameStatus } from './enums.js';
import { Pot, toggle_player_buttons_selection, 
    remove_pot_handlers, enable_chat, disable_chat } from './presentation_library.js';
import * as utils from './game_helper.js';
import '../css/style.css';
import '../images/background.png';

'use strict';

let game;
let game_id;
let player_name;
let is_player_one = true;
let player_initiated_game_restart = false;

let stomp_client;
let stomp_client_heart_beat_rate = 30000
let player_triggered_connection = false;
let retry_count = 0;

const chat_key_down = (event) => {
    if (event.key === "Enter") {
        send_chat_message();
    }
}

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
                disable_chat(chat_key_down);
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

function connect_to_socket () {
    if (stomp_client) {
        stomp_client.disconnect();
    }
    let socket = new SockJS(Constants.api_url + "/websocket");
    stomp_client = Stomp.over(socket);
    stomp_client.heartbeat.outgoing = stomp_client_heart_beat_rate;
    stomp_client.heartbeat.incoming = stomp_client_heart_beat_rate;
    stomp_client.debug = f => f;
    stomp_client.connect({}, function (frame) {
        stomp_client.subscribe("/topic/game-messaging." + game_id, function (response) {
            let responseMessage = JSON.parse(response.body);
            utils.append_chat_message(responseMessage);
        });
        stomp_client.subscribe("/topic/game-progress." + game_id, function (response) {
            game = JSON.parse(response.body);
            handle_gameplay_websocket_response();
        });
        enable_chat(chat_key_down);
        //to notify other player to start
        if (player_triggered_connection) {
            player_triggered_connection = false;
            send_game_play_message();
        }
        //error scenario
        if (retry_count > 0) {
            game.gamePlayStatus = GameStatus.DISRUPTED;
            send_game_play_message();
            utils.sync_board_with_ui_stones (game);
            utils.add_pot_handlers(send_game_play_message, game_id, stomp_client, is_player_one, player_name);
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

function error_connect_retry () {
    if (retry_count < Constants.retry_limit && game.gamePlayStatus != GameStatus.NEW) {
        disable_chat(chat_key_down);
        remove_pot_handlers();
        utils.connect_retry_message();
        setTimeout(connect_to_socket, Constants.retry_nominal_back_off + Constants.retry_back_off * retry_count);
        retry_count++;
    } else {
        if (stomp_client) {
            utils.game_error_ui_updates ();
            disable_chat(chat_key_down);
            stomp_client.disconnect();
        }
    }
}

/* Main method for sowing */
function sow_stones (src_pot, last_pot) {
    const children = src_pot.$().children();
    if(children.length === 0) {
        setTimeout(complete_turn, Constants.sowing_interval);
        return;
    }
    if(last_pot === null) {
        last_pot = src_pot;
    }
    let selected_stone = children.get(0);
    //reverse stone deque order when sowing has come all way back to same pot
    if ($(selected_stone).attr('hasMoved') == "true" && children.length > 1) {
        selected_stone = children.get(children.length - 1);
    }
    last_pot = last_pot.getNextSown(is_player_one);
    if(children.length == 1 &&
        is_player_one === last_pot.isTop() &&
        last_pot.$().children().length === 0 &&
        !last_pot.isMan())
    {
        utils.steal(last_pot, selected_stone, is_player_one);
    } else {
        if(children.length == 1) {
            //cater for last stone in the event it has already been moved
            if ($(selected_stone).attr('hasMoved') == "true") {
                $(selected_stone).attr('hasMoved', false);
                setTimeout(complete_turn, Constants.sowing_interval);
                return;
            } else {
                if (last_pot.id == src_pot.id) {
                    if (is_player_one === last_pot.isTop() &&
                    !last_pot.isMan())
                    {
                        utils.steal(last_pot, selected_stone, is_player_one);
                    }
                    setTimeout(complete_turn, Constants.sowing_interval);
                    return;
                }
                utils.move_stone(selected_stone, last_pot);
            }
        } else {
            if (last_pot.id == src_pot.id) {
                //tag the selected stone and move the next stone
                $(selected_stone).attr('hasMoved', true);
                let next_stone = children.get(1);
                last_pot = last_pot.getNextSown(is_player_one);
                utils.move_stone(next_stone, last_pot);
            } else {
                if ($(selected_stone).attr('hasMoved') != "true") {
                    utils.move_stone(selected_stone, last_pot);
                }
            }
        }
    }
    setTimeout(sow_stones, Constants.sowing_interval, src_pot, last_pot);
}
  
/* Runs at the end of each player's turn */
function complete_turn () {
    if (utils.is_board_and_game_ui_misaligned (game)) {
        utils.sync_board_with_ui_stones (game);
    }
    update_game_parameters();
    let player_one_house_count = game.mancalaBoard[Constants.player_one_house_index].stones;
    let player_two_house_count = game.mancalaBoard[Constants.player_two_house_index].stones;
    utils.update_house_counters(player_one_house_count, player_two_house_count);
}
  
/* Executed when there is an update from the server */
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
    sow_stones (new Pot(Constants.map_board_to_pots[game.selectedStoneContainerIndex]), null);
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
    }
}
  
const game_connect_key_down = (event) => {
    if (event.key === "Enter") {
        connect_to_specific_game();
        utils.set_game_id_input("");
    }
}

function on_page_load () {
    utils.set_game_status_message(Constants.on_page_load_string);
    Constants.game_id_input.addEventListener("keydown", game_connect_key_down);
    if (window.location.hash == "#_=_") {
        window.location.hash = "";
    }
}

document.addEventListener ("DOMContentLoaded", function()
{
    on_page_load();
    utils.populate_row("pt");
    utils.populate_row("pb");
    document.getElementById ("new_game_button").addEventListener ("click", start_new_game, false);
    document.getElementById ("connect_to_game_button").addEventListener ("click", connect_to_specific_game, false);
    document.getElementById ("game_restart_button").addEventListener ("click", restart_game, false);
 });
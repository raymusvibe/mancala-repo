import $ from 'jquery';
import * as Constants from './constants.js';
import { Player, GameStatus, GameWinner } from './enums.js';
import { Pot, colors, place_new_stone, remove_action_handlers, disable_chat,
    add_listeners, toggle_player_buttons_selection, remove_pot_handlers } from './presentation_library.js';
import { sow_stones, sync_board_with_ui_stones } from './sowing.js';

'use strict';

let game;
let is_player_one;

function ui_start_new_game (game_from_api) {
    game = game_from_api;
    is_player_one = true;
    Constants.new_game_button.classList.add("disable");
    Constants.connect_to_game_button.classList.add("disable");
    Constants.game_restart_button.classList.add("disable");
    Constants.game_id_input.disabled = true;
    Constants.game_status_message.innerHTML = "Ask friend to join with " + game.gameId;
    clear_chat_messages();
}

function ui_connect_to_game (game_from_api) {
    game = game_from_api;
    is_player_one = true;
    Constants.new_game_button.classList.add("disable");
    Constants.connect_to_game_button.classList.add("disable");
    Constants.game_restart_button.classList.add("disable");
    Constants.game_id_input.disabled = true;
    Constants.game_status_message.innerHTML = Constants.connect_to_game_string;
    Constants.game_id_input.value = "";
    clear_chat_messages();
}

function ui_restart_game () {
    is_player_one = false;
    Constants.new_game_button.classList.add("disable");
    Constants.connect_to_game_button.classList.add("disable");
    Constants.game_restart_button.classList.add("disable");
    Constants.game_id_input.disabled = true;
    reset_board();
}

function ui_error_connect_retry () {
    disable_chat();
    remove_pot_handlers();
    connect_retry_message();
}

function connection_error (player_name, stomp_client, send_game_play_message) {
    sync_board_with_ui_stones (game);
    add_pot_handlers(player_name, stomp_client, send_game_play_message);
    update_game_parameters();
    toggle_player_buttons_selection(is_player_one, player_name);
}

function update_house_counters () {
    let player_one_house_count = game.mancalaBoard[Constants.player_one_house_index].stones;
    let player_two_house_count = game.mancalaBoard[Constants.player_two_house_index].stones;
    document.getElementById("player_one_house_count").innerHTML = "Player one house count: " + player_one_house_count;
    document.getElementById("player_two_house_count").innerHTML = "Player two house count: " + player_two_house_count;
}

function reset_board () {
    $("div.stone").remove();
    update_house_counters();
    populate_row("pt");
    populate_row("pb");
}

function handle_game_restart_request () {
    Constants.new_game_button.classList.add("disable");
    Constants.connect_to_game_button.classList.add("disable");
    Constants.game_restart_button.classList.add("disable");
    Constants.game_id_input.disabled = true;
    reset_board();
}

function game_over () {
    Constants.new_game_button.classList.remove("disable");
    Constants.connect_to_game_button.classList.remove("disable");
    Constants.game_restart_button.classList.remove("disable");
    Constants.game_id_input.disabled = false;
    reset_board();
}

function service_error () {
    $("div.message").remove();
    Constants.new_game_button.classList.remove("disable");
    Constants.connect_to_game_button.classList.remove("disable");
    Constants.game_restart_button.classList.add("disable");
    Constants.game_id_input.disabled = false;
    reset_board();
    game_error_message();
    disable_chat();
}

function populate_row (row) {
    let n = 0;
    for(let c = 0; c < 6; c++) {
        for(let i = 1; i <= 6; i++,n++) {
            place_new_stone(row + i,colors[n % colors.length]);
        }
    }
}

 function display_game_outcome (player_name) {
    let player_one_stones = game.mancalaBoard[Constants.player_one_house_index].stones;
    let player_two_stones = game.mancalaBoard[Constants.player_two_house_index].stones;
    let winning_total = (game.winner == GameWinner.PLAYER_ONE)? player_one_stones : player_two_stones;
    let game_outcome_string;
    
    if (game.winner == GameWinner.DRAW) {
        game_outcome_string = Constants.draw_message_string;
    } else if (game.winner == GameWinner.PLAYER_ONE) {
        if (player_name == Player.ONE) {
            game_outcome_string = construct_game_winner_message(winning_total);
        } else {
            game_outcome_string = construct_game_loser_message(winning_total);
        }
    } else {
        if (player_name == Player.TWO) {
            game_outcome_string = construct_game_winner_message(winning_total);
        } else {
            game_outcome_string = construct_game_loser_message(winning_total);
        }
    }
    Constants.game_status_message.innerHTML = game_outcome_string;
}

function add_pot_handlers (player_name, stomp_client, send_game_play_message) {
  if (is_player_one) {
      if (player_name === Player.ONE) {
        remove_action_handlers (".topmid .pot");
        add_listeners (game.gameId, ".topmid .pot", stomp_client, send_game_play_message);
      } else {
      remove_action_handlers (".botmid .pot");
      }
  } else {
      if (player_name === Player.TWO) {
        remove_action_handlers (".botmid .pot");
        add_listeners (game.gameId, ".botmid .pot", stomp_client, send_game_play_message)
      } else {
        remove_action_handlers (".topmid .pot");
      }
  }
}

function handle_gameplay_websocket_response (game_from_api, player_name, player_triggered_game_restart, stomp_client, send_game_play_message) {
    game = game_from_api;
    if (game.gamePlayStatus === GameStatus.FINISHED) {
        display_game_outcome(player_name);
        game_over();
        remove_pot_handlers();
        return;
    }
    if (game.gamePlayStatus === GameStatus.RESTARTING && !player_triggered_game_restart) {
        is_player_one = false;
        handle_game_restart_request ();
        //restart sets player two as active
        if (player_name === Player.ONE) {
            player_one_restart_message ();
            return;
        }
    } 
    let src_pot = new Pot(Constants.map_board_to_pots[game.selectedStoneContainerIndex]);
    const number_of_children = src_pot.$().children().length;
    sow_stones (src_pot, null, is_player_one, game);
    setTimeout(update_game_parameters, Constants.sowing_interval * (number_of_children + 1), player_name, stomp_client, send_game_play_message);
  }

function update_game_parameters (player_name, stomp_client, send_game_play_message) {
    if(game.gamePlayStatus != GameStatus.FINISHED) {
        let message;
        if (game.activePlayer === Player.ONE) {
            is_player_one = true;
            message = (player_name === Player.ONE)? Constants.player_one_turn_message_string : Constants.opponent_turn_message_string;
            set_game_status_message(message);
        } else {
            is_player_one = false;
            message = (player_name == Player.TWO)? Constants.player_two_turn_message_string : Constants.opponent_turn_message_string;
            set_game_status_message(message);
        }
        toggle_player_buttons_selection(is_player_one, player_name);
        add_pot_handlers (player_name, stomp_client, send_game_play_message)
        update_house_counters();
    }
}

function clear_chat_messages () {
    $("div.message").remove();
}

function set_game_id_input (input) {
    Constants.game_id_input.value = input;
}

function set_game_chat_message_input (input) {
    Constants.chat_message_input.value = input;
}

function set_game_status_message (msg) {
    Constants.game_status_message.innerHTML = msg;
}

function player_one_restart_message () {
    Constants.game_status_message.innerHTML = Constants.player_one_restart_string;
}

function missing_game_id_message () {
    Constants.game_status_message.innerHTML = Constants.missing_game_id_string;
}

function game_error_message () {
    Constants.game_status_message.innerHTML = Constants.game_error_string;
}

function connect_retry_message () {
    Constants.game_status_message.innerHTML = Constants.connect_retry_string;
}

function connect_to_game_error_message () {
    Constants.game_status_message.innerHTML = Constants.connect_to_game_error_message_string;
}

function empty_chat_text_error_message () {
    Constants.game_status_message.innerHTML = Constants.empty_chat_text_error_message_string;
}

function construct_game_winner_message (winning_total) {
    return Constants.winning_message_string + winning_total + "!";
}

function construct_game_loser_message (winning_total) {
    return Constants.losing_message_string + winning_total + ".";
}

export {
  set_game_id_input,
  set_game_status_message,
  missing_game_id_message,
  connect_to_game_error_message,
  empty_chat_text_error_message,
  ui_start_new_game,
  ui_connect_to_game,
  ui_restart_game,
  ui_error_connect_retry,
  connection_error,
  service_error,
  populate_row,
  set_game_chat_message_input,
  handle_gameplay_websocket_response
}
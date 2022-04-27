import $ from 'jquery';
import * as Constants from './constants.js';
import { Player, GameWinner } from './enums.js';
import { colors, place_new_stone, remove_action_handlers, add_listeners } from './presentation_library.js';

'use strict';

function ui_start_new_game (game_id) {
    Constants.new_game_button.classList.add("disable");
    Constants.connect_to_game_button.classList.add("disable");
    Constants.game_restart_button.classList.add("disable");
    Constants.game_id_input.disabled = true;
    Constants.game_status_message.innerHTML = "Ask friend to join with " + game_id;
    clear_chat_messages();
}

function ui_connect_to_game () {
    Constants.new_game_button.classList.add("disable");
    Constants.connect_to_game_button.classList.add("disable");
    Constants.game_restart_button.classList.add("disable");
    Constants.game_id_input.disabled = true;
    Constants.game_status_message.innerHTML = Constants.connect_to_game_string;
    Constants.game_id_input.value = "";
    clear_chat_messages();
}

function ui_restart_game (player_one_house_count, player_two_house_count) {
    Constants.new_game_button.classList.add("disable");
    Constants.connect_to_game_button.classList.add("disable");
    Constants.game_restart_button.classList.add("disable");
    Constants.game_id_input.disabled = true;
    reset_ui_board(player_one_house_count, player_two_house_count);
}

function update_house_counters (player_one_house_count, player_two_house_count) {
    if(player_one_house_count) {
        document.getElementById("player_one_house_count").innerHTML = "Player one house count: " + player_one_house_count;
        document.getElementById("player_two_house_count").innerHTML = "Player two house count: " + player_two_house_count;
    }
}

function reset_ui_board (player_one_house_count, player_two_house_count) {
    $("div.stone").remove();
    update_house_counters(player_one_house_count, player_two_house_count);
    populate_row("pt");
    populate_row("pb");
}

function handle_game_restart_request (player_one_house_count, player_two_house_count) {
    Constants.new_game_button.classList.add("disable");
    Constants.connect_to_game_button.classList.add("disable");
    Constants.game_restart_button.classList.add("disable");
    Constants.game_id_input.disabled = true;

    reset_ui_board(player_one_house_count, player_two_house_count);
}

function game_over_ui_updates (player_one_house_count, player_two_house_count) {
    Constants.new_game_button.classList.remove("disable");
    Constants.connect_to_game_button.classList.remove("disable");
    Constants.game_restart_button.classList.remove("disable");
    Constants.game_id_input.disabled = false;
    reset_ui_board(player_one_house_count, player_two_house_count);
}

function game_error_ui_updates (player_one_house_count, player_two_house_count) {
    $("div.message").remove();
    Constants.new_game_button.classList.remove("disable");
    Constants.connect_to_game_button.classList.remove("disable");
    Constants.game_restart_button.classList.add("disable");
    Constants.game_id_input.disabled = false;
    reset_ui_board(player_one_house_count, player_two_house_count);
    game_error_message();
}

function populate_row (row) {
    let n = 0;
    for(let c = 0; c < 6; c++) {
        for(let i = 1; i <= 6; i++,n++) {
            place_new_stone(row + i,colors[n % colors.length]);
        }
    }
}

 function display_game_outcome (game, player_name) {
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

function add_pot_handlers (send_game_play_message, game_id, stomp_client, is_player_one, player_name) {
  if (is_player_one) {
      if (player_name === Player.ONE) {
        remove_action_handlers (".topmid .pot");
        add_listeners (".topmid .pot", send_game_play_message, game_id, stomp_client);
      } else {
      remove_action_handlers (".botmid .pot");
      }
  } else {
      if (player_name === Player.TWO) {
        remove_action_handlers (".botmid .pot");
        add_listeners (".botmid .pot", send_game_play_message, game_id, stomp_client);
      } else {
        remove_action_handlers (".topmid .pot");
      }
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
  player_one_restart_message,
  missing_game_id_message,
  game_error_message,
  game_over_ui_updates,
  connect_retry_message,
  connect_to_game_error_message,
  empty_chat_text_error_message,
  ui_start_new_game,
  ui_connect_to_game,
  ui_restart_game,
  handle_game_restart_request,
  update_house_counters,
  game_error_ui_updates,
  populate_row,
  display_game_outcome,
  add_listeners,
  add_pot_handlers,
  set_game_chat_message_input
}
import $ from 'jquery';
import * as Constants from './constants.js';
import { Player, GameWinner } from './enums.js';
import { Pot, colors, place_new_stone, position_stone, remove_action_handlers } from './presentation_library.js';

'use strict';

function clear_chat_messages() {
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

function player_one_restart_message() {
    Constants.game_status_message.innerHTML = Constants.player_one_restart_string;
}

function missing_game_id_message() {
    Constants.game_status_message.innerHTML = Constants.missing_game_id_string;
}

function game_error_message() {
    Constants.game_status_message.innerHTML = Constants.game_error_string;
}

function connect_retry_message() {
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

function ui_start_new_game(game_id) {;
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

function populate_row(row) {
    let n = 0;
    for(let c = 0; c < 6; c++) {
        for(let i = 1; i <= 6; i++,n++) {
            place_new_stone(row + i,colors[n % colors.length]);
        }
    }
}

function update_house_counters(player_one_house_count, player_two_house_count) {
    if(player_one_house_count) {
        document.getElementById("player_one_house_count").innerHTML = "Player one house count: " + player_one_house_count;
        document.getElementById("player_two_house_count").innerHTML = "Player two house count: " + player_two_house_count;
    }
}

function reset_ui_board(player_one_house_count, player_two_house_count) {
    $("div.stone").remove();
    update_house_counters(player_one_house_count, player_two_house_count);
    populate_row("pt");
    populate_row("pb");
}

function ui_restart_game (player_one_house_count, player_two_house_count) {
    Constants.new_game_button.classList.add("disable");
    Constants.connect_to_game_button.classList.add("disable");
    Constants.game_restart_button.classList.add("disable");
    Constants.game_id_input.disabled = true;
    reset_ui_board(player_one_house_count, player_two_house_count);
}

function handle_game_restart_request(player_one_house_count, player_two_house_count) {
    Constants.new_game_button.classList.add("disable");
    Constants.connect_to_game_button.classList.add("disable");
    Constants.game_restart_button.classList.add("disable");
    Constants.game_id_input.disabled = true;

    reset_ui_board(player_one_house_count, player_two_house_count);
}

function game_over_ui_updates(player_one_house_count, player_two_house_count) {
    Constants.new_game_button.classList.remove("disable");
    Constants.connect_to_game_button.classList.remove("disable");
    Constants.game_restart_button.classList.remove("disable");
    Constants.game_id_input.disabled = false;
    reset_ui_board(player_one_house_count, player_two_house_count);
}

function game_error_ui_updates(player_one_house_count, player_two_house_count) {
    $("div.message").remove();
    Constants.new_game_button.classList.remove("disable");
    Constants.connect_to_game_button.classList.remove("disable");
    Constants.game_restart_button.classList.add("disable");
    Constants.game_id_input.disabled = false;
    reset_ui_board(player_one_house_count, player_two_house_count);
    game_error_message();
}

function map_pots_to_board(src_pot) {
    if (src_pot.isMan()) {
        throw "Invalid selection made";
    }
    if (src_pot.isTop()) {
        return src_pot.getNumber() - 1;
    } else {
        return src_pot.getNumber() + Constants.player_one_house_index;
    }
}

function append_chat_message(response) {
    let display_name = (response.sender == Player.ONE)? "Player One#" : "Player Two#";
    let content = '<div class="message">'
                  + '<p class="chat_display">' + display_name + ' :> ' + response.textMessage + '</p>'
                  + '</div>'

    $("#chat_messages").append(content);
    set_game_chat_message_input("");
}

function is_board_and_game_ui_misaligned (game) {
    for (let i = 0; i <= Constants.player_two_house_index; i++ ) {
        let src_pot = new Pot(Constants.map_board_to_pots[i]);
        let children = src_pot.$().children();
        if(children.length != game.mancalaBoard[i].stones) {
          return true;
        }
    }
    return false;
}

function sync_board_with_ui_stones (game) {
    $("div.stone").remove();
    let n = 0;
    for (let i = 0; i <= Constants.player_two_house_index; i++) {
        for(let j = 0; j < game.mancalaBoard[i].stones; j++, n++)
        {
          place_new_stone(Constants.map_board_to_pots[i], colors[n % colors.length]);
        }
    }
}

/* Moves stones */
function move_stone(stone, dest_pot)
{
    position_stone(stone, dest_pot);
    $(stone).appendTo(dest_pot.$());
}

/* Steal from opponent */
function steal(last_pot, selected_stone, is_player_one) {
    let opposite_pot = last_pot.getOpposite();
    const house_pot = (is_player_one)? new Pot('mt') : new Pot('mb');
    opposite_pot.$().children().each(function(idx,stolen_stone)
    {
        move_stone(stolen_stone, house_pot);
    });
    move_stone(selected_stone, house_pot);
 }

 function display_game_outcome(game, player_name) {
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

function add_listeners(class_list, send_game_play_message, game_id, stomp_client) {
  $(class_list).on("mouseenter", function()
  {
      $(this).css( {
      "background-color":"rgba(255, 255, 255, 0.40)",
      "cursor":"pointer"
      });
  }).on("mouseleave", function()
  {
      $(this).css( {
      "background-color":"rgba(255, 255, 255, 0.15)",
      "cursor":"arrow"
      });
  }).on("click", function()
  {
      // check if move is valid
      $(class_list).off();
      let src_pot = new Pot($(this).attr("id"));
      src_pot.$().css("background-color","rgba(255, 255, 255, 0.15)");
      send_game_play_message (game_id, map_pots_to_board(src_pot), stomp_client);
  });
}

function add_pot_handlers(send_game_play_message, game_id, stomp_client, is_player_one, player_name) {
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
  map_pots_to_board,
  append_chat_message,
  populate_row,
  is_board_and_game_ui_misaligned,
  sync_board_with_ui_stones,
  move_stone,
  steal,
  display_game_outcome,
  add_listeners,
  add_pot_handlers
}
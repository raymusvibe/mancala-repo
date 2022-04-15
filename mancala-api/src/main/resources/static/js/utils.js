const game_status_message = document.getElementById("game_status");
const new_game_button = document.getElementById("new_game_button");
const connect_to_game_button = document.getElementById("connect_to_game_button");
const game_restart_button = document.getElementById("game_restart_button");
const player_one_button = document.getElementById("player_one_button");
const player_two_button = document.getElementById("player_two_button");
const chat_heading = document.getElementById("chat_heading");
const chat_controls = document.getElementById("chat_controls");
const chat_messages = document.getElementById("chat_messages");
const chat_message_input = document.getElementById("chat_message");
const game_id_input = document.getElementById("game_id");

const player_one_restart_string = "Restarting the game. Player two will start.";
const on_page_load_string = "Please start a new game or enter a game id from a friend to play.";
const missing_game_id_string = "Please enter game id to connect.";
const game_error_string = "Unable to continue previous game, please start new game or join another.";
const connect_retry_string = "Connection to service lost. Trying to reconnect, please wait...";
const connect_to_game_string = "Success! Waiting for player one to start, you're player two.";
const winning_message_string = "Congratulations, you won with a total of ";
const losing_message_string = "Sorry, you lost. Your opponent won with a total of ";
const draw_message_string = "The game is a draw!";
const player_one_turn_message_string = "It is your turn, player one.";
const player_two_turn_message_string = "It is your turn, player two.";
const opponent_turn_message_string = "Waiting on the other player to complete their turn.";
const game_state_correction_message_string = "The game state has been updated, please continue.";
const game_connection_error_message_string = "Invalid game ID provided. A game ID can only be used once.";
const empty_chat_text_error_message_string = "Please enter a message first to send.";

const chat_key_down = (event) => {
  if (event.key === "Enter") {
    send_chat_message();
  }
}

const game_connect_key_down = (event) => {
  if (event.key === "Enter") {
    connect_to_specific_game();
    game_id_input.value = "";
  }
}

function enable_game_connect_key_down() {
  game_id_input.addEventListener("keydown", game_connect_key_down);
}

function ui_start_new_game() {
  player_name = Player.ONE;
  opponent_name = Player.TWO;
  
  new_game_button.classList.add("disable");
  connect_to_game_button.classList.add("disable");
  game_restart_button.classList.add("disable");
  game_id_input.disabled = true;
  game_status_message.innerHTML = "Ask friend to join with " + game_id;
}

function ui_connect_to_game () {
    player_name = Player.TWO;
    opponent_name = Player.ONE;
    is_player_one = null;

    new_game_button.classList.add("disable");
    connect_to_game_button.classList.add("disable");
    game_restart_button.classList.add("disable");
    game_id_input.disabled = true;
    game_status_message.innerHTML = connect_to_game_string;
    game_id_input.value = "";
}

function restart_game() {
  player_initiated_game_restart = true;
  
  new_game_button.classList.add("disable");
  connect_to_game_button.classList.add("disable");
  game_restart_button.classList.add("disable");
  game_id_input.disabled = true;

  reset_board();
  game.gamePlayStatus = GameStatus.RESTARTING;
  game.selectedStoneContainerIndex = player_one_house_index;
  game_play ();
}

function handle_game_restart_request() {
  new_game_button.classList.add("disable");
  connect_to_game_button.classList.add("disable");
  game_restart_button.classList.add("disable");
  game_id_input.disabled = true;

  reset_board();
  game.gamePlayStatus = GameStatus.IN_PROGRESS;
}

function reset_board() {
  $("div.stone").remove();
  is_player_one = false;
  game.activePlayer = Player.TWO;
  update_house_counters(true);
  populate_row("pt");
  populate_row("pb");
}

function reset_stones() {
  for (let i = 0; i <= player_two_house_index; i++) {
    if (i == player_one_house_index || i == player_two_house_index) {
      game.mancalaBoard[i].stones = 0;
    } else {
      game.mancalaBoard[i].stones = number_of_stones_per_pot;
    }
  }
}

function update_house_counters(is_new_game) {
  if (is_new_game) {
    reset_stones ();
  }
  let player_one_house_count = game.mancalaBoard[player_one_house_index].stones;
  let player_two_house_count = game.mancalaBoard[player_two_house_index].stones;
  document.getElementById("player_one_house_count").innerHTML = "Player one house count: " + player_one_house_count;
  document.getElementById("player_two_house_count").innerHTML = "Player two house count: " + player_two_house_count;
}

function update_game_parameters() {
  if(game.gamePlayStatus != GameStatus.FINISHED) {
    let message;
    if (game.activePlayer == Player.ONE) {
      is_player_one = true;
      message = (player_name == Player.ONE)? player_one_turn_message_string : opponent_turn_message_string;
      game_status_message.innerHTML = message;
    } else {
      is_player_one = false;
      message = (player_name == Player.TWO)? player_two_turn_message_string : opponent_turn_message_string;
      game_status_message.innerHTML = message;
    }
    toggle_player_buttons_selection();
    add_pot_handlers();
  }
}

function is_game_over() {
  let is_game_over = true;
  for (let i = 0; i < player_one_house_index; i++) {
    if (game.mancalaBoard[i].stones > 0) {
      is_game_over = false;
      break;
    }
  }
  if (is_game_over) {
    return is_game_over;
  }
  is_game_over = true;
  for (let i = player_one_house_index + 1; i < player_two_house_index; i++) {
    if (game.mancalaBoard[i].stones > 0){
      is_game_over = false;
      break;
    }
  }
  return is_game_over;
}

function determine_winner() {
  remove_pot_handlers();
  let player_one_count = 0;
  for (let i = 0; i <= player_one_house_index; i++) {
    player_one_count += game.mancalaBoard[i].stones;
  }
  let player_two_count = 0;
  for (let i = player_one_house_index + 1; i <= player_two_house_index; i++) {
    player_two_count += game.mancalaBoard[i].stones;
  }
  let winner;
  if (player_one_count == player_two_count) {
    game_status_message.innerHTML = draw_message_string;
    winner = GameWinner.DRAW;
  } else {
    let winning_total = (player_one_count > player_two_count)? player_one_count : player_two_count;
    let winner_string;
     if (player_one_count > player_two_count) {
        winner = Player.ONE;
        if (player_name == Player.ONE) {
            winner_string = construct_game_winner_message(winning_total);
        } else {
            winner_string = construct_game_loser_message (winning_total);
        }
     } else {
        winner = Player.TWO;
        if (player_name == Player.TWO) {
            winner_string = construct_game_winner_message(winning_total);
        } else {
            winner_string = construct_game_loser_message(winning_total);
        }
     }
    game_status_message.innerHTML = winner_string;
  }
  if (game.gamePlayStatus != GameStatus.FINISHED) {
    game.gamePlayStatus = GameStatus.FINISHED;
    game.winner = winner;
    game_play();
  }
  game_over_updates();
}

function game_over_updates() {
    new_game_button.classList.remove("disable");
    connect_to_game_button.classList.remove("disable");
    game_restart_button.classList.remove("disable");
    game_id_input.disabled = false;
    reset_board();
}

function game_error_updates() {
    $("div.message").remove();
    new_game_button.classList.remove("disable");
    connect_to_game_button.classList.remove("disable");
    game_restart_button.classList.add("disable");
    game_id_input.disabled = false;
    reset_board();
    disable_chat();
    game_error_message();
}

function enable_chat() {
    chat_heading.classList.remove("hidden");
    chat_controls.classList.remove("hidden");
    chat_messages.classList.remove("hidden");
    chat_message_input.removeEventListener("keydown", chat_key_down);
    chat_message_input.addEventListener("keydown", chat_key_down);
}

function disable_chat() {
    chat_heading.classList.add("hidden");
    chat_controls.classList.add("hidden");
    chat_messages.classList.add("hidden");
    chat_message_input.removeEventListener("keydown", chat_key_down);
}

function clear_chat_messages() {
    $("div.message").remove();
}

function map_pots_to_board(src_pot) {
    if (src_pot.isMan()) {
        throw "Invalid selection made";
    }
    if (src_pot.isTop()) {
        return src_pot.getNumber() - 1;
    } else {
        return src_pot.getNumber() + player_one_house_index;
    }
}

function board_total() {
  let total = 0;
  for (let i = 0; i <= player_two_house_index; i++) {
    total += game.mancalaBoard[i].stones;
  }
  return total;
}

function on_page_load() {
    game_status_message.innerHTML = on_page_load_string;
    enable_game_connect_key_down ();
    if (window.location.hash == "#_=_") {
      window.location.hash = "";
    }
}

function player_one_restart_message() {
    game_status_message.innerHTML = player_one_restart_string;
}

function missing_game_id_message() {
    game_status_message.innerHTML = missing_game_id_string;
}

function game_error_message() {
    game_status_message.innerHTML = game_error_string;
}

function connect_retry_message() {
    game_status_message.innerHTML = connect_retry_string;
}

function game_connection_error_message () {
    game_status_message.innerHTML = game_connection_error_message_string;
}

function empty_chat_text_error_message () {
    game_status_message.innerHTML = empty_chat_text_error_message_string;
}

function game_state_correction_message() {
    game_status_message.innerHTML = game_state_correction_message_string;
}

function construct_game_winner_message (winning_total) {
    return winning_message_string + winning_total + "!";
}
function construct_game_loser_message (winning_total) {
    return losing_message_string + winning_total + ".";
}
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
const connect_to_game_error_message_string = "Invalid game ID provided. (A game ID can only be used once).";
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
    is_player_one = true;

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

  reset_ui_board();
  game.gamePlayStatus = GameStatus.RESTARTING;
  game.selectedStoneContainerIndex = player_one_house_index;
  game_play ();
}

function handle_game_restart_request() {
  new_game_button.classList.add("disable");
  connect_to_game_button.classList.add("disable");
  game_restart_button.classList.add("disable");
  game_id_input.disabled = true;

  reset_ui_board();
  game.gamePlayStatus = GameStatus.IN_PROGRESS;
}

function reset_ui_board() {
  $("div.stone").remove();
  is_player_one = false;
  game.activePlayer = Player.TWO;
  update_house_counters();
  populate_row("pt");
  populate_row("pb");
}

function update_house_counters() {
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

function display_winner() {
  remove_pot_handlers();
  let winning_total = (game.winner == Player.ONE)? game.mancalaBoard[player_one_house_index].stones : game.mancalaBoard[player_two_house_index].stones;
  let winner_string;
  if (game.winner == Player.ONE) {
    if (player_name == Player.ONE) {
        winner_string = construct_game_winner_message(winning_total);
    } else {
        winner_string = construct_game_loser_message(winning_total);
    }
  } else {
    if (player_name == Player.TWO) {
        winner_string = construct_game_winner_message(winning_total);
    } else {
        winner_string = construct_game_loser_message(winning_total);
    }
  }
  game_status_message.innerHTML = winner_string;
  game_over_ui_updates();
}

function game_over_ui_updates() {
    new_game_button.classList.remove("disable");
    connect_to_game_button.classList.remove("disable");
    game_restart_button.classList.remove("disable");
    game_id_input.disabled = false;
    reset_ui_board();
}

function game_error_ui_updates() {
    $("div.message").remove();
    new_game_button.classList.remove("disable");
    connect_to_game_button.classList.remove("disable");
    game_restart_button.classList.add("disable");
    game_id_input.disabled = false;
    reset_ui_board();
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

function connect_to_game_error_message () {
    game_status_message.innerHTML = connect_to_game_error_message_string;
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
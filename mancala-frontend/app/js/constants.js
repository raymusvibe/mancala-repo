const api_url = "https://localhost";

const retry_limit = 10;
const retry_nominal_back_off = 500;
const retry_back_off = 2000;

const player_one_house_index = 6;
const player_two_house_index = 13;

const game_status_message = document.getElementById("game_status");
const game_id_input = document.getElementById("game_id");

const new_game_button = document.getElementById("new_game_button");
const connect_to_game_button = document.getElementById("connect_to_game_button");
const game_restart_button = document.getElementById("game_restart_button");

const player_one_button = document.getElementById("player_one_button");
const player_two_button = document.getElementById("player_two_button");

const chat_heading = document.getElementById("chat_heading");
const chat_controls = document.getElementById("chat_controls");
const chat_messages = document.getElementById("chat_messages");
const chat_message_input = document.getElementById("chat_message");

const player_one_restart_string = "Restarting the game. Player two will start.";
const missing_game_id_string = "Please enter game id to connect.";
const game_error_string = "Connection failed. Please start a new game or join another.";
const connect_retry_string = "Connection to service lost. Trying to reconnect, please wait...";
const connect_to_game_string = "Success! Waiting for player one to start, you're player two.";
const draw_message_string = "The game is a draw! Each player had a total of 36.";
const winning_message_string = "Congratulations, you won with a total of ";
const losing_message_string = "Sorry you lost. Your opponent won with a total of ";
const connect_to_game_error_message_string = "Invalid game ID provided. (A game ID can only be used once).";
const empty_chat_text_error_message_string = "Please enter a message first to send.";
const player_one_turn_message_string = "It is your turn, player one.";
const player_two_turn_message_string = "It is your turn, player two.";
const opponent_turn_message_string = "Waiting on the other player to complete their turn.";
const on_page_load_string = "Please start a new game or enter a game id from a friend to play.";

const sowing_interval = 350;
const map_board_to_pots = [
    "pt1", "pt2", "pt3", "pt4", "pt5", "pt6", "mt", 
    "pb1", "pb2", "pb3", "pb4", "pb5", "pb6", "mb"];

export {
    api_url,
    retry_limit,
    retry_nominal_back_off,
    retry_back_off,
    player_one_house_index,
    player_two_house_index,
    game_status_message,
    game_id_input,
    new_game_button,
    connect_to_game_button,
    game_restart_button,
    player_one_button,
    player_two_button,
    chat_heading,
    chat_controls,
    chat_messages,
    chat_message_input,
    player_one_restart_string,
    missing_game_id_string,
    game_error_string,
    connect_retry_string,
    connect_to_game_string,
    draw_message_string,
    winning_message_string,
    losing_message_string,
    connect_to_game_error_message_string,
    empty_chat_text_error_message_string,
    player_one_turn_message_string,
    player_two_turn_message_string,
    opponent_turn_message_string,
    on_page_load_string,
    sowing_interval,
    map_board_to_pots
}
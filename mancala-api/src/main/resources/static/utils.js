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
const game_error_string = "An unexpected error occurred. Please start a new game or join another.";
const initialise_new_game_connection_string = "Success! Waiting for player one to start, you're player two."

const chatKeyDown = (event) => {
        if (event.key === "Enter") {
          send_chat_message();
        }
    }

const gameConnectKeyDown = (event) => {
        if (event.key === "Enter") {
          connect_to_specific_game();
          game_id_input.value = "";
        }
    }

var clear_chat_messages = function () {
    $("div.message").remove();
}

var map_pots_to_board = function (src_pot) {
    if (src_pot.isMan()) {
        throw "Invalid selection made";
    }
    if (src_pot.isTop()) {
        return src_pot.getNumber() - 1;
    } else {
        return src_pot.getNumber() + playerOneHouseIndex;
    }
}

var board_total = function () {
  let total = 0;
  for (let i = 0; i <= playerTwoHouseIndex; i++) {
    total += game.mancalaBoard[i].stones;
  }
  return total;
}

var addListeners = function (class_list) {
    $(class_list)
        .mouseenter(function()
      {
        $(this).css( {
          "background-color":"rgba(255, 255, 255, 0.40)",
          "cursor":"pointer"
        });
      }).mouseleave(function()
      {
        $(this).css( {
          "background-color":"rgba(255, 255, 255, 0.15)",
          "cursor":"arrow"
        });
      }).click(function()
      {
        console.log("Selected Pot id: " + $(this).attr("id"));
        // check if move is valid
        $(class_list).off();
        game.selectedStoneContainerIndex = map_pots_to_board(new Pot($(this).attr("id")));
        sow_beads (new Pot($(this).attr("id")), null, true);
      });
}

var addPotHandlers = function()
{
  if (isPlayerOne) {
    if (player_name == Player.ONE) {
        remove_action_handlers (".topmid .pot");
        addListeners (".topmid .pot");
    } else {
        remove_action_handlers (".botmid .pot");
    }
  } else {
    if (player_name == Player.TWO) {
        remove_action_handlers (".botmid .pot");
        addListeners (".botmid .pot");
    } else {
        remove_action_handlers (".topmid .pot");
    }
  }
};

var update_counters = function (is_new_game) {
  if (is_new_game) {
    reset_stones ();
  }
  let player_one_house_count = game.mancalaBoard[playerOneHouseIndex].stones;
  let player_two_house_count = game.mancalaBoard[playerTwoHouseIndex].stones;
  document.getElementById("player_one_house_count").innerHTML = "Player one house count: " + player_one_house_count;
  document.getElementById("player_two_house_count").innerHTML = "Player two house count: " + player_two_house_count;
}

var reset_stones = function () {
    for (let i = 0; i <= playerTwoHouseIndex; i++) {
        if (i == playerOneHouseIndex || i == playerTwoHouseIndex) {
            game.mancalaBoard[i].stones = 0;
        } else {
            game.mancalaBoard[i].stones = number_of_beads_per_pot;
        }
    }
}

var reset_board = function () {
  $("div.bead").remove();
  isPlayerOne = false;
  game.activePlayer = Player.TWO;
  update_counters(true);
  populate_row("pt");
  populate_row("pb");
}

var game_over_updates = function () {
    new_game_button.classList.remove("disable");
    connect_to_game_button.classList.remove("disable");
    game_restart_button.classList.remove("disable");
    game_id_input.disabled = false;
    reset_board();
}

var game_error_updates = function () {
    $("div.message").remove();
    new_game_button.classList.remove("disable");
    connect_to_game_button.classList.remove("disable");
    game_id_input.disabled = false;
    game_error_message();
    reset_board ();
    disable_chat();
}

var enable_game_connect_key_down = function () {
    game_id_input.addEventListener("keydown", gameConnectKeyDown);
}

var enable_chat = function () {
    chat_heading.classList.remove("hidden");
    chat_controls.classList.remove("hidden");
    chat_messages.classList.remove("hidden");
    chat_message_input.removeEventListener("keydown", chatKeyDown);
    chat_message_input.addEventListener("keydown", chatKeyDown);
}

var disable_chat = function () {
    chat_heading.classList.add("hidden");
    chat_controls.classList.add("hidden");
    chat_messages.classList.add("hidden");
    chat_message_input.removeEventListener("keydown", chatKeyDown);
}

var handle_new_game_request = function () {
    new_game_button.classList.add("disable");
    connect_to_game_button.classList.add("disable");
    game_restart_button.classList.add("disable");
    game_id_input.disabled = true;

    reset_board();
    game.gamePlayStatus = GameStatus.IN_PROGRESS;
}

var new_game_updates = function () {
    new_game_button.classList.add("disable");
    connect_to_game_button.classList.add("disable");
    game_restart_button.classList.add("disable");
    game_id_input.disabled = true;

    reset_board();
    game.gamePlayStatus = GameStatus.NEW;
    game.selectedStoneContainerIndex = playerOneHouseIndex;
}

var restart_game = function () {
  initiated_game_restart = true;
  new_game_updates();
  game_play ();
}

var is_game_over = function () {
  console.log ("Checking if the game is finished...");
  let is_game_over = true;
  for (let i = 0; i < playerOneHouseIndex; i++) {
    if (game.mancalaBoard[i].stones > 0) {
      is_game_over = false;
      break;
    }
  }
  if (is_game_over) {
    return is_game_over;
  }
  is_game_over = true;
  for (let i = playerOneHouseIndex + 1; i < playerTwoHouseIndex; i++) {
    if (game.mancalaBoard[i].stones > 0){
      is_game_over = false;
      break;
    }
  }
  return is_game_over;
}

var determine_winner = function () {
  console.log ("The game is finished");
  remove_action_handlers (".botmid .pot");
  remove_action_handlers (".topmid .pot");
  let player_one_count = 0;
  for (let i = 0; i <= playerOneHouseIndex; i++) {
    player_one_count += game.mancalaBoard[i].stones;
  }
  let player_two_count = 0;
  for (let i = playerOneHouseIndex + 1; i <= playerTwoHouseIndex; i++) {
    player_two_count += game.mancalaBoard[i].stones;
  }
  let winner;
  if (player_one_count == player_two_count) {
    game_status_message.innerHTML = "The game is a draw!";
    winner = GameWinner.DRAW;
  } else {
    let winning_total = (player_one_count > player_two_count)? player_one_count : player_two_count;
    let winner_string;
     if (player_one_count > player_two_count) {
        if (player_name == Player.ONE) {
            winner_string = "Congratulations, you won with a total of " + winning_total + "!";
        } else {
            winner_string = "Sorry, you lost. Your opponent won with a total of " + winning_total + ".";
        }
     } else {
        if (player_name == Player.TWO) {
            winner_string = "Congratulations, you won with a total of " + winning_total + "!";
        } else {
            winner_string = "Sorry, you lost. Your opponent won with a total of " + winning_total + ".";
        }
     }
    winner = (player_one_count > player_two_count)? Player.ONE : Player.TWO;
    game_status_message.innerHTML = winner_string;
  }
  if (game.gamePlayStatus != GameStatus.FINISHED) {
    game.gamePlayStatus = GameStatus.FINISHED;
    game.winner = winner;
    game_play ();
  }
  game_over_updates();
}

var update_game_parameters = function () {
    let message;
    if (game.activePlayer == Player.ONE) {
        isPlayerOne = true;
        message = (player_name == Player.ONE)? "It is your turn, player one." : "Waiting on the other player to complete their turn.";
        game_status_message.innerHTML = message;
    } else {
        isPlayerOne = false;
        message = (player_name == Player.TWO)? "It is your turn, player two." : "Waiting on the other player to complete their turn.";
        game_status_message.innerHTML = message;
    }
    toggle_player_buttons_selection();
    addPotHandlers();
}

var remove_action_handlers = function (class_list) {
  $(class_list).unbind('mouseenter');
  $(class_list).unbind('mouseleave');
  $(class_list).unbind('click');
}

var initialise_new_game = function () {
    new_game_button.classList.add("disable");
    connect_to_game_button.classList.add("disable");
    game_restart_button.classList.add("disable");
    game_id_input.disabled = true;
    game_status_message.innerHTML = "Ask friend to join with " + game_id;
}

var initialise_new_game_connection = function () {
    new_game_button.classList.add("disable");
    connect_to_game_button.classList.add("disable");
    game_restart_button.classList.add("disable");
    game_id_input.disabled = true;
    game_status_message.innerHTML = initialise_new_game_connection_string;
    game_id_input.value = "";
}

var toggle_player_buttons_selection = function() {
  if (isPlayerOne) {
    if (player_name == Player.ONE) {
        player_one_button.classList.remove("button2");
        player_one_button.classList.add("button2_selected");
        player_two_button.classList.remove("button2_selected");
        player_two_button.classList.add("button2");
    } else {
        player_two_button.classList.remove("button2_selected");
        player_two_button.classList.add("button2");
    }
  } else {
    if (player_name == Player.TWO) {
        player_two_button.classList.remove("button2");
        player_two_button.classList.add("button2_selected");
        player_one_button.classList.remove("button2_selected");
        player_one_button.classList.add("button2");
    } else {
        player_one_button.classList.remove("button2_selected");
        player_one_button.classList.add("button2");
    }
  }
}

var on_page_load = function () {
    game_status_message.innerHTML = on_page_load_string;
    enable_game_connect_key_down ();
}

var append_chat_message = function (response) {
    let display_name = (response.sender == Player.ONE)? "Player One#" : "Player Two#";
    let content = "<div class=\"message\">"
                    +    "<p class=\"chat_display\">" + display_name + " :> " + response.message + "</p>"
                    + "</div>"

    $("#chat_messages").append(content);
    chat_message_input.value = "";
}

var player_one_restart_message = function () {
    game_status_message.innerHTML = player_one_restart_string;
}

var missing_game_id_message = function () {
    game_status_message.innerHTML = missing_game_id_string;
}

var game_error_message = function () {
    game_status_message.innerHTML = game_error_string;
}
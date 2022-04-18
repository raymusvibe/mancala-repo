var number_of_pots_per_player = 6;
var number_of_stones_per_pot = 6;
var player_one_house_index = 6;
var player_two_house_index = 13;
var total_stones_count = 72;

var player_initiated_game_restart = false;
var is_player_one = true;

var game_id;
var game;
var player_name;
var opponent_name;

var map_board_to_pots = ["pt1", "pt2", "pt3", "pt4", "pt5", "pt6", "mt", "pb1", "pb2", "pb3", "pb4", "pb5", "pb6", "mb"];

let last_pot_after_full_revolution = null;
const sowing_interval = 350;

/*Main method for sowing*/
function sow_stones(src_pot, last_pot) {
  const children = src_pot.$().children();
  if(children.length === 0) {
    setTimeout(complete_turn,sowing_interval,src_pot)
    return;
  }
  if(last_pot === null) {
    last_pot = src_pot;
  }
  let selected_stone = children.get(0);
  //reverse stone deque order when sowing has come all way back to same pot (Or simply skip stone at position zero)
  if ($(selected_stone).attr('hasMoved') == "true" && children.length > 1) {
    selected_stone = children.get(children.length - 1);
  }
  last_pot = last_pot.getNextSown(true);
  // steal
  if(children.length == 1 &&
     is_player_one === last_pot.isTop() &&
     last_pot.$().children().length === 0 &&
     !last_pot.isMan())
  {
    steal(src_pot, last_pot, selected_stone);
  } else {
    if(children.length == 1) {
      //cater for last stone in the event it has already been moved
      if ($(selected_stone).attr('hasMoved') == "true") {
        $(selected_stone).attr('hasMoved', false);
        complete_turn(src_pot);
        return;
      } else {
        if (last_pot.id == src_pot.id) {
          if (is_player_one === last_pot.isTop() &&
            !last_pot.isMan())
          {
            steal(src_pot, last_pot, selected_stone);
          }
          complete_turn(src_pot);
          return;
        }
        move_stone(selected_stone, src_pot, last_pot);
      }
    } else {
      if (last_pot.id == src_pot.id) {
        //tag the selected stone and move the next stone
        $(selected_stone).attr('hasMoved', true);
        let next_stone = children.get(1);
        last_pot = last_pot.getNextSown(true);
        move_stone(next_stone, src_pot, last_pot);
        last_pot_after_full_revolution = last_pot;
      } else {
        if ($(selected_stone).attr('hasMoved') != "true") {
          move_stone(selected_stone, src_pot, last_pot);
          //keep track of variable in case player turn goes all way round
          last_pot_after_full_revolution = last_pot;
        }
      }
    }
  }
  setTimeout(sow_stones,sowing_interval,src_pot,last_pot);
}

/*Moves stones and updates game object in live play, but not for the simulation of opponents play*/
function move_stone(stone, src_pot, dest_pot)
{
  position_stone(stone, dest_pot);
  $(stone).appendTo(dest_pot.$());
}

/*Runs at the end of each player's turn*/
function complete_turn(src_pot) {
  last_pot_after_full_revolution = null;
  if (is_board_and_game_ui_misaligned ()) {
        sync_board_with_ui_stones ();
      }
  update_house_counters();
  update_game_parameters();
}

/*Executed when there is an update from the server*/
function handle_gameplay_websocket_response() {
  if (game.gamePlayStatus == GameStatus.FINISHED) {
    display_game_outcome ();
    return;
  }
  if (game.gamePlayStatus == GameStatus.RESTARTING && !player_initiated_game_restart) {
    handle_game_restart_request ();
    //restart sets player two as active
    if (player_name == Player.ONE) {
      player_one_restart_message ();
      return;
    }
  } else {
    if (game.gamePlayStatus != GameStatus.IN_PROGRESS) {
      game.gamePlayStatus = GameStatus.IN_PROGRESS;
      player_initiated_game_restart = false;
    }
  }
  sow_stones (new Pot(map_board_to_pots[game.selectedStoneContainerIndex]), null);
}

function steal(src_pot, last_pot, selected_stone) {
  let opposite_pot = last_pot.getOpposite();
  const house_pot = (is_player_one)? new Pot('mt') : new Pot('mb');
  opposite_pot.$().children().each(function(idx,stolen_stone)
  {
    move_stone(stolen_stone, opposite_pot, house_pot);
  });
  move_stone(selected_stone, src_pot, house_pot);
 }

 $(document).ready( function()
 {
   on_page_load();
   populate_row("pt");
   populate_row("pb");
 });
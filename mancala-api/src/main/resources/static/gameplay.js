var number_of_pots_per_player = 6;
var number_of_beads_per_pot = 6;
var playerOneHouseIndex = 6;
var playerTwoHouseIndex = 13;
var total_beads_count = 72;
var last_pot_sown_on_completing_full_revolution;
var initiated_game_restart = false;
var isPlayerOne = null;

let map_board_to_pots = ["pt1", "pt2", "pt3", "pt4", "pt5", "pt6", "mt", "pb1", "pb2", "pb3", "pb4", "pb5", "pb6", "mb"];

/*Move's beads. Also updates game object in live play, but not for the simulations*/
var move_bead = function(bead, src_pot, dest_pot, is_steal, is_live_play)
{
    if (is_live_play) {
      let src_index;
      let dest_index;;
      if (isPlayerOne) {
        if (is_steal) {
          src_index = src_pot.getNumber() + number_of_pots_per_player;
        } else {
          src_index = src_pot.getNumber() - 1;
        }
        if(dest_pot.isTop()) {
          if (dest_pot.isMan()){
            $(bead).attr('hasMoved', true);
            dest_index = playerOneHouseIndex;
          } else {
            dest_index = dest_pot.getNumber() - 1;
          }
        } else {
          dest_index = dest_pot.getNumber() + number_of_pots_per_player;
        }
      } else {
        if (is_steal) {
          src_index = src_pot.getNumber() - 1;
        } else {
          src_index = src_pot.getNumber() + number_of_pots_per_player;
        }
        if (dest_pot.isBottom()) {
          if (dest_pot.isMan()) {
            $(bead).attr('hasMoved', true);
            dest_index = playerTwoHouseIndex;
          } else {
            dest_index = dest_pot.getNumber() + number_of_pots_per_player;
          }
        } else {
          dest_index = dest_pot.getNumber() - 1;
        }
      }
      if (src_index == dest_index) {
        return;
      }

      let source_bead_count = game.mancalaBoard[src_index].stones;
      let dest_bead_count = game.mancalaBoard[dest_index].stones;

      if (source_bead_count == 0) {
        throw "Trying to move beads from empty container!";
      }

      source_bead_count--;
      dest_bead_count++;
      game.mancalaBoard[src_index].stones = source_bead_count;
      game.mancalaBoard[dest_index].stones = dest_bead_count;
    }

  position_bead(bead, dest_pot);
  $(bead).appendTo(dest_pot.$());
}

/*Run's at the end of each player's turn*/
var complete_turn = function (src_pot, last_pot, is_live_play)
{
  last_pot_sown_on_completing_full_revolution = null;
  update_counters(false);

  if(board_total () != total_beads_count) {
    throw "Invalid board total";
  }

  if (is_game_over ()) {
    determine_winner();
  } else {
    if (is_live_play) {
        console.log ("Completing turn...");
        console.log(JSON.stringify(game,null,'\t'));
        src_pot.$().css("background-color","rgba(255, 255, 255, 0.15)");
        game.selectedStoneContainerIndex = map_pots_to_board(src_pot);
        game_play();
    } else {
        console.log("Completing simulation...");
        update_game_parameters();
    }
  }
}

/*Main method for sowing*/
var sow_beads = function(src_pot, last_pot, is_live_play)
{
  const children = src_pot.$().children();
  if(children.length === 0)
  {
    complete_turn(src_pot, last_pot, is_live_play);
    return;
  }
  if(last_pot === null)
  {
    last_pot = src_pot;
  }
  let selected_bead = children.get(0);
  //reverse bead deque order when sowing has come all way back to same pot (Or simply skip bead at position zero)
  if ($(selected_bead).attr('hasMoved') == "true" && children.length > 1) {
    selected_bead = children.get(children.length - 1);
  }
  last_pot = last_pot.getNextSown(true);
  // steal
  if(children.length == 1 &&
     isPlayerOne === last_pot.isTop() &&
     last_pot.$().children().length === 0 &&
     !last_pot.isMan())
  {
    steal(src_pot, last_pot, is_live_play, selected_bead);
  }
  else
  {
    if(children.length == 1)
    {
      //cater for last bead in the event it has already been moved
      if ($(selected_bead).attr('hasMoved') == "true") {
          $(selected_bead).attr('hasMoved', false);
          complete_turn(src_pot, last_pot_sown_on_completing_full_revolution, is_live_play);
          return;
      } else {
          if (last_pot.id == src_pot.id) {
              if (isPlayerOne === last_pot.isTop() &&
                  !last_pot.isMan())
              {
                  steal(src_pot, last_pot, is_live_play, selected_bead);
              }
              complete_turn(src_pot, last_pot, is_live_play);
              return;
          }
          move_bead(selected_bead, src_pot, last_pot, false, is_live_play);
      }
    } else {
        if (last_pot.id == src_pot.id) {
          //tag the selected bead and move the next bead
          $(selected_bead).attr('hasMoved', true);
          let next_bead = children.get(1);
          last_pot = last_pot.getNextSown(true);
          move_bead(next_bead, src_pot, last_pot, false, is_live_play);
          last_pot_sown_on_completing_full_revolution = last_pot;
        } else {
          if ($(selected_bead).attr('hasMoved') != "true") {
            move_bead(selected_bead, src_pot, last_pot, false, is_live_play);
            //keep track of variable in case player turn goes all way round
            last_pot_sown_on_completing_full_revolution = last_pot;
          }
        }
    }
  }
  setTimeout(sow_beads,350,src_pot,last_pot,is_live_play)
}

/*Handle websocket update*/
var action_response = function () {
    console.log("Actioning game update...");
    if (game.gamePlayStatus == GameStatus.FINISHED) {
        sow_beads(new Pot(map_board_to_pots[game.selectedStoneContainerIndex]), null, false);
        return;
    }
    if (game.gamePlayStatus == GameStatus.NEW && !initiated_game_restart) {
        handle_new_game_request ();
        //restart set's player two as active
        if (player_name == Player.ONE) {
            player_one_restart_message ();
            return;
        }
    } else {
        if (game.gamePlayStatus != GameStatus.IN_PROGRESS) {
            game.gamePlayStatus = GameStatus.IN_PROGRESS;
            initiated_game_restart = false;
        }
    }
    //three simulation scenario's. Condition one at the start of the game will run simulation on player two's board when player one makes first move
    let is_simulation = false;
    if (isPlayerOne === null) {
        if (game.selectedStoneContainerIndex != playerOneHouseIndex && game.selectedStoneContainerIndex != playerTwoHouseIndex){
            isPlayerOne = true;
            sow_beads(new Pot(map_board_to_pots[game.selectedStoneContainerIndex]), null, false);
            is_simulation =true;
        }
    }
    //simulate opponent play after normal turn changes
    else if (isPlayerOne !== null && isPlayerOne && game.activePlayer == Player.TWO
                || isPlayerOne !== null && !isPlayerOne && game.activePlayer == Player.ONE) {
                if (player_name == game.activePlayer)  {
                    sow_beads(new Pot(map_board_to_pots[game.selectedStoneContainerIndex]), null, false);
                    is_simulation =true;
                }
    }
    // simulate opponent play when other player has a repeat play
    else if (isPlayerOne !== null && !isPlayerOne && game.activePlayer == Player.TWO
        || isPlayerOne !== null && isPlayerOne && game.activePlayer == Player.ONE) {
        if (player_name != game.activePlayer)  {
            sow_beads (new Pot(map_board_to_pots[game.selectedStoneContainerIndex]), null, false);
            is_simulation =true;
        }
    }
    //turn change
    if (!is_simulation) {
        update_game_parameters();
    }
}

var steal = function (src_pot, last_pot, is_live_play, selected_bead) {
     let opposite_pot = last_pot.getOpposite();

     console.log ("Stealing " + opposite_pot.$().children().length +
           " from " + opposite_pot.id + " by " + last_pot.id);

     const house_pot = (isPlayerOne)? new Pot('mt') : new Pot('mb');
     opposite_pot.$().children().each(function(idx,stolen_bead)
     {
         move_bead(stolen_bead, opposite_pot, house_pot, true, is_live_play);
     });
     move_bead(selected_bead, src_pot, house_pot, false, is_live_play);
 }

 $(document).ready( function()
 {
   on_page_load();
   populate_row("pt");
   populate_row("pb");
 });
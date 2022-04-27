import $ from 'jquery';
import * as Constants from './constants.js';
import { Pot, colors, position_stone, place_new_stone } from './presentation_library.js';

function move_stone (stone, dest_pot)
{
    position_stone(stone, dest_pot);
    $(stone).appendTo(dest_pot.$());
}

function steal(last_pot, selected_stone, is_player_one) {
    let opposite_pot = last_pot.getOpposite();
    const house_pot = (is_player_one)? new Pot('mt') : new Pot('mb');
    opposite_pot.$().children().each(function(idx,stolen_stone)
    {
        move_stone(stolen_stone, house_pot);
    });
    move_stone(selected_stone, house_pot);
 }

function is_board_and_game_ui_misaligned (game) {
    for (let i = 6; i <= Constants.player_two_house_index; i+=7 ) {
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

function complete_turn (game) {
    if (is_board_and_game_ui_misaligned (game)) {
        sync_board_with_ui_stones (game);
    }  
}

function sow_stones (src_pot, last_pot, is_player_one, game) {
    const children = src_pot.$().children();
    if(children.length === 0) {
        setTimeout(complete_turn, Constants.sowing_interval, game);
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
        steal(last_pot, selected_stone, is_player_one);
    } else {
        if(children.length == 1) {
            //cater for last stone in the event it has already been moved
            if ($(selected_stone).attr('hasMoved') == "true") {
                $(selected_stone).attr('hasMoved', false);
                setTimeout(complete_turn, Constants.sowing_interval, game);
                return;
            } else {
                if (last_pot.id == src_pot.id) {
                    if (is_player_one === last_pot.isTop() &&
                    !last_pot.isMan())
                    {
                        steal(last_pot, selected_stone, is_player_one);
                    }
                    setTimeout(complete_turn, Constants.sowing_interval, game);
                    return;
                }
                move_stone(selected_stone, last_pot);
            }
        } else {
            if (last_pot.id == src_pot.id) {
                //tag the selected stone and move the next stone
                $(selected_stone).attr('hasMoved', true);
                let next_stone = children.get(1);
                last_pot = last_pot.getNextSown(is_player_one);
                move_stone(next_stone, last_pot);
            } else {
                if ($(selected_stone).attr('hasMoved') != "true") {
                    move_stone(selected_stone, last_pot);
                }
            }
        }
    }
    setTimeout(sow_stones, Constants.sowing_interval, src_pot, last_pot, is_player_one, game);
}

export { sow_stones, sync_board_with_ui_stones }
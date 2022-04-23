import $ from 'jquery';
import * as Constants from './constants.js';
import { Player } from './enums.js';

'use strict';

const pot_center = new Point(30,30);
const proximity_threshold = 20 * 20;

const colors = [
    new Color(255,0,0,0.7), new Color(0,255,0,0.7), new Color(0,71,171,0.7),
    new Color(255,255,0,0.7), new Color(0,255,255,0.7), new Color(255,128,0,0.7),
    new Color(255,51,153,0.7), new Color(0,102,0,0.7)
];

function Point(x,y)
{
    this.x = x;
    this.y = y;
    this.plus = function(p) {
        return new Point(
          this.x + p.x,
          this.y + p.y
        );
    }
    this.minus = function(p) {
        return new Point(
          this.x - p.x,
          this.y - p.y
        );
    }
    this.normSq = function() {
        return x * x + y * y;
    }
}

function Color(r,g,b,a)
{
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
    this.toString = function() {
        return "rgba("+Math.floor(this.r)+","+Math.floor(this.g)+","+Math.floor(this.b)+","+this.a+")";
    }
    this.lerpTo = function(dest,alpha) {
        const acomp = 1 - alpha;
        return new Color(
            r * acomp + dest.r * alpha,
            g * acomp + dest.g * alpha,
            b * acomp + dest.b * alpha,
            a * acomp + dest.a * alpha
        );
    }
}

function Pot(id_in)
{
    if(!(
      (id_in.charAt(0) === 'p' || id_in.charAt(0) === 'm') &&
      (id_in.charAt(1) === 't' || id_in.charAt(1) === 'b') &&
      (id_in.charAt(2) >= 1    || id_in.charAt(2) <= 6 )))
    {
        throw "invalid id for pot construction";
    }
    this.id = id_in;
    this.isTop = function() {
        return this.id.charAt(1) === 't';
    };
    this.isBottom = function() {
        return !this.isTop();
    };
    this.isMan = function() {
        return this.id.charAt(0) === 'm';
    };
    this.getSide = function() {
        return this.id.charAt(1);
    }
    this.getOtherSide = function() {
        if(this.getSide() === 't') {
            return 'b';
        } else {
            return 't';
        }
    }
    this.getNumber = function()
    {
        return parseInt(this.id.charAt(2));
    }
    this.getOpposite = function()
    {
        if(this.isMan()) {
            throw "cannot get opposite of mancala pot";
        }
        return new Pot("p" + this.getOtherSide() + (7-this.getNumber()));
    }
    this.getNextSown = function(is_player_one)
    {
        if(this.isMan()) {
            if(this.isTop()) {
                return new Pot("pb1");
            } else {
                return new Pot("pt1");
            }
        } else {
            if(this.getNumber() === 6) {
                if(is_player_one) {
                    if(this.isTop()) {
                        return new Pot('mt');
                    } else {
                        return new Pot('pt1');
                    }
                } else {
                    if(this.isBottom()) {
                        return new Pot('mb');
                    } else {
                        return new Pot('pb1');
                    }
                }
            } else {
                return new Pot('p'+this.getSide()+(this.getNumber()+1));
            }
        }
    }
    this.$ = function() {
        return $('#'+this.id);
    }
}

function setbg_rgba(e,c)
{
    const hi = c.lerpTo(new Color(255,255,255,0),0.8);
    hi.a = 0.85;
    const lo = c.lerpTo(new Color(0,0,0,0),0.8);
    lo.a = 0.85;
    const grad =  "radial-gradient(farthest-corner at 9px 9px," +
      hi + " 0%, " + hi + " 8%, " + c + " 30%, " +
      lo + " 90%)";
    e.css("background-image",grad );
}

function read_pos(stone)
{
    return new Point(
        parseInt($(stone).css("left").slice(0,-2)),
        parseInt($(stone).css("top").slice(0,-2))
    );
}

function generate_pot_offset( radius )
{
    const theta = Math.PI * (2 * Math.random() - 1);
    const r = radius * Math.random();
    return new Point(
        Math.floor( r * Math.cos(theta) ),
        Math.floor( r * Math.sin(theta) )
    );
}

function pos_proximity_test(test_pos,dest_pot,dist)
{
    let too_close = false;
    dest_pot.$().children().each(function(idx,stone)
    {
        const pos_stone = read_pos(stone);
        if( pos_stone.minus(test_pos).normSq() < dist ) {
            too_close = true;
            return false;
        }
    });
    return !too_close;
}

function set_stone_pos(stone,pos) {
    $(stone).css( {
        "top":pos.y + "px","left":pos.x + "px"
    } );
}

function position_stone(stone, dest_pot) {
    let dsq = proximity_threshold;
    let done = false;
    while( !done ) {
        dsq--;
        const cand_pos = pot_center.plus(
            generate_pot_offset( 25 )
        );
        if(pos_proximity_test(cand_pos,dest_pot,dsq)) {
            set_stone_pos(stone,cand_pos);
            done = true;
        }
    }
}

function place_new_stone(id, c)
{
    const stone = $("<div>",{"class":"stone"});
    $(stone).attr ('hasMoved', false);
    setbg_rgba(stone,c);
    const dest_pot = new Pot(id);
    position_stone(stone,dest_pot);
    dest_pot.$().append(stone);
}

function toggle_player_buttons_selection(is_player_one, player_name) {
    if (is_player_one) {
        if (player_name == Player.ONE) {
            Constants.player_one_button.classList.remove("button_two");
            Constants.player_one_button.classList.add("button_two_selected");
            Constants.player_two_button.classList.remove("button_two_selected");
            Constants.player_two_button.classList.add("button_two");
        } else {
            Constants.player_two_button.classList.remove("button_two_selected");
            Constants.player_two_button.classList.add("button_two");
        }
    } else {
      if (player_name == Player.TWO) {
          Constants.player_two_button.classList.remove("button_two");
          Constants.player_two_button.classList.add("button_two_selected");
          Constants.player_one_button.classList.remove("button_two_selected");
          Constants.player_one_button.classList.add("button_two");
      } else {
          Constants.player_one_button.classList.remove("button_two_selected");
          Constants.player_one_button.classList.add("button_two");
      }
    }
}

function remove_action_handlers(class_list) {
    $(class_list).off('mouseenter');
    $(class_list).off('mouseleave');
    $(class_list).off('click');
}

function remove_pot_handlers() {
    remove_action_handlers (".topmid .pot");
    remove_action_handlers (".botmid .pot");
}

function enable_chat(chat_key_down) {
    Constants.chat_heading.classList.remove("hidden");
    Constants.chat_controls.classList.remove("hidden");
    Constants.chat_messages.classList.remove("hidden");
    Constants.chat_message_input.removeEventListener("keydown", chat_key_down);
    Constants.chat_message_input.addEventListener("keydown", chat_key_down);
}

function disable_chat(chat_key_down) {
    Constants.chat_heading.classList.add("hidden");
    Constants.chat_controls.classList.add("hidden");
    Constants.chat_messages.classList.add("hidden");
    Constants.chat_message_input.removeEventListener("keydown", chat_key_down);
}

export {
    Pot,
    colors,
    position_stone,
    place_new_stone,
    toggle_player_buttons_selection,
    remove_action_handlers,
    remove_pot_handlers,
    enable_chat,
    disable_chat
}
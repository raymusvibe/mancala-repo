let pot_center = new Point(30,30);
let proximity_threshold = 20 * 20;

let colors = [
  new Color(255,0,0,0.7),
  new Color(0,255,0,0.7),
  new Color(0,0,255,0.7),
  new Color(255,255,0,0.7),
  new Color(0,255,255,0.7)
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
    return "rgba("+Math.floor(this.r)+","+Math.floor(this.g)+","+Math.floor(this.b)+","+this.a+")"
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
  this.getNextSown = function()
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

function read_pos(bead)
{
  return new Point(
    parseInt($(bead).css("left").slice(0,-2)),
    parseInt($(bead).css("top").slice(0,-2))
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
  dest_pot.$().children().each(function(idx,bead)
  {
    const pos_bead = read_pos(bead);
    if( pos_bead.minus(test_pos).normSq() < dist ) {
      too_close = true;
      return false;
    }
  });
  return !too_close;
}

function set_bead_pos(bead,pos) {
  $(bead).css( {
    "top":pos.y + "px","left":pos.x + "px"
  } );
}

function position_bead(bead, dest_pot) {
  let dsq = proximity_threshold;
  let done = false;
  while( !done ) {
    dsq--;
    const cand_pos = pot_center.plus(
      generate_pot_offset( 25 )
    );
    if(pos_proximity_test(cand_pos,dest_pot,dsq)) {
      set_bead_pos(bead,cand_pos);
      done = true;
    }
  }
}

function place_new_bead(id, c)
{
  const bead = $("<div>",{"class":"bead"});
  $(bead).attr ('hasMoved', false);
  setbg_rgba(bead,c);
  const dest_pot = new Pot(id);
  position_bead(bead,dest_pot);
  dest_pot.$().append(bead);
}

function add_listeners(class_list) {
  $(class_list).mouseenter(function()
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
    // check if move is valid
    $(class_list).off();
    game.selectedStoneContainerIndex = map_pots_to_board(new Pot($(this).attr("id")));
    sow_beads (new Pot($(this).attr("id")), null, true);
  });
}

function remove_action_handlers(class_list) {
  $(class_list).unbind('mouseenter');
  $(class_list).unbind('mouseleave');
  $(class_list).unbind('click');
}

function add_pot_handlers() {
  if (is_player_one) {
    if (player_name == Player.ONE) {
      remove_action_handlers (".topmid .pot");
      add_listeners (".topmid .pot");
    } else {
      remove_action_handlers (".botmid .pot");
    }
  } else {
    if (player_name == Player.TWO) {
      remove_action_handlers (".botmid .pot");
      add_listeners (".botmid .pot");
    } else {
      remove_action_handlers (".topmid .pot");
    }
  }
};

function remove_pot_handlers() {
  remove_action_handlers (".topmid .pot");
  remove_action_handlers (".botmid .pot");
};

function toggle_player_buttons_selection() {
  if (is_player_one) {
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

function append_chat_message(response) {
  let display_name = (response.sender == Player.ONE)? "Player One#" : "Player Two#";
  let content = '<div class="message">'
                + '<p class="chat_display">' + display_name + ' :> ' + response.message + '</p>'
                + '</div>'

  $("#chat_messages").append(content);
  chat_message_input.value = "";
}

function populate_row(row) {
  let n = 0;
  for(let c = 0; c < 6; c++) {
    for(let i = 1; i <= 6; i++,n++) {
      place_new_bead(row + i,colors[n % colors.length]);
    }
  }
}

function sync_board_with_ui_beads () {
  $("div.bead").remove();
  let n = 0;
  for (let i = 0; i <= player_two_house_index; i++) {
    for(let j = 0; j < game.mancalaBoard[i].stones; j++, n++)
    {
      place_new_bead(map_board_to_pots[i], colors[n % colors.length]);
    }
  }
}
var pot_center = new Point(30,30);
var proximity_threshold = 20 * 20;
var mancala_board;
var number_of_pots_per_player = 6;
var number_of_beads_per_pot = 6;
var playerOneHouseIndex = 6;
var playerTwoHouseIndex = 13;
var last_pot_sown_on_completing_full_revolution;
var total_beads_count = 72;
var colors = [
  new Color(255,0,0,0.7),
  new Color(0,255,0,0.7),
  new Color(0,0,255,0.7),
  new Color(255,255,0,0.7),
  new Color(0,255,255,0.7)
];
var isPlayerOne = true;

var initialiseBoard = function () {
  mancala_board = [];
  for (let i = 0; i <=  playerTwoHouseIndex; i++) {
      var map = new Map ();
      if (i != playerOneHouseIndex && i != playerTwoHouseIndex) {
          mancala_board.push(map.set(i, number_of_beads_per_pot));
      } else {
          mancala_board.push(map.set(i, 0));
      }
  }
}

function Point(x,y)
{
  this.x = x;
  this.y = y;
  this.plus = function(p)
  {
    return new Point(
      this.x + p.x,
      this.y + p.y
    );
  }
  this.minus = function(p)
  {    
    return new Point(
      this.x - p.x,
      this.y - p.y
    );
  }
  this.normSq = function()
  {
    return x * x + y * y;
  }
}

function Color(r,g,b,a)
{
  this.r = r;
  this.g = g;
  this.b = b;
  this.a = a;
  this.toString = function()
  {
    return "rgba("+Math.floor(this.r)+","+Math.floor(this.g)+","+Math.floor(this.b)+","+this.a+")"
  }
  this.lerpTo = function(dest,alpha)
  {
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
  this.isTop = function()
  {
    return this.id.charAt(1) === 't';
  };
  this.isBottom = function()
  {
    return !this.isTop();
  };
  this.isMan = function()
  {
    return this.id.charAt(0) === 'm';
  };
  this.getSide = function()
  {
    return this.id.charAt(1);
  }
  this.getOtherSide = function()
  {
    if(this.getSide() === 't')
    {
      return 'b';
    }
    else
    {
      return 't';
    }
  }
  this.getNumber = function()
  {
    return parseInt(this.id.charAt(2));
  }
  this.getOpposite = function()
  {
    if(this.isMan())
    {
      throw "cannot get opposite of mancala pot";
    }
    return new Pot("p" + this.getOtherSide() + (7-this.getNumber()));
  }
  this.getNextSown = function()
  {
    if(this.isMan())
    {
      if(this.isTop())
      {
        return new Pot("pb1");
      }
      else
      {
        return new Pot("pt1");
      }
    }
    else
    {
      if(this.getNumber() === 6)
      {
        if(isPlayerOne)
        {
          if(this.isTop())
          {
            return new Pot('mt');
          }
          else
          {
            return new Pot('pt1');
          }
        }
        else
        {
          if(this.isBottom())
          {
            return new Pot('mb');
          }
          else
          {
            return new Pot('pb1');
          }
        }
      }
      else
      {
        return new Pot('p'+this.getSide()+(this.getNumber()+1));
      }
    }    
  }
  this.$ = function()
  {
    return $('#'+this.id);
  }
}

var setbg_rgba = function(e,c)
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

var read_pos = function(bead)
{
  return new Point(
    parseInt($(bead).css("left").slice(0,-2)),
    parseInt($(bead).css("top").slice(0,-2))
  );
}

var generate_pot_offset = function( radius )
{
  const theta = Math.PI * (2 * Math.random() - 1);
  const r = radius * Math.random();
  return new Point( 
    Math.floor( r * Math.cos(theta) ),
    Math.floor( r * Math.sin(theta) )
  );
}

var pos_proximity_test = function(test_pos,dest_pot,dist)
{ 
  var too_close = false;
  dest_pot.$().children().each(function(idx,bead)
  {
    const pos_bead = read_pos(bead);
    if( pos_bead.minus(test_pos).normSq() < dist )
    {
      too_close = true;
      return false;
    }
  });
  return !too_close;
}

var set_bead_pos = function(bead,pos)
{
  $(bead).css( {
    "top":pos.y + "px","left":pos.x + "px"
  } );  
}

var position_bead = function(bead,dest_pot)
{
  let dsq = proximity_threshold;
  let done = false;
  while( !done )
  {
    dsq--;
    const cand_pos = pot_center.plus( 
      generate_pot_offset( 25 )
    );
    if(pos_proximity_test(cand_pos,dest_pot,dsq))
    {
      set_bead_pos(bead,cand_pos);
      done = true;
    }    
  }
}

var move_bead = function(bead, src_pot, dest_pot, is_steal)
{
  var src_index;
  var dest_index;;
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
  var src_api_pot = mancala_board[src_index];
  var dest_api_pot = mancala_board[dest_index];
  var source_bead_count = src_api_pot.get(src_index);
  var dest_bead_count = dest_api_pot.get(dest_index);
  source_bead_count--;
  dest_bead_count++;

  src_api_pot.set(src_index, source_bead_count);
  dest_api_pot.set(dest_index, dest_bead_count);
  mancala_board[src_index] = src_api_pot;
  mancala_board[dest_index] = dest_api_pot;
  
  console.log ("Sowing into: " + dest_pot.id);
  position_bead(bead,dest_pot);
  $(bead).appendTo(dest_pot.$());
}

var is_game_over = function () {
  var is_game_over = true;
  for (let i = 0; i < playerOneHouseIndex; i++) {
    if (mancala_board[i].get(i) > 0){
      is_game_over = false;
      break;
    }
  }
  if (is_game_over) {
    return is_game_over;
  }

  is_game_over = true;
  for (let i = playerOneHouseIndex + 1; i < playerTwoHouseIndex; i++) {
    if (mancala_board[i].get(i) > 0){
      is_game_over = false;
      break;
    }
  }
  return is_game_over;
}

var determine_winner = function () {
  remove_action_handlers (".botmid .pot");
  remove_action_handlers (".topmid .pot");
  var player_one_count = 0;
  for (let i = 0; i <= playerOneHouseIndex; i++) {
    player_one_count += mancala_board[i].get(i);
  }
  var player_two_count = 0;
  for (let i = playerOneHouseIndex + 1; i <= playerTwoHouseIndex; i++) {
    player_two_count += mancala_board[i].get(i);
  }
  if (player_one_count == player_two_count) {
    alert("Game over, each players has a total of " + player_one_count + ". The game is a draw!");
    return;
  }
  var winning_total = (player_one_count > player_two_count)? player_one_count : player_two_count;
  var winner = (player_one_count > player_two_count)? "player one" : "player two";
  alert ("Game over, the winner is " + winner + " with a total of " + winning_total + "! Press re-start to play again!");
}

var place_new_bead = function(id,c)
{
  const bead = $("<div>",{"class":"bead"});
  $(bead).attr ('hasMoved', false);
  setbg_rgba(bead,c);
  const dest_pot = new Pot(id);
  position_bead(bead,dest_pot);
  dest_pot.$().append(bead);
}

var populate_row = function(row)
{
  let n = 0;
  for(let c = 0; c < 6; c++)
  {
    for(let i = 1; i <= 6; i++,n++)
    {
      place_new_bead(row + i,colors[n % colors.length]);
    }
  }
}

var toggle_player_buttons_selection = function() {
  if (isPlayerOne) {
    var player_one = document.getElementById("player_one_button");
    player_one.classList.remove("button2");
    player_one.classList.add("button2_selected");

    var player_two = document.getElementById("player_two_button");
    player_two.classList.remove("button2_selected");
    player_two.classList.add("button2");
  } else {
    var player_one = document.getElementById("player_one_button");
    player_one.classList.remove("button2_selected");
    player_one.classList.add("button2");

    var player_two = document.getElementById("player_two_button");
    player_two.classList.remove("button2");
    player_two.classList.add("button2_selected");
    
  }
}

var board_total = function () {
  var total = 0;
  for (let i = 0; i <= playerTwoHouseIndex; i++) {
    total += mancala_board[i].get(i);
  }
  return total;
}

var complete_turn = function (src_pot, last_pot)
{
  last_pot_sown_on_completing_full_revolution = null;
  update_counters();
  //console.log (mancala_board);
  if(board_total () != total_beads_count) {
    throw "Invalid board total";
  }
  if(isPlayerOne) {
    console.log ("Last play: PLAYER ONE");
  } else {
    console.log ("Last play: PLAYER TWO");
  }
  src_pot.$().css("background-color","rgba(255, 255, 255, 0.15)");
  if (is_game_over ()) {
    determine_winner();
  } else {
    if(isPlayerOne) {
        if (last_pot.isMan()) {
            addPotHandlers();
        } else {
            isPlayerOne = !isPlayerOne;
            addPotHandlers();
        }
    } else {
        if (last_pot.isMan()) {
            addPotHandlers();
        } else {
            isPlayerOne = !isPlayerOne;
            addPotHandlers();
        }
    }
    toggle_player_buttons_selection();
  } 
}

var update_counters = function () {
  var player_one_house_count = mancala_board[playerOneHouseIndex].get(playerOneHouseIndex);
  var player_two_house_count = mancala_board[playerTwoHouseIndex].get(playerTwoHouseIndex);
  document.getElementById("player_one_house_count").innerHTML = "Player one house count: " + player_one_house_count;
  document.getElementById("player_two_house_count").innerHTML = "Player two house count: " + player_two_house_count;
}

var string_out = function(src_pot,last_pot)
{ 
  const children = src_pot.$().children();
  if(children.length === 0)
  {
    complete_turn(src_pot, last_pot);
    return;
  }
  if(last_pot === undefined)
  {
    last_pot = src_pot;
  }
  let selected_bead = children.get(0);
  //reverse bead order when sowing has come all way back to same pot
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
    var opposite_pot = last_pot.getOpposite();
    if (opposite_pot.$().children().length > 0) {
      console.log ("Stealing " + opposite_pot.$().children().length +
      " from " + opposite_pot.id + " by " + last_pot.id);
    }
    opposite_pot.$().children().each(function(idx,stolen_bead)
    {
        if (isPlayerOne) {
            move_bead(stolen_bead, opposite_pot, new Pot('mt'), true);
        } else {
            move_bead(stolen_bead, opposite_pot, new Pot('mb'), true);
        }
    });
    if (isPlayerOne) {
        move_bead(selected_bead, src_pot, new Pot('mt'), false);
    } else {
        move_bead(selected_bead, src_pot, new Pot('mb'), false);
    }
  }
  else
  {
    if(children.length == 1)
    {
      //cater for last bead in the event it has already been moved
      if ($(selected_bead).attr('hasMoved') == "true") {
          $(selected_bead).attr('hasMoved', false);
          complete_turn(src_pot, last_pot_sown_on_completing_full_revolution);
          return;
      } else {
          if (last_pot.id == src_pot.id) {
              complete_turn(src_pot, last_pot);
              return;
          }
          move_bead(selected_bead, src_pot, last_pot, false);
      }
    } else {
        if (last_pot.id == src_pot.id) {
          //tag the selected bead and move the next bead
          $(selected_bead).attr('hasMoved', true);
          var next_bead = children.get(1);
          last_pot = last_pot.getNextSown(true);
          move_bead(next_bead, src_pot, last_pot, false);
          last_pot_sown_on_completing_full_revolution = last_pot;
        } else {
          if ($(selected_bead).attr('hasMoved') != "true") {
            move_bead(selected_bead, src_pot, last_pot, false);
            //keep track of variable in case player turn goes all way round
            last_pot_sown_on_completing_full_revolution = last_pot;
          }
        }
    }
  }
  setTimeout(string_out,400,src_pot,last_pot)
}

var addPotHandlers = function()
{
  var class_list;
  if (isPlayerOne) {
    class_list = ".topmid .pot";
    remove_action_handlers (".botmid .pot");
  } else {
    class_list = ".botmid .pot";
    remove_action_handlers (".topmid .pot");
  }
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
    string_out(new Pot($(this).attr("id")));
  });  
};

var remove_action_handlers = function (class_list) {
  $(class_list).unbind('mouseenter');
  $(class_list).unbind('mouseleave');
  $(class_list).unbind('click');
}

var restart_game = function () {
  $("div.bead").remove();
  isPlayerOne = !isPlayerOne;
  toggle_player_buttons_selection();
  addPotHandlers();
  initialiseBoard();
  update_counters();
  populate_row("pt");
  populate_row("pb");
}

$(document).ready( function()
{
  toggle_player_buttons_selection();
  addPotHandlers();
  initialiseBoard();
  populate_row("pt");
  populate_row("pb");
});
import { set_game_id_input, set_game_status_message, populate_row } from './game_helper.js';
import { start_new_game, connect_to_specific_game, restart_game } from './api.js';
import { on_page_load_string, game_id_input } from './constants.js';

import '../css/style.css';
import '../images/background.png';

const game_connect_key_down = (event) => {
    if (event.key === "Enter") {
        connect_to_specific_game();
        set_game_id_input("");
    }
}

function on_page_load () {
    set_game_status_message(on_page_load_string);
    game_id_input.addEventListener("keydown", game_connect_key_down);
    if (window.location.hash == "#_=_") {
        window.location.hash = "";
    }
}

document.addEventListener ("DOMContentLoaded", function()
{
    on_page_load();
    populate_row("pt");
    populate_row("pb");
    document.getElementById ("new_game_button").addEventListener ("click", start_new_game, false);
    document.getElementById ("connect_to_game_button").addEventListener ("click", connect_to_specific_game, false);
    document.getElementById ("game_restart_button").addEventListener ("click", restart_game, false);
 });
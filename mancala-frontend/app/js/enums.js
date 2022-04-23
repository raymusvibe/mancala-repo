'use strict';

const Player = function(){
    return {
        'ONE':"PLAYER_ONE",
        'TWO':"PLAYER_TWO"
    }
}();

const GameStatus = function(){
    return {
        'NEW':"NEW",
        'IN_PROGRESS':"IN_PROGRESS",
        'DISRUPTED':"DISRUPTED",
        'RESTARTING':"RESTARTING",
        'FINISHED':"FINISHED"
    }
}();

const GameWinner = function(){
    return {
        'DRAW':"DRAW",
        'PLAYER_ONE':"PLAYER_ONE",
        'PLAYER_TWO':"PLAYER_TWO"
    }
}();

export { Player, GameStatus, GameWinner }
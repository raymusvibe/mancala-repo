package com.bol.games.mancala.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StoneContainer implements Serializable {

    @Getter
    private int mancalaGameIndex;
    private int stones;

    @JsonIgnore
    public void addStone () {
        this.stones++;
    }

    @JsonIgnore
    public void removeStone () {
        this.stones--;
    }

    @JsonIgnore
    public void addStones (Integer stones){
        this.stones+= stones;
    }

    @JsonIgnore
    public boolean isEmpty (){
        return this.stones == 0;
    }

    @JsonIgnore
    public int getAllStonesAndEmptyContainer() {
        int allStones = this.stones;
        this.stones = 0;
        return allStones;
    }

    @Override
    public String toString() {
        return  mancalaGameIndex + ":" + stones ;
    }
}

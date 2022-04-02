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
    public void addStones (Integer stones){
        this.stones+= stones;
    }

    @JsonIgnore
    public Boolean isEmpty (){
        return this.stones == 0;
    }

    @JsonIgnore
    public int getAllStonesAndEmptyContainer() {
        int stones = this.stones;
        this.stones = 0;
        return stones;
    }

    @Override
    public String toString() {
        return  mancalaGameIndex + ":" + stones ;
    }
}

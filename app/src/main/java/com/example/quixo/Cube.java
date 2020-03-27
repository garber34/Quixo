package com.example.quixo;


import android.content.Context;
import android.util.AttributeSet;


public class Cube extends androidx.appcompat.widget.AppCompatImageView {
    public Cube(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    //the position of the plane head.
    int xPos=0,yPos=0,playerClaim=0;


    public void setPosInGrid(int x,int y) {
        xPos=x;yPos=y;
    }

    public int[] getPosInGrid() {
        int[] pos=new int[2];
        pos[0]=xPos;
        pos[1]=yPos;
        return pos;
    }

    public void setPlayer(int currentPlayer) {
        playerClaim=currentPlayer;
    }

    public int getPlayerClaim() {
        return playerClaim;
    }
}
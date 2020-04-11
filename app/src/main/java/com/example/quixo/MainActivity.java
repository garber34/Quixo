package com.example.quixo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    int currentPlayer, currentPlayerBackup;
    int[] pos = new int[2];

    Cube[][] board = new Cube[5][5];
    int[][] boardBackupPlayer = new int[5][5];
    Drawable[][] boardBackupDrawables = new Drawable[5][5];
    Cube tappedImageView;
    Cube tempCube;
    float originalX;
    float originalY;

    ImageView btnMove1, btnMove2, btnMove3;
    TextView txtPlayer;
    Button btnUndo;
    Button restartGame;

    ViewPropertyAnimator cubeMover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentPlayer = 1;
        txtPlayer = findViewById(R.id.txtPlayer);
        txtPlayer.setText("Player " + currentPlayer);

        //initialize playing board by looping through rows and columns
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 5; i++) {
                String imageViewID = "R" + j + "C" + i;
                int resID = getResources().getIdentifier(imageViewID, "id", getPackageName());
                board[j][i] = findViewById(resID);
                board[j][i].setPosInGrid(j, i);
                board[j][i].setPlayer(0);
            }
        }

        //initialize backup array of Drawables and players
        backupBoard();

        //set up undo and restart buttons
        btnUndo = findViewById(R.id.btnUndo);
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                undoChoice();
            }
        });

        restartGame = findViewById(R.id.btnRestart);
        restartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartGame();
            }
        });

    }

    public void cubeSelected(View imageView) {
        //add chosen cube's data to temp object and get grid position
        resetButtons();
        if (tappedImageView != null) tappedImageView.setImageAlpha(255);


        tappedImageView = (Cube) imageView;
        pos = tappedImageView.getPosInGrid();
        String btnMove1Name, btnMove2Name, btnMove3Name;
        int btnMove1ResID, btnMove2ResID, btnMove3ResID;

        /*check whether appropriate selection is made, if yes activate move buttons, if no give warning message:
         1) cube must be from edge of board and
         2) not claimed by opposite player
         */
        if (pos[0] != 0 && pos[0] != 4 && pos[1] != 0 && pos[1] != 4) {
            Toast.makeText(this, "Must select a cube from the edge", Toast.LENGTH_SHORT).show();
        } else if ((currentPlayer == 1 && tappedImageView.getPlayerClaim() == 2) || (currentPlayer == 2 && tappedImageView.getPlayerClaim() == 1)) {
            Toast.makeText(this, "Block already claimed", Toast.LENGTH_SHORT).show();
        } else {
            tappedImageView.setImageAlpha(0);
            Toast.makeText(this, "X:"+pos[0]+" Y:"+pos[1], Toast.LENGTH_SHORT).show();
            //check if cube is in corner, if so activate 2 opposite buttons
            if (pos[0] == 0 && pos[1] == 0) {
                btnMove1 = findViewById(R.id.UpC0);
                btnMove1.setVisibility(View.VISIBLE);
                btnMove2 = findViewById(R.id.LeftR0);
                btnMove2.setVisibility(View.VISIBLE);
            } else if (pos[0] == 0 && pos[1] == 4) {
                btnMove1 = findViewById(R.id.UpC4);
                btnMove1.setVisibility(View.VISIBLE);
                btnMove2 = findViewById(R.id.RightR0);
                btnMove2.setVisibility(View.VISIBLE);
            } else if (pos[0] == 4 && pos[1] == 0) {
                btnMove1 = findViewById(R.id.DownC0);
                btnMove1.setVisibility(View.VISIBLE);
                btnMove2 = findViewById(R.id.LeftR4);
                btnMove2.setVisibility(View.VISIBLE);
            } else if (pos[0] == 4 && pos[1] == 4) {
                btnMove1 = findViewById(R.id.DownC4);
                btnMove1.setVisibility(View.VISIBLE);
                btnMove2 = findViewById(R.id.RightR4);
                btnMove2.setVisibility(View.VISIBLE);
            } else {
                if (pos[0] == 0) {
                    btnMove1Name = "UpC" + pos[1];
                    btnMove2Name = "LeftR" + pos[0];
                    btnMove3Name = "RightR" + pos[0];
                } else if (pos[0] == 4) {
                    btnMove1Name = "DownC" + pos[1];
                    btnMove2Name = "LeftR" + pos[0];
                    btnMove3Name = "RightR" + pos[0];
                } else {
                    btnMove1Name = "UpC" + pos[1];
                    btnMove2Name = "DownC" + pos[1];
                    if (pos[1] == 0) {
                        btnMove3Name = "LeftR" + pos[0];
                    } else btnMove3Name = "RightR" + pos[0];

                }

                btnMove1ResID = getResources().getIdentifier(btnMove1Name, "id", getPackageName());
                btnMove2ResID = getResources().getIdentifier(btnMove2Name, "id", getPackageName());
                btnMove3ResID = getResources().getIdentifier(btnMove3Name, "id", getPackageName());
                btnMove1 = findViewById(btnMove1ResID);
                btnMove1.setVisibility(View.VISIBLE);

                btnMove2 = findViewById(btnMove2ResID);
                btnMove2.setVisibility(View.VISIBLE);

                btnMove3 = findViewById(btnMove3ResID);
                btnMove3.setVisibility(View.VISIBLE);
            }
        }

    }

    public void moveCubes(final View imageView) {
        backupBoard();
        tappedImageView.setImageAlpha(255);
        if (currentPlayer == 1) {
            tappedImageView.setImageResource(R.drawable.o);
            tappedImageView.setPlayer(1);
        } else {
            tappedImageView.setImageResource(R.drawable.x);
            tappedImageView.setPlayer(2);
        }
       originalX=tappedImageView.getX();
        originalY=tappedImageView.getY();

        animateMove(tappedImageView, imageView.getX(), imageView.getY());

        cubeMover.withStartAction(new Runnable() {
            @Override
            public void run() {
                setClickable(false);
            }
        }).withEndAction(new Runnable() {
            @Override
            public void run() {

            final String tag = (String) imageView.getTag();
            switch (tag){
                case "Up": animateMove(tappedImageView, board[4][pos[1]].getX(), board[4][pos[1]].getY());
                            break;
                case "Down": animateMove(tappedImageView, board[0][pos[1]].getX(),board[0][pos[1]].getY());
                            break;
                case "Right": animateMove(tappedImageView, board[pos[0]][0].getX(), board[pos[0]][0].getY());
                break;
                case "Left": animateMove(tappedImageView, board[pos[0]][4].getX(), board[pos[0]][4].getY());
                break;
            }
              cubeMover.withEndAction(new Runnable() {
                  @Override
                  public void run() {
                      actualLooping(tag);

                  }
              }).start();

            }
        }).start();



//check for winner, reset the move buttons and change the current player

    }

    public void resetButtons() {
        if (btnMove1 != null) btnMove1.setVisibility(View.INVISIBLE);
        if (btnMove2 != null) btnMove2.setVisibility(View.INVISIBLE);
        if (btnMove3 != null) btnMove3.setVisibility(View.INVISIBLE);
        btnMove1 = null;
        btnMove2 = null;
        btnMove3 = null;
    }

    public void changePlayer() {
        checkForWinner(board);
        if (currentPlayer == 1) {
            currentPlayer = 2;
        } else {
            currentPlayer = 1;
        }
        txtPlayer.setText("Player " + currentPlayer);
    }

    public boolean checkForWinner(Cube[][] board) {
        int array[] = new int[5];
        // Toast.makeText(this, "Need to implement winner check", Toast.LENGTH_SHORT).show();
        //make single array out of row, send it to be checked, if receive true return true otherwise make send next row.
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 5; i++) {
                array[i] = board[j][i].getPlayerClaim();
            }
            if (checkSingleArray(array)) return true;
        }

        // if no rows are winner, make arrays out of columns, send to be checked
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 5; i++) {
                array[i] = board[i][j].getPlayerClaim();
            }
            if (checkSingleArray(array)) return true;
        }
        //if no columns are winner, make array out of diagonal, send to be checked
        for (int j = 0; j < 5; j++) {
            array[j] = board[j][j].getPlayerClaim();
            if (checkSingleArray(array)) return true;
        }
        for (int j = 0; j < 5; j++) {
            array[j] = board[j][4 - j].getPlayerClaim();
            if (checkSingleArray(array)) return true;
        }
        return false;
    }

    public void undoChoice() {
        currentPlayer = currentPlayerBackup;
        txtPlayer.setText("Player" + currentPlayer);
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 5; i++) {

                board[j][i].setImageDrawable(boardBackupDrawables[j][i]);
                board[j][i].setPlayer(boardBackupPlayer[j][i]);
            }
        }


    }

    public void backupBoard() {

        currentPlayerBackup = currentPlayer;
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 5; i++) {
                boardBackupDrawables[j][i] = board[j][i].getDrawable();
                boardBackupPlayer[j][i] = board[j][i].getPlayerClaim();
            }
        }
    }

    public void restartGame() {
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 5; i++) {

                board[j][i].setImageResource(android.R.drawable.ic_delete);
                board[j][i].setPlayer(0);
            }
        }
        currentPlayer = 1;
        txtPlayer.setText("Player " + currentPlayer);
        tappedImageView.setImageAlpha(255);
        backupBoard();
        resetButtons();
    }

    public boolean checkSingleArray(int array[]) {
        int first = array[0];
        if (first == 0) return false;
        else {
            for (int i = 1; i < 5; i++) {
                if (array[i] != first)
                    return false;
            }
            return true;
        }
    }

    public void animateMove(Cube cubetoMove, float targetX, float targetY){

cubeMover = cubetoMove.animate()
        .x(targetX)
        .y(targetY)
        .setDuration(750);

    }

    public void actualLooping(String tag) {
        //depending on direction, loop through the cubes transferring image and player number from the previous cube
        switch (tag) {
            case "Up":
                for (int j = pos[0]; j < 4; j++) {
                    if (j == pos[0]) {
                        animateMove(board[j + 1][pos[1]], originalX, originalY);
                        board[0][pos[1]] = findViewById(board[1][pos[1]].getId());
                        board[0][pos[1]].setPosInGrid(0, pos[1]);
                    } else {
                        animateMove(board[j + 1][pos[1]], board[j][pos[1]].getX(), board[j][pos[1]].getY());
                        board[j][pos[1]] = findViewById(board[j + 1][pos[1]].getId());
                        board[j][pos[1]].setPosInGrid(j, pos[1]);
                    }

                }
                board[4][pos[1]] = findViewById(tappedImageView.getId());
                board[4][pos[1]].setPosInGrid(4, pos[1]);
                break;
            case "Down":
                for (int j = pos[0]; j > 0; j--) {
                    if (j == pos[0]) {
                        animateMove(board[j - 1][pos[1]], originalX, originalY);
                        board[4][pos[1]] = findViewById(board[3][pos[1]].getId());
                        board[4][pos[1]].setPosInGrid(4, pos[1]);
                    } else {
                        animateMove(board[j - 1][pos[1]], board[j][pos[1]].getX(), board[j][pos[1]].getY());
                        board[j][pos[1]] = findViewById(board[j - 1][pos[1]].getId());
                        board[j][pos[1]].setPosInGrid(j, pos[1]);
                    }

                }
                board[0][pos[1]] = findViewById(tappedImageView.getId());
                board[0][pos[1]].setPosInGrid(4, pos[1]);
                break;
            case "Left":
                for (int j = pos[1]; j < 4; j++) {
                    if (j == pos[1]) {
                        animateMove(board[pos[0]][j + 1], originalX, originalY);
                        board[pos[0]][0] = findViewById(board[pos[0]][1].getId());
                        board[pos[0]][0].setPosInGrid(pos[0], 0);
                    } else {
                        animateMove(board[pos[0]][j+1], board[pos[0]][j].getX(), board[pos[0]][j].getY());
                        board[pos[0]][j] = findViewById(board[pos[0]][j+1].getId());
                        board[pos[0]][j].setPosInGrid(pos[0], j);
                    }

                }
                board[pos[0]][4] = findViewById(tappedImageView.getId());
                board[pos[0]][4].setPosInGrid(pos[0], 4);
                break;
            case "Right":
                for (int j = pos[1]; j > 0; j--) {
                    if (j == pos[1]) {
                        animateMove(board[pos[0]][j - 1], originalX, originalY);
                        board[pos[0]][4] = findViewById(board[pos[0]][3].getId());
                        board[pos[0]][4].setPosInGrid(pos[0], 4);
                    } else {
                        animateMove(board[pos[0]][j-1], board[pos[0]][j].getX(), board[pos[0]][j].getY());
                        board[pos[0]][j] = findViewById(board[pos[0]][j-1].getId());
                        board[pos[0]][j].setPosInGrid(pos[0], j);
                    }

                }
                board[pos[0]][0] = findViewById(tappedImageView.getId());
                board[pos[0]][0].setPosInGrid(pos[0], 0);
                break;
        }


        setClickable(true);
        if (checkForWinner(board)) {

            final AlertDialog winnerDialog = new AlertDialog.Builder(MainActivity.this).create();
            winnerDialog.setTitle("Winner!");
            winnerDialog.setMessage("Player " + currentPlayer + " wins!");
            winnerDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Play Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            restartGame();
                            winnerDialog.dismiss();
                        }
                    }
            );
            winnerDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Exit Game", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }
            );
            winnerDialog.show();

        } else {
            resetButtons();
            changePlayer();
        }
    }

    public void setClickable (boolean bit){
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 5; i++) {
                 board[j][i].setClickable(bit);
            }
        }

    }

}

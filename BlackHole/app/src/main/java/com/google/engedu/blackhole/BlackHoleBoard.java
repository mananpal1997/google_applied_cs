/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.blackhole;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/* Class that represent the state of the game.
 * Note that the buttons on screen are not updated by this class.
 */
public class BlackHoleBoard {
    // The number of turns each player will take.
    public final static int NUM_TURNS = 10;
    // Size of the game board. Each player needs to take 10 turns and leave one empty tile.
    public final static int BOARD_SIZE = NUM_TURNS * 2 + 1;
    // Relative position of the neighbors of each tile. This is a little tricky because of the
    // triangular shape of the board.
    public final static int[][] NEIGHBORS = {{-1, -1}, {0, -1}, {-1, 0}, {1, 0}, {0, 1}, {1, 1}};
    // When we get to the Monte Carlo method, this will be the number of games to simulate.
    private static final int NUM_GAMES_TO_SIMULATE = 2000;
    // The tiles for this board.
    public BlackHoleTile[] tiles;
    // The number of the current player. 0 for user, 1 for computer.
    private int currentPlayer;
    // The value to assign to the next move of each player.
    private int[] nextMove = {1, 1};
    // A single random object that we'll reuse for all our random number needs.
    private static final Random random = new Random();

    // Constructor. Nothing to see here.
    BlackHoleBoard() {
        tiles = new BlackHoleTile[BOARD_SIZE];
        reset();
    }

    // Copy board state from another board. Usually you would use a copy constructor instead but
    // object allocation is expensive on Android so we'll reuse a board instead.
    public void copyBoardState(BlackHoleBoard other) {
        this.tiles = other.tiles.clone();
        this.currentPlayer = other.currentPlayer;
        this.nextMove = other.nextMove.clone();
    }

    // Reset this board to its default state.
    public void reset() {
        currentPlayer = 0;
        nextMove[0] = 1;
        nextMove[1] = 1;
        for (int i = 0; i < BOARD_SIZE; i++) {
            tiles[i] = null;
        }
    }

    // Translates column and row coordinates to a location in the array that we use to store the
    // board.
    protected int coordsToIndex(int col, int row) {
        return col + row * (row + 1) / 2;
    }

    // This is the inverse of the method above.
    protected Coordinates indexToCoords(int i) {
        Coordinates result = null;
        double temp = (Math.sqrt(8 * i + 1) - 1) / 2;
        int row = (int)temp;
        int col = (int)Math.ceil((temp - row) * row);
        result = new Coordinates(col, row);
        return result;
    }

    // Getter for the number of the player's next move.
    public int getCurrentPlayerValue() {
        return nextMove[currentPlayer];
    }

    // Getter for the number of the current player.
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    // Check whether the current game is over (only one blank tile).
    public boolean gameOver() {
        int empty = -1;
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (tiles[i] == null) {
                if (empty == -1) {
                    empty = i;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    // Pick a random valid move on the board. Returns the array index of the position to play.
    public int pickRandomMove() {
        ArrayList<Integer> possibleMoves = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (tiles[i] == null) {
                possibleMoves.add(i);
            }
        }
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }

    // Pick a good move for the computer to make. Returns the array index of the position to play.
    public int pickMove() {
        BlackHoleBoard copy = new BlackHoleBoard();
        ArrayList<Integer> empty_tiles = new ArrayList<>();
        HashMap<Integer, ArrayList<Integer>> possible_moves = new HashMap<>();
        // initialization
        for(int i=0; i<BOARD_SIZE; i++) possible_moves.put(i, new ArrayList<Integer>());

        for(int i=0; i<NUM_GAMES_TO_SIMULATE; i++) {
            copy.copyBoardState(this);
            for(int j=0; j<BOARD_SIZE; j++) if(copy.tiles[j] == null) empty_tiles.add(j);

            int player = 1, rand_id = (int) Math.random() * empty_tiles.size();
            int user_moves = nextMove[0], comp_moves = nextMove[1];;

            int starting_tile = empty_tiles.get(rand_id);
            empty_tiles.remove(rand_id);

            copy.tiles[starting_tile] = new BlackHoleTile(player, player == 1 ? comp_moves : user_moves);
            comp_moves++; player = 0;

            for(int j=0; j<empty_tiles.size(); j++) {
                rand_id = (int) Math.random() * empty_tiles.size();
                int rand_tile = empty_tiles.get(rand_id);
                copy.tiles[rand_tile] = new BlackHoleTile(player, player == 1 ? comp_moves : user_moves);
                empty_tiles.remove(rand_id);

                if(player == 0) user_moves++; else comp_moves++;
                player = (player == 1) ? 0 : 1;
            }

            ArrayList<Integer> score_list = possible_moves.get(starting_tile);
            score_list.add(copy.getScore());
            possible_moves.put(starting_tile, score_list);
        }

        int score = Integer.MAX_VALUE, move = -1;
        for(int i=0; i<BOARD_SIZE; i++) {
            ArrayList<Integer> temp = possible_moves.get(i);
            if(tiles[i] == null && !temp.isEmpty()) {
                int avg_score = 0;
                for(int s:temp) avg_score += s;
                avg_score /= temp.size();

                if(avg_score < score) {
                    score = avg_score;
                    move = i;
                }
            }
        }

        return move;
    }

    // Makes the next move on the board at position i. Automatically updates the current player.
    public void setValue(int i) {
        tiles[i] = new BlackHoleTile(currentPlayer, nextMove[currentPlayer]);
        nextMove[currentPlayer]++;
        currentPlayer++;
        currentPlayer %= 2;
    }

    /* If the game is over, computes the score for the current board by adding up the values of
     * all the tiles that surround the empty tile.
     * Otherwise, returns 0.
     */
    public int getScore() {
        int score = 0;

        ArrayList<BlackHoleTile> tiles_sucked = new ArrayList<>();
        for(int i=0; i<BOARD_SIZE; i++)
            if(tiles[i] == null) { tiles_sucked = getNeighbors(indexToCoords(i)); break; }

        for(int i=0; i<tiles_sucked.size(); i++)
            score += (tiles_sucked.get(i).player == 0) ? -tiles_sucked.get(i).value : tiles_sucked.get(i).value;

        return score;
    }

    // Helper for getScore that finds all the tiles around the given coordinates.
    private ArrayList<BlackHoleTile> getNeighbors(Coordinates coords) {
        ArrayList<BlackHoleTile> result = new ArrayList<>();
        for(int[] pair : NEIGHBORS) {
            BlackHoleTile n = safeGetTile(coords.x + pair[0], coords.y + pair[1]);
            if (n != null) {
                result.add(n);
            }
        }
        return result;
    }

    // Helper for getNeighbors that gets a tile at the given column and row but protects against
    // array over/underflow.
    private BlackHoleTile safeGetTile(int col, int row) {
        if (row < 0 || col < 0 || col > row) {
            return null;
        }
        int index = coordsToIndex(col, row);
        if (index >= BOARD_SIZE) {
            return null;
        }
        return tiles[index];
    }
}

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

package com.google.engedu.puzzle8;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;

import java.util.ArrayList;


public class PuzzleBoard {

    private static final int NUM_TILES = 3;
    private static final int[][] NEIGHBOUR_COORDS = {
            { -1, 0 },
            { 1, 0 },
            { 0, -1 },
            { 0, 1 }
    };
    private ArrayList<PuzzleTile> tiles;
    private PuzzleBoard previous;
    private int steps;

    public PuzzleBoard getPrevious() { return previous; }

    PuzzleBoard(Bitmap bitmap, int parentWidth) {
        steps = 0;
        previous = null;
        tiles = new ArrayList<>();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, parentWidth, parentWidth, true);
        for (int y=0; y<NUM_TILES; y++) {
            for (int x=0; x<NUM_TILES; x++) {
                int num = y * NUM_TILES + x;
                if (num != NUM_TILES * NUM_TILES - 1) {
                    Bitmap tileBitmap = Bitmap.createBitmap(
                            scaledBitmap,
                            x * scaledBitmap.getWidth() / NUM_TILES,
                            y * scaledBitmap.getHeight() / NUM_TILES,
                            parentWidth / NUM_TILES,
                            parentWidth / NUM_TILES
                    );
                    PuzzleTile tile = new PuzzleTile(tileBitmap, num);
                    tiles.add(tile);
                } else  tiles.add(null);
            }
        }
    }

    PuzzleBoard(PuzzleBoard otherBoard, int steps) {
        previous = otherBoard;
        this.steps = steps + 1;
        tiles = (ArrayList<PuzzleTile>) otherBoard.tiles.clone();
    }

    public void reset() {
        // Nothing for now but you may have things to reset once you implement the solver.
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return tiles.equals(((PuzzleBoard) o).tiles);
    }

    public void draw(Canvas canvas) {
        if (tiles == null) {
            return;
        }
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                tile.draw(canvas, i % NUM_TILES, i / NUM_TILES);
            }
        }
    }

    public boolean click(float x, float y) {
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                if (tile.isClicked(x, y, i % NUM_TILES, i / NUM_TILES)) {
                    return tryMoving(i % NUM_TILES, i / NUM_TILES);
                }
            }
        }
        return false;
    }

    private boolean tryMoving(int tileX, int tileY) {
        for (int[] delta : NEIGHBOUR_COORDS) {
            int nullX = tileX + delta[0];
            int nullY = tileY + delta[1];
            if (nullX >= 0 && nullX < NUM_TILES && nullY >= 0 && nullY < NUM_TILES &&
                    tiles.get(XYtoIndex(nullX, nullY)) == null) {
                swapTiles(XYtoIndex(nullX, nullY), XYtoIndex(tileX, tileY));
                return true;
            }

        }
        return false;
    }

    public boolean resolved() {
        for (int i = 0; i < NUM_TILES * NUM_TILES - 1; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile == null || tile.getNumber() != i)
                return false;
        }
        return true;
    }

    private int XYtoIndex(int x, int y) {
        return x + y * NUM_TILES;
    }

    protected void swapTiles(int i, int j) {
        PuzzleTile temp = tiles.get(i);
        tiles.set(i, tiles.get(j));
        tiles.set(j, temp);
    }

    public ArrayList<PuzzleBoard> neighbours() {
        ArrayList<PuzzleBoard> configurations = new ArrayList<>();
        int empty_tile = -1;
        for(int i=0; i<tiles.size(); i++) if(tiles.get(i) == null) { empty_tile = i; break; }
        int x = empty_tile / NUM_TILES, y = empty_tile % NUM_TILES;
        for(int i=0; i<4; i++) {
            int xx = NEIGHBOUR_COORDS[i][0] + x, yy = NEIGHBOUR_COORDS[i][1] + y;
            if(xx >= 0 && xx < NUM_TILES && yy >= 0 && yy < NUM_TILES) {
                PuzzleBoard board_copy = new PuzzleBoard(this, steps);
                PuzzleTile temp = board_copy.tiles.get(XYtoIndex(xx, yy));
                board_copy.tiles.set(XYtoIndex(xx, yy), null);
                board_copy.tiles.set(empty_tile, temp);
                configurations.add(board_copy);
            }
        }
        return configurations;
    }

    public int priority() {
        int manhatten_dist = steps;
        for(int i=0; i<NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if(tile != null) {
                int x = tile.getX(), y = tile.getY();
                int x_ = i % 3, y_ = i / 3;
                manhatten_dist += Math.abs(x - x_) + Math.abs(y - y_);
            }
        }
        return manhatten_dist;
    }

}

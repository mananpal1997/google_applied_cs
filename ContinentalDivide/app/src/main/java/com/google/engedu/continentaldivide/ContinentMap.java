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

package com.google.engedu.continentaldivide;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;


public class ContinentMap extends View {
    public static final int MAX_HEIGHT = 255;
    private Cell[] map;
    private int boardSize;
    private Random random = new Random();
    private int maxHeight = 0, minHeight = 0;

    private Integer[] DEFAULT_MAP = {
            1, 2, 3, 4, 5,
            2, 3, 4, 5, 6,
            3, 4, 5, 3, 1,
            6, 7, 3, 4, 5,
            5, 1, 2, 3, 4,
    };

    public ContinentMap(Context context) {
        super(context);

        boardSize = (int) (Math.sqrt(DEFAULT_MAP.length));
        map = new Cell[boardSize * boardSize];
        for (int i = 0; i < boardSize * boardSize; i++) {
            map[i] = new Cell();
            map[i].height = DEFAULT_MAP[i];
        }
        maxHeight = Collections.max(Arrays.asList(DEFAULT_MAP));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width;
        setMeasuredDimension(size, size);
    }

    private class Cell {
        protected int height = 0;
        protected boolean flowsNW = false;
        protected boolean flowsSE = false;
        protected boolean basin = false;
        protected boolean processing = false;
    }

    private Cell getMap(int x, int y) {
        if (x >=0 && x < boardSize && y >= 0 && y < boardSize)
            return map[x + boardSize * y];
        else
            return null;
    }

    public void clearContinentalDivide() {
        for (int i = 0; i < boardSize * boardSize; i++) {
            map[i].flowsNW = false;
            map[i].flowsSE = false;
            map[i].basin = false;
            map[i].processing = false;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        maxHeight = -1; minHeight = Integer.MAX_VALUE;
        for(int i=0; i<boardSize * boardSize; i++) {
            maxHeight = Math.max(maxHeight, getMap(i / boardSize, i % boardSize).height);
            minHeight = Math.min(minHeight, getMap(i / boardSize, i % boardSize).height);
        }

        int maxd = 127 / (maxHeight - minHeight);
        int cellWidth = getWidth() / boardSize;

        Paint paint = new Paint();
        paint.setTextSize((int)(cellWidth - cellWidth / 2.5));
        int r, g, b;

        for(int i=0; i<boardSize; i++) {
            for(int j=0; j<boardSize; j++) {
                r = g = b = 255 - maxd * getMap(i, j).height;

                if(getMap(i, j).flowsNW && getMap(i, j).flowsSE) g = b = 0;
                else if(getMap(i, j).flowsNW) r = b = 0;
                else if(getMap(i, j).flowsSE) r = g = 0;

                paint.setColor(Color.rgb(r, g, b));
                canvas.drawRect(j * cellWidth, i * cellWidth, (j + 1) * cellWidth, (i + 1) * cellWidth, paint);
                paint.setColor(Color.BLACK);
                canvas.drawText(
                        "" + getMap(i, j).height,
                        cellWidth * (3.0f * j + 1) / 3.0f,
                        cellWidth * (4.0f * i + 3) / 4.0f,
                        paint
                );
            }
        }
    }

    public void buildUpContinentalDivide(boolean oneStep) {
        if (!oneStep)
            clearContinentalDivide();
        boolean iterated = false;
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                Cell cell = getMap(x, y);
                if ((x == 0 || y == 0 || x == boardSize - 1 || y == boardSize - 1)) {
                    buildUpContinentalDivideRecursively(
                            x, y, x == 0 || y == 0, x == boardSize - 1 || y == boardSize - 1, -1);
                    if (oneStep) {
                        iterated = true;
                        break;
                    }
                }
            }
            if (iterated && oneStep)
                break;
        }
        invalidate();
    }

    public boolean isMaxHeight(int x, int y) {
        if(isValid(x + 1, y) && getMap(x + 1, y).height > getMap(x, y).height) return false;
        if(isValid(x - 1, y) && getMap(x - 1, y).height > getMap(x, y).height) return false;
        if(isValid(x, y + 1) && getMap(x, y + 1).height > getMap(x, y).height) return false;
        if(isValid(x, y - 1) && getMap(x, y - 1).height > getMap(x, y).height) return false;
        return true;
    }

    private void buildUpContinentalDivideRecursively(
            int x, int y, boolean flowsNW, boolean flowsSE, int previousHeight) {
        Cell cell = getMap(x, y);

        if(cell == null) return;
        if(cell.processing || cell.height < previousHeight) return;

        cell.flowsNW = flowsNW || cell.flowsNW;
        cell.flowsSE = flowsSE || cell.flowsSE;
        cell.processing = (cell.flowsNW && cell.flowsSE) || cell.processing;

        buildUpContinentalDivideRecursively(x - 1, y, cell.flowsNW, cell.flowsSE, cell.height);
        buildUpContinentalDivideRecursively(x + 1, y, cell.flowsNW, cell.flowsSE, cell.height);
        buildUpContinentalDivideRecursively(x, y - 1, cell.flowsNW, cell.flowsSE, cell.height);
        buildUpContinentalDivideRecursively(x, y + 1, cell.flowsNW, cell.flowsSE, cell.height);
    }

    public void buildDownContinentalDivide(boolean oneStep) {
        if (!oneStep)
            clearContinentalDivide();
        while (true) {
            int maxUnprocessedX = -1, maxUnprocessedY = -1, foundMaxHeight = -1;
            for (int y = 0; y < boardSize; y++) {
                for (int x = 0; x < boardSize; x++) {
                    Cell cell = getMap(x, y);
                    if (!(cell.flowsNW || cell.flowsSE || cell.basin) && cell.height > foundMaxHeight) {
                        maxUnprocessedX = x;
                        maxUnprocessedY = y;
                        foundMaxHeight = cell.height;
                    }
                }
            }
            if (maxUnprocessedX != -1) {
                buildDownContinentalDivideRecursively(maxUnprocessedX, maxUnprocessedY, foundMaxHeight + 1);
                if (oneStep) {
                    break;
                }
            } else {
                break;
            }
        }
        invalidate();
    }

    public boolean isValid(int x, int y) {
        if(x > boardSize -1 || x < 0 || y > boardSize -1 || y < 0) return false;
        return true;
    }

    private Cell buildDownContinentalDivideRecursively(int x, int y, int previousHeight) {
        Cell workingCell;

        if(getMap(x, y) == null) return new Cell();
        if(getMap(x, y).processing) return getMap(x, y);
        if(getMap(x, y).height > previousHeight) return new Cell();
        getMap(x, y).flowsNW = (x == 0) || (y == 0) || getMap(x, y).flowsNW;
        getMap(x, y).flowsSE = (x == boardSize-1) || (y == boardSize - 1) || getMap(x, y).flowsSE;

        workingCell = buildDownContinentalDivideRecursively(x + 1, y, getMap(x, y).height);
        getMap(x, y).flowsNW = getMap(x, y).flowsNW || workingCell.flowsNW;
        getMap(x, y).flowsSE = getMap(x, y).flowsSE || workingCell.flowsSE;

        workingCell = buildDownContinentalDivideRecursively(x, y + 1, getMap(x, y).height);
        getMap(x, y).flowsNW = getMap(x, y).flowsNW || workingCell.flowsNW;
        getMap(x, y).flowsSE = getMap(x, y).flowsSE || workingCell.flowsSE;

        workingCell = buildDownContinentalDivideRecursively(x - 1, y, getMap(x, y).height);
        getMap(x, y).flowsNW = getMap(x, y).flowsNW || workingCell.flowsNW;
        getMap(x, y).flowsSE = getMap(x, y).flowsSE || workingCell.flowsSE;

        workingCell = buildDownContinentalDivideRecursively(x, y - 1, getMap(x, y).height);
        getMap(x, y).flowsNW = getMap(x, y).flowsNW || workingCell.flowsNW;
        getMap(x, y).flowsSE = getMap(x, y).flowsSE || workingCell.flowsSE;

        getMap(x, y).processing = true;

        return getMap(x, y);
    }

    public boolean squareSizeTooSmall(int topLeft, int topRight, int bottomLeft, int bottomRight) {
        return (topRight - topLeft+1)*((bottomRight/boardSize - topLeft/boardSize) + 1) < 9;
    }

    public void diamondSquare(Cell[] map, int topLeft, int topRight, int bottomLeft, int bottomRight) {
        if(squareSizeTooSmall(topLeft,topRight, bottomLeft,bottomRight)) return;

        double mid_avg = map[topLeft].height + map[topRight].height + map[bottomLeft].height + map[bottomRight].height;
        mid_avg /= 4.0;

        int middleElementX = (bottomRight / boardSize + topRight / boardSize) / 2;
        int middleElementY = (topLeft % boardSize + topRight % boardSize) / 2;

        int middleElement       = middleElementX * boardSize + middleElementY;
        int topMiddleElement    = (topRight + topLeft) / 2;
        int bottomMiddleElement = (bottomRight + bottomLeft) / 2;
        int rightMiddleElement  = middleElement + (topRight - topLeft) / 2;
        int leftMiddleElement   = middleElement - (topRight - topLeft) / 2;


        map[middleElement].height       = (int) mid_avg + random.nextInt(10);
        map[topMiddleElement].height    = (map[topRight].height    + map[topLeft].height    ) / 2 + random.nextInt(8);
        map[bottomMiddleElement].height = (map[bottomRight].height + map[bottomLeft].height ) / 2 + random.nextInt(6);
        map[rightMiddleElement].height  = (map[topRight].height    + map[bottomRight].height) / 2 + random.nextInt(5);
        map[leftMiddleElement].height   = (map[topLeft].height     + map[bottomLeft].height ) / 2 + random.nextInt(3);

        diamondSquare(map, topLeft,           topMiddleElement,   middleElement,       leftMiddleElement);
        diamondSquare(map, middleElement,     rightMiddleElement, bottomRight,         bottomMiddleElement);
        diamondSquare(map, topMiddleElement,  topRight,           rightMiddleElement,  middleElement);
        diamondSquare(map, leftMiddleElement, middleElement,      bottomMiddleElement, bottomLeft);
    }

    public void generateTerrain(int detail) {
        int newBoardSize = (int) (Math.pow(2, detail) + 1);
        if (newBoardSize != boardSize * boardSize) {
            boardSize = newBoardSize;
            map = new Cell[boardSize * boardSize];
            for (int i = 0; i < boardSize * boardSize; i++) {
                map[i] = new Cell();
            }
        }

        map[0].height                               = random.nextInt(255);
        map[boardSize - 1].height                   = random.nextInt(255);
        map[boardSize*boardSize -1].height          = random.nextInt(255);
        map[boardSize*boardSize - boardSize].height = random.nextInt(255);

        diamondSquare(map, 0, boardSize - 1, boardSize * boardSize - boardSize, boardSize * boardSize - 1);

        invalidate();
    }
}

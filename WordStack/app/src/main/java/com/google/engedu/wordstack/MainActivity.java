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

package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private Stack<LetterTile> placedTiles;
    private String word1, word2, madeWord1, madeWord2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();
                if(word.length() == WORD_LENGTH) words.add(word);
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }
        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        placedTiles = new Stack<>();
        verticalLayout.addView(stackedLayout, 3);
        madeWord1 = ""; madeWord2 = "";

        View word1LinearLayout = findViewById(R.id.word1);
//        word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
//        word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                }
                placedTiles.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    if(v.getId() == R.id.word1) {
                        madeWord1 += tile.getLetter();
                        tile.moveToViewGroup((ViewGroup) v);
                    }
                    else {
                        madeWord2 += tile.getLetter();
                        tile.moveToViewGroup((ViewGroup) v);
                    }
                    if(stackedLayout.empty()) checkWin();
                    placedTiles.push(tile);
                    return true;
            }
            return false;
        }
    }

    public boolean onStartGame(View view) {
        stackedLayout.clear();
        placedTiles.clear();
        ((ViewGroup) findViewById(R.id.word1)).removeAllViews();
        ((ViewGroup) findViewById(R.id.word2)).removeAllViews();
        madeWord1 = ""; madeWord1 = "";
        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");
        int idx1 = (int) (Math.random() * words.size());
        int idx2 = (int) (Math.random() * words.size());
        while(idx1 == idx2) idx2 = (int) (Math.random() * words.size());
        word1 = words.get(idx1); word2 = words.get(idx2);

        String jumbled = "";
        int i=0, j=0,_;

        while(i < word1.length() && j < word2.length()) {
            _ = (int) (Math.random() * 50);
            if(_ % 2 == 1) { jumbled += word1.charAt(i); i++; }
            else { jumbled += word2.charAt(j); j++; }
        }
        while(i < word1.length()) { jumbled += word1.charAt(i); i++; }
        while(j < word2.length()) { jumbled += word2.charAt(j); j++; }

        messageBox.setText(jumbled);
        for(i=jumbled.length()-1; i>=0; i--) {
            LetterTile lt = new LetterTile(getApplicationContext(), jumbled.charAt(i));
            stackedLayout.push(lt);
        }

        return true;
    }

    public boolean onUndo(View view) {
        if(!placedTiles.isEmpty()) {
            if(((View)placedTiles.peek().getParent()).getId() == R.id.word1)
                madeWord1 = new StringBuilder(madeWord1).deleteCharAt(madeWord1.length()-1).toString();
            else
                madeWord2 = new StringBuilder(madeWord2).deleteCharAt(madeWord2.length()-1).toString();
            placedTiles.pop().moveToViewGroup(stackedLayout);
        }
        return true;
    }

    public void checkWin() {
        TextView messageBox = (TextView) findViewById(R.id.message_box);
        if(word1.equals(madeWord1) && word2.equals(madeWord2)) messageBox.setText("You win! " + word1 + " " + word2);
        else if(word1.equals(madeWord2) && word2.equals(madeWord1)) messageBox.setText("You win! " + word2 + " " + word1);
        else if(words.contains(madeWord1) && words.contains(madeWord2)) messageBox.setText("You found alternative words! " + madeWord1+ " " + madeWord2);
        else messageBox.setText("Try again");
    }
}

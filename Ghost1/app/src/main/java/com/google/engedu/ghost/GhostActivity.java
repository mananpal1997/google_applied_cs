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

package com.google.engedu.ghost;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;


public class GhostActivity extends AppCompatActivity {
    private static final String COMPUTER_TURN = "Computer's turn";
    private static final String USER_TURN = "Your turn";
    private GhostDictionary dictionary;
    private boolean userTurn = false;
    private Random random = new Random();
    TextView text, status;
    Button challenge, reset;
    public String fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghost);
        AssetManager assetManager = getAssets();

        fragment = "";

        text = (TextView) findViewById(R.id.ghostText);

        status = (TextView) findViewById(R.id.gameStatus);

        challenge = (Button) findViewById(R.id.button);
        challenge.setOnClickListener(challenge_handler);

        reset = (Button) findViewById(R.id.button2);

        InputStream inputStream = null;
        try {
            inputStream = assetManager.open("words.txt");
            dictionary = new SimpleDictionary(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        onStart(null);
    }

    View.OnClickListener challenge_handler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(dictionary.isWord(fragment) && fragment.length() >= 4) status.setText("You Win!");
            else if(dictionary.getAnyWordStartingWith(fragment) == null) status.setText("You Win!");
            else {
                text.setText(dictionary.getAnyWordStartingWith(fragment));
                status.setText("Computer Wins!");
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ghost, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handler for the "Reset" button.
     * Randomly determines whether the game starts with a user turn or a computer turn.
     * @param view
     * @return true
     */
    public boolean onStart(View view) {
        fragment = "";
        userTurn = random.nextBoolean();
        text.setText("");
        if (userTurn) {
            status.setText(USER_TURN);
        } else {
            status.setText(COMPUTER_TURN);
            computerTurn();
        }
        return true;
    }

    private void computerTurn() {
        // Do computer turn stuff then make it the user's turn again
        if(dictionary.isWord(fragment) && fragment.length() >= 4) {
            status.setText("Computer Wins!");
            return;
        }
        String res = dictionary.getAnyWordStartingWith(fragment);
        if(res == null) {
            status.setText("Computer Wins!");
            return;
        } else {
            fragment += res.charAt(fragment.length());
            text.append("" + res.charAt(fragment.length()));
            userTurn = true;
            status.setText(USER_TURN);
        }
    }

    /**
     * Handler for user key presses.
     * @param keyCode
     * @param event
     * @return whether the key stroke was handled.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z && userTurn) {
            int ascii = event.getUnicodeChar();
            if(ascii < 97) ascii += 32;
            text.append("" + (char)ascii);
            fragment += (char)ascii;

            userTurn = false;
            status.setText(COMPUTER_TURN);
            computerTurn();
        }
        return super.onKeyUp(keyCode, event);
    }
}

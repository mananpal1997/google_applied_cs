package com.ghostman.dicegame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private int user_score = 0, user_cur_score = 0;
    private int max_clicks = 50;

    Button roll, hold, reset;
    TextView t;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roll = (Button) findViewById(R.id.button);
        roll.setOnClickListener(roller);

        hold = (Button) findViewById(R.id.button2);
        hold.setOnClickListener(holder);

        reset = (Button) findViewById(R.id.button3);
        reset.setOnClickListener(resetter);

        Toast.makeText(this, "You've to maximize the score. You'll get 50 clicks.", Toast.LENGTH_LONG).show();
    }

    View.OnClickListener roller = new View.OnClickListener() {
        public void onClick(View v) {
            int num = (int) (Math.random() * 100);
            num = (num % 6) + 1;
            img = (ImageView) findViewById(R.id.imageView3);
            switch (num) {
                case 1: img.setImageResource(R.drawable.dice1); break;
                case 2: img.setImageResource(R.drawable.dice2); break;
                case 3: img.setImageResource(R.drawable.dice3); break;
                case 4: img.setImageResource(R.drawable.dice4); break;
                case 5: img.setImageResource(R.drawable.dice5); break;
                case 6: img.setImageResource(R.drawable.dice6); break;
                default: break;
            };
            if(num != 1) user_cur_score += num;
            else user_cur_score = 0;
            TextView t = (TextView) findViewById(R.id.textView);
            t.setText("Your score: " + user_score +
                    ", Your turn score: " + user_cur_score
            );

            max_clicks--;
            if(max_clicks == 0) {
                Toast.makeText(getApplicationContext(), "Game Over. Your final score is " + user_score, Toast.LENGTH_LONG).show();
                user_cur_score = user_score = 0;
                max_clicks = 50;
                t.setText("Your score: " + user_score +
                        ", Your turn score: " + user_cur_score
                );
            }
        }
    };

    View.OnClickListener holder = new View.OnClickListener() {
        public void onClick(View v) {
            user_score += user_cur_score;
            user_cur_score = 0;
            TextView t = (TextView) findViewById(R.id.textView);
            t.setText("Your score: " + user_score +
                    ", Your turn score: " + user_cur_score
            );

            max_clicks--;
            if(max_clicks == 0) {
                Toast.makeText(getApplicationContext(), "Game Over. Your final score is " + user_score, Toast.LENGTH_LONG).show();
                user_cur_score = user_score = 0;
                max_clicks = 50;
                t.setText("Your score: " + user_score +
                        ", Your turn score: " + user_cur_score
                );
            }
        }
    };

    View.OnClickListener resetter = new View.OnClickListener() {
        public void onClick(View v) {
            user_cur_score = user_score = 0;
            max_clicks = 50;
            t = (TextView) findViewById(R.id.textView);
            t.setText("Your score: " + user_score +
                    ", Your turn score: " + user_cur_score
            );
        }
    };
}

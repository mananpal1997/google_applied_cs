package com.google.engedu.worldladder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;

public class SolverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_solve);

        Intent intent = getIntent();
        final ArrayList<String> words = intent.getStringArrayListExtra("words");

        LinearLayout layout =(LinearLayout)findViewById(R.id.activity_solver);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView startTextView = new TextView(this);
        startTextView.setText(words.get(0));
        layout.addView(startTextView);

        View.OnFocusChangeListener listener;

        listener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                int vId= v.getId();
                EditText editText = (EditText)findViewById(vId);
                if(editText.getText().toString().equals(words.get(vId)))
                    editText.setTextColor(GREEN);
                else
                    editText.setTextColor(RED);
            }
        };


        for(int i=0;i<words.size()-2;i++)
        {
            EditText editText = new EditText(this);
            editText.setId(i+1);
            editText.setLayoutParams(params);
            editText.setOnFocusChangeListener(listener);
            layout.addView(editText);
        }
        TextView endTextView = new TextView(this);
        endTextView.setText(words.get(words.size()-1));
        layout.addView(endTextView);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        Button solverButton = new Button(this);
        solverButton.setText("Solve");
        solverButton.setLayoutParams(buttonParams);
        solverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0;i<words.size()-2;i++)
                {
                    EditText editText = (EditText)findViewById(i+1);
                    editText.setText(words.get(i+1));
                }
            }
        });
        layout.addView(solverButton);

    }
}
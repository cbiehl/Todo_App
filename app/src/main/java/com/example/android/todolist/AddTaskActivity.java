package com.example.android.todolist;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.android.todolist.data.TaskContract;

import java.util.ArrayList;


public class AddTaskActivity extends AppCompatActivity {

    // Declare a member variable to keep track of a task's selected mPriority
    private int mPriority;
    private String mDescription;
    private String mTaskID;
    private EditText et_description;
    private ImageView iv_mic;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        et_description = (EditText) findViewById(R.id.editTextTaskDescription);
        iv_mic = (ImageView) findViewById(R.id.micbutton);

        // Check if an existing view is edited or a new one is created
        Intent intent = getIntent();
        if(!(intent.getStringExtra(TaskContract.TaskEntry._ID).equals(getString(R.string.emptyID)))){
            mTaskID = intent.getStringExtra(TaskContract.TaskEntry._ID);
            mDescription = intent.getStringExtra(TaskContract.TaskEntry.COLUMN_DESCRIPTION);
            mPriority = intent.getIntExtra(TaskContract.TaskEntry.COLUMN_PRIORITY, 1);

            // Edit task entry
            et_description.setText(mDescription);

            switch(mPriority){
                case 1:
                    ((RadioButton) findViewById(R.id.radButton1)).setChecked(true);
                    break;
                case 2:
                    ((RadioButton) findViewById(R.id.radButton2)).setChecked(true);
                    break;
                case 3:
                    ((RadioButton) findViewById(R.id.radButton3)).setChecked(true);
                    break;
                default:
                    ((RadioButton) findViewById(R.id.radButton1)).setChecked(true);
                    break;
            }

        }else{
            // Initialize to highest mPriority by default (mPriority = 1)
            mTaskID = getString(R.string.emptyID);
            ((RadioButton) findViewById(R.id.radButton1)).setChecked(true);
            mPriority = 1;
        }

        iv_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(AddTaskActivity.this,
                                                new String[]{
                                                Manifest.permission.RECORD_AUDIO}, 0);
                                                //Manifest.permission.INTERNET,
                                                //Manifest.permission.ACCESS_NETWORK_STATE}, 0);

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication().getPackageName());

                SpeechRecognizer recognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                RecognitionListener listener = new RecognitionListener() {
                    @Override
                    public void onReadyForSpeech(Bundle params) {

                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        iv_mic.setBackgroundColor(Color.RED);
                    }

                    @Override
                    public void onRmsChanged(float rmsdB) {

                    }

                    @Override
                    public void onBufferReceived(byte[] buffer) {

                    }

                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onEndOfSpeech() {
                        int color = Color.TRANSPARENT;
                        Drawable background = AddTaskActivity.this.getWindow().getDecorView().getBackground();
                        if (background instanceof ColorDrawable)
                            color = ((ColorDrawable) background).getColor();
                        iv_mic.setBackgroundColor(color);
                    }

                    @Override
                    public void onError(int error) {
                        Snackbar.make(findViewById(R.id.linearlayout), R.string.nospeechrecognized, Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResults(Bundle results) {
                        ArrayList<String> voiceResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                        if(voiceResults == null || voiceResults.isEmpty())
                            Snackbar.make(findViewById(R.id.linearlayout), R.string.nospeechrecognized, Snackbar.LENGTH_LONG).show();

                        et_description.setText(voiceResults.get(0));
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {

                    }

                    @Override
                    public void onEvent(int eventType, Bundle params) {

                    }
                };

                recognizer.setRecognitionListener(listener);
                recognizer.startListening(intent);
            }
        });
    }


    /**
     * onClickAddTask is called when the "ADD" button is clicked.
     * It retrieves user input and inserts that new task data into the underlying database.
     */
    public void onClickAddTask(View view) {
        // Check if EditText is empty, if not retrieve input and store it in a ContentValues object
        // If the EditText input is empty -> don't create an entry
        String input = ((EditText) findViewById(R.id.editTextTaskDescription)).getText().toString();
        if (input.length() == 0) {
            return;
        }

        // Insert new task data via a ContentResolver
        // Create new empty ContentValues object
        ContentValues contentValues = new ContentValues();
        // Put the task description and selected mPriority into the ContentValues
        contentValues.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, input);
        contentValues.put(TaskContract.TaskEntry.COLUMN_PRIORITY, mPriority);
        // Insert or update the content values via a ContentResolver
        if(mTaskID.equals(getString(R.string.emptyID))){
            getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, contentValues);
            Snackbar.make(view, getString(R.string.insert_success), Snackbar.LENGTH_SHORT);
        }else{
            int updatedLines = getContentResolver().update(TaskContract.TaskEntry.CONTENT_URI, contentValues, TaskContract.TaskEntry._ID, new String[]{mTaskID});
            if(updatedLines > 0) {
                Snackbar.make(view, getString(R.string.update_success), Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }
        }

        // Finish activity (this returns back to MainActivity)
        finish();

    }


    /**
     * onPrioritySelected is called whenever a priority button is clicked.
     * It changes the value of mPriority based on the selected button.
     */
    public void onPrioritySelected(View view) {
        if (((RadioButton) findViewById(R.id.radButton1)).isChecked()) {
            mPriority = 1;
        } else if (((RadioButton) findViewById(R.id.radButton2)).isChecked()) {
            mPriority = 2;
        } else if (((RadioButton) findViewById(R.id.radButton3)).isChecked()) {
            mPriority = 3;
        }
    }

    @Override
    public void onBackPressed() {
        //check if description or priority has changed before asking whether to save
        boolean priorityChecked = false;
        switch(mPriority){
            case 1:
                if(((RadioButton)findViewById(R.id.radButton1)).isChecked())
                    priorityChecked = true;
                break;
            case 2:
                if(((RadioButton)findViewById(R.id.radButton2)).isChecked())
                    priorityChecked = true;
                break;
            case 3:
                if(((RadioButton)findViewById(R.id.radButton3)).isChecked())
                    priorityChecked = true;
                break;
        }

        if(priorityChecked && et_description.getText().toString().equals(mDescription)){
            NavUtils.navigateUpFromSameTask(AddTaskActivity.this);
            return;
        }

        // Ask whether to save the entered data
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.backbuttonmessage))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        onClickAddTask(findViewById(R.id.addButton));
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        NavUtils.navigateUpFromSameTask(AddTaskActivity.this);
                    }
                })
                .show();
    }
}

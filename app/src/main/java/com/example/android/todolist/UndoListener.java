package com.example.android.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.view.View;

import com.example.android.todolist.data.TaskContract;

/**
 * Created by D062629 on 06.06.2017.
 */

public class UndoListener implements View.OnClickListener
{
    private Activity mActivity;
    private LoaderManager mSupportLoaderManager;
    private String mDescription;
    private int mPriority;

    public UndoListener(Activity activity, LoaderManager loaderManager, String text, int priority){
        mActivity = activity;
        mSupportLoaderManager = loaderManager;
        mDescription = text;
        mPriority = priority;
    }

    @Override
    public void onClick(View v) {
        ContentValues contentValues = new ContentValues();
        // Put the description and selected priority into the ContentValues
        contentValues.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, mDescription);
        contentValues.put(TaskContract.TaskEntry.COLUMN_PRIORITY, mPriority);
        // Insert or update the content values via a ContentResolver
        mActivity.getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, contentValues);

        mSupportLoaderManager.restartLoader(MainActivity.TASK_LOADER_ID, null, (LoaderManager.LoaderCallbacks<Cursor>) mActivity);
    }
}

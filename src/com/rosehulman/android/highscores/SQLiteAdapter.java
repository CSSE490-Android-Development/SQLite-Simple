package com.rosehulman.android.highscores;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;


public class SQLiteAdapter {
    
    private static final String TAG = "HighScores.Database"; // Just the tag we use to log
    
    private static final String DATABASE_NAME = "highscores"; // Becomes the filename of the database
    private static final int DATABASE_VERSION = 1; // We increment this every time we change the database schema
                                                   // which will kick off an automatic upgrade
    private SQLiteOpenHelper mOpenHelper; // Our special object that helps us open the database
    private SQLiteDatabase mDb; // The actual database we're dealing with (once it's open)
    
    private String INSERT_SCORE_QUERY = "INSERT INTO scores VALUES(?, ?)"; // We'll use these later to add scores to the database
    private SQLiteStatement mInsertScoreQuery;
    
    // ====================================================================================================================
    // INNER HELPER CLASS
    // ====================================================================================================================
    
    /**
     * This class makes it really easy to open, create, and upgrade databases.
     */
    private static class OpenHelper extends SQLiteOpenHelper {
        OpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * Called when the database is first created.
         * This is where we should create our tables.
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "Creating table");
            db.execSQL("CREATE TABLE scores (name TEXT, score INT)");
        }

        /**
         * Automatically called whenever the version number of the database on
         * the phone does not match the current one. This is where we either migrate
         * our data or just smoke everything and recreate the tables.
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // In this case, we'll take the cheap way out and just delete everything
            Log.d(TAG, "Deleting all tables");
            db.execSQL("DROP TABLE IF EXISTS scores");
            
            // Then we use our already defined create step to recreate them all.
            // This guarantees that the database on the phone matches what we expect it to be
            onCreate(db);
        }
    }
    
    // ====================================================================================================================
    // END INNER HELPER CLASS
    // ====================================================================================================================
    
    public SQLiteAdapter(Context context) {
        // The main thing we need to do here is get access to the database
        // To do that, we use our new class
        
        Log.d(TAG, "Asking for access to the database");
        mOpenHelper = new OpenHelper(context);
        mDb = mOpenHelper.getWritableDatabase();
        
        // And there you have it. We have a copy of the database that we can interact with
        
        // We'll also need to do a one-time compile of our insert statement to make it into a true executable query
        mInsertScoreQuery = mDb.compileStatement(INSERT_SCORE_QUERY);
    }
    
    /**
     * This is how we'll get the scores from elsewhere in our program.
     */
    public List<Score> getScores() {
        Log.d(TAG, "Retrieving scores");
        List<Score> result = new LinkedList<Score>();
        
        // An easy way to get the information out of a table is to just use a regular query
        // NOTE: Instead of using an ORDER BY clause, we could sort the list afterwards, since
        //       Score implements Comparable. This is easier.
        Cursor c = mDb.rawQuery("SELECT * FROM scores ORDER BY score DESC", null);
        
        // Alternatively, we could use a more structured syntax
        c = mDb.query("scores", new String[] {"name", "score"}, null, null, null, null, "score DESC");
        
        if (c.moveToFirst()) { // This makes sure we have at least one result, then gets ready to return it
            do {
                Score s = new Score();
                s.setName(c.getString(0)); // These index positions come from the order of the columns
                s.setScore(c.getInt(1));
                Log.d(TAG, "Score: " + s);
                result.add(s);
            } while (c.moveToNext()); // This moves us to the next result, exiting if we have no more
        }
        return result;
    }
    
    /**
     * This is how we'll add scores from elsewhere in our program.
     */
    public void addScore(Score score) {
        Log.d(TAG, "Inserting score");
        // To perform an insertion, we "bind" our values to our insert query, then execute it.
        // The values we bind replace the question marks in the original string.
        mInsertScoreQuery.bindString(1, score.getName()); // Here we start with 1 instead of 0
        mInsertScoreQuery.bindDouble(2, score.getScore()); // There isn't a bindInt, so we make do
        mInsertScoreQuery.executeInsert();
        // That's it! Our data has now been added to the database.
    }
    
    /**
     * This is how we'll delete scores from elsewhere in our program.
     * Since we don't keep track of any kind of ID number per score entry (that would be an extra column),
     * the best we can do is just delete in the database where the name and score match certain values.
     * In real life, ID numbers are almost always used.
     */
     public void deleteScore(Score score) {
         Log.d(TAG, "Deleting score");
         // This is basically like binding in addScore, except it all happens in one method call
         mDb.delete("scores", "name = ? AND score = ?", new String[] {score.getName(), Integer.toString(score.getScore())});
     }
}
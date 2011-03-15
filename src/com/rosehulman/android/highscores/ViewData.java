package com.rosehulman.android.highscores;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.rosehulman.android.hellosqlite.R;

public class ViewData extends Activity {
    
    // If we write our database adapter first, this code becomes very elegant and simple
    
    private SQLiteAdapter mDbAdapter; // Our special class that does the database magic
    private ListView mScoresView;
    private ArrayAdapter<Score> mArrayAdapter;
    
    private static final int DIALOG_ID = 1;
    
    private List<Score> mScoresList;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_data);
        mScoresView = (ListView) findViewById(R.id.scores_list);
        
        // This is all it takes to set up our database connection in the activity
        mDbAdapter = new SQLiteAdapter(this);
        
        // Now we get our list of scores from the database
        mScoresList = mDbAdapter.getScores();
        
        mArrayAdapter = new ArrayAdapter<Score>(this, android.R.layout.simple_list_item_1, mScoresList);
        
        mScoresView.setAdapter(mArrayAdapter);
        mScoresView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDbAdapter.deleteScore(mScoresList.get(position));
                updateScoresList();
                mArrayAdapter.notifyDataSetChanged();
            }});
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.menu_add) {
            showDialog(DIALOG_ID);
        }
        return false;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        super.onCreateDialog(id);
        if (id == DIALOG_ID) {
            final Dialog d = new Dialog(this);
            d.setContentView(R.layout.add_dialog);
            d.setTitle("Add Score");
          
            Button addButton = (Button) d.findViewById(R.id.add_score_button);
            addButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Score s = new Score();
                    EditText nameEntry = (EditText) d.findViewById(R.id.name_entry);
                    EditText scoreEntry = (EditText) d.findViewById(R.id.score_entry);
                    s.setName(nameEntry.getText().toString());
                    int score;
                    try {
                        score = Integer.parseInt(scoreEntry.getText().toString());
                    } catch (Exception e) {
                        score = 0;
                    }
                    s.setScore(score);
                    mDbAdapter.addScore(s);
                    updateScoresList();
                    mArrayAdapter.notifyDataSetChanged();
                    nameEntry.setText("");
                    scoreEntry.setText("");
                    d.dismiss();
                }
            });
            
            return d;
        }
        return null;
    }
    
    /**
     * This method is necessary because when we use ListViews, we don't want to create a new List object, but
     * rather repopulate the old one with the latest scores 
     */
    private void updateScoresList() {
        List<Score> scores = mDbAdapter.getScores();
        mScoresList.clear();
        mScoresList.addAll(scores);
    }
}
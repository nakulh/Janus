package com.android.bike.janus;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import AdapterClasses.PeopleFeed;
import AdapterClasses.PeopleFeedAdapter;

public class PeopleActivity extends AppCompatActivity {

    private final String TOOLBAR_TITLE = "People";
    static final String TAG = PeopleActivity.class.getSimpleName();

    private Toolbar toolbar;

    private RecyclerView mRecyclerView;
    private PeopleFeedAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private EditText searchEditText;
    private ImageView clearImageView;
    private ImageView addPersonImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);

        //set a Toolbar to replace the ActionBar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle(TOOLBAR_TITLE);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        PeopleFeed peopleFeed = new PeopleFeed();
        mAdapter = new PeopleFeedAdapter(peopleFeed.createFeeds());
        mRecyclerView.setAdapter(mAdapter);

        //Find clearImageView
        clearImageView = (ImageView) findViewById(R.id.clearImageView);

        //Find addPersonImageView
        addPersonImageView = (ImageView) findViewById(R.id.addPersonImageView);

        //Find searchEditText
        searchEditText = (EditText) findViewById(R.id.searchEditText);

        //Set OnClickListener on searchEditText
        searchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                v.requestFocus();
                addPersonImageView.setVisibility(View.GONE);
                clearImageView.setVisibility(View.VISIBLE);
                showKeyboard(searchEditText);
            }
        });

        //Set OnFocusChangeListener to searchEditText
        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    clearImageView.setVisibility(View.GONE);
                    addPersonImageView.setVisibility(View.VISIBLE);
                }
            }
        });

        //Set OnClickListener on clearImageView
        clearImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_people, menu);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if (searchEditText.isFocused()){
                Rect outRect = new Rect();
                searchEditText.getGlobalVisibleRect(outRect);
                if(!outRect.contains((int)event.getRawX(),(int)event.getRawY())){
                    searchEditText.clearFocus();
                    hideKeyboard(searchEditText);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void hideKeyboard(View v){
        InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
    }

    private void showKeyboard(View v){
        InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }
}

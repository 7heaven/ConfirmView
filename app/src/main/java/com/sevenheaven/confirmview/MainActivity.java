package com.sevenheaven.confirmview;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private ConfirmView confirmView;

    private ConfirmView.ConfirmState state = ConfirmView.ConfirmState.ConfirmStateFail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        confirmView = (ConfirmView) findViewById(R.id.confirm_view);
        confirmView.setConfirmState(state);
        confirmView.setClickable(true);
        confirmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(state){
                    case ConfirmStateSuccess:
                        state = ConfirmView.ConfirmState.ConfirmStateFail;

                        Log.d("state", "success");
                        break;
                    case ConfirmStateFail:
                        state = ConfirmView.ConfirmState.ConfirmStateSuccess;
                        break;
                }

                confirmView.setConfirmState(state);
                confirmView.startPhareAnimation();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                confirmView.startPhareAnimation();
            }
        }, 1000L);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
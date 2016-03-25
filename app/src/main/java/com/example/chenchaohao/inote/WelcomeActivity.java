package com.example.chenchaohao.inote;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.os.Handler.Callback;

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class WelcomeActivity extends Activity {
    android.os.Handler handler = new android.os.Handler();
    private ShimmerTextView shimer_tv ;
    private Shimmer shimmer;

    class Loading implements Runnable{
        @Override
        public void run(){
            startActivity(new Intent(getApplication(),MainActivity.class));
            WelcomeActivity.this.finish();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        shimer_tv = (ShimmerTextView)findViewById(R.id.shimmer_start);
        shimmer = new Shimmer();
        shimmer.setRepeatCount(100)
                .setDuration(1000)
                .setStartDelay(100)
                .setDirection(Shimmer.ANIMATION_DIRECTION_RTL).setAnimatorListener(shimmer.getAnimatorListener());
        shimmer.start(shimer_tv);
        copyfile(Environment.getExternalStorageDirectory()+"/tesseract", "eng.traineddata");
        handler.postDelayed(new Loading(), 2000);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
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
    private void copyfile(String fileDirPath,String filename){
        fileDirPath = fileDirPath + "/tessdata";
        String filePath = fileDirPath  + "/" +filename;
        try{
            File dir = new File(fileDirPath);
            if(!dir.exists()){
                dir.mkdirs();
            }
            File file = new File(filePath);
            if(!file.exists()){
                InputStream input = getAssets().open("tessdata/eng.traineddata",AssetManager.ACCESS_STREAMING);
                FileOutputStream output = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int count = 0;
                while((count = input.read(buffer)) > 0){
                    output.write(buffer,0,count);
                }
                output.close();
                input.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}

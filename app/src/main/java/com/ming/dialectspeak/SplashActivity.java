package com.ming.dialectspeak;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.iflytek.sunflower.FlowerCollector;
import com.iflytek.voiceads.AdError;
import com.iflytek.voiceads.AdKeys;
import com.iflytek.voiceads.IFLYAdListener;
import com.iflytek.voiceads.IFLYAdSize;
import com.iflytek.voiceads.IFLYBannerAd;
import com.iflytek.voiceads.IFLYFullScreenAd;
import com.ming.util.Utils;

public class SplashActivity extends AppCompatActivity {
    private Handler handler;
    private ImageView img_bottom;
    private String adID="DE9574710B86CE10B8ED9E3F158926D1";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        delayToActivity();
        initImageView();
        initXFAD();

    }

    @Override
    protected void onResume() {
        super.onResume();
        FlowerCollector.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FlowerCollector.onPause(this);
    }

    private void initXFAD(){
        final IFLYFullScreenAd iflyFullScreenAd=IFLYFullScreenAd.createFullScreenAd(SplashActivity.this,adID);
        iflyFullScreenAd.setAdSize(IFLYAdSize.FULLSCREEN);
        iflyFullScreenAd.setParameter(AdKeys.FULLSCREEN_BOTTOM_TRANSPARENT,"3000");
        iflyFullScreenAd.setParameter(AdKeys.DEBUG_MODE,"false");
        iflyFullScreenAd.setPadding(0,0,0,400);
        iflyFullScreenAd.loadAd(new IFLYAdListener() {
            @Override
            public void onAdReceive() {
                iflyFullScreenAd.showAd();
            }

            @Override
            public void onAdFailed(AdError adError) {
               // Toast.makeText(SplashActivity.this, ""+adError.getErrorCode()+"/"+adError.getErrorDescription(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClick() {

            }

            @Override
            public void onAdClose() {

            }

            @Override
            public void onAdExposure() {

            }
        });
    }
    private void initImageView(){
        img_bottom=(ImageView)findViewById(R.id.img_splash_bottom);
        Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.logo);
        img_bottom.setImageBitmap(new Utils().toRoundBitmap(bitmap));
    }

    private void delayToActivity(){
      handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this,MainActivity.class));
                SplashActivity.this.finish();
            }
        },3000);
    }

}

package com.ming.dialectspeak;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.iflytek.autoupdate.IFlytekUpdate;
import com.iflytek.autoupdate.IFlytekUpdateListener;
import com.iflytek.autoupdate.UpdateConstants;
import com.iflytek.autoupdate.UpdateInfo;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.sunflower.FlowerCollector;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AudioManager.OnAudioFocusChangeListener{
    private ViewPager viewPager;
    private ArrayList<View> list_view=new ArrayList<View>();
    private TextView tv_yuyin,tv_wenzi,tv_setting;
    private ImageView img_yuyin,img_wenzi,img_setting;
    private String filepath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"gugu";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID+"=574e9996");
        AudioManager audioManager=(AudioManager)getSystemService(AUDIO_SERVICE);
        int result=audioManager.requestAudioFocus(MainActivity.this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);

        initViewPager();
        autoUpdate();
        requestPersi();
        createDir();

    }
    private void createDir(){
        File file=new File(filepath);
        if(!file.exists()){
            file.mkdirs();
        }
    }
    private int getBackValue(){
        SharedPreferences sharedPreferences=getSharedPreferences("backvalue", Activity.MODE_PRIVATE);
        return sharedPreferences.getInt("back",0);
    }
    private void requestPersi(){
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},2);
        }
    }
    private void autoUpdate(){
        IFlytekUpdate iFlytekUpdate=IFlytekUpdate.getInstance(MainActivity.this);
        iFlytekUpdate.setDebugMode(false);
        iFlytekUpdate.setParameter(UpdateConstants.EXTRA_WIFIONLY,"true");
        iFlytekUpdate.setParameter(UpdateConstants.EXTRA_STYLE,UpdateConstants.UPDATE_UI_DIALOG);
        iFlytekUpdate.autoUpdate(MainActivity.this,iFlytekUpdateListener);
    }
    private IFlytekUpdateListener iFlytekUpdateListener=new IFlytekUpdateListener() {
        @Override
        public void onResult(int i, UpdateInfo updateInfo) {

        }
    };

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

    private void initViewPager(){
        tv_yuyin=(TextView)findViewById(R.id.tv_yuyin_bot);
        tv_wenzi=(TextView)findViewById(R.id.tv_wenzi_bot);
        tv_setting=(TextView)findViewById(R.id.tv_setting_bot);
        img_yuyin=(ImageView)findViewById(R.id.img_yuyin);
        img_wenzi=(ImageView)findViewById(R.id.img_wenzi);
        img_setting=(ImageView)findViewById(R.id.img_setting);


        viewPager=(ViewPager)findViewById(R.id.viewpager);
        LayoutInflater inflater=getLayoutInflater().from(this);
        View view_yuyin=inflater.inflate(R.layout.fragment_yuyin_view,null);
        View view_wenzi=inflater.inflate(R.layout.fragment_wenzi_view,null);
        View view_setting=inflater.inflate(R.layout.fragment_setting_view,null);

        list_view.add(view_yuyin);
        list_view.add(view_wenzi);
        list_view.add(view_setting);

        viewPager.setAdapter(new MainViewPagerAdapter(list_view));

        viewPager.setCurrentItem(0);
        initBottom1();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
               switch(position){
                   case 0:
                       initBottom1();
                       break;
                   case 1:
                       initBottom2();
                       break;
                   case 2:
                       initBottom3();
                       break;
               }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }
    private class MainViewPagerAdapter extends PagerAdapter{
        private ArrayList<View> listview;
        public MainViewPagerAdapter(ArrayList<View> listvie){
            this.listview=listvie;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(listview.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(listview.get(position),0);
            return list_view.get(position);
        }

        @Override
        public int getCount() {
            return listview.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }
    }
    private void initBottom1(){
        getSupportActionBar().setTitle(getResources().getString(R.string.yuyin));
        tv_yuyin.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
        tv_wenzi.setTextColor(ContextCompat.getColor(this,R.color.tv_normal));
        tv_setting.setTextColor(ContextCompat.getColor(this,R.color.tv_normal));
        img_yuyin.setImageResource(R.mipmap.ic_tab_circle_on);
        img_wenzi.setImageResource(R.mipmap.ic_tab_course);
        img_setting.setImageResource(R.mipmap.ic_tab_me);

    }
    private void initBottom2(){
        getSupportActionBar().setTitle(getResources().getString(R.string.wenzi));
        tv_yuyin.setTextColor(ContextCompat.getColor(this,R.color.tv_normal));
        tv_wenzi.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
        tv_setting.setTextColor(ContextCompat.getColor(this,R.color.tv_normal));
        img_yuyin.setImageResource(R.mipmap.ic_tab_circle);
        img_wenzi.setImageResource(R.mipmap.ic_tab_course_on);
        img_setting.setImageResource(R.mipmap.ic_tab_me);
    }
    private void initBottom3(){
        getSupportActionBar().setTitle(getResources().getString(R.string.setting));
        tv_yuyin.setTextColor(ContextCompat.getColor(this,R.color.tv_normal));
        tv_wenzi.setTextColor(ContextCompat.getColor(this,R.color.tv_normal));
        tv_setting.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
        img_yuyin.setImageResource(R.mipmap.ic_tab_circle);
        img_wenzi.setImageResource(R.mipmap.ic_tab_course);
        img_setting.setImageResource(R.mipmap.ic_tab_me_on);
    }
    public void layoutYuyin(View view){
        viewPager.setCurrentItem(0);
        initBottom1();
    }
    public void layoutWenzi(View view){
        viewPager.setCurrentItem(1);
        initBottom2();
    }
    public void layoutSetting(View view){
        viewPager.setCurrentItem(2);
        initBottom3();

    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange){
            case AudioManager.AUDIOFOCUS_GAIN:

                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
             if(getBackValue() == 1){
                 Intent intent = new Intent(Intent.ACTION_MAIN);
                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 intent.addCategory(Intent.CATEGORY_HOME);
                 startActivity(intent);
                 return true;
             }else {
                 finish();
             }
        }
        return super.onKeyDown(keyCode, event);
    }
}

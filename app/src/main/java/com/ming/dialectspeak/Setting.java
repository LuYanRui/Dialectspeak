package com.ming.dialectspeak;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ming.util.Utils;


public class Setting extends Fragment {
    private View root_view;
    private RelativeLayout layout_speed,layout_speed1,layout_time,layout_time1,
    layout_share,layout_update,layout_about;
    private View view_line1,view_line2;
    private SeekBar sbar_speed,sbar_time;
    private TextView tv_speed,tv_time;
    private Boolean isSpeed=false;
    private Boolean isTime=false;
    private Boolean isContent=false;
    private ImageView img_bottom;
    private LinearLayout layout_content;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root_view=inflater.inflate(R.layout.fragment_setting, container, false);
        initCompant();
        return root_view;
    }
    private  void initCompant(){
        layout_content=(LinearLayout)root_view.findViewById(R.id.layout_about_content);
        img_bottom=(ImageView)root_view.findViewById(R.id.img_setting_bottom);
        layout_speed=(RelativeLayout)root_view.findViewById(R.id.layout_setting_1);
        layout_speed1=(RelativeLayout)root_view.findViewById(R.id.layout_setting_1_1);
        layout_time=(RelativeLayout)root_view.findViewById(R.id.layout_setting_2);
        layout_time1=(RelativeLayout)root_view.findViewById(R.id.layout_setting_1_2);
        layout_share=(RelativeLayout)root_view.findViewById(R.id.layout_setting_3);
        layout_update=(RelativeLayout)root_view.findViewById(R.id.layout_setting_4);
        layout_about=(RelativeLayout)root_view.findViewById(R.id.layout_setting_5);

        view_line1=(View)root_view.findViewById(R.id.view_setting_1);
        view_line2=(View)root_view.findViewById(R.id.view_setting_2);

        sbar_speed=(SeekBar)root_view.findViewById(R.id.seekBarspeed);
        sbar_time=(SeekBar)root_view.findViewById(R.id.seekBartime);

        tv_speed=(TextView)root_view.findViewById(R.id.tv_setting_speed);
        tv_time=(TextView)root_view.findViewById(R.id.tv_setting_time);

        Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.logo);
        img_bottom.setImageBitmap(new Utils().toRoundBitmap(bitmap));

        sbar_speed.setProgress(getSpeedValue());
        sbar_time.setProgress(getTimeValue());
        tv_speed.setText("语速:"+getSpeedValue());
        tv_time.setText("时间:"+getTimeValue());

        layout_speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSpeed == true){
                    isSpeed=false;
                    layout_speed1.setVisibility(View.GONE);
                    view_line1.setVisibility(View.VISIBLE);
                }else {
                    isSpeed=true;
                    layout_speed1.setVisibility(View.VISIBLE);
                    view_line1.setVisibility(View.GONE);
                }

            }
        });
        layout_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTime == true){
                    isTime=false;
                    layout_time1.setVisibility(View.GONE);
                    view_line2.setVisibility(View.VISIBLE);
                }else {
                    isTime=true;
                    layout_time1.setVisibility(View.VISIBLE);
                    view_line2.setVisibility(View.GONE);
                }
            }
        });
        layout_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,"我正在使用《咕咕方言》，感觉挺好玩的，各大安卓市场都有下载哦~");
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent,"将《咕咕方言》分享到"));
            }
        });
        layout_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "已经是最新版本", Toast.LENGTH_SHORT).show();
            }
        });
        layout_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(isContent == true){
                   isContent=false;
                   layout_content.setVisibility(View.GONE);
               }else {
                   layout_content.setVisibility(View.VISIBLE);
                   isContent=true;
               }
            }
        });
        sbar_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_speed.setText("语速:"+progress);
                setSpeedValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbar_time.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_time.setText("时间:"+progress);
                setTimeValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    private void setSpeedValue(int value){
        SharedPreferences sharedPreferences=getContext().getSharedPreferences("speedvalue", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt("value",value);
        editor.apply();
    }
    private void setTimeValue(int value){
        SharedPreferences sharedPreferences=getContext().getSharedPreferences("timevalue", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt("value",value);
        editor.apply();
    }
    private int getSpeedValue(){
        SharedPreferences sharedPreferences=getContext().getSharedPreferences("speedvalue", Activity.MODE_PRIVATE);
        return sharedPreferences.getInt("value",50);
    }
    private int getTimeValue(){
        SharedPreferences sharedPreferences=getContext().getSharedPreferences("timevalue", Activity.MODE_PRIVATE);
        return sharedPreferences.getInt("value",5);
    }
    @Override
    public void onDetach() {
        super.onDetach();
    }
}

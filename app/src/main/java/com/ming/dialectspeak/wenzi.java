package com.ming.dialectspeak;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.voiceads.AdError;
import com.iflytek.voiceads.AdKeys;
import com.iflytek.voiceads.IFLYAdListener;
import com.iflytek.voiceads.IFLYAdSize;
import com.iflytek.voiceads.IFLYBannerAd;
import com.ming.util.Utils;

public class wenzi extends Fragment {
    private Button btn_read;
    private SpeechSynthesizer mts;
    private View root_view;
    private EditText eidt_yuyin;
    private ImageButton img_share,img_share2;
    private ImageButton img_go;
    private RelativeLayout layout_bottom;
    private ImageView img_pause;
    private String read_person="aisxying";
    private ProgressBar progressBar_yuyin;
    private PopupWindow pw_read;
    private AnimationDrawable animationDrawable;
    private TextView tv_process;
    private TextView tv_proceee_vaule;
    private PopupWindow pw_loading;
    private Boolean isSpeeking=false;
    private int times=0;
    private int times_dao=0;
    // 后台播放文件路径
    private String yuansheng_msg="";
    private View view_loading,view_read;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayout;
    private View viewtoast;
    private TextView tv_toast;
    private String filepath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"gugu";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root_view=inflater.inflate(R.layout.fragment_wenzi, container, false);
        initCompant();
        popLoadingWin();
        popReadWin();
        return root_view;
    }
    private int getTimeValue(){
        SharedPreferences sharedPreferences=getContext().getSharedPreferences("timevalue", Activity.MODE_PRIVATE);
        return sharedPreferences.getInt("value",5);
    }
    private void createWindowManager() {
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mLayout = new WindowManager.LayoutParams();
        mLayout.type = WindowManager.LayoutParams.TYPE_TOAST;
        mLayout.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayout.format = PixelFormat.RGBA_8888;
        mLayout.gravity = Gravity.TOP | Gravity.CENTER;
        mLayout.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayout.height = WindowManager.LayoutParams.WRAP_CONTENT;

        viewtoast=getLayoutInflater(null).inflate(R.layout.playtoast,null);
        tv_toast=(TextView)viewtoast.findViewById(R.id.tv_toastnum);
        mWindowManager.addView(viewtoast,mLayout);
    }
    private void initCompant(){
        btn_read=(Button)root_view.findViewById(R.id.btn_wenzi_choice);
        img_share=(ImageButton)root_view.findViewById(R.id.img_wenzi_share);
        img_share2=(ImageButton)root_view.findViewById(R.id.img_wenzi_shareper);
        img_go=(ImageButton)root_view.findViewById(R.id.img_wenzi_go);
        img_pause=(ImageView)root_view.findViewById(R.id.img_wenzi_pause);
        eidt_yuyin=(EditText)root_view.findViewById(R.id.edit_yuyin);
        tv_proceee_vaule=(TextView)root_view.findViewById(R.id.tv_process_vaule);
        progressBar_yuyin=(ProgressBar)root_view.findViewById(R.id.processbar_wenzi);
        layout_bottom=(RelativeLayout) root_view.findViewById(R.id.layout_wenzi_bottom);

        initbtnRead();

        btn_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //popReadWin();
                pw_read.showAtLocation(view_read, Gravity.TOP,0,0);
            }
        });

        img_share.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        img_share.setImageResource(R.mipmap.trans_share_press);
                        break;
                    case MotionEvent.ACTION_UP:
                        img_share.setImageResource(R.mipmap.trans_share_normal);
                        break;
                }
                return false;
            }
        });
        img_share2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if(yuansheng_msg.isEmpty()){
                            Toast.makeText(getContext(), "还没有内容哦", Toast.LENGTH_SHORT).show();
                        }else {
                            AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                            builder.setTitle("是否开始原声分享？");
                            builder.setMessage("例：5秒后将后台播放当前选择的语音，此时可以在微信聊天页面按住发送语音键录制后台播放的语音并发送。");
                            builder.setPositiveButton("开始", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    createWindowManager();
                                    times_dao=getTimeValue();
                                    tv_toast.setText(String.valueOf(times_dao));
                                    setBackValue(1);
                                    final Handler handler_time=new Handler();
                                    final Runnable runnable =new Runnable() {
                                        @Override
                                        public void run() {
                                            times++;
                                            if(times == getTimeValue()){
                                                mWindowManager.removeView(viewtoast);
                                                Intent intent=new Intent(getContext(),PlayIntentService.class);
                                                intent.putExtra("content",yuansheng_msg);
                                                getContext().startService(intent);
                                                times=0;
                                                times_dao=0;
                                                tv_toast.setTextSize(17);
                                                setBackValue(0);
                                                handler_time.removeCallbacks(this);
                                            }else {
                                                if(--times_dao == 1){
                                                    tv_toast.setTextSize(13);
                                                    tv_toast.setText("Play");
                                                    handler_time.postDelayed(this,1000);
                                                }else {
                                                    tv_toast.setText(String.valueOf(times_dao));
                                                    handler_time.postDelayed(this,1000);
                                                }
                                            }
                                        }
                                    };
                                    handler_time.postDelayed(runnable,1000);

                                }
                            });
                            builder.setNegativeButton("下次吧", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }
                        img_share2.setImageResource(R.mipmap.translator_man_press);
                        break;
                    case MotionEvent.ACTION_UP:
                        img_share2.setImageResource(R.mipmap.translator_man_normal);
                        break;
                }
                return false;
            }
        });
        img_go.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        img_go.setImageResource(R.mipmap.translate_press);
                        if(!eidt_yuyin.getText().toString().trim().isEmpty()){
                           // popLoadingWin();
                            animationDrawable.start();
                            pw_loading.showAtLocation(view_loading, Gravity.CENTER,0,0);
                            speeckSynthesizer(eidt_yuyin.getText().toString().trim());
                        }else{
                            Toast.makeText(getContext(), "文字不可为空", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        img_go.setImageResource(R.mipmap.translate_normal);
                        break;
                }
                return false;
            }
        });
        img_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSpeeking == true){
                    isSpeeking=false;
                    img_pause.setImageResource(R.mipmap.navitts_replay_normal);
                    mts.resumeSpeaking();
                }else {
                    isSpeeking=true;
                    img_pause.setImageResource(R.mipmap.navitts_replay_pause);
                    mts.pauseSpeaking();
                }
            }
        });
    }
    private void initbtnRead(){
        switch (getReadPerson("wenzireadperson")){
            case "aisxying":
                read_person="aisxying";
                btn_read.setText("转成:陕西话");
                break;
            case "aisxqiang":
                read_person="aisxqiang";
                btn_read.setText("转成:湖南话");
                break;
            case "xiaokun":
                read_person="xiaokun";
                btn_read.setText("转成:河南话");
                break;
            case "aisxrong":
                read_person="aisxrong";
                btn_read.setText("转成:四川话");
                break;
            case "xiaoqian":
                read_person="xiaoqian";
                btn_read.setText("转成:东北话");
                break;
            case "aisxlin":
                read_person="aisxlin";
                btn_read.setText("转成:台普");
                break;
            case "dalong":
                read_person="dalong";
                btn_read.setText("转成:粤语男");
                break;
            case "xiaomei":
                read_person="xiaomei";
                btn_read.setText("转成:粤语女");
                break;
            case "xiaoxin":
                read_person="xiaoxin";
                btn_read.setText("转成:小新");
                break;
            case "xiaowanzi":
                read_person="xiaowanzi";
                btn_read.setText("转成:小丸子");
                break;
            case "xiaoyu":
                read_person="xiaoyu";
                btn_read.setText("转成:普通话");
                break;
        }
    }
    private void setReadPerson(String person){
        SharedPreferences share=getContext().getSharedPreferences("wenzireadperson", Activity.MODE_PRIVATE);
        SharedPreferences.Editor edit=share.edit();
        edit.putString("person",person);
        edit.apply();

    }
    private String getReadPerson(String name){
        SharedPreferences share=getContext().getSharedPreferences(name,Activity.MODE_PRIVATE);
        return share.getString("person","aisxying");
    }
    private void popLoadingWin(){
        view_loading=LayoutInflater.from(getContext()).inflate(R.layout.loading,null);
        ImageView img_loading=(ImageView)view_loading.findViewById(R.id.img_loading_sp);
        tv_process=(TextView)view_loading.findViewById(R.id.tv_loading_process);
        animationDrawable=(AnimationDrawable)img_loading.getBackground();
        pw_loading=new PopupWindow(view_loading, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,false);
        pw_loading.setOutsideTouchable(false);
        pw_loading.setBackgroundDrawable(new BitmapDrawable());
        pw_loading.setAnimationStyle(android.support.v7.appcompat.R.style.Animation_AppCompat_Dialog);
//        if(pw_loading.isShowing()){
//            pw_loading.dismiss();
//        }else{
//            animationDrawable.start();
//            pw_loading.showAtLocation(view_loading, Gravity.CENTER,0,0);
//        }

    }
    private SynthesizerListener mSynListener=new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            layout_bottom.setVisibility(View.VISIBLE);

        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {
            if(i == 100){
                if(pw_loading.isShowing()){
                    pw_loading.dismiss();
                    animationDrawable.stop();
                }
            }else {
                tv_process.setText(getResources().getString(R.string.loading)+i+"%");
            }
        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {
             progressBar_yuyin.setProgress(i+1);
             tv_proceee_vaule.setText(i+1+"%");
        }

        @Override
        public void onCompleted(SpeechError speechError) {
             progressBar_yuyin.setProgress(0);
             tv_proceee_vaule.setText("");
             layout_bottom.setVisibility(View.GONE);
             mts.destroy();
             mts=null;
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };
    private int getSpeedValue(){
        SharedPreferences sharedPreferences=getContext().getSharedPreferences("speedvalue", Activity.MODE_PRIVATE);
        return sharedPreferences.getInt("value",50);
    }
    private void setBackValue(int value){
        SharedPreferences sharedPreferences=getContext().getSharedPreferences("backvalue",Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt("back",value);
        editor.apply();
    }
    private void speeckSynthesizer(String speakcontent){
        mts= SpeechSynthesizer.createSynthesizer(getContext(),null);
        mts.setParameter(SpeechConstant.VOICE_NAME, read_person);
        mts.setParameter(SpeechConstant.SPEED,String.valueOf(getSpeedValue()));
        mts.setParameter(SpeechConstant.VOLUME,"80");
        yuansheng_msg=filepath+"/"+"wenzishare"+".pcm";
        mts.setParameter(SpeechConstant.TTS_AUDIO_PATH,yuansheng_msg);
        mts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mts.startSpeaking(speakcontent,mSynListener);

    }
    @Override
    public void onDetach() {
        super.onDetach();
    }
    private void popReadWin(){
        view_read=LayoutInflater.from(getContext()).inflate(R.layout.activity_read_choice_,null);
        ImageView img_close=(ImageView)view_read.findViewById(R.id.img_read_close);
        LinearLayout layout_sp1=(LinearLayout)view_read.findViewById(R.id.layout_spre1);
        LinearLayout layout_sp2=(LinearLayout)view_read.findViewById(R.id.layout_spre2);
        LinearLayout layout_sp3=(LinearLayout)view_read.findViewById(R.id.layout_spre3);
        LinearLayout layout_sp4=(LinearLayout)view_read.findViewById(R.id.layout_spre4);
        LinearLayout layout_sp5=(LinearLayout)view_read.findViewById(R.id.layout_spre5);
        LinearLayout layout_sp6=(LinearLayout)view_read.findViewById(R.id.layout_spre6);
        LinearLayout layout_sp7=(LinearLayout)view_read.findViewById(R.id.layout_spre7);
        LinearLayout layout_sp8=(LinearLayout)view_read.findViewById(R.id.layout_spre8);
        LinearLayout layout_sp9=(LinearLayout)view_read.findViewById(R.id.layout_spre9);
        LinearLayout layout_sp10=(LinearLayout)view_read.findViewById(R.id.layout_spre10);
        LinearLayout layout_sp11=(LinearLayout)view_read.findViewById(R.id.layout_spre11);

        final TextView tv_read1=(TextView)view_read.findViewById(R.id.tv_read_1);
        final TextView tv_read2=(TextView)view_read.findViewById(R.id.tv_read_2);
        final TextView tv_read3=(TextView)view_read.findViewById(R.id.tv_read_3);
        final TextView tv_read4=(TextView)view_read.findViewById(R.id.tv_read_4);
        final TextView tv_read5=(TextView)view_read.findViewById(R.id.tv_read_5);
        final TextView tv_read6=(TextView)view_read.findViewById(R.id.tv_read_6);
        final TextView tv_read7=(TextView)view_read.findViewById(R.id.tv_read_7);
        final TextView tv_read8=(TextView)view_read.findViewById(R.id.tv_read_8);
        final TextView tv_read9=(TextView)view_read.findViewById(R.id.tv_read_9);
        final TextView tv_read10=(TextView)view_read.findViewById(R.id.tv_read_10);
        final TextView tv_read11=(TextView)view_read.findViewById(R.id.tv_read_11);

        pw_read=new PopupWindow(view_read, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,true);
        pw_read.setOutsideTouchable(false);
        pw_read.setAnimationStyle(android.support.v7.appcompat.R.style.Animation_AppCompat_DropDownUp);
//        if(pw_read.isShowing()){
//            pw_read.dismiss();
//        }else{
//            pw_read.showAtLocation(view, Gravity.TOP,0,0);
//        }
        switch (getReadPerson("wenzireadperson")){
            case "aisxying":
                tv_read1.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                break;
            case "aisxqiang":
                tv_read2.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                break;
            case "xiaokun":
                tv_read3.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                break;
            case "aisxrong":
                tv_read4.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                break;
            case "xiaoqian":
                tv_read5.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                break;
            case "aisxlin":
                tv_read6.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                break;
            case "dalong":
                tv_read7.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                break;
            case "xiaomei":
                tv_read8.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                break;
            case "xiaoxin":
                tv_read9.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                break;
            case "xiaowanzi":
                tv_read10.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                break;
            case "xiaoyu":
                tv_read11.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                break;

        }
        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw_read.dismiss();
                initbtnRead();
            }
        });
        layout_sp1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_read1.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                setReadPerson("aisxying");
            }
        });
        layout_sp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_read2.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                setReadPerson("aisxqiang");
            }
        });
        layout_sp3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_read3.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                setReadPerson("xiaokun");
            }
        });
        layout_sp4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_read4.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                setReadPerson("aisxrong");
            }
        });
        layout_sp5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_read5.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                setReadPerson("xiaoqian");
            }
        });
        layout_sp6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_read6.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                setReadPerson("aisxlin");
            }
        });
        layout_sp7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_read7.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                setReadPerson("dalong");
            }
        });
        layout_sp8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_read8.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                setReadPerson("xiaomei");
            }
        });
        layout_sp9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_read9.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                setReadPerson("xiaoxin");
            }
        });
        layout_sp10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_read10.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read11.setTextColor(getResources().getColor(R.color.tv_normal));
                setReadPerson("xiaowanzi");
            }
        });
        layout_sp11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_read11.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_read2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read5.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read6.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read7.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read8.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read9.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read10.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_read1.setTextColor(getResources().getColor(R.color.tv_normal));
                setReadPerson("xiaoyu");
            }
        });
    }
}

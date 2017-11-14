package com.ming.dialectspeak;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.voiceads.AdError;
import com.iflytek.voiceads.AdKeys;
import com.iflytek.voiceads.IFLYAdListener;
import com.iflytek.voiceads.IFLYAdSize;
import com.iflytek.voiceads.IFLYBannerAd;
import com.ming.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import myview.VoiceLineView;


public class yuyin extends Fragment {
    private SpeechRecognizer mIat;
    private SpeechSynthesizer mts;
    private ArrayList<String> list_buffer=new ArrayList<String>();
    // 默认发音为陕西话
    private String read_person ="aisxying";
    // 默认接受语言为普通话
    private String speech_person="mandarin";
    private String launage="zh_cn";
    // 保存所说内容
    private String speech_msg="";
    private View root_view;
    private Button btn_speech,btn_read,btn_start;
    private ListView listView_result;
    private float down_y=0;
    private int times=0;
    private int times_dao=0;
    private VoiceLineView voiceLineView;
    private LinearLayout layout_back,layout_shiju;
    private TextView tv_title;
    private boolean isCancle=false;
    private TextView tv_process;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayout;
    private View viewtoast;
    private TextView tv_toast;
    private AnimationDrawable animationDrawable;
    private PopupWindow pw_speech,pw_read,pw_recoring,pw_loading;
    private ArrayList<String> list_msg=new ArrayList<String>();
    private BaseAdapter listMsgAdapter;
    private IFLYBannerAd bannerAd;
    private View view_loading,view_speech,view_read;
    private ArrayList<String> list_share_path=new ArrayList<String>();
    private String filepath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"gugu";
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    voiceLineView.setVolume(msg.arg1*10);
                    break;
            }
            super.handleMessage(msg);
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root_view=inflater.inflate(R.layout.fragment_yuyin, container, false);

        bannerad();
        initCompant();
        initSpeech();

        popLoadingWin();
        popSpeechWin();
        popReadWin();
        return root_view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        btn_speech=(Button) root_view.findViewById(R.id.btn_yuyin_speech);
        btn_read=(Button) root_view.findViewById(R.id.btn_yuyin_read);
        btn_start=(Button) root_view.findViewById(R.id.btn_start_yuyin);
        layout_shiju=(LinearLayout)root_view.findViewById(R.id.layout_shiju);
        listView_result=(ListView)root_view.findViewById(R.id.listView_result);

        initbtnSpeech();
        initbtnRead();

        listMsgAdapter=new ListMsgAdapter(getContext());
        listView_result.setAdapter(listMsgAdapter);

        listView_result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
            }
        });

        btn_speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //popSpeechWin();
                pw_speech.showAtLocation(view_speech, Gravity.BOTTOM,0,0);
            }
        });
        btn_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              // popReadWin();
                pw_read.showAtLocation(view_read, Gravity.TOP,0,0);
            }
        });

        btn_start.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        down_y=event.getY();
                        btn_start.setBackground(getResources().getDrawable(R.drawable.btnyuyin_press));
                        popRecoringWin();
                        mIat.startListening(mRecoListener);
                        break;
                    case MotionEvent.ACTION_UP:
                        if(isCancle == true){
                            mIat.cancel();
                            if(pw_recoring.isShowing()){
                                pw_recoring.dismiss();
                                pw_recoring=null;
                            }
                        }else{
                            if(pw_recoring.isShowing()){
                                pw_recoring.dismiss();
                                pw_recoring=null;
                            }
                            animationDrawable.start();
                            pw_loading.showAtLocation(view_loading, Gravity.CENTER,0,0);
                            mIat.stopListening();

//                            if(pw_loading!=null){
//                                if(pw_loading.isShowing()){
//                                    animationDrawable.stop();
//                                    pw_loading.dismiss();
//                                }
//                            }else{
//                                animationDrawable.start();
//                                pw_loading.showAtLocation(view_loading, Gravity.CENTER,0,0);
//                                mIat.stopListening();
//                            }
//                            if(pw_loading != null){
//                                if(pw_loading.isShowing()){
//                                    pw_loading.dismiss();
//                                    animationDrawable.stop();
//                                    animationDrawable=null;
//                                    pw_loading=null;
//                                }
//                                popLoadingWin();
//                                mIat.stopListening();
//                            }else {
//                                popLoadingWin();
//                                mIat.stopListening();
//                            }
                        }
//                        if(pw_recoring.isShowing()){
//                            pw_recoring.dismiss();
//                            voiceLineView=null;
//                            pw_recoring=null;
//                        }
                        btn_start.setBackground(getResources().getDrawable(R.drawable.popwin_shaw));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float move_y=event.getY();
                        if(down_y - move_y >200){
                            isCancle=true;
                            layout_back.setBackground(getResources().getDrawable(R.drawable.loading_shap));
                            tv_title.setText("松开取消录音");
                        }else{
                            isCancle=false;
                            layout_back.setBackground(getResources().getDrawable(R.drawable.loading_shape));
                            tv_title.setText("上滑取消录音");
                        }
                        break;
                }
                return false;
            }
        });
    }
    private void initbtnSpeech(){
        switch (getSpeechPerson("speechperson")){
            case "mandarin":
                speech_person="mandarin";
                btn_speech.setText("普通话");
                break;
            case "cantonese":
                speech_person="cantonese";
                btn_speech.setText("粤语");
                break;
            case "lmz":
                speech_person="lmz";
                btn_speech.setText("四川话");
                break;
            case "henanese":
                speech_person="henanese";
                btn_speech.setText("河南话");
                break;
            case "en_us":
                speech_person="en_us";
                btn_speech.setText("英文");
                break;
        }
    }
    private void initbtnRead(){
        switch (getReadPerson("readperson")){
            case "aisxying":
                read_person="aisxying";
                btn_read.setText("陕西话");
                break;
            case "aisxqiang":
                read_person="aisxqiang";
                btn_read.setText("湖南话");
                break;
            case "xiaokun":
                read_person="xiaokun";
                btn_read.setText("河南话");
                break;
            case "aisxrong":
                read_person="aisxrong";
                btn_read.setText("四川话");
                break;
            case "xiaoqian":
                read_person="xiaoqian";
                btn_read.setText("东北话");
                break;
            case "aisxlin":
                read_person="aisxlin";
                btn_read.setText("台普");
                break;
            case "dalong":
                read_person="dalong";
                btn_read.setText("粤语男");
                break;
            case "xiaomei":
                read_person="xiaomei";
                btn_read.setText("粤语女");
                break;
            case "xiaoxin":
                read_person="xiaoxin";
                btn_read.setText("小新");
                break;
            case "xiaowanzi":
                read_person="xiaowanzi";
                btn_read.setText("小丸子");
                break;
            case "xiaoyu":
                read_person="xiaoyu";
                btn_read.setText("普通话");
                break;
        }
    }
    private void initSpeech(){
        mIat= SpeechRecognizer.createRecognizer(getContext(), null);
        mIat.setParameter(SpeechConstant.DOMAIN, "iat");
        if(speech_person.equals("en_us")){
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
            mIat.setParameter(SpeechConstant.ACCENT, null);
        }else{
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            mIat.setParameter(SpeechConstant.ACCENT, speech_person);
        }

    }
    private RecognizerListener mRecoListener=new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
            Message msg=handler.obtainMessage();
            msg.what=1;
            msg.arg1=i;
            handler.sendMessage(msg);
        }

        @Override
        public void onBeginOfSpeech() {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean islast) {
            if(recognizerResult.getResultString() != null){
                parseJson(recognizerResult.getResultString());
            }
            if(islast){
                if(list_buffer.size() != 0){
                    for(int i=0;i<list_buffer.size();i++){
                        if(!list_buffer.get(i).startsWith("。")){
                            speech_msg+=list_buffer.get(i);
                        }
                    }
                    list_msg.add(speech_msg);
                    speeckSynthesizer(speech_msg);
                    list_buffer.clear();
                    speech_msg="";
                    listView_result.setVisibility(View.VISIBLE);
                    layout_shiju.setVisibility(View.GONE);
                    listMsgAdapter.notifyDataSetChanged();
                }

            }
        }

        @Override
        public void onError(SpeechError speechError) {
            Toast.makeText(getContext(), speechError.getErrorDescription(), Toast.LENGTH_SHORT).show();
            if(pw_loading != null){
                if(pw_loading.isShowing()){
                    pw_loading.dismiss();
                    animationDrawable.stop();
                    //animationDrawable=null;
                    //pw_loading=null;
                }
            }

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    private String parseJson(String jsonmsg){
        try {
            JSONObject object=new JSONObject(jsonmsg);
            JSONArray array_1=object.getJSONArray("ws");
            for(int i=0;i<array_1.length();i++){
                JSONObject object_2=array_1.getJSONObject(i);
                JSONArray array_2=object_2.getJSONArray("cw");
                for (int e=0;e<array_2.length();e++){
                    JSONObject object_3=array_2.getJSONObject(e);
                    if(!object_3.getString("w").isEmpty()){
                        list_buffer.add(object_3.getString("w"));
                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }
    private SynthesizerListener mSynListener=new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {
            if(i == 100){
                if(pw_loading.isShowing()){
                    pw_loading.dismiss();
                    animationDrawable.stop();
                    //animationDrawable=null;
                    //pw_loading=null;
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

        }

        @Override
        public void onCompleted(SpeechError speechError) {
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
    private int getTimeValue(){
        SharedPreferences sharedPreferences=getContext().getSharedPreferences("timevalue", Activity.MODE_PRIVATE);
        return sharedPreferences.getInt("value",5);
    }
    private void speeckSynthesizer(String speakcontent){
        String filepathmsg="";
        mts=SpeechSynthesizer.createSynthesizer(getContext(),null);
        mts.setParameter(SpeechConstant.VOICE_NAME, read_person);
        mts.setParameter(SpeechConstant.SPEED,String.valueOf(getSpeedValue()));
        mts.setParameter(SpeechConstant.VOLUME,"80");
        filepathmsg=filepath+"/"+new Utils().getCurrentDateStr()+".pcm";
        list_share_path.add(filepathmsg);
        mts.setParameter(SpeechConstant.TTS_AUDIO_PATH,filepathmsg);
        mts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mts.startSpeaking(speakcontent,mSynListener);

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
//            if(pw_loading.isShowing()){
//                pw_loading.dismiss();
//            }else{
//                animationDrawable.start();
//                pw_loading.showAtLocation(view, Gravity.CENTER,0,0);
//            }

    }
    private void bannerad(){
        bannerAd = IFLYBannerAd.createBannerAd(getContext(), "DDA589EDFEAC5FEA4C0EDDD525D02FD0");
        bannerAd.setAdSize(IFLYAdSize.BANNER);
        bannerAd.setParameter(AdKeys.DEBUG_MODE, "false");
        bannerAd.setParameter(AdKeys.DOWNLOAD_ALERT, "false");
        bannerAd.loadAd(bannerlistener);
        LinearLayout layout_ad=(LinearLayout)root_view.findViewById(R.id.layout_addad);
        layout_ad.addView(bannerAd);
    }
    IFLYAdListener bannerlistener=new IFLYAdListener() {
        @Override
        public void onAdReceive() {
            bannerAd.showAd();
        }

        @Override
        public void onAdFailed(AdError adError) {

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
    };
    private void popRecoringWin(){
            View view=LayoutInflater.from(getContext()).inflate(R.layout.recoring,null);
            layout_back=(LinearLayout)view.findViewById(R.id.linear_re_back);
            tv_title=(TextView)view.findViewById(R.id.tv_recoring_title);
            voiceLineView = (VoiceLineView)view.findViewById(R.id.voicLine);
            voiceLineView.start();

            pw_recoring=new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,false);
            pw_recoring.setOutsideTouchable(false);
            pw_recoring.setBackgroundDrawable(new BitmapDrawable());
            pw_recoring.setAnimationStyle(android.support.v7.appcompat.R.style.Animation_AppCompat_Dialog);
            if(pw_recoring.isShowing()){
                pw_recoring.dismiss();
            }else{
                pw_recoring.showAtLocation(view, Gravity.CENTER,0,0);
            }

    }
    private void setSpeechPerson(String person){
        SharedPreferences share=getContext().getSharedPreferences("speechperson",Activity.MODE_PRIVATE);
        SharedPreferences.Editor edit=share.edit();
        edit.putString("person",person);
        edit.apply();

    }
    private String getSpeechPerson(String name){
        SharedPreferences share=getContext().getSharedPreferences(name,Activity.MODE_PRIVATE);
        return share.getString("person","mandarin");
    }
    private void setReadPerson(String person){
        SharedPreferences share=getContext().getSharedPreferences("readperson",Activity.MODE_PRIVATE);
        SharedPreferences.Editor edit=share.edit();
        edit.putString("person",person);
        edit.apply();

    }
    private String getReadPerson(String name){
        SharedPreferences share=getContext().getSharedPreferences(name,Activity.MODE_PRIVATE);
        return share.getString("person","aisxying");
    }
    private void popSpeechWin(){
        view_speech=LayoutInflater.from(getContext()).inflate(R.layout.activity_speech_choice_,null);
        ImageView img_close=(ImageView)view_speech.findViewById(R.id.img_speech_close);
        LinearLayout layout_choice1=(LinearLayout)view_speech.findViewById(R.id.layout_spch1);
        LinearLayout layout_choice2=(LinearLayout)view_speech.findViewById(R.id.layout_spch2);
        LinearLayout layout_choice3=(LinearLayout)view_speech.findViewById(R.id.layout_spch3);
        LinearLayout layout_choice4=(LinearLayout)view_speech.findViewById(R.id.layout_spch4);
        LinearLayout layout_choice5=(LinearLayout)view_speech.findViewById(R.id.layout_spch5);
        final TextView tv_speech1=(TextView)view_speech.findViewById(R.id.tv_speech_1);
        final TextView tv_speech2=(TextView)view_speech.findViewById(R.id.tv_speech_2);
        final TextView tv_speech3=(TextView)view_speech.findViewById(R.id.tv_speech_3);
        final TextView tv_speech4=(TextView)view_speech.findViewById(R.id.tv_speech_4);
        final TextView tv_speech5=(TextView)view_speech.findViewById(R.id.tv_speech_5);

        pw_speech=new PopupWindow(view_speech, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,true);
        pw_speech.setOutsideTouchable(false);
        pw_speech.setAnimationStyle(android.support.v7.appcompat.R.style.Animation_AppCompat_DropDownUp);

        switch (getSpeechPerson("speechperson")){
            case "mandarin":
                tv_speech1.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_speech2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech5.setTextColor(getResources().getColor(R.color.tv_normal));
                break;
            case "cantonese":
                tv_speech2.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_speech1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech5.setTextColor(getResources().getColor(R.color.tv_normal));
                break;
            case "lmz":
                tv_speech3.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_speech2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech5.setTextColor(getResources().getColor(R.color.tv_normal));
                break;
            case "henanese":
                tv_speech4.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_speech2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech5.setTextColor(getResources().getColor(R.color.tv_normal));
                break;
            case "en_us":
                tv_speech5.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_speech2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech1.setTextColor(getResources().getColor(R.color.tv_normal));
                break;
        }
        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw_speech.dismiss();
                initbtnSpeech();
                initSpeech();
            }
        });
        layout_choice1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_speech1.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_speech2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech5.setTextColor(getResources().getColor(R.color.tv_normal));
                setSpeechPerson("mandarin");
            }
        });
        layout_choice2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_speech2.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_speech1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech5.setTextColor(getResources().getColor(R.color.tv_normal));
                setSpeechPerson("cantonese");
            }
        });
        layout_choice3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_speech3.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_speech2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech5.setTextColor(getResources().getColor(R.color.tv_normal));
                setSpeechPerson("lmz");
            }
        });
        layout_choice4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_speech4.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_speech2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech1.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech5.setTextColor(getResources().getColor(R.color.tv_normal));
                setSpeechPerson("henanese");
            }
        });
        layout_choice5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_speech5.setTextColor(getResources().getColor(R.color.colorPrimary));
                tv_speech2.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech3.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech4.setTextColor(getResources().getColor(R.color.tv_normal));
                tv_speech1.setTextColor(getResources().getColor(R.color.tv_normal));
                setSpeechPerson("en_us");
            }
        });
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
        switch (getReadPerson("readperson")){
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
    private class ListMsgAdapter extends BaseAdapter{
        private LayoutInflater listInflater;

        public ListMsgAdapter(Context context){
            this.listInflater=LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return list_msg.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if(convertView == null){
                convertView=listInflater.inflate(R.layout.listview_item,null);
                holder=new ViewHolder();
                holder.btn_yuansheng_share=(Button)convertView.findViewById(R.id.btn_yuansheng_share);
                //holder.btn_url_share=(Button)convertView.findViewById(R.id.btn_url_share);
                holder.layout_start_bottom=(RelativeLayout)convertView.findViewById(R.id.layout_listitem_bottom);
                holder.tv_start_msg=(TextView)convertView.findViewById(R.id.tv_start_msg);
                holder.img_start_sound=(ImageView)convertView.findViewById(R.id.img_start_sound);
                convertView.setTag(holder);
            }else{
                holder=(ViewHolder)convertView.getTag();
            }

            holder.tv_start_msg.setText(list_msg.get(position));

            holder.btn_yuansheng_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                    builder.setTitle("是否开始原声分享？");
                    builder.setMessage("例：5秒后将后台播放当前选择的语音，此时可以在微信聊天页面按住发送语音键录制后台播放的语音并发送。");
                    builder.setPositiveButton("开始", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
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
                                        intent.putExtra("content",list_share_path.get(position));
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
            });
//            holder.btn_url_share.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });
            holder.layout_start_bottom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(list_msg.size() != 0){
                        if(list_share_path.size()>0){
                            Intent intent =new Intent(getContext(),PlayIntentService.class);
                            intent.putExtra("content",list_share_path.get(position));
                            getContext().startService(intent);
                        }else {
                            Toast.makeText(getContext(), "读取数据出错，请重新登录", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            return convertView;
        }
    }
    public final class ViewHolder{
        public Button btn_yuansheng_share;
        public Button btn_url_share;
        public RelativeLayout layout_start_bottom;
        public TextView tv_start_msg;
        public ImageView img_start_sound;
    }
    private void setBackValue(int value){
        SharedPreferences sharedPreferences=getContext().getSharedPreferences("backvalue",Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt("back",value);
        editor.apply();
    }
}

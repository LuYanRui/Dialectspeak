package com.ming.dialectspeak;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class PlayIntentService extends IntentService implements AudioManager.OnAudioFocusChangeListener{
    public PlayIntentService() {
        super("PlayIntentService");
    }
    private SpeechSynthesizer mts;
    private String content;
    private String read_person;
    private String speed;
    public  Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private AudioPlayer audioPlayer;
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            AudioManager audioManager=(AudioManager)getSystemService(AUDIO_SERVICE);
            int result=audioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
            content=intent.getStringExtra("content");

            initAudioPlayer();
        }
    }
    private void initAudioPlayer(){
        audioPlayer=new AudioPlayer(handler);
        AudioParam audioParam=getAudioParam();
        audioPlayer.setAudioParam(audioParam);
        byte[] data=getPCMData();
        audioPlayer.setDataSource(data);
        audioPlayer.prepare();

        audioPlayer.play();
        //audioPlayer.pause();
        //audioPlayer.stop();
    }
    public AudioParam getAudioParam()
    {
        //	A：采样率16KHZ或者8KHZ，单声道，采样精度16bit的PCM或者WAV格式的音频44100
        AudioParam audioParam = new AudioParam();
        audioParam.mFrequency = 16000;

        audioParam.mChannel = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        audioParam.mSampBit = AudioFormat.ENCODING_PCM_16BIT;

        return audioParam;
    }

    /*
     * 获得PCM音频数据
     */
    public byte[] getPCMData()
    {

        File file = new File(content);
        if (file == null){
            return null;
        }

        FileInputStream inStream;
        try {
            inStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        byte[] data_pack = null;
        if (inStream != null){
            long size = file.length();

            data_pack = new byte[(int) size];
            try {
                inStream.read(data_pack);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }

        }

        return data_pack;
    }
    private void speeckSynthesizer(String speakcontent,String read_person,String speed){
        mts=SpeechSynthesizer.createSynthesizer(this,null);
        mts.setParameter(SpeechConstant.VOICE_NAME, read_person);
        mts.setParameter(SpeechConstant.SPEED,speed);
        mts.setParameter(SpeechConstant.VOLUME,"80");
        mts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mts.startSpeaking(speakcontent,mSynListener);

    }
    private SynthesizerListener mSynListener=new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

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

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

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
    public void onDestroy() {
        super.onDestroy();
        audioPlayer.stop();
        audioPlayer.release();
    }
}

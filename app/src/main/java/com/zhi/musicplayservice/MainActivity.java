package com.zhi.musicplayservice;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.zhi.service.MusicInterface;
import java.io.File;

public class MainActivity extends Activity implements View.OnClickListener{

    private Intent intent;
    private MusicInterface musicInterface;
    private ServiceConnection conn = new MusicServiceConnection();
    private PhoneStateListener phoneStateListener = new MusicPhoneStateListener();
    private String path;

    private EditText mEtFilename;
    private Button mBtnPlay;
    private Button mBtnPause;
    private Button mBtnReplay;
    private Button mBtnStop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initEvents();
        path = getPath();

        intent =new Intent(MainActivity.this, MusicPlayService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    public final class MusicPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state){
                case TelephonyManager.CALL_STATE_RINGING:  // 来电
                    musicInterface.pause();
                    break;
                case TelephonyManager.CALL_STATE_IDLE:  // 挂断
                    musicInterface.pause();
                    break;
            }
        }
    }

    public class MusicServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicInterface = (MusicInterface) service;

            /*必须在这里注册手机状态更改的系统服务*/
            TelephonyManager manager = (TelephonyManager) App.getContext().getSystemService(Context.TELEPHONY_SERVICE);
            manager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicInterface = null;
        }
    }

    private void initEvents() {
        mBtnPlay.setOnClickListener(this);
        mBtnPause.setOnClickListener(this);
        mBtnReplay.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
    }

    private void initViews() {
        mEtFilename = (EditText) findViewById(R.id.et_filename);
        mBtnPlay = (Button) findViewById(R.id.btn_play);
        mBtnPause = (Button) findViewById(R.id.btn_pause);
        mBtnReplay = (Button) findViewById(R.id.btn_replay);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
    }

    private String getPath(){
        Editable editable = mEtFilename.getText();
        if(null == editable || "".equals(editable.toString().trim())){
            Toast.makeText(MainActivity.this, R.string.str_filename_error, Toast.LENGTH_SHORT).show();
            return "";
        }
        String filename = editable.toString();
        File file = new File(Environment.getExternalStorageDirectory(), filename);
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        return "";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_play:
                if(null == path || "".equals(path.trim())){
                    Toast.makeText(MainActivity.this, R.string.str_filepath_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                musicInterface.play(path);
                mBtnPause.setText("暂停");
                break;
            case R.id.btn_pause:
                int state = musicInterface.pause();
                if(MusicPlayService.TYPE_STATE_PLAY == state){

                    mBtnPause.setText("暂停");
                } else if(MusicPlayService.TYPE_STATE_PAUSE == state){
                    mBtnPause.setText("继续");
                }
                break;
            case R.id.btn_replay:
                musicInterface.replay();
                mBtnPause.setText("暂停");
                break;
            case R.id.btn_stop:
                musicInterface.stop();
                mBtnPause.setText("暂停");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != intent){
            stopService(intent);
        }
    }
}

package com.zhi.musicplayservice;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import com.zhi.service.MusicInterface;

import java.io.IOException;

/**
 * Created by Administrator on 2016/11/3.
 */
public class MusicPlayService extends Service {

    public static final int TYPE_STATE_PLAY = 0x1;  // 继续
    public static final int TYPE_STATE_PAUSE = 0x2;  // 暂停

    private IBinder binder = new MusicPlayBinder();
    private MediaPlayer mediaPlayer;
    private boolean isPause;
    private int position;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MusicPlayBinder extends Binder implements MusicInterface {
        private String path;

        @Override
        public void play(String path) {
            this.path = path;
            mediaPlayer.reset();
            musicPlay(0);
        }

        @Override
        public int pause() {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                position = mediaPlayer.getCurrentPosition();
                isPause = true;   // 暂停
                return TYPE_STATE_PAUSE;
            }
            if(isPause) {  // 当暂停键被按过之后
                if(position>0 && path !=null){
                    mediaPlayer.start();
                    isPause = false;
                }
                return TYPE_STATE_PLAY;
            }
            return -1;
        }

        @Override
        public void replay() {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(0);
            } else {
                if(null != path){
                    path = null;
                }
            }
        }

        @Override
        public void stop() {
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
        }

        @Override
        public void releaseService() {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        private void musicPlay(int position) {
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(new MusicPreparedListener(position));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public class MusicPreparedListener implements MediaPlayer.OnPreparedListener {
            private int position;

            public MusicPreparedListener(int position) {
                this.position = position;
            }

            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
                mediaPlayer.seekTo(position);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}
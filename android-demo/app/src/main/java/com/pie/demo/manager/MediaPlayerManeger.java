package com.pie.demo.manager;

import android.media.MediaPlayer;

public class MediaPlayerManeger {

    private static volatile MediaPlayerManeger instance;
    private MediaPlayer mediaPlayer;
    public MediaPlayerManegerinterface mediaPlayerManegerinterface;

    public static MediaPlayerManeger getInstance() {
        if (instance == null) {
            synchronized (MediaPlayerManeger.class) {
                if (null == instance) {
                    instance = new MediaPlayerManeger();
                }
            }
        }
        return instance;

    }

    public void mDestory() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.stop();
            this.mediaPlayer.reset();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
    }

    public void mPlay(String paramString) {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.reset();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();

        try {
            this.mediaPlayer.setDataSource(paramString);
            this.mediaPlayer.prepareAsync();
            this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mediaPlayerManegerinterface != null) {
                        mediaPlayerManegerinterface.onComplete();
                    }
                }
            });
            this.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMediaPlayerManegerinterface(MediaPlayerManegerinterface paramMediaPlayerManegerinterface) {
        this.mediaPlayerManegerinterface = paramMediaPlayerManegerinterface;
    }
}

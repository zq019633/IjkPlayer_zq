package com.ciweek.ijkplayer_zq.CustomPlayer;


public interface PlayerControl {

    void start();
    void restart();
    void pause();
    void seekTo(int pos);

    boolean isIdle();
    boolean isPreparing();
    boolean isPrepared();
    boolean isBufferingPlaying();
    boolean isBufferingPaused();
    boolean isPlaying();
    boolean isPaused();
    boolean isError();
    boolean isCompleted();

    boolean isFullScreen();
    boolean isTinyWindow();
    boolean isNormal();

    long getDuration();
    long getCurrentPosition();
    int getBufferPercentage();

    void enterFullScreen();
    boolean exitFullScreen();
    void enterTinyWindow();
    boolean exitTinyWindow();

    void release();
}

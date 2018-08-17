package com.ciweek.ijkplayer_zq.CustomPlayer;

public class PlayerManager {

    private static PlayerManager instance;

    private CustomIjkPlayer mVideoPlayer;

    public static synchronized PlayerManager getPlayerManager(){
        if(instance==null){
            instance=new PlayerManager();
            return instance;
        }else{
            return instance;
        }
    }

    /**
     * 当前播放器的内核 是原生还是ijk
     * @param videoPlayer
     */
    public void setCurrentVideoPlayer(CustomIjkPlayer videoPlayer) {
        mVideoPlayer = videoPlayer;
    }

    public void releaseVideoPlayer() {
        if(mVideoPlayer!=null){
            mVideoPlayer.release();
            mVideoPlayer = null;
        }

    }
}

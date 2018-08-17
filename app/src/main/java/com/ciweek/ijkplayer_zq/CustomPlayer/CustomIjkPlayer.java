package com.ciweek.ijkplayer_zq.CustomPlayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.Map;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 把Imediaplayer 的播放逻辑 封装在这个类里
 */
public class CustomIjkPlayer extends FrameLayout implements PlayerControl, TextureView.SurfaceTextureListener {


    public static final int STATE_ERROR = -1;          // 播放错误
    public static final int STATE_IDLE = 0;            // 播放未开始
    public static final int STATE_PREPARING = 1;       // 播放准备中
    public static final int STATE_PREPARED = 2;        // 播放准备就绪
    public static final int STATE_PLAYING = 3;         // 正在播放
    public static final int STATE_PAUSED = 4;          // 暂停播放
    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
     **/
    public static final int STATE_BUFFERING_PLAYING = 5;
    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停)
     **/
    public static final int STATE_BUFFERING_PAUSED = 6;
    public static final int STATE_COMPLETED = 7;       // 播放完成

    public static final int PLAYER_NORMAL = 10;        // 普通播放器
    public static final int PLAYER_FULL_SCREEN = 11;   // 全屏播放器
    public static final int PLAYER_TINY_WINDOW = 12;   // 小窗口播放器

    public static final int PLAYER_TYPE_IJK = 111;      // IjkPlayer
    public static final int PLAYER_TYPE_NATIVE = 222;   // Android原生MediaPlayer

    //默认内核 使用ijk 播放  默认的状态 空闲状态  默认的播放器 普通
    private int mPlayerType = PLAYER_TYPE_IJK;
    private int mCurrentState = STATE_IDLE;

    //播放器类型
    private int mPlayerStyle = PLAYER_NORMAL;


    private Context mContext;
    private FrameLayout mContainer;
    private PlayerControlUI mController;
    private IMediaPlayer mMediaPlayer;


    private TextureView mTextureView;

    private SurfaceTexture mSurfaceTexture;


    private Map<String, String> mHeaders;
    private String mUrl;
    private int mBufferPercentage;
    private boolean isListShow;

    public CustomIjkPlayer(@NonNull Context context) {
        this(context, null);
    }

    public CustomIjkPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }


    private void init() {
        mContainer = new FrameLayout(mContext);
        mContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mContainer, params);



    }
    //在 布局上 添加 TextureView（显示）  和 ui （控制） 并且进行关联

    /**
     * 用于
     *
     * @param playerControlUI
     */
    public CustomIjkPlayer setController(PlayerControlUI playerControlUI) {

        mController = playerControlUI;
        mController.setVideoPlayer(this);
        mContainer.removeView(mController);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(mController, params);
        return this;
    }

    public CustomIjkPlayer setUp(String url, Map<String, String> headers) {
        mUrl = url;
        mHeaders = headers;
        return this;
    }

    public CustomIjkPlayer setThumb(String url) {
        mController.setThumb(url);
        return this;
    }

    public CustomIjkPlayer setTitle(String title) {
        mController.setTitle(title);
        return this;
    }

    @Override
    public void start() {
        PlayerManager.getPlayerManager().releaseVideoPlayer();
        PlayerManager.getPlayerManager().setCurrentVideoPlayer(this);
        if (mCurrentState == STATE_IDLE
                || mCurrentState == STATE_ERROR
                || mCurrentState == STATE_COMPLETED
                ) {
            initMediaPlayer();
            initTextureView();
            addTextureView();

        }

    }

    @SuppressLint("NewApi")
    private void addTextureView() {
        mContainer.removeView(mTextureView);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(mTextureView, 0, params);

    }

    @SuppressLint("NewApi")
    private void initTextureView() {
        if (mTextureView == null) {
            mTextureView = new TextureView(mContext);
            mTextureView.setSurfaceTextureListener(this);

        }
    }

    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            switch (mPlayerType) {
                case PLAYER_TYPE_NATIVE:
                    mMediaPlayer = new AndroidMediaPlayer();
                    break;
                case PLAYER_TYPE_IJK:
                default:
                    mMediaPlayer = new IjkMediaPlayer();
                    break;
            }

            //准备播放的监听
            mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
            //播放状态的监听
            mMediaPlayer.setOnInfoListener(mOnInfoListener);
            //播放完成的监听
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            //获取缓冲区百分比的监听
            mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
            //播放错误的监听
            mMediaPlayer.setOnErrorListener(mOnErrorListener);

        }
    }

    @Override
    public void restart() {
        if (mCurrentState == STATE_PAUSED) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
            mController.setControllerState(mPlayerStyle, mCurrentState);
        }
        if (mCurrentState == STATE_BUFFERING_PAUSED) {
            mMediaPlayer.start();
            mCurrentState = STATE_BUFFERING_PLAYING;
            mController.setControllerState(mPlayerStyle, mCurrentState);
        }


    }

    @Override
    public void pause() {
        if (mCurrentState == STATE_PLAYING) {
            mMediaPlayer.pause();
            mCurrentState = STATE_PAUSED;
            mController.setControllerState(mPlayerStyle, mCurrentState);
        }
        if (mCurrentState == STATE_BUFFERING_PLAYING) {
            mMediaPlayer.pause();
            mCurrentState = STATE_BUFFERING_PAUSED;
            mController.setControllerState(mPlayerStyle, mCurrentState);
        }


    }

    @Override
    public void seekTo(int pos) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(pos);
        }
    }

    @Override
    public boolean isIdle() {
        return mCurrentState == STATE_IDLE;
    }

    @Override
    public boolean isPreparing() {
        return mCurrentState == STATE_PREPARING;
    }

    @Override
    public boolean isPrepared() {
        return mCurrentState == STATE_PREPARED;
    }

    @Override
    public boolean isBufferingPlaying() {
        return mCurrentState == STATE_BUFFERING_PLAYING;
    }

    @Override
    public boolean isBufferingPaused() {
        return mCurrentState == STATE_BUFFERING_PAUSED;
    }

    @Override
    public boolean isPlaying() {
        return mCurrentState == STATE_PLAYING;
    }

    @Override
    public boolean isPaused() {
        return mCurrentState == STATE_PAUSED;
    }

    @Override
    public boolean isError() {
        return mCurrentState == STATE_ERROR;
    }

    @Override
    public boolean isCompleted() {
        return mCurrentState == STATE_COMPLETED;
    }

    @Override
    public boolean isFullScreen() {
        return mPlayerStyle == PLAYER_FULL_SCREEN;
    }


    @Override
    public boolean isTinyWindow() {
        return mPlayerStyle == PLAYER_TINY_WINDOW;
    }

    @Override
    public boolean isNormal() {
        return mPlayerStyle == PLAYER_NORMAL;
    }

    @Override
    public long getDuration() {
        return mMediaPlayer != null ? mMediaPlayer.getDuration() : 0;
    }

    @Override
    public long getCurrentPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    @Override
    public int getBufferPercentage() {
        return mBufferPercentage;
    }

    @Override
    public void enterFullScreen() {

    }

    @Override
    public boolean exitFullScreen() {
        return false;
    }

    @Override
    public void enterTinyWindow() {

    }

    @Override
    public boolean exitTinyWindow() {
        return false;
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mContainer.removeView(mTextureView);
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if (mController != null) {
            mController.reset();
        }
        mCurrentState = STATE_IDLE;
        mPlayerStyle = PLAYER_NORMAL;
    }

    /**
     * 设置播放器类型
     *
     * @param playerType IjkPlayer or MediaPlayer.
     */
    public void setPlayerType(int playerType) {
        mPlayerType = playerType;
    }

    /**
     * 以下四个方法 是未了让TextureView 的使用者知道 Surfacetexture 已经准备好
     * 这样就可以把Surfacetexture交给相应的内容源
     *
     * @param surfaceTexture
     * @param i
     * @param i1
     */

    @SuppressLint("NewApi")
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surfaceTexture;
            openMediaPlayer();
        } else {
            mTextureView.setSurfaceTexture(mSurfaceTexture);
        }
    }

    /**
     * 打开播放器
     *
     * @param
     */
    private void openMediaPlayer() {
        try {
            mMediaPlayer.setDataSource(mContext.getApplicationContext(), Uri.parse(mUrl), mHeaders);
            mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
            mMediaPlayer.prepareAsync();
            int duration = (int) mMediaPlayer.getDuration();

            mCurrentState = STATE_PREPARING;
            mController.setdDuration(duration);
            mController.setControllerState(mPlayerStyle, mCurrentState);
        } catch (Exception e) {
            Toast.makeText(mContext, "打开播放器错误", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return mSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }


    private IMediaPlayer.OnPreparedListener mOnPreparedListener
            = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mediaPlayer) {
            mediaPlayer.start();

            //当前的状态是准备的状态
            mCurrentState = STATE_PREPARED;
            mController.setControllerState(mPlayerStyle, mCurrentState);

        }
    };


    private IMediaPlayer.OnCompletionListener mOnCompletionListener
            = new IMediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            mCurrentState = STATE_COMPLETED;
            mController.setControllerState(mPlayerStyle, mCurrentState);
            //播放完成把当前的播放器对象置空

        }
    };


    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener
            = new IMediaPlayer.OnBufferingUpdateListener() {


        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
            mBufferPercentage = percent;
        }
    };

    private IMediaPlayer.OnErrorListener mOnErrorListener
            = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            mCurrentState = STATE_ERROR;
            mController.setControllerState(mPlayerStyle, mCurrentState);
            return false;
        }
    };


    private IMediaPlayer.OnInfoListener mOnInfoListener
            = new IMediaPlayer.OnInfoListener() {

        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
            switch (what) {
                //准备播放的状态
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:

                    mCurrentState = STATE_PLAYING;
                    mController.setControllerState(mPlayerStyle, mCurrentState);
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    //暂停时候的缓冲

                    if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED) {
                        mCurrentState = STATE_BUFFERING_PAUSED;

                    } else {
                        //正在播放时候的缓冲
                        mCurrentState = STATE_BUFFERING_PLAYING;
                    }
                    mController.setControllerState(mPlayerStyle, mCurrentState);
                    break;
                //缓冲结束
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:

                    if (mCurrentState == STATE_BUFFERING_PLAYING) {
                        mCurrentState = STATE_PLAYING;
                    } else if (mCurrentState == STATE_BUFFERING_PAUSED) {
                        mCurrentState = STATE_PAUSED;
                    }
                    mController.setControllerState(mPlayerStyle, mCurrentState);
                    break;


            }


            return false;
        }
    };


    public CustomIjkPlayer listTag(boolean b) {
        isListShow = b;
        mController.setIsListShow(isListShow);
        return this;
    }
}

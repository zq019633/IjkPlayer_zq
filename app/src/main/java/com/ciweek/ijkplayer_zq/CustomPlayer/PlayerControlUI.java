package com.ciweek.ijkplayer_zq.CustomPlayer;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ciweek.ijkplayer_zq.R;

import java.util.Locale;

public class PlayerControlUI extends FrameLayout implements View.OnClickListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener {

    //更新时间
    private static final int UPDATETIME = 1;

    //隐藏
    private static final int HIDECONTROLLER = 2;
    private final Context mContext;
    private ImageView startButton;
    private ImageView fullscreenButton;
    private SeekBar progressBar;
    private TextView currentTimeTextView;
    private TextView totalTimeTextView;
    private ProgressBar loading;
    private ImageView back;
    private TextView retry_btn;
    private RelativeLayout ijkLayout;
    private ProgressBar bottom_progress;
    private ImageView thumb;
    private TextView titleView;
    private TextView listTotal;
    private ImageView speed;
    private RelativeLayout rl_bright;
    private RelativeLayout rl_volume;
    private FrameLayout fl_touch_layout;
    private TextView tv_volume;
    private TextView tv_brightness;
    private TextView tv_fast_forward;
    private LinearLayout bottomContainer;
    private RelativeLayout topContainer;
    private CustomIjkPlayer mCustomIjkPlayer;
    private GestureDetector gestureDetector;

    //是否显示控制栏
    private boolean mIsShowBar = true;
    private boolean isListShow;
    private String currentTime;
    private String totalTime;
    //播放完成
    private boolean isCompleted = false;

    public PlayerControlUI(@NonNull Context context) {
        this(context, null);
    }

    public PlayerControlUI(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.video_layout_for_ijk, this, true);

        startButton = findViewById(R.id.start);
        fullscreenButton = findViewById(R.id.fullscreen);
        progressBar = findViewById(R.id.bottom_seek_progress);
        currentTimeTextView = findViewById(R.id.current);
        totalTimeTextView = findViewById(R.id.total);


        loading = findViewById(R.id.loading);
        ijkLayout = findViewById(R.id.ijkLayout);
        retry_btn = findViewById(R.id.replay_text);
        back = findViewById(R.id.back);
        bottom_progress = findViewById(R.id.bottom_progress);
        thumb = findViewById(R.id.thumb);
        titleView = findViewById(R.id.VideoTitle);

        listTotal = findViewById(R.id.listTotal);

        //显示快进或快退
        speed = findViewById(R.id.speed);

        //音量和亮度的布局
        rl_bright = findViewById(R.id.rl_bright);
        rl_volume = findViewById(R.id.rl_volume);


        //触摸 控制栏
        fl_touch_layout = findViewById(R.id.fl_touch_layout);
        tv_volume = findViewById(R.id.tv_volume);
        tv_brightness = findViewById(R.id.tv_brightness);
        tv_fast_forward = findViewById(R.id.tv_fast_forward);


        //头部标题布局 和底部seekBar 布局
        bottomContainer = findViewById(R.id.layout_bottom);
        topContainer = findViewById(R.id.layout_top);
        ijkLayout.setOnTouchListener(this);

        listTotal.setVisibility(VISIBLE);


        initListener();
        initGestureDetector();


    }

    private void initGestureDetector() {
        gestureDetector = new GestureDetector(mContext, mPlayerGestureListener);
    }

    private GestureDetector.OnGestureListener mPlayerGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (!isListShow && !isCompleted) {
                toggleController();
            }


            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }
    };

    /**
     * 触摸开关
     */
    private void toggleController() {
        handler.removeCallbacks(mHideSkipTipRunnable);
        mIsShowBar = !mIsShowBar;
        setControlBarVisible(mIsShowBar);
        if (mIsShowBar) {
            handler.postDelayed(mHideSkipTipRunnable, 5000);
        }

    }

    //隐藏
    private Runnable mHideSkipTipRunnable = new Runnable() {
        @Override
        public void run() {
            mIsShowBar = false;
            hideControllBar();
        }
    };

    private void setControlBarVisible(boolean mIsShowBar) {
        if (mIsShowBar) {
            showControllBar();
        } else {
            hideControllBar();
        }
    }

    /**
     * 隐藏控制栏
     */
    private void hideControllBar() {
        bottomContainer.setVisibility(INVISIBLE);
        startButton.setVisibility(INVISIBLE);
        titleView.setVisibility(INVISIBLE);
        bottom_progress.setVisibility(VISIBLE);
    }

    /**
     * 显示控制栏
     */
    private void showControllBar() {
        bottomContainer.setVisibility(VISIBLE);
        startButton.setVisibility(VISIBLE);
        titleView.setVisibility(VISIBLE);
        bottom_progress.setVisibility(INVISIBLE);

    }

    /**
     * 点击事件
     */
    private void initListener() {
        startButton.setOnClickListener(this);
        progressBar.setOnSeekBarChangeListener(this);

    }

    public void setVideoPlayer(CustomIjkPlayer customIjkPlayer) {
        mCustomIjkPlayer = customIjkPlayer;

        if (mCustomIjkPlayer.isIdle()) {
            back.setVisibility(GONE);
            startButton.setVisibility(VISIBLE);
            bottomContainer.setVisibility(GONE);

        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                listTotal.setVisibility(GONE);

                if (isCompleted) {

                    mCustomIjkPlayer.start();
                    isListShow = false;
                    isCompleted = false;
                }else{
                    isListShow = false;
                    togglePlayStatus();
                }

                break;
        }
    }

    private void togglePlayStatus() {

        if (mCustomIjkPlayer.isIdle()) {
            mCustomIjkPlayer.start();
            handler.sendEmptyMessage(UPDATETIME);

        }
        if (mCustomIjkPlayer.isPlaying() || mCustomIjkPlayer.isBufferingPlaying()) {
            mCustomIjkPlayer.pause();

        } else if (mCustomIjkPlayer.isPaused() || mCustomIjkPlayer.isBufferingPaused()) {
            mCustomIjkPlayer.restart();

        }

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATETIME:
                    int duration = (int) mCustomIjkPlayer.getDuration();
                    int currentPosition = (int) mCustomIjkPlayer.getCurrentPosition();
                    currentTime = buildTimeMilli(currentTimeTextView, currentPosition);
                    totalTime = buildTimeMilli(totalTimeTextView, duration);

                    progressBar.setMax(duration);
                    progressBar.setProgress(currentPosition);

                    int bufferPercentage = mCustomIjkPlayer.getBufferPercentage();
                    Log.e("zq" ,"缓冲="+bufferPercentage);
                    progressBar.setSecondaryProgress(bufferPercentage*1000);

                    bottom_progress.setMax(duration);
                    bottom_progress.setProgress(currentPosition);
                    bottom_progress.setSecondaryProgress(bufferPercentage * 1000);


                    handler.sendEmptyMessageDelayed(UPDATETIME, 1000);
                    break;
                case HIDECONTROLLER:


                    break;
            }
        }
    };


    /**
     * 时间转化
     *
     * @param textView
     * @param duration
     * @return
     */
    private String buildTimeMilli(TextView textView, int duration) {
        long total_seconds = duration / 1000;
        long hours = total_seconds / 3600;
        long minutes = (total_seconds % 3600) / 60;
        long seconds = total_seconds % 60;
        if (duration <= 0) {
            return "--:--";
        }
        if (hours >= 100) {
            textView.setText(String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds));
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);

        } else if (hours > 0) {
            textView.setText(String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds));
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            textView.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));

            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }


    }

    /**
     * 播放器的类型和 当前视频播放的状态
     *
     * @param mPlayerStyle
     * @param mCurrentState
     */
    public void setControllerState(int mPlayerStyle, int mCurrentState) {
        if (mCustomIjkPlayer != null) {
            switch (mPlayerStyle) {
                //正常状态
                case CustomIjkPlayer.PLAYER_NORMAL:
                    bottom_progress.setVisibility(VISIBLE);

                    break;
                //全屏状态
                case CustomIjkPlayer.PLAYER_FULL_SCREEN:
                    bottomContainer.setVisibility(VISIBLE);
                    topContainer.setVisibility(VISIBLE);
                    back.setVisibility(VISIBLE);
                    break;

            }

            switch (mCurrentState) {
                case CustomIjkPlayer.STATE_IDLE:
                    Log.e("zq", "UI界面 当前是空闲的");


                    loading.setVisibility(INVISIBLE);
                    break;
                case CustomIjkPlayer.STATE_PREPARING:
                    Log.e("zq", "UI界面 当前准备中");
                    startButton.setVisibility(INVISIBLE);
                    loading.setVisibility(VISIBLE);

                    break;
                case CustomIjkPlayer.STATE_PREPARED:
                    loading.setVisibility(INVISIBLE);
                    Log.e("zq", "UI界面 准备就绪");
                    startButton.setImageResource(R.drawable.zq_click_play_selector);
                    break;
                case CustomIjkPlayer.STATE_PAUSED:
                    Log.e("zq", "UI界面 暂停");
                    startButton.setVisibility(VISIBLE);
                    loading.setVisibility(INVISIBLE);
                    startButton.setImageResource(R.drawable.zq_click_play_selector);

                    break;
                case CustomIjkPlayer.STATE_BUFFERING_PLAYING:
                    loading.setVisibility(VISIBLE);
                    startButton.setVisibility(INVISIBLE);
                    break;
                case CustomIjkPlayer.STATE_BUFFERING_PAUSED:
                   loading.setVisibility(VISIBLE);
                    startButton.setVisibility(INVISIBLE);
                    break;

                case CustomIjkPlayer.STATE_PLAYING:
                    thumb.setVisibility(GONE);
//                    startButton.setVisibility(VISIBLE);
                    loading.setVisibility(INVISIBLE);
                    mIsShowBar = false;
                    Log.e("zq", "UI界面 当前是播放中");
                    startButton.setImageResource(R.drawable.zq_click_pause_selector);
                    break;

                case CustomIjkPlayer.STATE_COMPLETED:
                    loading.setVisibility(INVISIBLE);
                    startButton.setVisibility(VISIBLE);
                    startButton.setImageResource(R.drawable.zq_click_replay_selector);


                    mCustomIjkPlayer.pause();
                    isCompleted = true;
                    Log.e("zq", "UI界面 当前播放完成");
                    break;

            }


        }


    }

    public void setThumb(String url) {
        Glide.with(mContext).load(url).into(thumb);

    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            endGesture();

        }

        gestureDetector.onTouchEvent(motionEvent);
        return true;

    }

    /**
     * 结束识别
     */
    private void endGesture() {
        if(mIsShowBar){
            bottom_progress.setVisibility(INVISIBLE);
            startButton.setVisibility(VISIBLE);
        }

    }

    public void setIsListShow(boolean isListShow) {
        this.isListShow = isListShow;
    }

    /**
     * 控制器恢复到最初的状态
     */
    public void reset() {
        isListShow = true;
        startButton.setImageResource(R.drawable.zq_click_play_selector);
        bottomContainer.setVisibility(INVISIBLE);
        startButton.setVisibility(VISIBLE);


    }

    public void setdDuration(int duration) {
        buildTimeMilli(totalTimeTextView, duration);
        buildTimeMilli(listTotal, duration);
    }

    /**
     * 以下三个方法是seekbar的监听
     * @param seekBar
     * @param
     * @param b
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        buildTimeMilli(currentTimeTextView, progress);

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        progressBar.setSecondaryProgress((int) mCustomIjkPlayer.getCurrentPosition());
        handler.removeCallbacks(mHideSkipTipRunnable);

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        Log.e("zq", "当前进度" + progress);
        progressBar.setSecondaryProgress((int) mCustomIjkPlayer.getCurrentPosition());
        mCustomIjkPlayer.seekTo(progress);

        handler.postDelayed(mHideSkipTipRunnable, 5000);
    }
}

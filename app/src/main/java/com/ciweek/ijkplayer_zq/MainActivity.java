package com.ciweek.ijkplayer_zq;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ciweek.ijkplayer_zq.CustomPlayer.CustomIjkPlayer;
import com.ciweek.ijkplayer_zq.CustomPlayer.PlayerControlUI;

public class MainActivity extends AppCompatActivity {

    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();

    }

    private void initListener() {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,RvActivity.class));
            }
        });
    }

    private void initView() {
        btn = findViewById(R.id.btn);

        CustomIjkPlayer mVideoPlayer = findViewById(R.id.cip);
        mVideoPlayer.setPlayerType(CustomIjkPlayer.PLAYER_TYPE_IJK);

        PlayerControlUI mController = new PlayerControlUI(this);



        mVideoPlayer.setUp("http://wxsnsdy.tc.qq.com/105/20210/snsdyvideodownload?filekey=30280201010421301f0201690402534804102ca905ce620b1241b726bc41dcff44e00204012882540400&bizid=1023&hy=SH&fileparam=302c020101042530230204136ffd93020457e3c4ff02024ef202031e8d7f02030f42400204045a320a0201000400", null);
        mVideoPlayer.setController(mController)
                .setThumb("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-30-43.jpg")
                .setTitle("办公室小野");




    }
}

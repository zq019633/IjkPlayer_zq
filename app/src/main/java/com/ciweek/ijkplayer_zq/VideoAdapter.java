package com.ciweek.ijkplayer_zq;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ciweek.ijkplayer_zq.CustomPlayer.CustomIjkPlayer;
import com.ciweek.ijkplayer_zq.CustomPlayer.PlayerControlUI;

import java.util.ArrayList;
import java.util.zip.Inflater;

class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyHolder> {
    private final RvActivity context;
    private final ArrayList mList;

    public VideoAdapter(RvActivity rvActivity, ArrayList list) {
        this.context=rvActivity;
        this.mList=list;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_video, viewGroup, false);
        MyHolder holder=new MyHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        PlayerControlUI mController = new PlayerControlUI(context);

        myHolder.itemVideoPlayer.setUp(mList.get(i).toString(),null);
        myHolder.itemVideoPlayer.setController(mController)
            .listTag(true)
        ;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        private final CustomIjkPlayer itemVideoPlayer;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            itemVideoPlayer = itemView.findViewById(R.id.item_videoPlayer);


        }
    }
}

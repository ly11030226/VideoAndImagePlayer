package com.jzl.viewcycle.aboutViewCycle;

import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.jzl.viewcycle.BaseUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;


public class ScrollHelper {
    private static final String TAG = "ScrollHelper";
    private int firstVisible = 0;
    private int lastVisible = 0;
    private int playId;
    private int imageId;
    private int dx;
    private int dy;
    private PlayRunnable runnable;;
    private Handler playHandler = new Handler();


    public ScrollHelper(int playId, int imageId) {
        this.playId = playId;
        this.imageId = imageId;
    }

    public void onScrollStateChanged(RecyclerView view, int scrollState) {
        switch (scrollState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                playVideo(view);
                break;
        }
    }


    public void onScroll(int firstVisibleItem, int lastVisibleItem,int dx,int dy) {
        this.dx = dx;
        this.dy = dy;
        firstVisible = firstVisibleItem;
        lastVisible = lastVisibleItem;
    }



    private void playVideo(RecyclerView view) {
        GSYBaseVideoPlayer mGSYBaseVideoPlayer;
        if (view ==null) {
            return;
        }
        int width = BaseUtils.getScreenDisplayMetrics(view.getContext()).widthPixels;
        RecyclerView.LayoutManager mLayoutManager = view.getLayoutManager();
        int childCount = mLayoutManager.getChildCount();
        mGSYBaseVideoPlayer = mLayoutManager.getChildAt(0).findViewById(playId);
        int visiableState = mGSYBaseVideoPlayer.getVisibility();
//        MyLogger.i(TAG,"child count ... "+childCount+" width ... "+width);
        //由于设置全屏状态播放，layoutManager 最多有两个childCount
        if (childCount == 1) {
            if (visiableState == View.VISIBLE) {
                mGSYBaseVideoPlayer.startPlayLogic();
            }
        }else if (childCount == 2) {
            ImageView leftImage = mLayoutManager.getChildAt(0).findViewById(imageId);
            ImageView rightImage = mLayoutManager.getChildAt(1).findViewById(imageId);
            GSYBaseVideoPlayer leftVideo = mLayoutManager.getChildAt(0).findViewById(playId);
            GSYBaseVideoPlayer rightVideo = mLayoutManager.getChildAt(1).findViewById(playId);
            int leftVisiable = leftImage.getVisibility();
            int rightVisiable = rightImage.getVisibility();
            //左边显示图片 右边显示视频
            if (leftVisiable == View.VISIBLE && rightVisiable == View.GONE) {
                Rect rightRect = new Rect();
                rightVideo.getLocalVisibleRect(rightRect);
                int left = rightRect.left;
                int right = rightRect.right;
                Log.i(TAG,"left ... "+left+" right "+right);
                Log.i(TAG,"state ... "+rightVideo.getCurrentPlayer().getCurrentState());
                if ((rightVideo.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_NORMAL
                        || rightVideo.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_ERROR)) {
                    if (right>=width/2) {
                        sendPlayMessage(rightVideo);
                    }
                }else if (rightVideo.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_PLAYING) {
                    if (right < width/2) {
                        rightVideo.onVideoPause();
                    }
                }else if (rightVideo.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_PAUSE) {
                    if (right>=width/2) {
                        rightVideo.onVideoResume();
                    }
                }
            }//左边是视频 右边是图片
            else if (leftVisiable == View.GONE && rightVisiable == View.VISIBLE) {
                Rect rightRect = new Rect();
                rightImage.getLocalVisibleRect(rightRect);
                int left = rightRect.left;
                int right = rightRect.right;
                Log.i(TAG,"left ... "+left+" right "+right);
                if ((leftVideo.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_NORMAL
                        || leftVideo.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_ERROR)) {
                    if (right<=width/2) {
                        sendPlayMessage(leftVideo);
                    }
                }else if (leftVideo.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_PLAYING) {
                    if (right > width/2) {
                        leftVideo.onVideoPause();
                    }
                }else if (leftVideo.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_PAUSE) {
                    if (right<=width/2) {
                        leftVideo.onVideoResume();
                    }
                }
            }//左右都是视频
            else if (leftVisiable == View.GONE && rightVisiable == View.GONE) {
                Rect rightRect = new Rect();
                rightVideo.getLocalVisibleRect(rightRect);
                int left = rightRect.left;
                int right = rightRect.right;
                Log.i(TAG,"left ... "+left+" right "+right);
                if (leftVideo.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_PLAYING) {
                    if (right > width/2) {
                        sendPlayMessage(rightVideo);
                    }
                }else if (rightVideo.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_PLAYING) {
                    if (right<=width/2) {
                        sendPlayMessage(leftVideo);
                    }
                }else if ((leftVideo.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_NORMAL
                        || leftVideo.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_ERROR)) {
                    if (right <= width/2) {
                        sendPlayMessage(leftVideo);
                    }
                }else if ((rightVideo.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_NORMAL
                        || rightVideo.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_ERROR)) {
                    if (right >= width/2) {
                        sendPlayMessage(rightVideo);
                    }
                }
            }
        }
    }

    /**
     * 处理RecyclerView设置了PagerSnapHelper后，播放Video的逻辑
     */
    public void handleHavePagerSnapHelper(GSYBaseVideoPlayer video){
        if ((video.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_NORMAL
                || video.getCurrentPlayer().getCurrentState() == GSYBaseVideoPlayer.CURRENT_STATE_ERROR)) {
            sendPlayMessage(video);
        }
    }
    public void releaseVideo(){
        if (runnable!=null) {
            playHandler.removeCallbacks(runnable);
            runnable = null;
        }
    }

    private void sendPlayMessage(GSYBaseVideoPlayer gsyBaseVideoPlayer) {
        if (runnable != null) {
            GSYBaseVideoPlayer tmpPlayer = runnable.gsyBaseVideoPlayer;
            playHandler.removeCallbacks(runnable);
            runnable = null;
            if (tmpPlayer == gsyBaseVideoPlayer) {
                Log.i(TAG,"tmpPlayer == gsyBaseVideoPlayer");
                return;
            }
        }
        runnable = new PlayRunnable(gsyBaseVideoPlayer);
        //降低频率
        playHandler.postDelayed(runnable, 400);
    }


    private class PlayRunnable implements Runnable {

        GSYBaseVideoPlayer gsyBaseVideoPlayer;

        public PlayRunnable(GSYBaseVideoPlayer gsyBaseVideoPlayer) {
            this.gsyBaseVideoPlayer = gsyBaseVideoPlayer;
        }

        @Override
        public void run() {
            gsyBaseVideoPlayer.startPlayLogic();
        }
    }
}

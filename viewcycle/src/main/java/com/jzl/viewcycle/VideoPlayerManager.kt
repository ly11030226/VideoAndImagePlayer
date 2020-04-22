package com.jzl.viewcycle

import com.google.android.exoplayer2.Player
import com.shuyu.gsyvideoplayer.player.IPlayerManager
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class VideoPlayerManager {


    companion object{
        /**
         * 初始化操作
         */
        fun init(){
            //开启EXO模式
            PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
            //ijk关闭log
            IjkPlayerManager.setLogLevel(IjkMediaPlayer.IJK_LOG_SILENT)
            //切换渲染模式
            GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL)
        }

        fun setPlayerManager(playManager: Class<out IPlayerManager>){
            PlayerFactory.setPlayManager(playManager)
        }
        fun setLogLevel(logLevel :Int){
            IjkPlayerManager.setLogLevel(logLevel)
        }
        fun setShowType(type : Int){
            GSYVideoType.setShowType(type)
        }
    }

}
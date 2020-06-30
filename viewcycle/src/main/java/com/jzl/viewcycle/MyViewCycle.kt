package com.jzl.viewcycle
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.jzl.viewcycle.aboutViewCycle.MyLinearLayoutManager
import com.jzl.viewcycle.aboutViewCycle.RecyclerNormalAdapter
import com.jzl.viewcycle.aboutViewCycle.ScrollHelper
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import java.lang.ref.WeakReference

/**
 * 自定义RecyclerView
 */
class MyViewCycle : RecyclerView {

    companion object {
        const val TAG = "MyViewCycle"
        const val DEFAULT_STAY_TIME = 10
        const val AUTO_PLAY: Int = 0x11
        const val FIRST_IS_IMAGE_PLAY = 0x12
        const val TYPE_MP4 = ".mp4"
    }


    /**
     * 图片停留时长
     */
    private var mImageStayTime: Int = DEFAULT_STAY_TIME

    private var mAdapter: RecyclerNormalAdapter? = null
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mScrollHelper: ScrollHelper
    private lateinit var mHandler: MyHandler


    /**
     * 储存播放的视图
     */
    private var mDataList = ArrayList<String>()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyViewCycle)
        mImageStayTime = typedArray.getInt(R.styleable.MyViewCycle_imageStayTime, DEFAULT_STAY_TIME) * 1000
        typedArray.recycle()
    }

    fun bindDataAndStart(activity: Activity, list: List<String>,showImageMode: ShowImageMode?) {
        if (list.isNotEmpty()) {
            mDataList.clear()
            mDataList.addAll(list)
        }
        mHandler = MyHandler(this, activity)
        initRecyclerView()
        initData(showImageMode)
    }

    fun updateList(list: List<String>) {
        if (list.isNotEmpty()) {
            mDataList.clear()
            mDataList.addAll(list)
        }
        mAdapter?.notifyDataSetChanged()
    }

    private fun initRecyclerView() {
        mScrollHelper = ScrollHelper(R.id.video_player, R.id.iv_item)
        mLinearLayoutManager = MyLinearLayoutManager(context)
        mLinearLayoutManager.orientation = HORIZONTAL
        layoutManager = mLinearLayoutManager
        setHasFixedSize(true)
        val psh = PagerSnapHelper()
        psh.attachToRecyclerView(this)
    }

    private fun initData(showImageMode:ShowImageMode?) {
        mAdapter = RecyclerNormalAdapter(context, mDataList, MyPlayCompleteCallBack(this), mHandler)
        mAdapter?.addShowImageModeCallback(showImageMode)
        adapter = mAdapter
        //自助播放类
        this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var firstVisibleItem: Int = 0
            var lastVisibleItem: Int = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.i(TAG, "onScrolled")
                firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition()
                lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition()
                mScrollHelper.onScroll(firstVisibleItem, lastVisibleItem, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                try {
                    Log.i(TAG, "onScrollStateChanged")
                    if (newState == SCROLL_STATE_IDLE && firstVisibleItem == lastVisibleItem) {
                        mHandler.removeMessages(AUTO_PLAY)
                        playVideo(mScrollHelper, firstVisibleItem)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun playVideo(scrollHelper: ScrollHelper, pos: Int) {
        val mGSYBaseVideoPlayer =
            mLinearLayoutManager.getChildAt(0)?.findViewById<GSYBaseVideoPlayer>(
                R.id.video_player
            )
        val visiableState = mGSYBaseVideoPlayer!!.visibility
        if (visiableState == View.VISIBLE) {
            scrollHelper.handleHavePagerSnapHelper(mGSYBaseVideoPlayer)
        } else {
            val msg: Message = Message.obtain()
            msg.what = AUTO_PLAY
            msg.obj = pos
            mHandler.sendMessageDelayed(msg, mImageStayTime.toLong())
            releaseVideoAndNotify()
            mScrollHelper.releaseVideo()
        }
    }

    private fun releaseVideoAndNotify() {
        GSYVideoManager.releaseAllVideos()
        mAdapter?.notifyDataSetChanged()
    }

    fun onResume() {
        GSYVideoManager.onResume()
    }

    fun onPause() {
        GSYVideoManager.onPause()
    }

    fun onDestroy() {
        mHandler.removeCallbacksAndMessages(null)
    }

    interface PlayCompleteCallBack {
        fun playComplete(pos: Int) {}
    }

    interface ShowImageMode {
        fun getUrlAndImageView(url: String, iv: ImageView) {}
    }

    class MyPlayCompleteCallBack(private var myViewCycle: MyViewCycle) : PlayCompleteCallBack {
        override fun playComplete(pos: Int) {
            myViewCycle.smoothScrollToPosition(pos + 1)
        }
    }

    class MyHandler(private var myViewCycle: MyViewCycle, activity: Activity) :
        Handler(Looper.getMainLooper()) {
        private val a: WeakReference<Activity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            if (a.get() == null) {
                return
            }
//            val mActivity = a.get()
            when (msg.what) {
                AUTO_PLAY -> {
                    Log.i(TAG, "receive AUTO_PLAY")
                    val pos: Int = msg.obj as Int
                    myViewCycle.smoothScrollToPosition(pos + 1)
                }
                FIRST_IS_IMAGE_PLAY -> {
                    Log.i(TAG, "receive FIRST_IS_IMAGE_PLAY")
                    val mMessage = Message()
                    mMessage.what = AUTO_PLAY
                    mMessage.obj = 0
                    sendMessageDelayed(mMessage, myViewCycle.mImageStayTime.toLong())
                }
                else -> {
                }
            }
        }
    }
}


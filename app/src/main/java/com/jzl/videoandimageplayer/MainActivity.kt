package com.jzl.videoandimageplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.jzl.viewcycle.MyViewCycle
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object{
        const val TYPE_MP4 = ".mp4"
        const val TAG : String = "MainActivity"
        const val path = "/MyTemp/"
    }
    var list = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)
            val path = getExternalFilesDir(path)!!.absolutePath
            var f = File(path)
            if(f.exists()){
                var l = f.list()!!.asList()
                for (i in l.indices) {
                    var file = File(path,l[i])
                    if(file.exists()){
                        val uri = when{
                            (l[i].contains(TYPE_MP4)) ->{
                                //视频格式 file:/storage/emulated/0/Android/data/包名/files/MyTemp/xxx.mp4
                                file.toURI().toString()
                            }else ->{
                                //图片的格式 /storage/emulated/0/Android/data/包名/files/MyTemp/xxx.jpg
                                file.absolutePath
                            }
                        }
                        list.add(uri)
//                        Log.i(TAG,"第${i}的位置uri字符串是 ... $uri")
                    }
                }
                //对list进行排序
                list.sort()
            }else{
                f.mkdirs()
            }
            mvc.bindDataAndStart(this,list,object:MyViewCycle.ShowImageMode{
                override fun getUrlAndImageView(url: String, iv: ImageView) {
                    val file = File(url)
                    if (file.exists()) {
                        Glide.with(this@MainActivity).load(url).into(iv);
                    } else {
                        Glide.with(this@MainActivity).load(R.mipmap.image_empty).into(iv);
                    }
                }
            })
//            mvc.bindDataAndStart(this,list,null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        mvc.onResume()
    }

    override fun onPause() {
        super.onPause()
        mvc.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mvc.onDestroy()
    }
}

package com.jzl.viewcycle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.core.content.FileProvider;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class BaseUtils {
    private static final String TAG = "BaseUtils";
    /**
     * 获取本机ip
     *
     * @return
     */
    public static String getIP() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hostIp;
    }
    /**
     * 安装apk
     *
     * @param context
     * @param apkPathFile
     */
    public static void installApk(Context context, File apkPathFile) {
        try {
            /**
             * provider
             * 处理android 7.0 及以上系统安装异常问题
             */
            Intent install = new Intent();
            install.setAction(Intent.ACTION_VIEW);
            install.addCategory(Intent.CATEGORY_DEFAULT);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //在AndroidManifest中的android:authorities值
                Uri apkUri = FileProvider.getUriForFile(context, "com.szty.h5xinfa.fileprovider", apkPathFile);
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                Log.d(TAG, "apkUri=" + apkUri); install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                install.setDataAndType(Uri.fromFile(apkPathFile), "application/vnd.android.package-archive");
            }
            context.startActivity(install);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            Log.e(TAG, "installApk: is error",null);
            if (apkPathFile.exists()) {
                apkPathFile.delete();
            }
        }
    }


    /** 删除文件夹以及目录下的文件
      * @param filePath 被删除目录的文件路径
      * @return 目录删除成功返回true，否则返回false
      */

    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag){
                    break;
                }
            }
        }
        if (!flag){
            return false;
        }
        //删除当前空目录
        return dirFile.delete();
    }

    public static boolean deleteFile(String str){
        File file = new File(str);
        if (file.isFile()&&file.exists()) {
            return file.delete();
        }
        return false;
    }


    /**
      *  根据路径删除指定的目录或文件，无论存在与否
      *  @param filePath 要删除的目录或文件
      *  @return 删除成功返回 true，否则返回 false。
      */
    public static boolean deleteFolder(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(filePath);
            } else {
                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }

 /**
  * 获取版本号
  *
  * @return
  */
    public static String getVersionCode(Activity activity) {
        // 包管理器 可以获取清单文件信息
        PackageManager packageManager = activity.getPackageManager();
        try {
            // 获取包信息
            // 参1 包名 参2 获取额外信息的flag 不需要的话 写0
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    activity.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取本地视频的第一帧
     *
     * @param localPath
     * @return
     */
    public static Bitmap getLocalVideoBitmap(String localPath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {

            //根据文件路径获取缩略图
            retriever.setDataSource(localPath);
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return bitmap;
    }

    /**
     * 获取本地视频的第一帧
     *
     * @param context
     * @return
     */
    public static Bitmap getLocalVideoBitmap(Context context, Uri uri) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //根据文件路径获取缩略图
            retriever.setDataSource(context,uri);
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return bitmap;
    }
    /**
     * 得到屏幕信息
     * getScreenDisplayMetrics().heightPixels 屏幕高
     * getScreenDisplayMetrics().widthPixels 屏幕宽
     *
     * @return
     */
    public static DisplayMetrics getScreenDisplayMetrics(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = manager.getDefaultDisplay();
        display.getMetrics(displayMetrics);

        return displayMetrics;

    }

    public static int getScreenWidth(Context context){
        int width = 0;
        if (context!=null) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            width = dm.widthPixels;
        }
        return width;
    }

    public static int getScreenHeight(Context context){
        int height = 0;
        if (context!=null) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            height = dm.heightPixels;
        }
        return height;
    }

}

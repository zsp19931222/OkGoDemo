package com.example.administrator.okgodemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.administrator.okgodemo.model.ApkModel;
import com.example.administrator.okgodemo.ui.NumberProgressBar;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.db.DownloadManager;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.utils.HttpUtils;
import com.lzy.okgo.utils.IOUtils;
import com.lzy.okgo.utils.OkLogger;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadListener;
import com.lzy.okserver.download.DownloadTask;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import rx.Observer;

public class MainActivity extends AppCompatActivity {
    private String Url = "";
    private ApkModel apkModel;
    private List<ApkModel> apks;
    private NumberProgressBar progressBar;
    private Button button;
    private OkDownload okDownload;
    private List<Progress> progressList;
    private DownloadTask downloadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setData();
    }

    /**
     * 下载文件
     */
    private void downloadFile() {
        RxPermissions.getInstance(this).request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(observer);

        Toast.makeText(this, "已添加到下载任务", Toast.LENGTH_SHORT).show();
    }

    /**
     * 初始化
     */
    private void init() {
        progressBar = findViewById(R.id.pbProgress);
        button = findViewById(R.id.dowm);

        okDownload = OkDownload.getInstance();
        okDownload.setFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CTVideo/");
        okDownload.getThreadPool().setCorePoolSize(3);
        //从数据库中恢复数据
        progressList = DownloadManager.getInstance().getAll();
        OkDownload.restore(progressList);
    }

    /**
     * 设置下载内容
     */

    private void setData() {
        progressBar.setMax(100);
        progressBar.setProgress((int) (progressList.get(0).fraction * 100));
        for (int i = 0; i < progressList.size(); i++) {
            Progress progress = progressList.get(i);
            downloadTask = okDownload.getTask(progressList.get(i).tag);
            setInit(progress, this);
        }

        apks = new ArrayList<>();
        ApkModel apk1 = new ApkModel();
        apk1.name = "斗鱼";
        apk1.iconUrl = "http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/0c10c4c0155c9adf1282af008ed329378d54112ac";
        apk1.url = "https://staticlive.douyucdn.cn/upload/client/douyu_client_2_0v1_2_0.apk";
        apks.add(apk1);
    }


    /**
     * 获取权限
     */
    Observer observer = new Observer<Boolean>() {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
        }

        @Override
        public void onNext(Boolean o) {
            if (o) {
                for (ApkModel apk : apks) {
                    GetRequest<File> request = OkGo.<File>get(apk.url);
                    //这里第一个参数是tag，代表下载任务的唯一标识
                    OkDownload.request(apk.url, request)
                            .priority(apk.priority)//
                            .extra1(apk)//
                            .register(new LogDownloadListener())
                            .start();
                }
            } else {
                Toast.makeText(MainActivity.this, "SD卡下载权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 下载过程监听
     */
    public class LogDownloadListener extends DownloadListener {

        public LogDownloadListener() {
            super("LogDownloadListener");
        }

        @Override
        public void onStart(Progress progress) {
            progressBar.setProgress((int) (progress.fraction * 100));
            button.setText("暂停");
        }

        @Override
        public void onProgress(Progress progress) {
            progressBar.setProgress((int) (progress.fraction * 100));
            button.setText("暂停");
        }

        @Override
        public void onError(Progress progress) {
            progressBar.setProgress((int) (progress.fraction * 100));
            button.setText("出错");
            progress.exception.printStackTrace();
        }

        @Override
        public void onFinish(File file, Progress progress) {
            progressBar.setProgress((int) (progress.fraction * 100));
            Toast.makeText(MainActivity.this, "下载完成:" + progress.filePath, Toast.LENGTH_SHORT).show();
            button.setText("重新下载");
        }

        @Override
        public void onRemove(Progress progress) {
            progressBar.setProgress((int) (progress.fraction * 100));
        }
    }

    /**
     * 根据本地缓存判断显示与进度条
     */
    private void setInit(final Progress progress, final Context context) {
        switch (progress.status) {
            case Progress.NONE:
                button.setText("下载");
                break;
            case Progress.PAUSE:
                button.setText("继续");
                break;
            case Progress.ERROR:
                button.setText("出错");
                break;
            case Progress.WAITING:
                button.setText("等待");

                break;
            case Progress.FINISH:
                button.setText("重新下载");
                break;
            case Progress.LOADING:
                button.setText("暂停");
                break;
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (progress.status) {
                    case Progress.NONE:
                        downloadFile();
                        break;
                    case Progress.PAUSE:
                        downloadFile();
                        break;
                    case Progress.ERROR:
                        downloadFile();
                        break;
                    case Progress.WAITING:
                        downloadFile();
                        break;
                    case Progress.FINISH:
                        downloadTask.restart();
                        break;
                    case Progress.LOADING:
                        downloadTask.pause();
                        break;
                }
            }
        });
    }
}

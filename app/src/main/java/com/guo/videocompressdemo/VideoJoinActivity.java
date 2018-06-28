package com.guo.videocompressdemo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.guo.videocompressdemo.compress.Util;
import com.qiniu.pili.droid.shortvideo.PLShortVideoComposer;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by guoh on 2018/6/28.
 * 功能描述：
 * 需要的参数：
 */
public class VideoJoinActivity extends AppCompatActivity {


    private static final int REQUEST_FOR_VIDEO_FILE = 0;

    private PLShortVideoComposer mShortVideoComposer;

    private Button btn_select_file;
    private Button btn_start_join;

    private TextView tv_file_in_path;
    private TextView tv_file_join_progress;
    private TextView tv_file_out_path;
    private TextView tv_join_file_time;
    private TextView tv_join_result;
    private Spinner spResolution;
    private Spinner spBitrate;

    private long startJoinTime;
    private long endJoinTime;

    private List<String> videos=new ArrayList<>();


    private String outputRootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_video_join);
        initViews();
        initData();
        addListener();
    }

    private void initViews(){
        btn_select_file=findViewById(R.id.btn_select_file);
        btn_start_join=findViewById(R.id.btn_start_join);

        tv_file_in_path=findViewById(R.id.tv_file_path_in);
        tv_file_join_progress=findViewById(R.id.tv_file_join_progress);
        tv_file_out_path=findViewById(R.id.tv_file_path_out);
        tv_join_file_time=findViewById(R.id.tv_join_file_time);
        tv_join_result=findViewById(R.id.tv_join_result);
        spResolution=findViewById(R.id.sp_resolution);
        spBitrate=findViewById(R.id.sp_bitrate);
    }

    private void addListener(){
        btn_select_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (Build.VERSION.SDK_INT < 19) {
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("video/*");
                } else {
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("video/*");
                }
                startActivityForResult(intent, REQUEST_FOR_VIDEO_FILE);
            }
        });
        btn_start_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videos.size()<2){
                    Toast.makeText(VideoJoinActivity.this, "选择拼接的视频最小数量为2",Toast.LENGTH_SHORT).show();
                    return;
                }
                final String outPath = outputRootPath+ File.separator  + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mp4";
                PLVideoEncodeSetting setting = new PLVideoEncodeSetting(VideoJoinActivity.this);
                //设置视频分辨率
                setting.setEncodingSizeLevel(getEncodingSizeLevel(spResolution.getSelectedItemPosition()));
                //设置比特率
                startJoinTime = System.currentTimeMillis();
                setting.setEncodingBitrate(getEncodingBitrateLevel(spBitrate.getSelectedItemPosition()));
                if (mShortVideoComposer.composeVideos(videos, outPath, setting, mVideoSaveListener)) {
                } else {
                    Toast.makeText(VideoJoinActivity.this, "开始拼接失败！",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void initData(){
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, RecordSettings.ENCODING_SIZE_LEVEL_TIPS_ARRAY);
        spResolution.setAdapter(adapter1);
        spResolution.setSelection(7);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, RecordSettings.ENCODING_BITRATE_LEVEL_TIPS_ARRAY);
        spBitrate.setAdapter(adapter2);
        spBitrate.setSelection(2);

        mShortVideoComposer = new PLShortVideoComposer(this);
    }

    private PLVideoSaveListener mVideoSaveListener = new PLVideoSaveListener() {
        @Override
        public void onSaveVideoSuccess(final String filepath) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    endJoinTime = System.currentTimeMillis();
                    tv_join_result.setText("join_result: "+"Join Success");
                    tv_file_out_path.setText("file_out_path:"+filepath);
                    tv_join_file_time.setText("join_file_time:"+((endJoinTime-startJoinTime)/1000)+"s");
                }
            });
        }

        @Override
        public void onSaveVideoFailed(final int errorCode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    endJoinTime = System.currentTimeMillis();
                    tv_join_result.setText("join_result: "+errorCode);
                    tv_join_file_time.setText("join_file_time:"+((endJoinTime-startJoinTime)/1000)+"s");
                }
            });
        }

        @Override
        public void onSaveVideoCanceled() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    endJoinTime = System.currentTimeMillis();
                    tv_join_result.setText("join_result: "+"Join Cancel");
                    tv_join_file_time.setText("join_file_time:"+((endJoinTime-startJoinTime)/1000)+"s");
                }
            });
        }

        @Override
        public void onProgressUpdate(final float percentage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_file_join_progress.setText("file_join_progress:"+(int) (100 * percentage)+"%");
                }
            });
        }
    };

    private PLVideoEncodeSetting.VIDEO_ENCODING_SIZE_LEVEL getEncodingSizeLevel(int position) {
        return RecordSettings.ENCODING_SIZE_LEVEL_ARRAY[position];
    }

    private int getEncodingBitrateLevel(int position) {
        return RecordSettings.ENCODING_BITRATE_LEVEL_ARRAY[position];
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FOR_VIDEO_FILE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                try {
                    String inputPath = Util.getFilePath(this, data.getData());
                    videos.add(inputPath);
                    tv_file_in_path.setText(tv_file_in_path.getText()+"\n"+inputPath);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}

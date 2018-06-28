package com.guo.videocompressdemo;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.guo.videocompressdemo.compress.Util;
import com.guo.videocompressdemo.compress.VideoCompress;
import com.qiniu.pili.droid.shortvideo.PLShortVideoTranscoder;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoCompressActivity extends AppCompatActivity {

    private static final int REQUEST_FOR_VIDEO_FILE = 0;
    private Button btnSelectFile;
    private Button btnStartCompress;
    private TextView tvFilePathIn;
    private TextView tvFileSizeIn;
    private TextView fileCompressProgress;
    private TextView tvFilePathOut;
    private TextView tvFileSizeOut;
    private TextView tvCompressFileTime;
    private TextView tvCompressResult;
    private TextView tvFileInDuration;
    private long startCompressTime;
    private long endCompressTime;

    private String outputRootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_video_compress);
        initViews();
        setOnClick();
    }

    private void initViews(){
        btnSelectFile=findViewById(R.id.btn_select_file);
        btnStartCompress=findViewById(R.id.btn_start_compress);
        tvFilePathIn=findViewById(R.id.tv_file_path_in);
        tvFileSizeIn=findViewById(R.id.tv_file_size_in);
        fileCompressProgress=findViewById(R.id.tv_file_compress_progress);
        tvFilePathOut=findViewById(R.id.tv_file_path_out);
        tvFileSizeOut=findViewById(R.id.tv_file_size_out);
        tvCompressFileTime=findViewById(R.id.tv_compress_file_time);
        tvCompressResult=findViewById(R.id.tv_compress_result);
        tvFileInDuration =findViewById(R.id.tv_file_duration_in);
    }



    private void setOnClick(){
        btnSelectFile.setOnClickListener(new View.OnClickListener() {
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
        btnStartCompress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String outPath = outputRootPath+ File.separator  + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mp4";
//                  useIsoParser(1920,1080,1000*1000,outPath);
                useQiNiuParser(1920,1080,1000*1000,outPath);
                }
        });
    }

    //使用七牛云的压缩
    private void useQiNiuParser(int width,int height,int bitrate,String outPath){
        PLShortVideoTranscoder mShortVideoTranscoder=new PLShortVideoTranscoder(VideoCompressActivity.this,tvFilePathIn.getText().toString().replace("file_in_path:",""), outPath);
        mShortVideoTranscoder.transcode(width, height, bitrate, 0, false, new PLVideoSaveListener() {
            @Override
            public void onSaveVideoSuccess(final String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fileCompressProgress.setText("file_compress_progress:"+ "100%");
                        tvCompressResult.setText("compress_result:"+"Compress Success");
                        endCompressTime = System.currentTimeMillis();
                        tvCompressFileTime.setText("compress_file_time:"+((endCompressTime-startCompressTime)/1000)+"s");
                        try {
                            tvFileSizeOut.setText("file_out_size:"+ android.text.format.Formatter.formatFileSize(VideoCompressActivity.this, Util.getFileSize(tvFilePathOut.getText().toString().replace("file_out_path:",""))));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onSaveVideoFailed(final int errorCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvCompressResult.setText("compress_result:"+"Compress Failed");
                        endCompressTime = System.currentTimeMillis();
                        tvCompressFileTime.setText("compress_file_time:"+((endCompressTime-startCompressTime)/1000)+"s");
                    }
                });
            }

            @Override
            public void onSaveVideoCanceled() {
            }

            @Override
            public void onProgressUpdate(final float percentage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fileCompressProgress.setText("file_compress_progress:"+String.valueOf((int) (100 * percentage)) + "%");
                    }
                });
            }
        });
        tvFilePathOut.setText("file_out_path:"+outPath);
        startCompressTime = System.currentTimeMillis();
    }

    //系统MediaCodec压缩
    private void useIsoParser(int width,int height,int bitrate,final String outPath){
        //调用系统压缩
        VideoCompress.compressVideoWithBitRate(width,height,bitrate,tvFilePathIn.getText().toString().replace("file_in_path:",""), outPath, new VideoCompress.CompressListener() {
            @Override
            public void onStart() {
                tvFilePathOut.setText("file_out_path:"+outPath);
                startCompressTime = System.currentTimeMillis();
            }

            @Override
            public void onSuccess() {
                fileCompressProgress.setText("file_compress_progress:"+ "100%");
                tvCompressResult.setText("compress_result:"+"Compress Success");
                endCompressTime = System.currentTimeMillis();
                tvCompressFileTime.setText("compress_file_time:"+((endCompressTime-startCompressTime)/1000)+"s");
                try {
                    tvFileSizeOut.setText("file_out_size:"+ android.text.format.Formatter.formatFileSize(VideoCompressActivity.this, Util.getFileSize(tvFilePathOut.getText().toString().replace("file_out_path:",""))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail() {
                tvCompressResult.setText("compress_result:"+"Compress Failed");
                endCompressTime = System.currentTimeMillis();
                tvCompressFileTime.setText("compress_file_time:"+((endCompressTime-startCompressTime)/1000)+"s");
            }

            @Override
            public void onProgress(float percent) {
                fileCompressProgress.setText("file_compress_progress:"+String.valueOf(percent) + "%");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FOR_VIDEO_FILE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                try {
                    String inputPath = Util.getFilePath(this, data.getData());
                    tvFilePathIn.setText("file_in_path:"+inputPath);
                    tvFileSizeIn.setText("file_in_size:"+ android.text.format.Formatter.formatFileSize(VideoCompressActivity.this,Util.getFileSize(inputPath)));

                    //获取视频时间
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(inputPath);
                    mediaPlayer.prepare();

                    tvFileInDuration.setText("file_in_duration:"+(mediaPlayer.getDuration()/1000)+"s");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }


}

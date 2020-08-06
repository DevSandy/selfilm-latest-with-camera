package com.example.selfilm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.selfilm.base.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;

public class VideoPreviewActivity extends BaseActivity {

    @BindView(R.id.fl)
    FrameLayout mFlVideo;
    @BindView(R.id.videoView)
    VideoView mVideoView;
    @BindView(R.id.iv_thumb)
    ImageView mIvThumb;
    @BindView(R.id.iv_play)
    ImageView mIvPlay;


    private EditText edcaption;
    private String mVideoPath;
    private String mVideoThumb;
    private ImageView cancelbtn;
    private EditText captioned;
    private EditText hashtaged;
    private Button uploadbtn;
    private String maudiopath= Environment.getExternalStorageDirectory().toString() + File.separator + "video.mp3";;


    public static void startActivity(Context context, String videoPath, String videoThumb) {
        Intent intent = new Intent(context, VideoPreviewActivity.class);
        intent.putExtra("path", videoPath);
        intent.putExtra("thumb", videoThumb);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_video_preview;
    }

//    @Override
//    protected void setSupportActionBar(Toolbar toolbar) {
//
//    }

    @Override
    protected void init() {
        mVideoPath = getIntent().getStringExtra("path");
        mVideoThumb = getIntent().getStringExtra("thumb");
    }

    @Override
    protected void initView() {
        cancelbtn = (ImageView)findViewById(R.id.cancelbtn);
        captioned = (EditText)findViewById(R.id.captioned);
        hashtaged = (EditText)findViewById(R.id.hashtaged);
        uploadbtn = (Button)findViewById(R.id.uploadbtn);

        uploadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    uploadvideo();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });



        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(VideoPreviewActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();
                int videoWidth = mp.getVideoWidth();
                int videoHeight = mp.getVideoHeight();
                float videoProportion = (float) videoWidth / (float) videoHeight;
                int screenWidth = mFlVideo.getWidth();
                int screenHeight = mFlVideo.getHeight();
                float screenProportion = (float) screenWidth / (float) screenHeight;
                if (videoProportion > screenProportion) {
                    lp.width = screenWidth;
                    lp.height = (int) ((float) screenWidth / videoProportion);
                } else {
                    lp.width = (int) (videoProportion * (float) screenHeight);
                    lp.height = screenHeight;
                }
                mVideoView.setLayoutParams(lp);

                Log.e("videoView",
                        "videoWidth:" + videoWidth + ", videoHeight:" + videoHeight);
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mIvPlay.setVisibility(View.VISIBLE);
                mIvThumb.setVisibility(View.VISIBLE);
                Glide.with(getApplicationContext())
                        .load(mVideoThumb)
                        .into(mIvThumb);
            }
        });
        videoStart();
    }

    public void uploadvideo() throws IOException {
        new AudioExtractor().genVideoUsingMuxer(mVideoPath, maudiopath, -1, -1, true, false);
        Toast.makeText(this, mVideoPath, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent  = new Intent(VideoPreviewActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    @OnClick({R.id.iv_play})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_play:
                mIvThumb.setVisibility(View.GONE);
                mIvPlay.setVisibility(View.GONE);
                videoStart();
                break;
        }
    }

    public void videoStart() {
        mVideoView.start();
    }

    public void videoPause() {
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }

    public void videoDestroy() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //全屏显示
//        if (Build.VERSION.SDK_INT >= 19) {
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        } else {
//            View decorView = getWindow().getDecorView();
//            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
//            decorView.setSystemUiVisibility(option);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoDestroy();
    }
    public class AudioExtractor {

        private static final int DEFAULT_BUFFER_SIZE = 1 * 1024 * 1024;
        private static final String TAG = "AudioExtractorDecoder";

        /**
         * @param srcPath  the path of source video file.
         * @param dstPath  the path of destination video file.
         * @param startMs  starting time in milliseconds for trimming. Set to
         *                 negative if starting from beginning.
         * @param endMs    end time for trimming in milliseconds. Set to negative if
         *                 no trimming at the end.
         * @param useAudio true if keep the audio track from the source.
         * @param useVideo true if keep the video track from the source.
         * @throwsIOException
         */
        @SuppressLint("NewApi")
        public void genVideoUsingMuxer(String srcPath, String dstPath, int startMs, int endMs, boolean useAudio, boolean useVideo) throws IOException {
            // Set up MediaExtractor to read from the source.
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(srcPath);
            int trackCount = extractor.getTrackCount();
            // Set up MediaMuxer for the destination.
            MediaMuxer muxer;
            muxer = new MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            // Set up the tracks and retrieve the max buffer size for selected
            // tracks.
            HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>(trackCount);
            int bufferSize = -1;
            for (int i = 0; i < trackCount; i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                boolean selectCurrentTrack = false;
                if (mime.startsWith("audio/") && useAudio) {
                    selectCurrentTrack = true;
                } else if (mime.startsWith("video/") && useVideo) {
                    selectCurrentTrack = true;
                }
                if (selectCurrentTrack) {
                    extractor.selectTrack(i);
                    int dstIndex = muxer.addTrack(format);
                    indexMap.put(i, dstIndex);
                    if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                        int newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                        bufferSize = newSize > bufferSize ? newSize : bufferSize;
                    }
                }
            }
            if (bufferSize < 0) {
                bufferSize = DEFAULT_BUFFER_SIZE;
            }
            // Set up the orientation and starting time for extractor.
            MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
            retrieverSrc.setDataSource(srcPath);
            String degreesString = retrieverSrc.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            if (degreesString != null) {
                int degrees = Integer.parseInt(degreesString);
                if (degrees >= 0) {
                    muxer.setOrientationHint(degrees);
                }
            }
            if (startMs > 0) {
                extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            }
            // Copy the samples from MediaExtractor to MediaMuxer. We will loop
            // for copying each sample and stop when we get to the end of the source
            // file or exceed the end time of the trimming.
            int offset = 0;
            int trackIndex = -1;
            ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            muxer.start();
            while (true) {
                bufferInfo.offset = offset;
                bufferInfo.size = extractor.readSampleData(dstBuf, offset);
                if (bufferInfo.size < 0) {
                    Log.d(TAG, "Saw input EOS.");
                    bufferInfo.size = 0;
                    break;
                } else {
                    bufferInfo.presentationTimeUs = extractor.getSampleTime();
                    if (endMs > 0 && bufferInfo.presentationTimeUs > (endMs * 1000)) {
                        Log.d(TAG, "The current sample is over the trim end time.");
                        break;
                    } else {
                        bufferInfo.flags = extractor.getSampleFlags();
                        trackIndex = extractor.getSampleTrackIndex();
                        muxer.writeSampleData(indexMap.get(trackIndex), dstBuf, bufferInfo);
                        extractor.advance();
                    }
                }
            }
            muxer.stop();
            muxer.release();
            return;
        }
    }
}


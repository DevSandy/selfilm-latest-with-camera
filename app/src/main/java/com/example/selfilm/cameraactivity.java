package com.example.selfilm;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ankushgrover.hourglass.Hourglass;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import ai.deepar.ar.AREventListener;
import ai.deepar.ar.CameraResolutionPreset;
import ai.deepar.ar.DeepAR;

public class cameraactivity extends AppCompatActivity implements SurfaceHolder.Callback, AREventListener {

    private CameraGrabber cameraGrabber;
    private int defaultCameraDevice = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private int cameraDevice = defaultCameraDevice;
    private DeepAR deepAR;
    private ImageView btn_capture;
    Boolean isRecording = false;
    Boolean time15secboolean = true;
    Boolean time30secboolean = false;
    String filepath;
    Boolean pausetimer = false;
    String outputFile = Environment.getExternalStorageDirectory() +"/selfilm";
    Bitmap bmThumbnail;


    private ImageButton switchCamera;
    private ImageButton maskbtn;
    private ImageButton effectbtn;
    private ImageButton filterbtn;
    private TextView fliptxt;
    private TextView masktext;
    private TextView effecttext;
    private TextView filtertext;
    private int currentMask=0;
    TextView time30sec;
    TextView time15sec;
    private int currentEffect=0;
    private int currentFilter=0;
    private int timerint = 15000;

    private Hourglass hourglass;

    private int screenOrientation;

    ArrayList<String> masks;
    ArrayList<String> effects;
    ArrayList<String> filters;

    private int activeFilterType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cameraactivity);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO },
                    1);
        } else {
            // Permission has already been granted
            initialize();
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return; // no permission
                }
                initialize();
            }
        }
    }


    private void initialize() {
        setContentView(R.layout.activity_cameraactivity);
        initializeDeepAR();
        initializeFilters();
        initalizeViews();
    }

    private void initializeFilters() {
        masks = new ArrayList<>();
        masks.add("none");
        masks.add("aviators");
        masks.add("bigmouth");
        masks.add("dalmatian");
        masks.add("flowers");
        masks.add("koala");
        masks.add("lion");
        masks.add("smallface");
        masks.add("teddycigar");
        masks.add("kanye");
        masks.add("tripleface");
        masks.add("sleepingmask");
        masks.add("fatify");
        masks.add("obama");
        masks.add("mudmask");
        masks.add("pug");
        masks.add("slash");
        masks.add("twistedface");
        masks.add("grumpycat");

        effects = new ArrayList<>();
        effects.add("none");
        effects.add("fire");
        effects.add("rain");
        effects.add("heart");
        effects.add("blizzard");

        filters = new ArrayList<>();
        filters.add("none");
        filters.add("filmcolorperfection");
        filters.add("tv80");
        filters.add("drawingmanga");
        filters.add("sepia");
        filters.add("bleachbypass");
    }

    private void initalizeViews() {
        ImageButton previousMask = findViewById(R.id.previousMask);
        ImageButton nextMask = findViewById(R.id.nextMaskk);
        maskbtn = findViewById(R.id.maskbtn);
        effectbtn = findViewById(R.id.effectbtn);
        filterbtn = findViewById(R.id.filterbtn);
        fliptxt = findViewById(R.id.fliptxt);
        masktext = findViewById(R.id.masktxt);
        effecttext = findViewById(R.id.effecttxt);
        filtertext = findViewById(R.id.filtertxt);
        switchCamera = findViewById(R.id.switchCamera);
        final RadioButton radioMasks = findViewById(R.id.masks);
        final RadioButton radioEffects = findViewById(R.id.effects);
        final RadioButton radioFilters = findViewById(R.id.filters);

        SurfaceView arView = findViewById(R.id.surface);
        arView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deepAR.onClick();
            }
        });
        arView.getHolder().addCallback(this);
        // Surface might already be initialized, so we force the call to onSurfaceChanged
        arView.setVisibility(View.GONE);


        arView.setVisibility(View.VISIBLE);


        ImageView btn_capture = findViewById(R.id.btn_capture);
         time15sec = findViewById(R.id.time15sec);
         time30sec = findViewById(R.id.time30sec);
        btn_capture.setImageDrawable(ContextCompat.getDrawable(cameraactivity.this, R.drawable.ic_record_stop_));
        time15sec.setBackground(ContextCompat.getDrawable(cameraactivity.this, R.drawable.blackbackgroundtimer));
        time15sec.setTextColor(ContextCompat.getColor(cameraactivity.this, R.color.white));


        time15sec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                time15secboolean=true;
                time30secboolean=false;
                time15sec.setBackground(ContextCompat.getDrawable(cameraactivity.this, R.drawable.blackbackgroundtimer));
                time15sec.setTextColor(ContextCompat.getColor(cameraactivity.this, R.color.white));
                time30sec.setBackground(ContextCompat.getDrawable(cameraactivity.this, R.drawable.bg_white_corner_5));
                time30sec.setTextColor(ContextCompat.getColor(cameraactivity.this, R.color.black));
                timerint = 15000;
            }
        });
        time30sec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                time15secboolean=false;
                time30secboolean=true;
                time15sec.setBackground(ContextCompat.getDrawable(cameraactivity.this, R.drawable.bg_white_corner_5));
                time15sec.setTextColor(ContextCompat.getColor(cameraactivity.this, R.color.black));
                time30sec.setBackground(ContextCompat.getDrawable(cameraactivity.this, R.drawable.blackbackgroundtimer));
                time30sec.setTextColor(ContextCompat.getColor(cameraactivity.this, R.color.white));
                timerint = 30000;
            }
        });


        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording == false){
                    if (pausetimer == false){
                        isRecording = true;
                        Animation animation = AnimationUtils.loadAnimation(cameraactivity.this, R.anim.scale);
                        btn_capture.startAnimation(animation);
                        btn_capture.setImageDrawable(ContextCompat.getDrawable(cameraactivity.this, R.drawable.ic_record_start));
                        starttimerrec15();

                    }else {
                        Toast.makeText(cameraactivity.this, "resumed", Toast.LENGTH_SHORT).show();
                        isRecording = true;
                        pausetimer = false;
                        hourglass.resumeTimer();
                        time15sec.setVisibility(View.INVISIBLE);
                        time30sec.setVisibility(View.INVISIBLE);
                        switchCamera.setVisibility(View.INVISIBLE);
                        maskbtn.setVisibility(View.INVISIBLE);
                        filterbtn.setVisibility(View.INVISIBLE);
                        effectbtn.setVisibility(View.INVISIBLE);
                        effecttext.setVisibility(View.INVISIBLE);
                        masktext.setVisibility(View.INVISIBLE);
                        filtertext.setVisibility(View.INVISIBLE);
                        fliptxt.setVisibility(View.INVISIBLE);
                    }

                }else {
                    btn_capture.setImageDrawable(ContextCompat.getDrawable(cameraactivity.this, R.drawable.ic_record_stop_));
                    isRecording = false;
                    pausetimer = true;
                    Toast.makeText(cameraactivity.this, "paused", Toast.LENGTH_SHORT).show();
                    hourglass.pauseTimer();
//                    btn_capture.setVisibility(View.VISIBLE);
                    time15sec.setVisibility(View.VISIBLE);
                    time30sec.setVisibility(View.VISIBLE);
                    switchCamera.setVisibility(View.VISIBLE);
                    maskbtn.setVisibility(View.VISIBLE);
                    filterbtn.setVisibility(View.VISIBLE);
                    effectbtn.setVisibility(View.VISIBLE);
                    effecttext.setVisibility(View.VISIBLE);
                    masktext.setVisibility(View.VISIBLE);
                    filtertext.setVisibility(View.VISIBLE);
                    fliptxt.setVisibility(View.VISIBLE);
                }
            }
        });


        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraDevice = cameraGrabber.getCurrCameraDevice() ==  Camera.CameraInfo.CAMERA_FACING_FRONT ?  Camera.CameraInfo.CAMERA_FACING_BACK :  Camera.CameraInfo.CAMERA_FACING_FRONT;
                cameraGrabber.changeCameraDevice(cameraDevice);
            }
        });

        previousMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoPrevious();
            }
        });

        nextMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoNext();
            }
        });

        radioMasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioEffects.setChecked(false);
                radioFilters.setChecked(false);
                activeFilterType = 0;
            }
        });
        radioEffects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioMasks.setChecked(false);
                radioFilters.setChecked(false);
                activeFilterType = 1;
            }
        });
        radioFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radioEffects.setChecked(false);
                radioMasks.setChecked(false);
                activeFilterType = 2;
            }
        });
    }
    /*
            get interface orientation from
            https://stackoverflow.com/questions/10380989/how-do-i-get-the-current-orientation-activityinfo-screen-orientation-of-an-a/10383164
         */

    public void starttimerrec15(){

        // Record the left half of the screen
        Rect subframe = new Rect();
        subframe.left = 0;
        subframe.right = deepAR.getRenderWidth();
        subframe.top = 0;
        subframe.bottom = deepAR.getRenderHeight();
        filepath = Environment.getExternalStorageDirectory().toString() + File.separator + "video.mp4";
        deepAR.startVideoRecording(filepath);

//        btn_capture.setVisibility(View.INVISIBLE);
        time15sec.setVisibility(View.INVISIBLE);
        time30sec.setVisibility(View.INVISIBLE);
        switchCamera.setVisibility(View.INVISIBLE);
        maskbtn.setVisibility(View.INVISIBLE);
        filterbtn.setVisibility(View.INVISIBLE);
        effectbtn.setVisibility(View.INVISIBLE);
        effecttext.setVisibility(View.INVISIBLE);
        masktext.setVisibility(View.INVISIBLE);
        filtertext.setVisibility(View.INVISIBLE);
        fliptxt.setVisibility(View.INVISIBLE);

        hourglass = new Hourglass(timerint, 1000) {
            @Override
            public void onTimerTick(long timeRemaining) {
                // Update UI
//                Toast.makeText(cameraactivity.this, String.valueOf(timeRemaining), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTimerFinish() {
                // Timer finished
                isRecording = false;
                pausetimer = false;
//                Toast.makeText(cameraactivity.this, "Timer finished and recording" + filepath, Toast.LENGTH_SHORT).show();
                deepAR.stopVideoRecording();
//                btn_capture.setVisibility(View.VISIBLE);
                time15sec.setVisibility(View.VISIBLE);
                time30sec.setVisibility(View.VISIBLE);
                switchCamera.setVisibility(View.VISIBLE);
                maskbtn.setVisibility(View.VISIBLE);
                filterbtn.setVisibility(View.VISIBLE);
                effectbtn.setVisibility(View.VISIBLE);

            }
        };
        hourglass.startTimer();


    }

    private int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }


    private void initializeDeepAR() {
        deepAR = new DeepAR(this);
        deepAR.setLicenseKey("e073cef93336e7327264f5b5f238e75879e88536bdb70d2e19c54d63a926f3f9da1061acbc71cfeb\n");
        deepAR.initialize(this, this);

        cameraGrabber = new CameraGrabber(cameraDevice);
        screenOrientation = getScreenOrientation();

        switch (screenOrientation) {
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                cameraGrabber.setScreenOrientation(90);
                break;
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                cameraGrabber.setScreenOrientation(270);
                break;
            default:
                cameraGrabber.setScreenOrientation(0);
                break;
        }

        cameraGrabber.setResolutionPreset(CameraResolutionPreset.P640x480);

        final Activity context = this;
        cameraGrabber.initCamera(new CameraGrabberListener() {
            @Override
            public void onCameraInitialized() {
                cameraGrabber.setFrameReceiver(deepAR);
                cameraGrabber.startPreview();
            }

            @Override
            public void onCameraError(String errorMsg) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Camera error");
                builder.setMessage(errorMsg);
                builder.setCancelable(true);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    private String getFilterPath(String filterName) {
        if (filterName.equals("none")) {
            return null;
        }
        return "file:///android_asset/" + filterName;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent  = new Intent(cameraactivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoNext() {
        if (activeFilterType == 0) {
            currentMask = (currentMask + 1) % masks.size();
            deepAR.switchEffect("mask", getFilterPath(masks.get(currentMask)));
        } else if (activeFilterType == 1) {
            currentEffect = (currentEffect + 1) % effects.size();
            deepAR.switchEffect("effect", getFilterPath(effects.get(currentEffect)));
        } else if (activeFilterType == 2) {
            currentFilter = (currentFilter + 1) % filters.size();
            deepAR.switchEffect("filter", getFilterPath(filters.get(currentFilter)));
        }
    }

    private void gotoPrevious() {
        if (activeFilterType == 0) {
            currentMask = (currentMask - 1) % masks.size();
            deepAR.switchEffect("mask", getFilterPath(masks.get(currentMask)));
        } else if (activeFilterType == 1) {
            currentEffect = (currentEffect - 1) % effects.size();
            deepAR.switchEffect("effect", getFilterPath(effects.get(currentEffect)));
        } else if (activeFilterType == 2) {
            currentFilter = (currentFilter - 1) % filters.size();
            deepAR.switchEffect("filter", getFilterPath(filters.get(currentFilter)));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cameraGrabber == null) {
            return;
        }
        cameraGrabber.setFrameReceiver(null);
        cameraGrabber.stopPreview();
        cameraGrabber.releaseCamera();
        cameraGrabber = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deepAR == null) {
            return;
        }
        deepAR.setAREventListener(null);
        deepAR.release();
        deepAR = null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        deepAR.setRenderSurface(holder.getSurface(), width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (deepAR != null) {
            deepAR.setRenderSurface(null, 0, 0);
        }
    }

    @Override
    public void screenshotTaken(Bitmap bitmap) {
        CharSequence now = DateFormat.format("yyyy_MM_dd_hh_mm_ss", new Date());
        try {
            File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/DeepAR_" + now + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            MediaScannerConnection.scanFile(cameraactivity.this, new String[]{imageFile.toString()}, null, null);
            Toast.makeText(cameraactivity.this, "Screenshot saved", Toast.LENGTH_SHORT).show();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public void videoRecordingStarted() {

    }

    @Override
    public void videoRecordingFinished() {
        bmThumbnail = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Images.Thumbnails.MINI_KIND);
        Intent intent = new Intent(cameraactivity.this, TrimVideoActivity.class);
        intent.putExtra("videoPath",filepath);
//        intent.putExtra("thumb",bmThumbnail);
        startActivityForResult(intent, 100);
        finish();
//        Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void videoRecordingFailed() {

    }

    @Override
    public void videoRecordingPrepared() {

    }

    @Override
    public void shutdownFinished() {

    }

    @Override
    public void initialized() {

    }

    @Override
    public void faceVisibilityChanged(boolean b) {

    }

    @Override
    public void imageVisibilityChanged(String s, boolean b) {

    }

    @Override
    public void error(String s) {

    }

    @Override
    public void effectSwitched(String s) {

    }
}

package com.develappme.academy.whatsonmymind;


import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private CameraManager cameraManager;
    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener;
    private Size mPreviewSize;
    private String mCameraId;
    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallback;
    private CaptureRequest mPreviewCaptureRequest;
    private CaptureRequest.Builder mPreviewCaptureRequestBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private int mState;
    private static final int ACTIVITY_START_CAMERA_APP = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Take Photo Button!
        Button takePhotobtn = (Button) findViewById(R.id.takePhotoBtn);
        takePhotobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();

            }
        });

        Button fromPictures = (Button) findViewById(R.id.choosePhotoBtn);
        fromPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, Polaroid.class));
            }
        });


        mTextureView = (TextureView) findViewById(R.id.textureView);

        mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                setupCamera(width, height);
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        };

        mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice camera) {
                mCameraDevice = camera;
                createCameraPreviewSession();

            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                camera.close();
                mCameraDevice = null;

            }

            @Override
            public void onError(CameraDevice camera, int error) {
                camera.close();
                mCameraDevice = null;
            }
        };

        mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {

            private void process(CaptureResult result) {
                switch(mState)
                {
                    case STATE_PREVIEW:
                        // do nothing
                        break;
                    case STATE_WAIT_LOCK:
                        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                        if(afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED) {
                            unlockFocus();
                            Toast.makeText(getApplicationContext(), "Focus Locked Successful", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }

            @Override
            public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                super.onCaptureStarted(session, request, timestamp, frameNumber);
            }

            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                process(result);

            }

            @Override
            public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                super.onCaptureFailed(session, request, failure);
                Toast.makeText(getApplicationContext(), "Focus Locked unsuccessful", Toast.LENGTH_SHORT).show();
            }
        };

    }

    public void takePhoto(){
        Intent callCameraApplicationIntent = new Intent();
        callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(callCameraApplicationIntent, ACTIVITY_START_CAMERA_APP);
        lockFocus();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == ACTIVITY_START_CAMERA_APP) {
                //Uri selectedImageUri = data.getData();
                startActivity(new Intent(MainActivity.this, Polaroid.class));
            }
        }
    }

    public void setupCamera(int width, int height)
    {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try
        {
            for(String cameraId : cameraManager.getCameraIdList())
            {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if(cameraCharacteristics.get(cameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT)
                {
                    continue;
                }

                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mPreviewSize = getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                mCameraId = cameraId;
                return;
            }

        } catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    private Size getPreferredPreviewSize(Size[] mapSizes, int width, int height)
    {
        List<Size> collectorSizes = new ArrayList<>();
        for(Size option : mapSizes)
        {
            if(width > height)
            {
                if(option.getWidth() > width && option.getHeight() > height)
                {
                    collectorSizes.add(option);
                }
            } else{
                if(option.getWidth() > height && option.getHeight() > width)
                {
                    collectorSizes.add(option);
                }
            }
        }

        if(!collectorSizes.isEmpty())
        {
            Collections.min(collectorSizes, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() + rhs.getHeight());
                }
            });
        }

        return mapSizes[0];
    }

    private void openCamera()
    {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try{
            cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);

        } catch(CameraAccessException e){
            e.printStackTrace();
        }

    }

    private void closeCamera(){
        if(mCameraCaptureSession != null)
        {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }

        if(mCameraDevice != null)
        {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void createCameraPreviewSession()
    {
        try
        {
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            mPreviewCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            if(mCameraDevice == null) {
                                return;
                            }

                            try
                            {
                                mPreviewCaptureRequest = mPreviewCaptureRequestBuilder.build();
                                mCameraCaptureSession = session;
                                mCameraCaptureSession.setRepeatingRequest(mPreviewCaptureRequest,
                                        mSessionCaptureCallback, mBackgroundHandler);
                            } catch(CameraAccessException e){
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Toast.makeText(getApplicationContext(), "Create Camera Session Failed!",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }, null);

        } catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void openBackgroundThread(){
        mBackgroundThread = new HandlerThread("Camera2 background thread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void closeBackgroundThread(){
        mBackgroundThread.quitSafely();
        try
        {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }catch(InterruptedException e){
            e.printStackTrace();

        }
    }

    private void lockFocus(){
        try {
            mState = STATE_WAIT_LOCK;
            mPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CaptureRequest.CONTROL_AF_TRIGGER_START);
            mCameraCaptureSession.capture(mPreviewCaptureRequestBuilder.build(), mSessionCaptureCallback,
                    mBackgroundHandler);
        } catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void unlockFocus(){
        try {
            mState = STATE_PREVIEW;
            mPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
            mCameraCaptureSession.capture(mPreviewCaptureRequestBuilder.build(), mSessionCaptureCallback,
                    mBackgroundHandler);
        } catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        openBackgroundThread();
        if(mTextureView.isAvailable())
        {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            openCamera();
        } else{
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

    }

    @Override
    protected void onPause() {
        closeCamera();
        closeBackgroundThread();
        super.onPause();

    }

}

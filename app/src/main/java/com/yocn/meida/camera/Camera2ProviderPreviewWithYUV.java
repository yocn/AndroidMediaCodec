package com.yocn.meida.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.yocn.meida.base.Constant;
import com.yocn.meida.mediacodec.MediaCodecUtil;
import com.yocn.meida.presenter.Mp4Writer;
import com.yocn.meida.presenter.yuv.YUVFileWriter;
import com.yocn.meida.util.BitmapUtil;
import com.yocn.meida.util.CameraUtil;
import com.yocn.meida.util.LogUtil;
import com.yocn.meida.util.PermissionUtil;

import java.util.Arrays;

/**
 * @Author yocn
 * @Date 2019/8/2 10:58 AM
 * @ClassName Camera2ProviderPreviewWithYUV
 * Camera2 两路预览：
 * 1、使用TextureView预览，直接输出。
 * 2、使用ImageReader获取数据，输出格式为ImageFormat.YUV_420_888，java端转化为NV21，再使用YuvImage生成Bitmap实现预览。
 */
public class Camera2ProviderPreviewWithYUV extends BaseCameraProvider {
    private Activity mContext;
    private String mCameraId;
    private Handler mCameraHandler;
    private CameraDevice mCameraDevice;
    private TextureView mTextureView;
    private CaptureRequest.Builder mPreviewBuilder;
    private ImageReader mImageReader;
    private OnGetBitmapInterface mOnGetBitmapInterface;
    private Range<Integer> fpsRanges;
    private Mp4Writer mp4Writer;
    private String outMp4Path;

    public interface OnGetBitmapInterface {
        public void getABitmap(Bitmap bitmap);
    }

    public void setmOnGetBitmapInterface(OnGetBitmapInterface mOnGetBitmapInterface) {
        this.mOnGetBitmapInterface = mOnGetBitmapInterface;
    }

    public void startRecord() {
        if (mp4Writer != null) {
            mp4Writer.startWrite();
        }
    }

    public void stopRecord() {
        if (mp4Writer != null) {
            mp4Writer.endWrite();
        }
    }

    public Camera2ProviderPreviewWithYUV(Activity mContext, String outMp4Path) {
        this.mContext = mContext;
        this.outMp4Path = outMp4Path;
        HandlerThread handlerThread = new HandlerThread("camera");
        handlerThread.start();
        mCameraHandler = new Handler(handlerThread.getLooper());
    }

    public void initTexture(TextureView textureView) {
        mTextureView = textureView;
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                LogUtil.d("w/h->" + width + "|" + height);
                openCamera(width, height);
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
        });
    }

    private void openCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            for (int i = 0; i < cameraIds.length; i++) {
                //描述相机设备的属性类
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraIds[i]);
                Range<Integer>[] allFpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                for (Range<Integer> c : allFpsRanges) {
                    LogUtil.d(MediaCodecUtil.TAG, "c::" + c.toString());
                }
                fpsRanges = allFpsRanges[allFpsRanges.length - 1];
                //获取是前置还是后置摄像头
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                //使用后置摄像头
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    if (map != null) {
                        Size[] sizeMap = map.getOutputSizes(SurfaceTexture.class);
                        LogUtil.d("preview->" + previewSize.toString());
                        mCameraId = cameraIds[i];
                        for (Size s : sizeMap) {
                            LogUtil.d("s->" + s.toString());
                        }
                    }
                    mImageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(),
                            ImageFormat.YUV_420_888, 2);
                    mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mCameraHandler);
                    String[] params = new String[]{Manifest.permission.CAMERA};
                    if (!PermissionUtil.checkPermission(mContext, params)) {
                        PermissionUtil.requestPermission(mContext, "", 0, params);
                    }
                    cameraManager.openCamera(mCameraId, mStateCallback, mCameraHandler);
                }
            }
        } catch (CameraAccessException r) {

        }
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            try {
                mCameraDevice = camera;
                SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
                surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
                Surface previewSurface = new Surface(surfaceTexture);
                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRanges);
                mPreviewBuilder.addTarget(previewSurface);
                mPreviewBuilder.addTarget(mImageReader.getSurface());
                mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), mStateCallBack, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            LogUtil.d("mStateCallback----onOpened---");
            mp4Writer = new Mp4Writer(mContext, previewSize.getWidth(), previewSize.getHeight(), fpsRanges.getUpper(), outMp4Path);
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            LogUtil.d("mStateCallback----onDisconnected---");
            camera.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            LogUtil.d("mStateCallback----onError---" + error);
            camera.close();
        }
    };

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }
            int width = image.getWidth(), height = image.getHeight();
            byte[] i420bytes = CameraUtil.getDataFromImage(image, CameraUtil.COLOR_FormatI420);
            mp4Writer.write(i420bytes);

            byte[] i420RorateBytes = BitmapUtil.rotateYUV420Degree90(i420bytes, width, height);
            byte[] nv21bytes = BitmapUtil.I420Tonv21(i420RorateBytes, height, width);
            //TODO check YUV数据是否正常
//                BitmapUtil.dumpFile("mnt/sdcard/1.yuv", i420bytes);

            Bitmap bitmap = BitmapUtil.getBitmapImageFromYUV(nv21bytes, height, width);
            LogUtil.d("image->" + width + "|" + height);
            if (mOnGetBitmapInterface != null) {
                mOnGetBitmapInterface.getABitmap(bitmap);
            }
            image.close();
        }
    };

    private CameraCaptureSession.StateCallback mStateCallBack = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
//                session.capture(request, mSessionCaptureCallback, mCameraHandler);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                CaptureRequest request = mPreviewBuilder.build();
                // Finally, we start displaying the camera preview.
                session.setRepeatingRequest(request, null, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };

    public void closeCamera() {
        mCameraDevice.close();
        mImageReader.close();
        mp4Writer.endWrite();
    }

}

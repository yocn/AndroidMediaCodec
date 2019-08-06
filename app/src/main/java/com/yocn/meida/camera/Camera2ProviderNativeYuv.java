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
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.yocn.meida.util.BitmapUtil;
import com.yocn.meida.util.CameraUtil;
import com.yocn.meida.util.LogUtil;
import com.yocn.meida.util.PermissionUtil;

import java.util.Arrays;

import static com.yocn.meida.util.CameraUtil.COLOR_FormatI420;

/**
 * @Author yocn
 * @Date 2019/8/2 10:58 AM
 * @ClassName Camera1
 */
public class Camera2ProviderNativeYuv {
    private Activity mContext;
    private String mCameraId;
    private Handler mCameraHandler;
    private CameraDevice mCameraDevice;
    private TextureView mTextureView;
    private CaptureRequest.Builder mPreviewBuilder;
    private Size previewSize;
    private ImageReader mImageReader;
    private OnGetBitmapInterface mOnGetBitmapInterface;

    public interface OnGetBitmapInterface {
        public void getABitmap(Bitmap bitmap);
    }

    public void setmOnGetBitmapInterface(OnGetBitmapInterface mOnGetBitmapInterface) {
        this.mOnGetBitmapInterface = mOnGetBitmapInterface;
    }

    public Camera2ProviderNativeYuv(Activity mContext) {
        this.mContext = mContext;
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
            for (String cameraId : cameraManager.getCameraIdList()) {
                //描述相机设备的属性类
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                //获取是前置还是后置摄像头
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                //使用后置摄像头
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    if (map != null) {
                        Size[] sizeMap = map.getOutputSizes(SurfaceTexture.class);

                        previewSize = CameraUtil.getOptimalSize(sizeMap, width, height);
                        LogUtil.d("preview->" + previewSize.toString());
                        mCameraId = cameraId;
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

    int index = 0;
    boolean check = true;
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {

            Image image = reader.acquireNextImage();
            if (index++ % 10 == 0) {
                check = false;
                byte[] i420bytes = CameraUtil.getDataFromImage(image, COLOR_FormatI420);
                byte[] i420RorateBytes = BitmapUtil.rotateYUV420Degree90(i420bytes, image.getWidth(), image.getHeight());
                byte[] nv21bytes = BitmapUtil.I420Tonv21(i420RorateBytes, image.getHeight(), image.getWidth());
                //TODO check YUV数据是否正常
//                BitmapUtil.dumpFile("mnt/sdcard/1.yuv", i420bytes);

                Bitmap bitmap = BitmapUtil.getBitmapImageFromYUV(nv21bytes, image.getHeight(), image.getWidth());

                LogUtil.d("image->" + image.getWidth() + "|" + image.getHeight() + " format->" + image.getFormat());
                if (mOnGetBitmapInterface != null) {
                    mOnGetBitmapInterface.getABitmap(bitmap);
                }

            }
            image.close();
        }
    };

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            try {
                mCameraDevice = camera;
                SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
                surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
                Surface previewSurface = new Surface(surfaceTexture);
                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewBuilder.addTarget(previewSurface);
                mPreviewBuilder.addTarget(mImageReader.getSurface());
                mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), mStateCallBack, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            LogUtil.d("mStateCallback----onOpened---");
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
    }

}

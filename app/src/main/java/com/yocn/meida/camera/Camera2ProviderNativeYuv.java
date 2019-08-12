package com.yocn.meida.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
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

import com.yocn.libyuv.YUVTransUtil;
import com.yocn.meida.util.BitmapUtil;
import com.yocn.meida.util.CameraUtil;
import com.yocn.meida.util.LogUtil;
import com.yocn.meida.util.PermissionUtil;

import java.nio.ByteBuffer;
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

                    Integer maxFormats = characteristics.get(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_PROC);
                    LogUtil.d("maxFormats->" + maxFormats);

                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    if (map != null) {

                        int[] outputFormats = map.getOutputFormats();
                        for (int format : outputFormats) {
                            //小米8后置摄像头：
                            //{32,256,34,35,36,37}
                            //32|0x20|RAW_SENSOR
                            //256|0x100|JPEG
                            //34|0x22|PRIVATE
                            //35|0x23|YUV_420_888
                            //36|0x24|RAW_PRIVATE
                            //37|0x25|RAW10

                            //华为P20 后置摄像头 三个，原来华为P20有三个后置Camera...：
                            //32|0x20|RAW_SENSOR
                            //256|0x100|JPEG
                            //34|0x22|PRIVATE
                            //35|0x23|YUV_420_888
                            //其中第0号Camera有
                            //1144402265|0x44363159|DEPTH16
                            LogUtil.d("支持的格式format->" + format + " " + " cameraId->" + cameraId);
                        }
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
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }
//            if (index++ % 100 == 10) {

            try {
                Bitmap bitmap = getBitmapFromI420(image);
                LogUtil.d("image->" + image.getWidth() + "|" + image.getHeight() + " format->" + image.getFormat());
                if (mOnGetBitmapInterface != null) {
                    mOnGetBitmapInterface.getABitmap(bitmap);
                }
            } catch (Exception e) {
                LogUtil.d("e----->" + index);
                e.printStackTrace();
            }

//            }
            image.close();
        }
    };

    private Bitmap getBitmapFromI420(Image image) {
        int w = image.getWidth(), h = image.getHeight();
        int i420Size = w * h * 3 / 2;
        Image.Plane[] planes = image.getPlanes();
        //remaining0 = rowStride*(h-1)+w => 27632= 192*143+176
        int remaining0 = planes[0].getBuffer().remaining();
        //remaining2 = rowStride*(h/2-1)+w-1 =>  13807=  192*71+176-1
        int remaining2 = planes[2].getBuffer().remaining();
        //获取pixelStride，可能跟width相等，可能不相等
        byte[] yRawSrcBytes = new byte[remaining0];
        byte[] vRawSrcBytes = new byte[remaining2];
        planes[0].getBuffer().get(yRawSrcBytes);
        planes[2].getBuffer().get(vRawSrcBytes);

        byte[] argbBytes = new byte[w * h * 4];

        YUVTransUtil.getInstance().NV21ToArgb(yRawSrcBytes, planes[0].getRowStride(), vRawSrcBytes, planes[2].getRowStride(),
                argbBytes, w*4, w, h);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(argbBytes));
        return bitmap;
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

package com.yocn.meida.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
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

/**
 * @Author yocn
 * @Date 2019/8/2 10:58 AM
 * @ClassName Camera2ProviderPreviewWithYUV
 * Camera2 两路预览：
 * 1、使用TextureView预览，直接输出。
 * 2、使用ImageReader获取数据，输出格式为ImageFormat.YUV_420_888，java端转化为NV21，再使用YuvImage生成Bitmap实现预览。
 */
public class Camera2ProviderPreviewWithYUV2 extends BaseCameraProvider {
    private Activity mContext;
    private String mCameraId;
    private Handler mCameraHandler;
    private CameraDevice mCameraDevice;
    private TextureView mTextureView;
    private CaptureRequest.Builder mPreviewBuilder;
    private ImageReader mImageReader;
    private OnGetBitmapInterface mOnGetBitmapInterface;

    public interface OnGetBitmapInterface {
        public void getABitmap(Bitmap bitmap);
    }

    public void setmOnGetBitmapInterface(OnGetBitmapInterface mOnGetBitmapInterface) {
        this.mOnGetBitmapInterface = mOnGetBitmapInterface;
    }

    public Camera2ProviderPreviewWithYUV2(Activity mContext) {
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
                        StringBuilder sizes = new StringBuilder();
                        for (Size size : sizeMap) {
                            sizes.append(size.getWidth()).append(" | ").append(size.getHeight()).append("     ");
                        }
                        LogUtil.d("size->" + sizes.toString());
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
//            if ((index++ % 100) == 10) {
            Bitmap bitmap = getBitmapFromImage(image);
            try {
                if (mOnGetBitmapInterface != null) {
                    mOnGetBitmapInterface.getABitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.d(e.toString());
            }
//            }
            image.close();
        }
    };

    public Bitmap getBitmapFromImage(Image image) {
        long time1 = System.currentTimeMillis();
        int w = image.getWidth(), h = image.getHeight();
        int i420Size = w * h * 3 / 2;
        int picel1 = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        int picel2 = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888);

        Image.Plane[] planes = image.getPlanes();
        //remaining0 = rowStride*(h-1)+w => 27632= 192*143+176
        int remaining0 = planes[0].getBuffer().remaining();
        int remaining1 = planes[1].getBuffer().remaining();
        //remaining2 = rowStride*(h/2-1)+w-1 =>  13807=  192*71+176-1
        int remaining2 = planes[2].getBuffer().remaining();
        //获取pixelStride，可能跟width相等，可能不相等
        int pixelStride = planes[2].getPixelStride();
        int rowOffest = planes[2].getRowStride();
        byte[] nv21 = new byte[i420Size];
        byte[] yRawSrcBytes = new byte[remaining0];
        byte[] uRawSrcBytes = new byte[remaining1];
        byte[] vRawSrcBytes = new byte[remaining2];
        planes[0].getBuffer().get(yRawSrcBytes);
        planes[1].getBuffer().get(uRawSrcBytes);
        planes[2].getBuffer().get(vRawSrcBytes);
        if (pixelStride == w) {
            //两者相等，说明每个YUV块紧密相连，可以直接拷贝
            System.arraycopy(yRawSrcBytes, 0, nv21, 0, rowOffest * h);
            System.arraycopy(vRawSrcBytes, 0, nv21, rowOffest * h, rowOffest * h / 2 - 1);
        } else {
            byte[] ySrcBytes = new byte[w * h];
            byte[] uSrcBytes = new byte[w * h / 2 - 1];
            byte[] vSrcBytes = new byte[w * h / 2 - 1];
            for (int row = 0; row < h; row++) {
                //源数组每隔 rowOffest 个bytes 拷贝 w 个bytes到目标数组
                System.arraycopy(yRawSrcBytes, rowOffest * row, ySrcBytes, w * row, w);

                //y执行两次，uv执行一次
                if (row % 2 == 0) {
                    //最后一行需要减一
                    if (row == h - 2) {
                        System.arraycopy(vRawSrcBytes, rowOffest * row / 2, vSrcBytes, w * row / 2, w - 1);
                    } else {
                        System.arraycopy(vRawSrcBytes, rowOffest * row / 2, vSrcBytes, w * row / 2, w);
                    }
                }
            }
            System.arraycopy(ySrcBytes, 0, nv21, 0, w * h);
            System.arraycopy(vSrcBytes, 0, nv21, w * h, w * h / 2 - 1);
        }
        long time2 = System.currentTimeMillis();
        Bitmap bm = BitmapUtil.getBitmapImageFromYUV(nv21, w, h);
        long time3 = System.currentTimeMillis();
        Matrix m = new Matrix();
        m.setRotate(90, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap result = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);

        LogUtil.d("1-2:" + (time2 - time1) + " 2-3:" + (time3 - time2));
        return result;
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

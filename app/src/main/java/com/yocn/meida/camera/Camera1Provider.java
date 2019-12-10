package com.yocn.meida.camera;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.Surface;
import android.view.TextureView;

import com.yocn.meida.util.LogUtil;

import java.io.IOException;
import java.util.List;

/**
 * @Author yocn
 * @Date 2019/8/2 10:58 AM
 * @ClassName Camera1Provider
 */
public class Camera1Provider {
    TextureView mTextureView;
    int mCameraId = 0;
    private Camera.CameraInfo mCameraInfo;
    Camera mCamera;

    Activity mContext;

    public Camera1Provider(Activity context) {
        mContext = context;
    }

    public void setTextureView(TextureView textureView) {
        mTextureView = textureView;
        int num = Camera.getNumberOfCameras();
        for (int i = 0; i < num; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCameraId = i;
                mCameraInfo = info;
            }
        }
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
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
        });
    }

    private void openCamera() {
        mCamera = Camera.open(mCameraId);
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        int width = sizes.get(0).width;
        int height = sizes.get(0).height;
        LogUtil.d("w/h:" + width + "/" + height);
        setPreviewSize(width, height);
        setPreviewSurface(mTextureView.getSurfaceTexture());
        startPreview();
    }

    private void setPreviewSurface(SurfaceTexture previewSurface) {
        if (mCamera != null && previewSurface != null) {
            try {
                mCamera.setPreviewTexture(previewSurface);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setPreviewSize(int shortSide, int longSide) {
        if (mCamera != null && shortSide != 0 && longSide != 0) {
            float aspectRatio = (float) longSide / shortSide;
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            for (Camera.Size previewSize : supportedPreviewSizes) {
                if ((float) previewSize.width / previewSize.height == aspectRatio && previewSize.height <= shortSide && previewSize.width <= longSide) {
                    parameters.setPreviewSize(previewSize.width, previewSize.height);
                    mCamera.setParameters(parameters);
                    break;
                }
            }
        }
    }

    private int getCameraDisplayOrientation(Camera.CameraInfo cameraInfo) {
        int rotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
        }
        int result;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
            mCamera.setDisplayOrientation(getCameraDisplayOrientation(mCameraInfo));
            LogUtil.d("startPreview() called");
        }
    }

    /**
     * 停止预览。
     */
    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            LogUtil.d("stopPreview() called");
        }
    }
}

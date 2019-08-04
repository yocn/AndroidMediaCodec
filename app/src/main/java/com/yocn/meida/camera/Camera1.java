package com.yocn.meida.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.TextureView;

import com.yocn.meida.util.LogUtil;

import java.io.IOException;
import java.util.List;

/**
 * @Author yocn
 * @Date 2019/8/2 10:58 AM
 * @ClassName Camera1
 */
public class Camera1 {
    TextureView mTextureView;
    int mCameraId = 0;
    private Camera.CameraInfo mCameraInfo;
    Camera mCamera;

    public Camera1() {
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
        openCamera();
    }

    public void openCamera() {
        mCamera = Camera.open(mCameraId);
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        setPreviewSize(sizes.get(0).width, sizes.get(0).height);
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

    private void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
            LogUtil.d("startPreview() called");
        }
    }

    /**
     * 停止预览。
     */
    private void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            LogUtil.d("stopPreview() called");
        }
    }
}

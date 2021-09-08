package com.yocn.meida.camera;

import android.Manifest;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.util.Size;
import android.view.Surface;

import com.yocn.meida.util.PermissionUtil;
import com.yocn.meida.view.widget.AspectTextureView;

import java.util.ArrayList;
import java.util.List;

public class Camera2AllSizeProvider extends BaseCommonCameraProvider {
    private CaptureRequest.Builder mPreviewBuilder;
    private List<Size> outputSizes;

    public Camera2AllSizeProvider(Activity mContext) {
        super(mContext);
    }

    private void initCamera() {
        mCameraId = getCameraId(false);//默认使用后置相机
        //获取指定相机的输出尺寸列表，降序排序
        outputSizes = getCameraOutputSizes(mCameraId, SurfaceTexture.class);
        //初始化预览尺寸
        previewSize = outputSizes.get(0);
        if (getCameraInfoListener != null) {
            getCameraInfoListener.getInfos(outputSizes);
        }
    }

    int index = 0;

    public void initTexture(AspectTextureView... textureViews) {
        initCamera();
        mTextureViews = textureViews;
        int size = textureViews.length;
        for (AspectTextureView aspectTextureView : textureViews) {
            aspectTextureView.setSurfaceTextureListener(new SimplifyInterface.SimplifySurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    if (++index == size) {
                        // 所有的textureView都准备好
                        openCamera();
                    }
                }
            });
        }
    }

    private void openCamera() {
        try {
            String[] params = new String[]{Manifest.permission.CAMERA};
            if (!PermissionUtil.checkPermission(mContext, params)) {
                PermissionUtil.requestPermission(mContext, "", 0, params);
            }
            cameraManager.openCamera(mCameraId, mStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new SimplifyInterface.SimplifyCameraDeviceStateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            startPreviewSession(previewSize);
        }
    };

    public void startPreviewSession(Size size) {
        try {
            releaseCameraSession(session);
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            List<Surface> outputs = new ArrayList<>();
            for (AspectTextureView aspectTextureView : mTextureViews) {
                aspectTextureView.setSize(size.getHeight(), size.getWidth());
                SurfaceTexture surfaceTexture = aspectTextureView.getSurfaceTexture();
                surfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
                Surface previewSurface = new Surface(surfaceTexture);
                mPreviewBuilder.addTarget(previewSurface);
                outputs.add(previewSurface);
            }
            mCameraDevice.createCaptureSession(outputs, mStateCallBack, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraCaptureSession.StateCallback mStateCallBack = new SimplifyInterface.SimplifyStateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                Camera2AllSizeProvider.this.session = session;
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                CaptureRequest request = mPreviewBuilder.build();
                session.setRepeatingRequest(request, null, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    };

    public void closeCamera() {
        releaseCamera();
    }

}

package com.yocn.meida.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;

import com.yocn.meida.view.widget.AspectTextureView;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BaseCommonCameraProvider extends BaseCameraProvider {
    protected Activity mContext;
    protected String mCameraId;
    protected Handler mCameraHandler;
    protected CameraDevice mCameraDevice;
    protected CameraCaptureSession session;
    protected AspectTextureView[] mTextureViews;
    protected HandlerThread handlerThread;
    protected CameraManager cameraManager;
    protected GetCameraInfoListener getCameraInfoListener;

    public interface GetCameraInfoListener {
        void getInfos(List<Size> outputSizes);
    }

    public void setGetCameraInfoListener(GetCameraInfoListener getCameraInfoListener) {
        this.getCameraInfoListener = getCameraInfoListener;
    }

    protected BaseCommonCameraProvider(Activity mContext) {
        this.mContext = mContext;
        handlerThread = new HandlerThread("camera");
        handlerThread.start();
        mCameraHandler = new Handler(handlerThread.getLooper());
        cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
    }

    protected String getCameraId(boolean useFront) {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                int cameraFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (useFront) {
                    if (cameraFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                        return cameraId;
                    }
                } else {
                    if (cameraFacing == CameraCharacteristics.LENS_FACING_BACK) {
                        return cameraId;
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected List<Size> getCameraOutputSizes(String cameraId, Class clz) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            List<Size> sizes = Arrays.asList(configs.getOutputSizes(clz));
            Collections.sort(sizes, new Comparator<Size>() {
                @Override
                public int compare(Size o1, Size o2) {
                    return o1.getWidth() * o1.getHeight() - o2.getWidth() * o2.getHeight();
                }
            });
            Collections.reverse(sizes);
            return sizes;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected List<Size> getCameraOutputSizes(String cameraId, int format) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap configs = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            return Arrays.asList(configs.getOutputSizes(format));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void releaseCameraDevice(CameraDevice cameraDevice) {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    protected void releaseCameraSession(CameraCaptureSession session) {
        if (session != null) {
            session.close();
            session = null;
        }
    }

    protected void releaseCamera() {
        releaseCameraDevice(mCameraDevice);
        releaseCameraSession(session);
    }
}
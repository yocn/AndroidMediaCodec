package com.yocn.meida.view.activity;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

import com.yocn.media.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName SimpleOpenGLESActivity
 */
public class SimpleOpenGLESActivity extends BaseCameraActivity {
    public static String DESC = "Simple OpenGL ES";
    private GLSurfaceView.Renderer mRender;
    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = getLayoutInflater().inflate(R.layout.activity_simple_opengles, null);
        setContentView(rootView);
        initView(rootView);
        initData();
    }

    @Override
    protected void initView(View root) {
        glSurfaceView = root.findViewById(R.id.flsv_1);
    }

    @Override
    protected void initData() {
        initRenderer(glSurfaceView);
    }

    private void initRenderer(GLSurfaceView glSurfaceView) {
        mRender = new SimpleRender();
        glSurfaceView.setRenderer(mRender);
    }

    public static class SimpleRender implements GLSurfaceView.Renderer {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);//设置清屏颜色
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }
    }
}

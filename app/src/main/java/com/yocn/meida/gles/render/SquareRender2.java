package com.yocn.meida.gles.render;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SquareRender2 implements GLSurfaceView.Renderer {
    private final Square02 square02;

    public SquareRender2(Bitmap bitmap) {
        square02 = new Square02(bitmap);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(1.0f, 0.3f, 0.2f, 1.0f);
    }

    private float[] mvpMatrix = new float[16];

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        final float aspectRadio = (float) height / width;
        //orthoM(float[] m, int mOffset, float left, float right, float bottom, float top, float near, float far)
        Matrix.orthoM(mvpMatrix, 0, -1f, 1f, -aspectRadio, aspectRadio, -1f, 1f);
        square02.setMvpMatrix(mvpMatrix);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        square02.draw();
    }
}
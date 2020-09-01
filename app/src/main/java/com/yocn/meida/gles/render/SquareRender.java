package com.yocn.meida.gles.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.yocn.meida.gles.GlUtil;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SquareRender implements GLSurfaceView.Renderer {
    // 顶点着色器的脚本
    String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +         //接收传入的转换矩阵
                    " attribute vec4 vPosition;" +     // 应用程序传入顶点着色器的顶点位置
                    " void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +  //矩阵变换计算之后的位置
                    " }";

    // 片元着色器的脚本
    String fragmentShaderCode =
            " precision mediump float;" +  // 设置工作精度
                    " uniform vec4 vColor;" +       // 接收从顶点着色器过来的顶点颜色数据
                    " void main() {" +
                    "     gl_FragColor = vColor;" +  // 给此片元的填充色
                    " }";

    private FloatBuffer vertexBuffer;  //顶点坐标数据要转化成FloatBuffer格式

    // 数组中每3个值作为一个坐标点
    static final int COORDS_PER_VERTEX = 3;
    // 正方形的坐标数组
    static float squareCoords[] = {
            -1f, 1f, 0.0f, // top left
            -1f, -1f, 0.0f, // bottom left
            1f, 1f, 0.0f,  // top right
            1f, -1f, 0.0f  // bottom right
    };

    //顶点个数，计算得出
    private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    //一个顶点有3个float，一个float是4个字节，所以一个顶点要12字节
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    float r, g, b = 0.0f;
    //三角形的颜色数组，rgba
    private float[] mColor = {r, g, b, 1.0f};

    //这个可以理解为一个OpenGL程序句柄
    private int mProgram;

    public SquareRender() {
        /* 1、数据转换，顶点坐标数据float类型转换成OpenGL格式FloatBuffer，int和short同理*/
        vertexBuffer = GlUtil.createFloatBuffer(squareCoords);
    }


    // gles相关的代码应该在gles的线层内调用，也就是这三个声明周期内调用。否则可能会报 call to OpenGL ES API with no current context (logged once per thread)
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(1.0f, 0.3f, 0.2f, 1.0f);

        /* 加载编译顶点着色器和片元着色器*/
        int vertexShader = GlUtil.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = GlUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        /* 创建空的OpenGL ES程序，并把着色器添加进去*/
        mProgram = GLES20.glCreateProgram();

        // 添加顶点着色器到程序中
        GLES20.glAttachShader(mProgram, vertexShader);

        // 添加片段着色器到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);

        /* 4、链接程序*/
        GLES20.glLinkProgram(mProgram);
    }

    private float[] mvpMatrix = new float[16];

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        final float aspectRadio = (float) height / width;
        //orthoM(float[] m, int mOffset, float left, float right, float bottom, float top, float near, float far)
        Matrix.orthoM(mvpMatrix, 0, -1f, 1f, -aspectRadio, aspectRadio, -1f, 1f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        upgradeRGB();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // 将程序添加到OpenGL ES环境
        GLES20.glUseProgram(mProgram);

        // 获取顶点着色器的位置的句柄（这里可以理解为当前绘制的顶点位置）
        //当前绘制的顶点位置句柄
        int vPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // 启用顶点属性
        GLES20.glEnableVertexAttribArray(vPosition);

        //准备三角形坐标数据
        GLES20.glVertexAttribPointer(vPosition, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // 获取片段着色器的vColor属性
        //片元着色器颜色句柄
        int vColor = GLES20.glGetUniformLocation(mProgram, "vColor");

        // 设置绘制三角形的颜色
        GLES20.glUniform4fv(vColor, 1, mColor, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);

        // 禁用顶点数组
        GLES20.glDisableVertexAttribArray(vPosition);
    }

    public void upgradeRGB() {
        Log.d("yocn", "r:" + r + " g:" + g + " b:" + b);
        if (r > 1.0f && g > 1.0f && b > 1.0f) {
            r = 0.0f;
            g = 0.0f;
            b = 0.0f;
        }
        mColor[0] = r;
        mColor[1] = g;
        mColor[2] = b;
        if (g < 1.0f) {
            g += 0.1f;
            return;
        } else if (b < 1.0f) {
            b += 0.1f;
            return;
        } else if (r < 1.0f) {
            r += 0.1f;
            return;
        }
    }
}
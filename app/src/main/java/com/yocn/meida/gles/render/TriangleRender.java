package com.yocn.meida.gles.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.yocn.meida.gles.GlUtil;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TriangleRender implements GLSurfaceView.Renderer {
    // 顶点着色器的脚本
    String vertexShaderCode =
            " attribute vec4 vPosition;" +     // 应用程序传入顶点着色器的顶点位置
                    " void main() {" +
                    "     gl_Position = vPosition;" +  // 此次绘制此顶点位置
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
    //三角形的坐标数组
    static float triangleCoords[] = {
            0.0f, 0.5f, 0.0f, // top
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f  // bottom right
    };

    //顶点个数，计算得出
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    //一个顶点有3个float，一个float是4个字节，所以一个顶点要12字节
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    //三角形的颜色数组，rgba
    private float[] mColor = {0.2f, 0.0f, 0.8f, 1.0f};

    //这个可以理解为一个OpenGL程序句柄
    private int mProgram;

    public TriangleRender() {
        /* 1、数据转换，顶点坐标数据float类型转换成OpenGL格式FloatBuffer，int和short同理*/
        vertexBuffer = GlUtil.createFloatBuffer(triangleCoords);
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

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // 将程序添加到OpenGL ES环境
        GLES20.glUseProgram(mProgram);

        // 获取顶点着色器的位置的句柄（这里可以理解为当前绘制的顶点位置）
        //当前绘制的顶点位置句柄
        int vPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");

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

        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // 禁用顶点数组
        GLES20.glDisableVertexAttribArray(vPosition);
    }
}
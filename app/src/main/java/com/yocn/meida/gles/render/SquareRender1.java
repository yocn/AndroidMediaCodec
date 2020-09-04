package com.yocn.meida.gles.render;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.yocn.meida.gles.GlUtil;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SquareRender1 implements GLSurfaceView.Renderer {

    // 顶点着色器的脚本
    String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +         //接收传入的转换矩阵
                    "attribute vec4 vPosition;" +      //接收传入的顶点
                    "attribute vec2 aTexCoord;" +       //接收传入的顶点纹理位置
                    "varying vec2 vTextureCoord;" +     //增加用于传递给片元着色器的纹理位置变量
                    "void main() {" +
                    "gl_Position = uMVPMatrix * vPosition;" +  //矩阵变换计算之后的位置
                    "vTextureCoord = aTexCoord;" +
                    " }";


    // 片元着色器的脚本
    String fragmentShaderCode =
            "precision mediump float;" +  // 声明float类型的精度为中等(精度越高越耗资源)
                    "varying vec2 vTextureCoord;" +
                    "uniform sampler2D sTexture;" + //纹理采样器，代表一副纹理
                    " void main() {" +
                    "gl_FragColor = texture2D(sTexture,vTextureCoord);" +//进行纹理采样
                    " }";

    private FloatBuffer vertexBuffer;  //顶点坐标数据要转化成FloatBuffer格式
    private FloatBuffer texBuffer;  //顶点坐标数据要转化成FloatBuffer格式

    // 正方形的坐标数组
    static float squareCoords[] = {
            -1f, 1f, 0.0f, // top left
            -1f, -1f, 0.0f, // bottom left
            1f, 1f, 0.0f,  // top right
            1f, -1f, 0.0f  // bottom right
    };
    float[] colors = new float[]{
            0, 0,
            0, 1,
            1, 0,
            1, 1,
    };//纹理顶点数组

    private int mProgram;
    private int vPositionHandle;
    private int aTexCoordHandle;
    private int mMvpMatrixHandle;
    private int uTextureUnitLocationHandle;

    public SquareRender1(Bitmap bitmap) {
        mBitmap = bitmap;
        /* 1、数据转换，顶点坐标数据float类型转换成OpenGL格式FloatBuffer，int和short同理*/
        vertexBuffer = GlUtil.createFloatBuffer(squareCoords);
        texBuffer = GlUtil.createFloatBuffer(colors);
        mTextureId = createTexture();
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

        vPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        aTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");

        mMvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        uTextureUnitLocationHandle = GLES20.glGetUniformLocation(mProgram, "sTexture");
    }

    private float[] mvpMatrix = new float[16];

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        final float aspectRadio = (float) height / width;
        //orthoM(float[] m, int mOffset, float left, float right, float bottom, float top, float near, float far)
        Matrix.orthoM(mvpMatrix, 0, -1f, 1f, -aspectRadio, aspectRadio, -1f, 1f);
    }

    private int mTextureId;
    private Bitmap mBitmap;

    private int createTexture() {
        int[] texture = new int[1];
        if (mBitmap != null && !mBitmap.isRecycled()) {
            //生成纹理
            GLES20.glGenTextures(1, texture, 0);
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
            return texture[0];
        }
        return 0;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // 将程序添加到OpenGL ES环境
        GLES20.glUseProgram(mProgram);

        // 启用顶点属性
        GLES20.glEnableVertexAttribArray(vPositionHandle);
        GLES20.glEnableVertexAttribArray(aTexCoordHandle);
        mTextureId = createTexture();
        //准备三角形坐标数据
        GLES20.glVertexAttribPointer(vPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);
        //设置纹理坐标数据
        GLES20.glVertexAttribPointer(aTexCoordHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, texBuffer);

        GLES20.glUniformMatrix4fv(mMvpMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glUniform1i(uTextureUnitLocationHandle, 0);

        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

}
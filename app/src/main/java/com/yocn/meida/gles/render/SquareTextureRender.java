package com.yocn.meida.gles.render;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import com.yocn.meida.gles.GlUtil;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SquareTextureRender implements GLSurfaceView.Renderer {
    // 顶点着色器的脚本
    String vertexShaderCode =
            "attribute vec4 vPosition;" +     // 应用程序传入顶点着色器的顶点位置
            "attribute vec2 aTexCoord;" +       //接收传入的顶点纹理位置
            "varying vec2 vTextureCoord;" +
                    " void main() {" +
                    "     gl_Position = vPosition;" +  // 此次绘制此顶点位置
                    "     vTextureCoord = aTexCoord;" +  // 设置texture的坐标
                    " }";

    // 片元着色器的脚本
    String fragmentShaderCode =
            " precision mediump float;" +  // 声明float类型的精度为中等(精度越高越耗资源)
             "varying vec2 vTextureCoord;" +
             "uniform sampler2D sTexture;" + //纹理采样器，代表一副纹理
                    " void main() {" +
                    "gl_FragColor = texture2D(sTexture, vTextureCoord);" + //进行纹理采样，第一个参数代表图片纹理，第二个参数代表纹理坐标点
                    " }";

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorCoordsBuffer;

    // 数组中每3个值作为一个坐标点
    static final int COORDS_PER_VERTEX = 3;
    // 正方形的坐标数组
    static float squareCoords[] = {
            -0.8f, 0.8f, 0.0f, // top left
            -0.8f, -0.8f, 0.0f, // bottom left
            0.8f, 0.8f, 0.0f,  // top right
            0.8f, -0.8f, 0.0f  // bottom right
    };

    static float colorCoords[] = {
            0, 0,
            0, 1,
            1, 0,
            1, 1,
    };

    //顶点个数，计算得出
    private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    //一个顶点有3个float，一个float是4个字节，所以一个顶点要12字节
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    //三角形的颜色数组，rgba
    private float[] mColor = {0.2f, 0.0f, 0.8f, 1.0f};

    //这个可以理解为一个OpenGL程序句柄
    private int mProgram;

    private int mTextureId;
    private Bitmap mBitmap;

    public SquareTextureRender(Bitmap bitmap) {
        this.mBitmap = bitmap;
        /* 1、数据转换，顶点坐标数据float类型转换成OpenGL格式FloatBuffer，int和short同理*/
        vertexBuffer = GlUtil.createFloatBuffer(squareCoords);
        colorCoordsBuffer = GlUtil.createFloatBuffer(colorCoords);
    }

    private void initTexture() {
        int textures[] = new int[1]; //生成纹理id

        GLES20.glGenTextures(  //创建纹理对象
                1, //产生纹理id的数量
                textures, //纹理id的数组
                0  //偏移量
        );
        mTextureId = textures[0];

        //绑定纹理id，将对象绑定到环境的纹理单元
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);//设置MIN 采样方式
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);//设置MAG采样方式
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);//设置S轴拉伸方式
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);//设置T轴拉伸方式

        if (mBitmap == null){
            Log.e("lxb", "initTexture: mBitmap == null");
            return;
        }
        //加载图片
        GLUtils.texImage2D( //实际加载纹理进显存
                GLES20.GL_TEXTURE_2D, //纹理类型
                0, //纹理的层次，0表示基本图像层，可以理解为直接贴图
                mBitmap, //纹理图像
                0 //纹理边框尺寸
        );
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
        initTexture();
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
        int aTexCoord = GLES20.glGetAttribLocation(mProgram, "aTexCoord");

        // 启用顶点属性
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glEnableVertexAttribArray(aTexCoord);

        //准备三角形坐标数据
        GLES20.glVertexAttribPointer(vPosition, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        //设置纹理坐标数据
        GLES20.glVertexAttribPointer(aTexCoord, 2,
                GLES20.GL_FLOAT, false,
                2 * 4, colorCoordsBuffer);

        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);

        // 禁用顶点数组
        GLES20.glDisableVertexAttribArray(vPosition);
        GLES20.glDisableVertexAttribArray(aTexCoord);
    }
}
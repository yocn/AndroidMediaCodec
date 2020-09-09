package com.yocn.meida.gles.render;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.yocn.meida.gles.util.GlUtil;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SquareForTextureRender implements GLSurfaceView.Renderer {

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
                    "uniform sampler2D sTexture;" +
                    "void main() {\n" +
                            "    //四分镜就是把整张图片缩成四份，然后分别放在左上角、右上角、左下角、右下角等地方。我们可以通过改变纹理坐标（x和y）得到\n" +
                            "    //类似两分镜也是同理\n" +
                            "    vec2 uv = vTextureCoord;\n" +
                            "    if (uv.x <= 0.5) {\n" +
                            "        uv.x = uv.x * 2.0;\n" +
                            "    } else {\n" +
                            "        uv.x = (uv.x - 0.5) * 2.0;\n" +
                            "    }\n" +
                            "\n" +
                            "    if (uv.y <= 0.5) {\n" +
                            "        uv.y = uv.y * 2.0;\n" +
                            "    } else {\n" +
                            "        uv.y = (uv.y - 0.5) * 2.0;\n" +
                            "    }\n" +
                            "    gl_FragColor = texture2D(sTexture, uv);\n" +
                            "}";

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

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexCoordBuffer;
    private int mProgram;
    private int vPositionHandle;
    private int aTexCoordHandle;
    private int mMvpMatrixHandle;
    private int mTextureId;
    private Bitmap mBitmap;

    public SquareForTextureRender(Bitmap bitmap) {
        mBitmap = bitmap;
        /* 1、数据转换，顶点坐标数据float类型转换成OpenGL格式FloatBuffer，int和short同理*/
        mVertexBuffer = GlUtil.createFloatBuffer(squareCoords);
        mTexCoordBuffer = GlUtil.createFloatBuffer(colors);
    }

    // gles相关的代码应该在gles的线层内调用，也就是这三个声明周期内调用。否则可能会报 call to OpenGL ES API with no current context (logged once per thread)
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(1.0f, 0.3f, 0.2f, 1.0f);
        // 之前initTexture();放构造方法了，一直没找到原因，百思不得其姐了好久！！！！！！！！！！！！！
        initTexture();
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
    }

    private float[] mvpMatrix = new float[16];

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float surfaceRadio = (float) height / width;
        float picRadio = (float) mBitmap.getHeight() / mBitmap.getWidth();
        if (surfaceRadio > picRadio) {
            // 预览画面比较长
            //orthoM(float[] m, int mOffset, float left, float right, float bottom, float top, float near, float far)
            Matrix.orthoM(mvpMatrix, 0, -1f, 1f, -surfaceRadio / picRadio, surfaceRadio / picRadio, -1f, 1f);
        } else {
            //orthoM(float[] m, int mOffset, float left, float right, float bottom, float top, float near, float far)
            Matrix.orthoM(mvpMatrix, 0, -picRadio / surfaceRadio, picRadio / surfaceRadio, -1f, 1f, -1f, 1f);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // 将程序添加到OpenGL ES环境
        GLES20.glUseProgram(mProgram);

        /**设置数据*/
        // 启用顶点属性，最后对应禁用
        GLES20.glEnableVertexAttribArray(vPositionHandle);
        GLES20.glEnableVertexAttribArray(aTexCoordHandle);

        //设置三角形坐标数据（一个顶点三个坐标）
        GLES20.glVertexAttribPointer(vPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                3 * 4, mVertexBuffer);
        //设置纹理坐标数据
        GLES20.glVertexAttribPointer(aTexCoordHandle, 2,
                GLES20.GL_FLOAT, false,
                2 * 4, mTexCoordBuffer);

        // 将投影和视图转换传递给着色器，可以理解为给uMVPMatrix这个变量赋值为mvpMatrix
        GLES20.glUniformMatrix4fv(mMvpMatrixHandle, 1, false, mvpMatrix, 0);

        //设置使用的纹理编号
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //绑定指定的纹理id
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        /** 绘制三角形，三个顶点*/
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // 禁用顶点数组（好像不禁用也没啥问题）
        GLES20.glDisableVertexAttribArray(vPositionHandle);
        GLES20.glDisableVertexAttribArray(aTexCoordHandle);
    }


    protected int initTexture() {
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
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);//设置MIN 采样方式
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);//设置MAG采样方式
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);//设置S轴拉伸方式
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);//设置T轴拉伸方式

        if (mBitmap == null) {
            return -1;
        }
        //加载图片
        GLUtils.texImage2D( //实际加载纹理进显存
                GLES20.GL_TEXTURE_2D, //纹理类型
                0, //纹理的层次，0表示基本图像层，可以理解为直接贴图
                mBitmap, //纹理图像
                0 //纹理边框尺寸
        );

        return textures[0];
    }

}
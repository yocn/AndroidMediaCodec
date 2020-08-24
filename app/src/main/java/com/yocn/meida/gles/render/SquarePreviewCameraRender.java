package com.yocn.meida.gles.render;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.yocn.meida.gles.GlUtil;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.GL_FRAMEBUFFER;

public class SquarePreviewCameraRender implements GLSurfaceView.Renderer {
    // 顶点着色器的脚本
//    String vertexShaderCode =
//            "uniform mat4 uMVPMatrix;//接收传入的转换矩阵\n" +
//                    "attribute vec4 aPosition;//接收传入的顶点\n" +
//                    "attribute vec2 aTexCoord;//接收传入的顶点纹理位置\n" +
//                    "varying vec2 vTextureCoord;//增加用于传递给片元着色器的纹理位置变量\n" +
//                    "varying vec4 vPosition;//传顶点坐标给片元着色器\n" +
//                    "void main() {\n" +
//                    "    gl_Position = uMVPMatrix * aPosition;//矩阵变换计算之后的位置\n" +
//                    "    vPosition = uMVPMatrix * aPosition;//矩阵变换计算之后的位置\n" +
//                    "    vTextureCoord = aTexCoord;\n" +
//                    "}\n" +
//                    "\n" +
//                    "\n";

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
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES uTextureSampler;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main()\n" +
                    "{\n" +
                    "  vec4 vCameraColor = texture2D(uTextureSampler, vTextureCoord);\n" +
                    "  float fGrayColor = (0.3*vCameraColor.r + 0.59*vCameraColor.g + 0.11*vCameraColor.b);\n" +
                    "  gl_FragColor = vec4(fGrayColor, fGrayColor, fGrayColor, 1.0);\n" +
                    "}\n";

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorCoordsBuffer;

    // 数组中每3个值作为一个坐标点
    static final int COORDS_PER_VERTEX = 3;
    // 正方形的坐标数组
    static float[] squareCoords = {
            -0.8f, 0.8f, 0.0f, // top left
            -0.8f, -0.8f, 0.0f, // bottom left
            0.8f, 0.8f, 0.0f,  // top right
            0.8f, -0.8f, 0.0f  // bottom right
    };

    static float[] colorCoords = {
            0, 0,
            0, 1,
            1, 0,
            1, 1,
    };

    //顶点个数，计算得出
    private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    //一个顶点有3个float，一个float是4个字节，所以一个顶点要12字节
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private int mProgramId;
    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;

    public SquarePreviewCameraRender(SurfaceTexture mSurfaceTexture, int mTextureId) {
        this.mSurfaceTexture = mSurfaceTexture;
        this.mTextureId = mTextureId;
        /* 1、数据转换，顶点坐标数据float类型转换成OpenGL格式FloatBuffer，int和short同理*/
        vertexBuffer = GlUtil.createFloatBuffer(squareCoords);
        colorCoordsBuffer = GlUtil.createFloatBuffer(colorCoords);
    }

    // gles相关的代码应该在gles的线层内调用，也就是这三个声明周期内调用。否则可能会报 call to OpenGL ES API with no current context (logged once per thread)
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(1.0f, 0.3f, 0.2f, 1.0f);
        mProgramId = GlUtil.createProgram(vertexShaderCode, fragmentShaderCode);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mSurfaceTexture.updateTexImage();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // 将程序添加到OpenGL ES环境
        GLES20.glUseProgram(mProgramId);

        //绑定纹理，跟图片不同的是，这里是扩展纹理
        GLES20.glActiveTexture(GL_TEXTURE_EXTERNAL_OES);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);

        // 获取顶点着色器的位置的句柄（这里可以理解为当前绘制的顶点位置）
        //当前绘制的顶点位置句柄
        int vPosition = GLES20.glGetAttribLocation(mProgramId, "vPosition");
        int aTexCoord = GLES20.glGetAttribLocation(mProgramId, "aTexCoord");

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
        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // 禁用顶点数组
//        GLES20.glDisableVertexAttribArray(vPosition);
//        GLES20.glDisableVertexAttribArray(aTexCoord);
    }
}
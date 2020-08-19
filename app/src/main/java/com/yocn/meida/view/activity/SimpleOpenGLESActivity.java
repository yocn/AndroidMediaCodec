package com.yocn.meida.view.activity;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.yocn.media.R;
import com.yocn.meida.util.LogUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName SimpleOpenGLESActivity
 */
public class SimpleOpenGLESActivity extends BaseCameraActivity {
    public static String DESC = "Simple OpenGL ES";
    private GLSurfaceView glSurfaceView1;
    private GLSurfaceView glSurfaceView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_opengles);
        glSurfaceView1 = findViewById(R.id.flsv_1);
        glSurfaceView1.setRenderer(new SimpleColorRender());
        glSurfaceView2 = findViewById(R.id.flsv_2);
        // if doesn't set setEGLContextClientVersion maybe report 'glDrawArrays is called with VERTEX_ARRAY client state disabled!'
        glSurfaceView2.setEGLContextClientVersion(2);
        glSurfaceView2.setRenderer(new TriangleRender());
    }

    public static class SimpleColorRender implements GLSurfaceView.Renderer {
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);//设置清屏颜色
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            gl.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }
    }

    public static class TriangleRender implements GLSurfaceView.Renderer {
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
        private float[] mColor = {
                0.0f, 1.0f, 0.0f, 1.0f,
        };

        //当前绘制的顶点位置句柄
        private int vPosition;
        //片元着色器颜色句柄
        private int vColor;
        //这个可以理解为一个OpenGL程序句柄
        private int mProgram;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // gles相关的代码应该在gles的线层内调用，也就是这三个声明周期内调用。否则可能会报 call to OpenGL ES API with no current context (logged once per thrread)
            GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);//设置清屏颜色
            /* 1、数据转换，顶点坐标数据float类型转换成OpenGL格式FloatBuffer，int和short同理*/
            vertexBuffer = floatArray2FloatBuffer(triangleCoords);

            /* 2、加载编译顶点着色器和片元着色器*/
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                    vertexShaderCode);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                    fragmentShaderCode);

            /* 3、创建空的OpenGL ES程序，并把着色器添加进去*/
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
            vPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");

            // 启用顶点属性
            GLES20.glEnableVertexAttribArray(vPosition);

            //准备三角形坐标数据
            GLES20.glVertexAttribPointer(vPosition, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    vertexStride, vertexBuffer);

            // 获取片段着色器的vColor属性
            vColor = GLES20.glGetUniformLocation(mProgram, "vColor");

            // 设置绘制三角形的颜色
            GLES20.glUniform4fv(vColor, 1, mColor, 0);

            // 绘制三角形
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

            // 禁用顶点数组
            GLES20.glDisableVertexAttribArray(vPosition);
        }
    }

    /**
     * float 数组转换成FloatBuffer，OpenGL才能使用
     */
    public static FloatBuffer floatArray2FloatBuffer(float[] arr) {
        FloatBuffer mBuffer;
        // 初始化ByteBuffer，长度为arr数组的长度*4，因为一个int占4个字节
        ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
        // 数组排列用nativeOrder
        qbb.order(ByteOrder.nativeOrder());
        mBuffer = qbb.asFloatBuffer();
        mBuffer.put(arr);
        mBuffer.position(0);
        return mBuffer;
    }

    public static int loadShader(int shaderType, String source) {
        // 创造顶点着色器类型(GLES20.GL_VERTEX_SHADER)
        // 或者是片段着色器类型 (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(shaderType);
        // 添加上面编写的着色器代码并编译它
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        return shader;
    }
}

package com.yocn.meida.view.activity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.yocn.media.R;
import com.yocn.meida.gles.render.SimpleColorRender;
import com.yocn.meida.gles.render.SquareRender;
import com.yocn.meida.gles.render.TriangleRender;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName SimpleOpenGLESActivity
 */
public class SimpleOpenGLESActivity extends BaseCameraActivity {
    public static String DESC = "Simple OpenGL ES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_opengles);
        GLSurfaceView glSurfaceView1 = findViewById(R.id.flsv_1);
        GLSurfaceView glSurfaceView2 = findViewById(R.id.flsv_2);
        GLSurfaceView glSurfaceView3 = findViewById(R.id.flsv_3);
        GLSurfaceView glSurfaceView4 = findViewById(R.id.flsv_4);
        // if doesn't set setEGLContextClientVersion maybe report 'glDrawArrays is called with VERTEX_ARRAY client state disabled!'
        glSurfaceView1.setEGLContextClientVersion(2);
        glSurfaceView2.setEGLContextClientVersion(2);
        glSurfaceView3.setEGLContextClientVersion(2);
        glSurfaceView4.setEGLContextClientVersion(2);
        glSurfaceView1.setRenderer(new SimpleColorRender());
        glSurfaceView2.setRenderer(new TriangleRender());
        glSurfaceView3.setRenderer(new SquareRender());
        glSurfaceView4.setRenderer(new SquareRender());
    }
}

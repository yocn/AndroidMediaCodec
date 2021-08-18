package com.yocn.meida.view.activity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.yocn.media.R;
import com.yocn.meida.gles.render.SimpleColorRender;
import com.yocn.meida.gles.render.SquareForTextureRender;
import com.yocn.meida.gles.render.SquareRender;
import com.yocn.meida.gles.render.SquareTextureRender;
import com.yocn.meida.gles.render.SquareTextureRender2;
import com.yocn.meida.gles.render.TriangleRender;
import com.yocn.meida.util.BitmapUtil;
import com.yocn.meida.view.activity.camera.BaseCameraActivity;

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
        GLSurfaceView glSurfaceView1 = findViewById(R.id.flsv_1);
        GLSurfaceView glSurfaceView2 = findViewById(R.id.flsv_2);
        GLSurfaceView glSurfaceView3 = findViewById(R.id.flsv_3);
        GLSurfaceView glSurfaceView4 = findViewById(R.id.flsv_4);
        GLSurfaceView glSurfaceView5 = findViewById(R.id.flsv_5);
        GLSurfaceView glSurfaceView6 = findViewById(R.id.flsv_6);
        // if doesn't set setEGLContextClientVersion maybe report 'glDrawArrays is called with VERTEX_ARRAY client state disabled!'
        glSurfaceView1.setEGLContextClientVersion(2);
        glSurfaceView2.setEGLContextClientVersion(2);
        glSurfaceView3.setEGLContextClientVersion(2);
        glSurfaceView4.setEGLContextClientVersion(2);
        glSurfaceView5.setEGLContextClientVersion(2);
        glSurfaceView6.setEGLContextClientVersion(2);
        glSurfaceView1.setRenderer(new SimpleColorRender());
        glSurfaceView2.setRenderer(new TriangleRender());
        glSurfaceView3.setRenderer(new SquareRender());
        glSurfaceView4.setRenderer(new SquareTextureRender(BitmapUtil.getBitmapFromAssets(this, "show.jpeg")));
        glSurfaceView5.setRenderer(new SquareTextureRender2(BitmapUtil.getBitmapFromAssets(this, "show.jpeg")));
        glSurfaceView6.setRenderer(new SquareForTextureRender(BitmapUtil.getBitmapFromAssets(this, "show.jpeg")));
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_simple_opengles;
    }
}

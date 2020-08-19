package com.yocn.meida.gles;

public class GLProgram {
    public static class SimpleProgram {
        public static String vertexShader = "//simple_vertex_shader.glsl\n" +
                "attribute vec4 a_Position;//attribute变量，表示只能在vertex shader中使用\n" +
                "attribute vec4 a_Color;\n" +
                "varying vec4 v_Color;//varying 变量，表示要传给fragment shader的数据\n" +
                "void main()\n" +
                "{\n" +
                "    v_Color = a_Color;//之前cube定义的顶点颜色，传给fragment shader\n" +
                "    gl_Position = a_Position;//之前cube定义的顶点坐标，最终显示的坐标给gl_Position，OpenGL用gl_Position做为最终的位置值\n" +
                "}";
        public static String fragmentShader = "//simple_fragment_shader.glsl\n" +
                "precision mediump float; //设置精度，vertex shader默认是highp\n" +
                "varying vec4 v_Color;                                       \n" +
                "void main()                         \n" +
                "{                               \n" +
                "    gl_FragColor = v_Color;  //  gl_FragColor就是最终的颜色值                           \n" +
                "}";
    }
}

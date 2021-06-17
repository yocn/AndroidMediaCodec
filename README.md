Android Camera2从入门到放弃
=======

1.Camera使用TextureView预览
-------  

2.ImageReader获取Camera2回调数据
-------  

3.转换方式一：获取`YUV_420_888`格式回调，`I420Tonv21`转换成NV21格式，使用YuvImage(只接受NV21)获取Bitmap输出到ImageView预览
-------  

4.转换方式二：获取`YUV_420_888`格式回调，同转换为NV21，根据`Image`和`ImageReader`一步一步转换得出。
-------  

5.使用libYuv库做ARGB和YUV的转换。
-------  

6.使用GPUImage库做预览，可以选择各种gles效果。
-------  

详细文章可以查看 https://www.jianshu.com/p/b9d994f2b381

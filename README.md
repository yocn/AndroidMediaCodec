# Android Camera2从入门到放弃

关联了ffmpeg\libyuv等等库，可以作为参考
编译前需要做的：
1.下载NDK，在你的电脑里配置好，如果是mac请在.bash_profile/.zshrc等配置环境变量
```shell script
export NDK=/Your-Path/Android/sdk/ndk/20.1.5948944
export PATH=${PATH}:${NDK}
```
2. 因为ffmpeg使用了子模块，使用前请使用子模块来更新代码
```shell script
git submodule init
git submodule update
```
3. 编译ffmpeg等的so库
```shell script
cd libNative/src/main/jni
./build-all.sh
```
成功之后就可以啦~

1.Camera使用TextureView预览
2.ImageReader获取Camera2回调数据
3.转换方式一：获取`YUV_420_888`格式回调，`I420Tonv21`转换成NV21格式，使用YuvImage(只接受NV21)获取Bitmap输出到ImageView预览
4.转换方式二：获取`YUV_420_888`格式回调，同转换为NV21，根据`Image`和`ImageReader`一步一步转换得出。
5.使用libYuv库做ARGB和YUV的转换。
6.使用GPUImage库做预览，可以选择各种gles效果。

详细文章可以查看 https://www.jianshu.com/p/b9d994f2b381

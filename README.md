# Android 音视频处理集合

关联了ffmpeg\libyuv等等库，可以作为参考。
部分模块博客有介绍，详细可以移步我的博客：[简书 - YocnZhao](https://www.jianshu.com/u/96cd25086c38)

### 编译步骤：

1. 下载NDK，在你的电脑里配置好，如果是mac请在.bash_profile/.zshrc等配置环境变量
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

### 包含的内容：
1. **Camera2相关**

	1.1. Camera使用TextureView预览

	1.2. ImageReader获取Camera2回调数据

	1.3. 转换方式一：获取`YUV_420_888`格式回调，`I420Tonv21`转换成NV21格式，使用YuvImage(只接受NV21)获取Bitmap输出到ImageView预览

	1.4. 转换方式二：获取`YUV_420_888`格式回调，同转换为NV21，根据`Image`和`ImageReader`一步一步转换得出。

	1.5. 适配预览所有的尺寸，使画面不变形

	1.6. 使用libYuv库做ARGB和YUV的转换。

	1.7. 使用GPUImage库做预览，可以选择各种gles效果。

2. **FFmpeg相关**

	2.1. 解码播放视频

	2.2. 解码播放音频，存储pcm数据，使用libmp3lame库转成mp3

	2.3. 最简单的音视频同步

3. **MediaCodec相关**

	3.1. 视频解码播放到Surface，获取到yuv数据并存储

	3.2. 时频解码之后编码成mp4文件

4. **open gles相关**

	4.1. 部分gles坐标系入门
	
	4.2. 预览camera

5. **一个简单的yuv播放器**

---

![9d51a13aa26ff9d70e7da2ecd62259.jpg](https://s2.loli.net/2022/03/10/Oyb6iELjsQ5xGCp.jpg)

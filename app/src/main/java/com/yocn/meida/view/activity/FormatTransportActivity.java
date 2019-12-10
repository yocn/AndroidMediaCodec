package com.yocn.meida.view.activity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.yocn.libnative.YUVTransUtil;
import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.util.FileUtils;
import com.yocn.meida.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 * libyuv做ARGB和I420转换
 */
public class FormatTransportActivity extends BaseActivity {
    ImageView mShowIV;
    ImageView mRawShowIV;
    Button mClickBtn;
    Button mClickBtn2;
    public static String DESC = "图片转化测试";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        View rootView = getLayoutInflater().inflate(R.layout.activity_trans, null);
        setContentView(rootView);
        initView(rootView);
        initData();
    }

    @Override
    protected void initView(View root) {
        super.initView(root);
        mShowIV = root.findViewById(R.id.iv_show);
        mRawShowIV = root.findViewById(R.id.iv_show_raw);
        mClickBtn = root.findViewById(R.id.btn_click);
        mClickBtn2 = root.findViewById(R.id.btn_click_2);
        mClickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });
        mClickBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test2();
            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    YUVTransUtil mYUVTransUtil = new YUVTransUtil();

    private void test() {
        Bitmap image = null;
//        int w = 480, h = 640;
        int w = 1920, h = 1080;
        AssetManager am = getResources().getAssets();
        try {
            //读取assert 的文图
            InputStream is = am.open("show.jpeg");
            image = BitmapFactory.decodeStream(is);
            mRawShowIV.setImageBitmap(image);

            //将位图资源转为二进制数据，数据大小为w*h*4
            int bytes = image.getByteCount();
            LogUtil.d("image 的 bytes size->" + bytes);
            ByteBuffer buf = ByteBuffer.allocate(bytes);
            image.copyPixelsToBuffer(buf);
            byte[] byteArray = buf.array();

            //用于保存y分量数据
            byte[] ybuffer = new byte[w * h];
            //用于保存u分量数据
            byte[] ubuffer = new byte[w * h * 1 / 4];
            //用于保存v分量数据
            byte[] vbuffer = new byte[w * h * 1 / 4];
            //将位图数据argb转换为yuv I420 转换后的数据分别保存在 ybuffer、ubuffer和vbuffer里面
            mYUVTransUtil.ARGBToI420(byteArray, w * 4, ybuffer, w, ubuffer, (w + 1) / 2, vbuffer, (w + 1) / 2, w, h);


            //将上面的yuv数据保存到一个数组里面组成一帧yuv I420 数据 分辨率为w*h
            byte[] frameBuffer = new byte[w * h * 3 / 2];
            System.arraycopy(ybuffer, 0, frameBuffer, 0, w * h);
            System.arraycopy(ubuffer, 0, frameBuffer, w * h, w * h * 1 / 4);
            System.arraycopy(vbuffer, 0, frameBuffer, w * h * 5 / 4, w * h * 1 / 4);

            FileUtils.writeToFile(frameBuffer, "mnt/sdcard/tt.yuv", false);
            //用于保存将yuv数据转成argb数据
            byte[] rgbbuffer = new byte[w * h * 4];
            //将上面的yuv I420 还原成argb数据
            mYUVTransUtil.I420ToArgb(frameBuffer, w * h * 3 / 2, rgbbuffer, w * 4, 0, 0, w, h, w, h, 0, 0);
            //还原成位图
            Bitmap stitchBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            stitchBmp.copyPixelsFromBuffer(ByteBuffer.wrap(rgbbuffer));

            //显示还原的位图
            mShowIV.setImageBitmap(stitchBmp);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void test2() {
        int w = 640, h = 480;
        String path = Constant.getCacheYuvDir() + "/1.yuv";
        byte[] src = FileUtils.readFileToBytes(new File(path));
        byte[] rgbbuffer = new byte[w * h * 4];
        LogUtil.d("w * h * 3 / 2->" + (w * h * 3 / 2));
        LogUtil.d("src->" + src.length);
        LogUtil.d("w * 4->" + w * 4);
        mYUVTransUtil.I420ToArgb(src, w * h * 3 / 2, rgbbuffer, w * 4, 0, 0, w, h, w, h, 0, 0);

        //还原成位图
        Bitmap stitchBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        stitchBmp.copyPixelsFromBuffer(ByteBuffer.wrap(rgbbuffer));

        //显示还原的位图
        mShowIV.setImageBitmap(stitchBmp);
    }

}

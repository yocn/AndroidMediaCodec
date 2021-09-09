package com.yocn.meida.view.activity;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.yocn.libnative.X264Encoder;
import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.base.DataProvider;
import com.yocn.meida.util.PermissionsUtils;
import com.yocn.meida.view.widget.TopViewRecyclerView;

import androidx.annotation.NonNull;

/**
 * @author yocn
 */
public class MainActivity extends BaseActivity {
    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private TopViewRecyclerView topViewRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_main;
    }

    protected void initView(View root) {
        topViewRecyclerView = root.findViewById(R.id.tvrv_main);
    }

    protected void initData() {
        topViewRecyclerView.setActivity(this, DataProvider.Type.MAIN);
        String yuvPath = Constant.getTestFilePath("test.yuv");
        String x264Path = Constant.getTestFilePath("test.x264");
        new X264Encoder().initEncoder(yuvPath, x264Path, 544, 960, 30);
    }

    private void requestPermission() {
        PermissionsUtils.IPermissionsResult permissionsResult = new PermissionsUtils.IPermissionsResult() {
            @Override
            public void passPermissons() {
            }

            @Override
            public void forbitPermissons() {
                Toast.makeText(MainActivity.this, "need permission!", Toast.LENGTH_SHORT).show();
            }
        };
        PermissionsUtils.getInstance().chekPermissions(this, permissions, permissionsResult);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

}

package com.yocn.meida.view.activity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.yocn.media.R;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 * x264转换 未完成
 */
public class FormatTrans264Activity extends BaseActivity {
    ImageView mShowIV;
    Button mClickBtn;
    public static String DESC = "TODO：";

    @Override
    protected int getContentViewId() {
        return R.layout.activity_trans;
    }

    @Override
    protected void initView(View root) {
        mShowIV = root.findViewById(R.id.iv_show);
        mClickBtn = root.findViewById(R.id.btn_click);
        mClickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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


}

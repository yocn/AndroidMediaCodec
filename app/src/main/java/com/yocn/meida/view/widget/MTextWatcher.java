package com.yocn.meida.view.widget;

import android.text.Editable;
import android.text.TextWatcher;

import com.yocn.meida.util.LogUtil;

/**
 * @Author yocn
 * @Date 2019-11-09 18:46
 * @ClassName MTextWatcher
 */
public class MTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        LogUtil.d(s.toString());
    }
}

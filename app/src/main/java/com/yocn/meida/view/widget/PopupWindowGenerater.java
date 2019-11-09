package com.yocn.meida.view.widget;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;

import com.yocn.media.R;

import java.util.List;

/**
 * @Author yocn
 * @Date 2019-11-09 11:01
 * @ClassName PopupWindowGenerater
 */
public class PopupWindowGenerater<T> {
    ListPopupWindow listPopupWindow;
    Context mContext;

    public PopupWindowGenerater() {
    }

    public PopupWindowGenerater init(Context context) {
        listPopupWindow = new ListPopupWindow(context);
        this.mContext = context;
        return this;
    }

    public PopupWindowGenerater setItems(List<T> list, int... layoutIds) {
        int layoutId = layoutIds.length > 0 ? layoutIds[0] : R.layout.item_popup_list;
        listPopupWindow.setAdapter(new ArrayAdapter<T>(mContext, layoutId, list));
        listPopupWindow.setModal(true);
//        listPopupWindow.setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(R.color.write, null)));
        return this;
    }

    public PopupWindowGenerater setAnchorView(View anchor) {
        listPopupWindow.setAnchorView(anchor);
        return this;
    }

    public PopupWindowGenerater setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        listPopupWindow.setOnItemClickListener(listener);
        return this;
    }

    public void show() {
        listPopupWindow.show();
        listPopupWindow.getListView().setSelector(R.color.trans);
    }

    public void dismiss() {
        listPopupWindow.dismiss();
    }

}

package com.yocn.meida.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.yocn.media.R;
import com.yocn.meida.JumpBean;
import com.yocn.meida.util.LogUtil;
import com.yocn.meida.view.activity.FormatTrans264Activity;
import com.yocn.meida.view.activity.FormatTransportActivity;
import com.yocn.meida.view.activity.PreviewDataActivity;
import com.yocn.meida.view.activity.PreviewGPUImageActivity;
import com.yocn.meida.view.activity.PreviewNativeYUVActivity;
import com.yocn.meida.view.activity.PreviewPureActivity;
import com.yocn.meida.view.activity.PreviewYUVDataActivity;
import com.yocn.meida.view.activity.PreviewYUVDataActivity2;
import com.yocn.meida.view.activity.TestScrollActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @Author yocn
 * @Date 2019/8/4 10:24 AM
 * @ClassName MainAdapter
 */
public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CONTENT = 0;
    private static final int TYPE_HEAD = 1;

    private Context mContext;
    private int[] colors = {R.color.color1, R.color.color2, R.color.color3
            , R.color.color4, R.color.color5, R.color.color6
            , R.color.color7, R.color.color8, R.color.color9};

    private int[] textColor = {R.color.write, R.color.black, R.color.write
            , R.color.black, R.color.black, R.color.black
            , R.color.write, R.color.black, R.color.write};

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public static class VH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView tv_hint;
        final RelativeLayout all;
        int a = 1;

        VH(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            tv_hint = v.findViewById(R.id.tv_hint);
            all = v.findViewById(R.id.all);
        }
    }

    public static class VHHeader extends RecyclerView.ViewHolder {
        VideoView videoView;

        VHHeader(View v) {
            super(v);
//            videoView = v.findViewById(R.id.vv);
//            videoView.setVideoURI(Uri.parse("android.resource://com.yocn.media/" + R.raw.onboarding_bg));
//            videoView.start();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEAD;
        } else {
            return TYPE_CONTENT;
        }
    }

    private List<JumpBean> mDatas;

    public MainAdapter(List<JumpBean> data) {
        this.mDatas = data;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewholder, final int position) {
        int type = getItemViewType(position);
        LogUtil.d("bind---" + position);
        if (type == TYPE_CONTENT) {
            VH holder = (VH) viewholder;
            holder.title.setText(String.format("%s", mDatas.get(position - 1).getShow()));
            try {
                Class clazz = mDatas.get((position - 1)).getToClass();
                Field field = clazz.getField("DESC");
                String ss = (String) field.get(clazz);
                holder.tv_hint.setText(String.format("%s", ss));
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.d("e--->" + e.toString());
            }

            holder.tv_hint.setTextColor(mContext.getResources().getColor(textColor[(position - 1) % textColor.length]));
            holder.title.setTextColor(mContext.getResources().getColor(textColor[(position - 1) % textColor.length]));
//            holder.title.setTextColor(mDatas.get((position - 1)).getToClass());
            holder.all.setBackgroundResource(colors[(position - 1) % colors.length]);
            holder.itemView.setOnClickListener(v -> {
                //item 点击事件
                LogUtil.d("realPosition    click-" + (position - 1) + "    " + mDatas.get((position - 1)).getToClass() + " position-》" + (position - 1));
                if (mContext != null) {
                    mContext.startActivity(new Intent(mContext, mDatas.get(position - 1).getToClass()));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LogUtil.d("create---");
        RecyclerView.ViewHolder holder = null;
        //LayoutInflater.from指定写法
        if (viewType == TYPE_CONTENT) {
            holder = new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false));
        } else {
            holder = new VHHeader(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false));
        }
        return holder;
    }
}

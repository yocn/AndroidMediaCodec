package com.yocn.meida.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yocn.media.R;
import com.yocn.meida.JumpBean;
import com.yocn.meida.util.LogUtil;

import java.util.List;

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

    //② 创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder {
        public final TextView title;
        final RelativeLayout all;

        public VH(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            all = v.findViewById(R.id.all);
        }
    }

    //② 创建ViewHolder
    public static class VHHeader extends RecyclerView.ViewHolder {

        public VHHeader(View v) {
            super(v);
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

    int realPosition;

    //③ 在Adapter中实现3个方法
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewholder, final int position) {
        int type = getItemViewType(position);
        if (type == TYPE_CONTENT) {
            VH holder = (VH) viewholder;
            realPosition = position - 1;
            holder.title.setText(mDatas.get(position).getShow() + "");
            holder.title.setTextColor(mContext.getResources().getColor(textColor[realPosition % textColor.length]));
            holder.all.setBackgroundResource(colors[realPosition % colors.length]);
            LogUtil.d("onBindViewHolder-" + realPosition);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //item 点击事件
                    LogUtil.d("click-" + realPosition);
                    if (mContext != null) {
                        mContext.startActivity(new Intent(mContext, mDatas.get(realPosition).getToClass()));
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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

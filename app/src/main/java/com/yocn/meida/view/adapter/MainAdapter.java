package com.yocn.meida.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.VH> {

    private Context mContext;

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    //② 创建ViewHolder
    public static class VH extends RecyclerView.ViewHolder {
        public final TextView title;

        public VH(View v) {
            super(v);
            title = v.findViewById(R.id.title);
        }
    }

    private List<JumpBean> mDatas;

    public MainAdapter(List<JumpBean> data) {
        this.mDatas = data;
    }

    //③ 在Adapter中实现3个方法
    @Override
    public void onBindViewHolder(VH holder, final int position) {
        holder.title.setText(mDatas.get(position).getShow() + "");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //item 点击事件
                LogUtil.d("click-" + position);
                if (mContext != null) {
                    mContext.startActivity(new Intent(mContext, mDatas.get(position).getToClass()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater.from指定写法
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        return new VH(v);
    }
}

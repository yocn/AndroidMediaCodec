package com.yocn.meida.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yocn.media.R;
import com.yocn.meida.util.LogUtil;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @Author yocn
 * @Date 2019/8/4 10:24 AM
 * @ClassName MainAdapter
 */
public class GPUImageFilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CONTENT = 0;
    private OnSelectFilterInterface mInterface;

    private Context mContext;
    private int[] colors = {R.color.color1, R.color.color2, R.color.color3
            , R.color.color4, R.color.color5, R.color.color6
            , R.color.color7, R.color.color8, R.color.color9};

    private int[] textColor = {R.color.write, R.color.black, R.color.write
            , R.color.black, R.color.black, R.color.black
            , R.color.write, R.color.black, R.color.write};

    public interface OnSelectFilterInterface {
        public void selectFilter(int position);
    }

    public void setSelectListener(OnSelectFilterInterface listener) {
        mInterface = listener;
    }

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
        return TYPE_CONTENT;
    }

    private List<String> mDatas;

    public GPUImageFilterAdapter(List<String> data) {
        this.mDatas = data;
    }

    //③ 在Adapter中实现3个方法
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewholder, final int position) {
        int type = getItemViewType(position);
        if (type == TYPE_CONTENT) {
            VH holder = (VH) viewholder;
            holder.title.setText(mDatas.get(position));
            holder.title.setTextColor(mContext.getResources().getColor(textColor[(position) % textColor.length]));
            holder.all.setBackgroundResource(colors[(position) % colors.length]);
            holder.itemView.setOnClickListener(v -> {
                //item 点击事件
                LogUtil.d("realPosition    click-" + (position) + "    " + mDatas.get((position)) + " position-》" + (position));
                if (mInterface != null) {
                    mInterface.selectFilter(position);
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
        holder = new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gpuimage, parent, false));
        return holder;
    }
}

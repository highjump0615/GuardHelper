package com.highjump.guardhelper.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.highjump.guardhelper.R;
import com.highjump.guardhelper.view.ViewHolderItem;

/**
 * Created by Administrator on 2016/7/24.
 */
public class MainItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    private int ITEM_VIEW_TYPE_SEND = 0;
    private int ITEM_VIEW_TYPE_RECV = 1;

    public MainItemAdapter(Context ctx) {
        mContext = ctx;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vhRes = null;

        if (viewType == ITEM_VIEW_TYPE_SEND) {
            // 创建视图
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_main_item_send, parent, false);

            ViewHolderItem vh = new ViewHolderItem(v);
            vhRes = vh;
        }
        else if (viewType == ITEM_VIEW_TYPE_RECV) {
            // 创建视图
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_main_item_recv, parent, false);

            ViewHolderItem vh = new ViewHolderItem(v);
            vhRes = vh;
        }

        return vhRes;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        int nViewType = ITEM_VIEW_TYPE_RECV;

        if (position == 1) {
            nViewType = ITEM_VIEW_TYPE_SEND;
        }

        return nViewType;
    }
}

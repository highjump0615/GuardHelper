package com.highjump.guardhelper.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.highjump.guardhelper.R;
import com.highjump.guardhelper.model.ReportData;
import com.highjump.guardhelper.view.ViewHolderReport;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/24.
 */
public class MainItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ReportData> maryValue;
    private Context mContext;

    private int ITEM_VIEW_TYPE_SEND = 0;
    private int ITEM_VIEW_TYPE_RECV = 1;

    public MainItemAdapter(Context ctx, ArrayList<ReportData> values) {
        mContext = ctx;
        maryValue = values;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vhRes = null;

        if (viewType == ITEM_VIEW_TYPE_SEND) {
            // 创建视图
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_main_item_send, parent, false);

            ViewHolderReport vh = new ViewHolderReport(mContext, v);
            vhRes = vh;
        }
        else if (viewType == ITEM_VIEW_TYPE_RECV) {
            // 创建视图
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_main_item_recv, parent, false);

            ViewHolderReport vh = new ViewHolderReport(mContext, v);
            vhRes = vh;
        }

        return vhRes;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ViewHolderReport vh = (ViewHolderReport) holder;

        ReportData data = maryValue.get(position);
        vh.fillContent(data);
    }

    @Override
    public int getItemCount() {
        return maryValue.size();
    }

    @Override
    public int getItemViewType(int position) {
        int nViewType = ITEM_VIEW_TYPE_SEND;

        // 获取数据
        ReportData data = maryValue.get(position);

        // 如果是下发信息
        if (data.isReceive() == 1) {
            nViewType = ITEM_VIEW_TYPE_RECV;
        }

        return nViewType;
    }
}

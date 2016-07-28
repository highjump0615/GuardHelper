package com.highjump.guardhelper.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.highjump.guardhelper.R;
import com.highjump.guardhelper.model.ReportData;

/**
 * Created by Administrator on 2016/7/24.
 */
public class ViewHolderReport extends RecyclerView.ViewHolder {

    private TextView mTextMsg;
    private TextView mTextTime;

    public ViewHolderReport(View itemView) {
        super(itemView);

        mTextMsg = (TextView) itemView.findViewById(R.id.text_msg);
        mTextTime = (TextView) itemView.findViewById(R.id.text_time);
    }

    /**
     * 填充相应的信息
     * @param data - Report模型
     */
    public void fillContent(ReportData data) {
        mTextMsg.setText(data.getData());
        mTextTime.setText(data.getTime());
    }

}

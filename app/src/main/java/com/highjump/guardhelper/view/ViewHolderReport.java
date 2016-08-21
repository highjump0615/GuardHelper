package com.highjump.guardhelper.view;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.highjump.guardhelper.MainActivity;
import com.highjump.guardhelper.R;
import com.highjump.guardhelper.model.ReportData;

/**
 * Created by Administrator on 2016/7/24.
 */
public class ViewHolderReport extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Context mContext;

    private TextView mTextMsg;
    private TextView mTextTime;

    private ProgressBar mProgress;
    private ImageView mImgViewFail;

    private ImageView mImgViewMsg;
    private ImageView mImgViewPlay;

    private ReportData mData;

    public ViewHolderReport(Context ctx, View itemView) {
        super(itemView);

        mContext = ctx;

        // 文字
        mTextMsg = (TextView) itemView.findViewById(R.id.text_msg);

        // 图片
        mImgViewMsg = (ImageView) itemView.findViewById(R.id.imgview_msg);
        mImgViewMsg.setOnClickListener(this);

        // 播放视图
        mImgViewPlay = (ImageView) itemView.findViewById(R.id.imgview_play);

        mTextTime = (TextView) itemView.findViewById(R.id.text_time);
        mProgress = (ProgressBar) itemView.findViewById(R.id.progress_send);
        mImgViewFail = (ImageView) itemView.findViewById(R.id.imgview_fail);
    }

    /**
     * 填充相应的信息
     * @param data - Report模型
     */
    public void fillContent(ReportData data) {

        // 时间
        mTextTime.setText(data.getTime());

        // 文字信息
        if (data.getType() == ReportData.REPORT_TEXT) {
            mTextMsg.setText(data.getStringData());

            mTextMsg.setVisibility(View.VISIBLE);
            mImgViewMsg.setVisibility(View.GONE);

            if (mImgViewPlay != null) {
                mImgViewPlay.setVisibility(View.GONE);
            }
        }
        // 图片信息
        else if (data.getType() == ReportData.REPORT_IMAGE || data.getType() == ReportData.REPORT_VIDEO) {
            mImgViewMsg.setImageBitmap(data.getBitmapData());

            float scale = mContext.getResources().getDisplayMetrics().density;
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mImgViewMsg.getLayoutParams();
            layoutParams.width = (int) (data.getWidth() * scale /*+ mContext.getResources().getDimension(R.dimen.chat_bubble_image_horizontal_padding)*/);
            layoutParams.height = (int) (data.getHeight() * scale /*+ mContext.getResources().getDimension(R.dimen.chat_bubble_image_vertical_padding)*/);
            mImgViewMsg.setLayoutParams(layoutParams);

            mTextMsg.setVisibility(View.GONE);
            mImgViewMsg.setVisibility(View.VISIBLE);

            if (mImgViewPlay != null) {
                if (data.getType() == ReportData.REPORT_IMAGE) {
                    mImgViewPlay.setVisibility(View.GONE);
                } else {
                    mImgViewPlay.setVisibility(View.VISIBLE);
                }
            }
        }

        // 表示上传结果
        if (data.getStatus() == ReportData.STATUS_SUCCESS) {
            mProgress.setVisibility(View.GONE);
            mImgViewFail.setVisibility(View.GONE);
        }
        else if (data.getStatus() == ReportData.STATUS_FAILED) {
            mProgress.setVisibility(View.GONE);
            mImgViewFail.setVisibility(View.VISIBLE);
        }

        // 保存数据对象
        mData = data;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.imgview_msg:
                MainActivity activity = (MainActivity)mContext;
                activity.zoomImageFromThumb(view, mData);
                break;
        }
    }
}

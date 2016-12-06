package net.huray.bloodgc.logic;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import net.huray.bloodgc.model.BloodGlucose;

import java.util.List;

/**
 * Created by ahn on 2016-12-06.
 */

public class BloodGlucoseAdapter extends RecyclerView.Adapter<BloodGlucoseAdapter.GlucoseItemViewHolder> {
    List<BloodGlucose> mDataList;

    Context mContext;

    public BloodGlucoseAdapter(Context context) {
        mContext = context;
    }


    @Override
    public GlucoseItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }


    @Override
    public void onBindViewHolder(GlucoseItemViewHolder holder, int position) {
        if (mDataList != null) {
            holder.onBindViewHolder(mDataList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (mDataList == null) {
            return 0;
        } else {
            return mDataList.size();
        }
    }

    public void setDataList(List<BloodGlucose> dataList) {
        mDataList = dataList;
    }


    class GlucoseItemViewHolder extends RecyclerView.ViewHolder {

        public GlucoseItemViewHolder(View itemView) {
            super(itemView);
        }

        public void onBindViewHolder(BloodGlucose bloodGlucose) {

        }
    }
}

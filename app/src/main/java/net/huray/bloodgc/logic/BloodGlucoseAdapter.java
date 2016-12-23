package net.huray.bloodgc.logic;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

//import net.huray.bloodgc.model.BloodGlucose;

import net.huray.bloodgc.R;
import net.huray.bloodgc.model.BloodGlucose;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ahn on 2016-12-06.
 */

public class BloodGlucoseAdapter extends RecyclerView.Adapter<BloodGlucoseAdapter.GlucoseItemViewHolder> {

    private static final int SHOW_DATA_COUNT = 10;

    private static final int TYPE_ITEM = -1;
    private static final int TYPE_FOOTER = -3;

    private List<BloodGlucose> mDataList;
    private List<BloodGlucose> mShowList;
    private int mShowCount;
    private RecyclerViewClickListener mListener;

    private boolean isFooter;

    private Context mContext;

    public BloodGlucoseAdapter(Context context) {
        mContext = context;
    }

    @Override
    public GlucoseItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = 0;
        if (viewType == TYPE_FOOTER) {
            layout = R.layout.footer_list_item;
        } else {
            layout = R.layout.blood_glucose_list_item;
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        if (getFooterUse()) {
            return new GlucoseItemViewHolder(view, viewType);
        }else{
            return new GlucoseItemViewHolder(view, viewType, new ViewHolderClickListener() {
                @Override
                public void onViewHolderClick(int position) {
                    if(mListener != null && mDataList != null){
                        mListener.onItemClick(position);
                    }
                }
            });
        }
    }


    @Override
    public void onBindViewHolder(GlucoseItemViewHolder holder, int position) {
        if (holder.viewType == TYPE_FOOTER) {
            holder.onBindFooterViewHolder(needUpdate());
        } else if (holder.viewType == TYPE_ITEM) {
            if (getFooterUse()) {
                holder.onBindItemViewHolder(mShowList.get(position));
            } else {
                holder.onBindItemViewHolder(mDataList.get(position));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mDataList == null) {
            return 0;
        }
        if (getFooterUse()) {
            return mShowList.size() + 1;
        } else {
            return mDataList.size();
        }
    }

    public void setDataList(List<BloodGlucose> dataList) {
        mDataList = dataList;
        if (getFooterUse()) {
            if (mDataList != null) {
                initShowDataList();
            } else {
                mShowList = null;
            }
        }
    }

    private void initShowDataList() {
        if (mDataList != null && mShowList == null) {
            mShowList = new ArrayList<>();
            showDataChange(SHOW_DATA_COUNT);
        }
    }

    public void updateShowDataList() {
        if (mDataList != null && mDataList.size() > mShowList.size()) {
            showDataChange(SHOW_DATA_COUNT);
            notifyDataSetChanged();
        }
    }

    private void showDataChange(int count) {
        int maxCount = mShowCount + count;
        insertToNewShowData(maxCount);
    }

    private void insertToNewShowData(int loop) {
        while (mShowCount < loop) {
            if (mShowCount >= mDataList.size())
                break;
            mShowList.add(mDataList.get(mShowCount));
            mShowCount++;
        }
    }

    public void setFooterUse(boolean use) {
        isFooter = use;
    }

    private boolean getFooterUse() {
        return isFooter;
    }

    private boolean needUpdate() {
        if (mShowList.size() == mDataList.size())
            return false;

        return true;
    }

    @Override
    public int getItemViewType(int position) {
        if (isFooterPosition(position)) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    private boolean isFooterPosition(int position) {
        if (getFooterUse()) {
            int lastPosition = getItemCount() - 1;
            return getFooterUse() && position == lastPosition;
        } else {
            return false;
        }
    }

    public void setItemListener(RecyclerViewClickListener listener) {
        mListener = listener;
    }

    class GlucoseItemViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        ViewHolderClickListener mListener;
        int viewType;
        TextView mesureDate;
        TextView mesureValue;
        TextView mealTime;
        TextView loading;
        ProgressBar mProgressBar;

        GlucoseItemViewHolder(View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;

            if (viewType == TYPE_FOOTER) {
                loading = (TextView) itemView.findViewById(R.id.tv_loading);
                mProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            } else {
                mesureDate = (TextView) itemView.findViewById(R.id.tv_mesure_date);
                mesureValue = (TextView) itemView.findViewById(R.id.tv_mesure_value);
                mealTime = (TextView) itemView.findViewById(R.id.tv_meal_time);
            }
        }

        GlucoseItemViewHolder(View itemView, int viewType, @Nullable ViewHolderClickListener listener) {
            super(itemView);
            this.viewType = viewType;

            if (viewType == TYPE_FOOTER) {
                loading = (TextView) itemView.findViewById(R.id.tv_loading);
                mProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            } else {
                mListener = listener;
                if (mListener != null) {
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mListener.onViewHolderClick(getAdapterPosition());
                        }
                    });
                }
                mesureDate = (TextView) itemView.findViewById(R.id.tv_mesure_date);
                mesureValue = (TextView) itemView.findViewById(R.id.tv_mesure_value);
                mealTime = (TextView) itemView.findViewById(R.id.tv_meal_time);
            }
        }

        void onBindFooterViewHolder(boolean needUpdateData) {
            if (needUpdateData) {
                loading.setText(R.string.loading);
                mProgressBar.setVisibility(View.VISIBLE);
            } else {
                loading.setText(R.string.not_have_load_data);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }

        void onBindItemViewHolder(BloodGlucose bloodGlucose) {
            mesureDate.setText(bloodGlucose.getRecordDttm());
            mesureValue.setText(String.valueOf(bloodGlucose.getMeasure()));
            mealTime.setText(bloodGlucose.isAteFood() ? "식후" : "식전");

        }
    }
}

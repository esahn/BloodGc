package net.huray.bloodgc.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.huray.bloodgc.R;
import net.huray.bloodgc.logic.BloodGlucoseAdapter;
import net.huray.bloodgc.logic.RecyclerViewClickListener;
import net.huray.bloodgc.model.BloodGlucose;

import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class RecordFragment extends Fragment {
    RecyclerView mRecyclerView;
    BloodGlucoseAdapter mAdapter;


    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;

    private LinearLayoutManager mLayoutManager;
    List<BloodGlucose> mBloodGlucoses;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_record, container, false);
        initViews(rootView);

        return rootView;
    }

    private void initViews(View rootView) {
        Bundle extra = getArguments();
        boolean footerUse = extra.getBoolean("FooterUse");
        mAdapter = new BloodGlucoseAdapter(getActivity());
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_glucose_list);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter.setFooterUse(footerUse);
        mAdapter.setDataList(mBloodGlucoses);
        mAdapter.setItemListener(new RecyclerViewClickListener() {
            @Override
            public void onItemClick(int position) {
                BloodGlucose glucose = mBloodGlucoses.get(position);
                glucose.setAteFood(!glucose.isAteFood());
                mBloodGlucoses.set(position, glucose);
                setAdapterDataList(mBloodGlucoses);
            }
        });
        if (footerUse) {
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) //check for scroll down
                    {
                        visibleItemCount = mLayoutManager.getChildCount();
                        totalItemCount = mLayoutManager.getItemCount();
                        pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                        if (loading) {
                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                                loading = false;

                                Runnable run = new Runnable() {
                                    @Override
                                    public void run() {

                                        mAdapter.updateShowDataList();
                                        loading = true;
                                    }
                                };
                                Handler handler = new Handler();
                                handler.postDelayed(run, 2000);
                            }
                        }
                    }
                }
            });
        }
        mAdapter.notifyDataSetChanged();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void setDataList(List<BloodGlucose> dataList) {
        mBloodGlucoses = dataList;
    }

    public void setAdapterDataList(List<BloodGlucose> dataList) {
        mBloodGlucoses = dataList;
        mAdapter.setDataList(dataList);
        mAdapter.notifyDataSetChanged();
    }

}

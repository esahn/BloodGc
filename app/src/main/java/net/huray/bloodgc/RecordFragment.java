package net.huray.bloodgc;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.huray.bloodgc.logic.BloodGlucoseAdapter;

public class RecordFragment extends Fragment {
    RecyclerView mRecyclerView;
    BloodGlucoseAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_record, container, false);
        initViews(rootView);

        return rootView;
    }

    private void initViews(View rootView) {
        mAdapter = new BloodGlucoseAdapter(getActivity());
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_glucose_list);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
    }
}

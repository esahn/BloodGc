package net.huray.bloodgc.ui;

import android.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import net.huray.bloodgc.NFCApp;
import net.huray.bloodgc.R;
import net.huray.bloodgc.logic.BloodGlucoseAdapter;
import net.huray.bloodgc.model.BloodGlucose;

import java.util.ArrayList;
import java.util.List;

public class RecordListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.record_screen);
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_record_list);

        initViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return false;
    }

    private void initViews() {
        RecordFragment fragment = new RecordFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("FooterUse", true);
        fragment.setArguments(bundle);
        List<BloodGlucose> dataList = selectGlucoseList();

        if (dataList != null)
            fragment.setDataList(dataList);
        else
            Toast.makeText(getApplicationContext(), "저장된 데이터가 없습니다.", Toast.LENGTH_LONG).show();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.record_list_container, fragment).commit();

    }

    private List<BloodGlucose> selectGlucoseList() {
        List<BloodGlucose> glucoseList;
        if (NFCApp.getDataStore().select(BloodGlucose.class).get().firstOrNull() != null) {
            glucoseList = NFCApp.getDataStore().select(BloodGlucose.class).orderBy(BloodGlucose.RECORD_DTTM.desc()).get().toList();
            return glucoseList;
        } else {
            return null;
        }
    }
}

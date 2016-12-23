package net.huray.bloodgc.ui;

import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.huray.bloodgc.NFCApp;
import net.huray.bloodgc.R;
import net.huray.bloodgc.model.BloodGlucose;
import net.huray.bloodgc.nfc.NFCService;
import net.huray.bloodgc.util.ApiUtils;
import net.huray.bloodgc.widget.WidgetProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import io.requery.Persistable;
import io.requery.sql.EntityDataStore;


public class SyncBgmActivity extends AppCompatActivity {
    private static final TimeZone KOREA_TIME = TimeZone.getTimeZone("GMT+09:00");
    private static final String TAG = "DASH_BOARD_ACTIVITY";
    RecordFragment mFragment;
    Button mBtnSave;
    NfcAdapter mNfcAdapter; // NFC 어댑터
    PendingIntent mPendingIntent; // 수신받은 데이터가 저장된 인텐트

    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    List<BloodGlucose> mGlucoseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
            assert actionBar != null;
            actionBar.setTitle(R.string.bgm_connect_screen);
            actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_sync_bgm);
        initViews();
        initNfcAdapter();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent intent = new Intent(this, DashBoardActivity.class);
            startActivity(intent);
            finish();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, DashBoardActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        // NFC 태그 스캔으로 앱이 자동 실행되었을때
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()))
            // 인텐트에 포함된 정보를 분석해서 화면에 표시
            onNewIntent(getIntent());
    }

    @Override
    public void onPause() {
        super.onPause();
        // 앱이 종료될때 NFC 어댑터를 비활성화 한다
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }


    @Override
    public void onNewIntent(Intent intent) {

        NFCService nfcService = NFCService.getInstance();

        if (nfcService.getContext() == null) {

        } else {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            NfcV nfcV = null;
            try {
                nfcV = NfcV.get(tag);

                if (nfcV.isConnected())
                    nfcV.close();
            } catch (Exception e) {
                Log.d(TAG, ">>>>>>>>>>>>>>>>> NFC Tag is not nfcV : " + e.getMessage(), e);
            }

            if (nfcV != null) {
                nfcService.setTag(nfcV);
                try {
                    nfcService.connectTag();

                    new NfcReadThread().start();
                } catch (Exception e) {
                    Log.d(TAG, "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx : " + e.getMessage());
                    nfcService.sendErrorMsg("nfc_msg_e001");
                }
            } else {
                Log.d(TAG, ">>>>>>>>>>>>>> nfc : NFC태그를 찾을 수 없습니다.");
                nfcService.sendErrorMsg("nfc_msg_e003");
            }
        }

    }

    private void initViews() {
        mFragment = new RecordFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("FooterUse", false);
        mFragment.setArguments(bundle);
        mBtnSave = (Button) findViewById(R.id.btn_save_data);


        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertDataListToDB();
            }
        });

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.record_list_container, mFragment).commit();
    }

    private void initNfcAdapter() {
        NFCService.getInstance().init(this);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // NFC 어댑터가 null 이라면 칩이 존재하지 않는 것으로 간주
        if (mNfcAdapter == null) {
            Toast.makeText(this, "이 핸드폰은 NFC 기능을 지원하지 않습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        //mTextView.setText("Scan a NFC tag");

        // NFC 데이터 활성화에 필요한 인텐트를 생성
        Intent intent = new Intent(this, getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        // NFC 데이터 활성화에 필요한 인텐트 필터를 생성
        mFilters = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
        };
        mTechLists = new String[][]{new String[]{android.nfc.tech.NfcV.class.getName()}};
    }

    private void insertDataListToDB() {
        if (mGlucoseList != null) {
            NFCApp.getDataStore().insert(mGlucoseList);
            sendReceverToWidget();
            insertSuccess();
        } else {
            Log.d(TAG, "to insert data is null");
        }
    }

    private void insertSuccess() {
        mFragment.setAdapterDataList(null);
        setSaveButtonState(false);
        Toast.makeText(this, "저장이 완료되었습니다.", Toast.LENGTH_LONG).show();
        mGlucoseList = null;
        Intent intent = new Intent(this, DashBoardActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendReceverToWidget(){
        Intent intent = new Intent(this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        sendBroadcast(intent);
    }

    private BloodGlucose createFromBytes(byte[] data, int offset) {
        BloodGlucose glucose = new BloodGlucose();

        Integer year = 2000 + (data[3 + offset] & 0xff);
        Integer month = (data[4 + offset] & 0xff) - 1;
        Integer day = (data[5 + offset] & 0xff);
        Integer hourOfDay = (data[6 + offset] & 0xff);
        Integer minute = (data[7 + offset] & 0xff);
        Integer second = (data[8 + offset] & 0xff);


        Calendar cal = Calendar.getInstance(KOREA_TIME); // fixed timezone for generating consitent timestamps
        cal.clear();
        cal.set(year, month, day, hourOfDay, minute, second);


        int bs = ((data[10 + offset] & 0xff) * 256) + ((data[9 + offset] & 0xff));

        // check check-data
        if (bs > 20000) {
            bs -= 20000;
            //log.bsType = CurayContract.BsLogs.BsType.CONTROL_SOLUTION;
        }

        glucose.setAteFood(false);
        //log.period = CurayContract.BsLogs.PERIOD_AC;
        // check post cibum
        if (bs > 10000) {
            bs -= 10000;
            glucose.setAteFood(true);
//            log.period = CurayContract.BsLogs.PERIOD_PC;
        }

        glucose.setMeasure(bs);
        glucose.setRecordDttm(ApiUtils.formatDateTime(cal.getTimeInMillis()));
        Log.d(TAG, "year " + String.valueOf(year) + " " + " month " + String.valueOf(month + 1) + " " + "bs " + String.valueOf(bs));

        return glucose;
    }

    private void setSaveButtonState(boolean enable) {
        mBtnSave.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
    }

    private void hideTagText(){
        TextView hideText = (TextView)findViewById(R.id.please_tag_bgm);
        hideText.setVisibility(View.INVISIBLE);
    }

    private class NfcReadThread extends Thread {
        private static final int BLOCK_SIZE = 12;
        private static final int RESULT_SUCCESS = 0;
        private static final int RESULT_UNKNOWN = -10;

        private int result = RESULT_SUCCESS;

        @Override
        public void run() {
            NFCService nfcService = NFCService.getInstance();
            try {
                readData(nfcService);
            } catch (Exception e) {
                Log.d(TAG, ">>>>>>>>>>>>>> nfc communication error : " + e.getMessage());
                nfcService.sendErrorMsg("nfc_msg_e001");
                e.printStackTrace();
                result = RESULT_UNKNOWN;
            }

            try {
                nfcService.closeTag();
            } catch (Exception e) {
                Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>> NFC close Error : " + e.getMessage());
                result = RESULT_UNKNOWN;
            }


        }

        private void readData(NFCService nfcService) {
            nfcService.doTimeSync();

            if (!nfcService.doReadData()) {
//                return RESULT_ERROR_READ_DATA; // read-data error
            }

            if (!nfcService.chkReadData()) {
//                return RESULT_ERROR_CHECK_DATA; // check-data error
            } else {
//                LogUtils.LOGD(TAG, ">>>>>>>>>>>>>>> NFC CheckData OK!!");
                showData(nfcService.getNDEFMessage());
            }
//            String sn = nfcService.getSerialNumber();


            //return showData(sn, nfcService.getNDEFMessage());
        }

        private void showData(byte[] ndefMessage) {
            //EntityDataStore<Persistable> dataStore = NFCApp.getDataStore();
            List<BloodGlucose> readData = new ArrayList<>();
            for (int offset = 0; offset < ndefMessage.length; offset += BLOCK_SIZE) {

                readData.add(createFromBytes(ndefMessage, offset));
            }

            mGlucoseList = chkShowData(readData);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mGlucoseList != null) {
                        hideTagText();
                        setSaveButtonState(true);
                        mFragment.setAdapterDataList(mGlucoseList);
                    } else {
                        Toast.makeText(SyncBgmActivity.this, "새로 갱신된 데이터가 없습니다.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        /**
         * NFC에서 읽은 데이터랑 DB 데이터를 비교 후 DB에 없는 데이터만 가져옴
         *
         * @param readData NFC에서 읽어온 데이터
         * @return DB가 NULL 일 경우 넣은 매개변수를 그대로 리턴, 아니면 DB와 비교해서 없는 데이터만 리턴
         */
        private List<BloodGlucose> chkShowData(List<BloodGlucose> readData) {
            EntityDataStore<Persistable> dataStore = NFCApp.getDataStore();
            if (dataStore.select(BloodGlucose.class).get().firstOrNull() == null) {
                return readData;
            } else {
                List<BloodGlucose> returnData = new ArrayList<>();
                List<BloodGlucose> localData = dataStore.select(BloodGlucose.class).get().toList();
                for (BloodGlucose glucose : readData) {
                    if (localData.contains(glucose)) {
                        continue;
                    }
                    returnData.add(glucose);
                }
                return returnData.size() > 0 ? returnData : null;
            }
        }

//            Log.d(TAG, "Save Success!!");
//            if (mGlucoseList.size() > 0)
//                dataStore.insert(mGlucoseList);
    }

//        private int showData(String serialNumber, byte[] ndefMessage) {
//            long start = System.currentTimeMillis();
//
//            if (ndefMessage == null) {
//                return RESULT_ERROR_WRITE_DATA;
//            }
//
//            boolean invalidDataFound = false;
//            boolean logDttmErrorSuspected = false;
//            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
//
//            // set uri NOT to notify changes
//            //   Uri bsUri = CurayContract.addCallerIsSyncAdapterParameter(BsLogs.CONTENT_URI);
//
//            try {
//                for (int offset = 0; offset < ndefMessage.length; offset += BLOCK_SIZE) {
//
//                    NfcBsLog log = NfcBsLog.createFromBytes(NfcActivity.this, ndefMessage, offset);
//                    if (log != null) {
//                        final ContentValues values = log.toContentValues();
//                        values.put(SyncBaseColumns.IS_DIRTY, 1);
//
//                        if (!log.isValid()) {
//                            values.put(BsLogs.IS_INVALID, 1);
//                            values.put(BsLogs.NOTE, generateErrorNote(ndefMessage, offset));
//                            invalidDataFound = true;
//
//                            if (Config.DEBUG) LogUtils.LOGW(TAG, "invalid data found");
//                        }
//
//                        if (!logDttmErrorSuspected && log.isLogDttmErrorSuspected()) {
//                            logDttmErrorSuspected = true;
//
//                            if (Config.DEBUG) LogUtils.LOGW(TAG, "log date error suspected");
//                        }
//
//                        if (offset + BLOCK_SIZE >= ndefMessage.length) {
//                            // set uri to notify changes (at the last iteration)
//                            bsUri = BsLogs.CONTENT_URI;
//                        }
//
//                        operations.add(ContentProviderOperation.newInsert(bsUri)
//                                .withValues(values)
//                                .build());
//
//                        if (Config.DEBUG) {
//                            LogUtils.LOGW(TAG, "new record -- " + log.bs + " @ " + log.logDttm);
//                        }
//                    } else { // conflict
//                        if (Config.DEBUG) LogUtils.LOGW(TAG, "conflicted create ts");
//                    }
//                } // end for
//
//                if (Config.DEBUG)
//                    LogUtils.LOGI("nfc_parse", (System.currentTimeMillis() - start) + "");
//
//                getContentResolver().applyBatch(CurayContract.CONTENT_AUTHORITY, operations);
//                return invalidDataFound ? RESULT_SUCCESS_WITH_INVAID_DATA :
//                        logDttmErrorSuspected ? RESULT_SUCCESS_DATE_ERROR_SUSPECTED :
//                                RESULT_SUCCESS;
//
//            } catch (RemoteException | OperationApplicationException | ParseException e) {
//                e.printStackTrace();
//            }
//
//            return RESULT_ERROR_WRITE_DATA;
//        }
//
//
//    }

}


package net.huray.bloodgc.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.nfc.tech.NfcV;
import android.util.Log;

import net.huray.bloodgc.util.ByteUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class NFCService {

	private static final String TAG = "NFCService";

	private Activity context;
	private NFCDevice nfcDevice = null;
	private PendingIntent mPendingIntent;

	private byte[] WriteSingleBlockAnswer = null;
	private long cpt = 0;
	private long cpt1 = 0;
	private byte[] dataToWrite = new byte[4];
	private int errCtlFlag = 0;
	private byte[] fullNdefMessage = null;
	private byte[] finalMessage = null;
	private int numberOfBlockToRead = 0;
	private int numberOfBlockToRead1 = 0;
	private int numberOfBlockToRead2 = 0;
	private String sNDEFMessage = "nothing";

	private boolean g_timeSyncFlag = false;
	private byte[] timeDataToWrite = new byte[4];
	private byte[] timeDataToWrite1 = new byte[4];
	private byte[] addressStart = null;

	//added by DSKIM
	public byte[] nfcSerialNumber = new byte[12];
	public String SerialNumStr;

	//	public int gRotateFlagValue = 0;
	public int gReadSavePointValue = 0;
	public String gLastReadPointValue ="";


	//데이터 저장 시작 address 지정
	private final byte[] startDataAddress = new byte[]{(byte)0x00, (byte)0x00};

	private static class InstanceGenerator {
		private static NFCService INSTANCE = new NFCService(); 
	}

	public NFCService() {
		if (InstanceGenerator.INSTANCE != null) {
			throw new IllegalStateException("Already instantiated");
		}
	}

	public static NFCService getInstance() {
		return InstanceGenerator.INSTANCE;
	}

	public void init(Activity activity) {

		//Log.d(TAG, ">>>>>>>>>>>>>> nfcService init");
		context = activity;

	}


	public Boolean getTimeSyncStatus()
	{
		return g_timeSyncFlag;
	}

	public void setTimeSyncStatus(Boolean b)
	{
		g_timeSyncFlag = b;
	}



	public Context getContext() {
		return context;
	}


	public void sendErrorMsg(String msg) {
		if(context != null) {
			//			context.dispatchStatusEventAsync(msg, "nfc_receive_error");
			
		}
	}

	public void sendErrorMsg1() {
		if(context != null){
			//			context.dispatchStatusEventAsync(100 + "/" + 10, "nfc_receive_progress");
		}

	}

	public void setTag(NfcV tag) {
		nfcDevice = new NFCDevice();
		nfcDevice.setCurrentTag(tag);
		try {
			if(context != null) {
				//				context.dispatchStatusEventAsync("nfc_start", "nfc_start");
			} else {
				//Log.d(TAG, ">>>>>>>>>>>>>>>> context is Null");
			}
		} catch (Exception e) {
			LogUtils.LOGE(TAG, e.getMessage());
		}
	}

	public void connectTag() throws Exception {

		nfcDevice.getCurrentTag().connect();
	}

	public void closeTag() throws Exception {
		nfcDevice.getCurrentTag().close();
	}

	public NFCDevice getNFCDevice() {
		return nfcDevice;
	}

	/**
	 * 시스템 정보 취득
	 * @return
	 */
	public byte[] getSystemInfoFromTag() {

		byte[] response = new byte[] { (byte) 0x01 };
		byte[] GetSystemInfoFrame = new byte[2]; 

		// to know if tag's addresses are coded on 1 or 2 byte we consider 2  
		// then we wait the response if it's not good we trying with 1
		nfcDevice.setBasedOnTwoBytesAddress(true);	 

		GetSystemInfoFrame = new byte[] { (byte) 0x0A, (byte) 0x2B };	//1st flag=1

		for(int h=0; h<=1;h++)
		{
			try 
			{
				response = nfcDevice.getCurrentTag().transceive(GetSystemInfoFrame);
				if (response[0] == (byte) 0x00) 
				{
					//Used for DEBUG : //Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(GetSystemInfoFrame));
					if (h == 0)
						nfcDevice.setBasedOnTwoBytesAddress(true);	//1st (flag=1) = 2 add bytes (M24LR64 FREEDOM2)
					else
						nfcDevice.setBasedOnTwoBytesAddress(false);	//2nd (flag=0) = 1 add bytes (LRI M24LR04 FREEDOM1 !)
					h = 2;// to get out of the loop
				}
			}
			catch (Exception e)
			{
				//Used for DEBUG : //Log.i("Exception","Get System Info Exception " + e.getMessage());
				//Used for DEBUG : //Log.i("NFCCOmmand", "Response Get System Info " + Helper.ConvertHexByteArrayToString(response));
				nfcDevice.setBasedOnTwoBytesAddress(false);
			}

			//Used for DEBUG : //Log.i("NFCCOmmand", "Response Get System Info " + Helper.ConvertHexByteArrayToString(response));
			GetSystemInfoFrame = new byte[] { (byte) 0x02, (byte) 0x2B }; //2nd flag=0
		}
		return response;
	}


	//dskim: 실제 데이터를 수신한 만큼의 사이즈 변수
	int realsize = 0;
	//	int lastReadSavePoint = 0;
	//	byte [] finalReadSavePoint = new byte[2];
	int finalReadSavePoint = 0;

	int MaxCount = 0;
	int nCount = 0;

	//byte[] nfcSerialNumber = null;

	StringBuffer asciiSerialNumber = new StringBuffer(12);

	public String getSerialNumber() {
		//byte[] nfcSerialNumber = null;
		nfcSerialNumber = null;
		cpt = 0;
		/*
		while ((nfcSerialNumber == null || nfcSerialNumber[0] == 1) && cpt < 10) {
			nfcSerialNumber = Send_several_ReadSingleBlockCommands(
					nfcDevice.getCurrentTag(),new byte[]{0x00,0x06}, new byte[]{0x00,0x0c});
			cpt ++;
		}
		 */
		while ((nfcSerialNumber == null || nfcSerialNumber[0] == 1) && cpt < 10) {
			nfcSerialNumber = SendReadMultipleBlockCommandCustom2(nfcDevice.getCurrentTag(), new byte[]{0x00,0x06},
					new byte[]{0x00,0x04}, nfcDevice);
			cpt ++;
		}
		/*
		fullNdefMessage = SendReadMultipleBlockCommandCustom2(nfcDevice.getCurrentTag(), startDataAddress,
				ByteUtil.ConvertIntTo2bytesHexaFormat(numberOfBlockToRead), nfcDevice);
		 */
		SerialNumStr = ByteUtil.ConvertHexByteArrayToString(nfcSerialNumber);
        Log.d(TAG, ">>>>>>>>>>>>>> nfcSerialNumber : " + SerialNumStr);
		
		return SerialNumStr;
	}

	//	byte[] MaxCountBlock = new byte[2];



	public boolean doReadData() {

		if(context != null) {
			//comment by DSKIM: 총 읽어야 할 데이터 개수를 FLEX에 전달
			//			context.dispatchStatusEventAsync(500 + "/" + 135, "nfc_receive_progress");
		}

		boolean msgIsEmpty = false;
		byte[] resultBlock0 = new byte[4];		//CC Field 값 저정
		byte[] resultBlock1 = new byte[16];	//Block 13 ~ 16 저장 값
		cpt = 0;
		resultBlock0 = null;

		/*modified by DSKIM: unformatted tag로 변경 2014.04.23
		//while ((resultBlock0 == null || resultBlock0[0] == 1)&& cpt<1500)
		while ((resultBlock0 == null || resultBlock0[0] == 1) && cpt < 10) {
			resultBlock0 = SendReadSingleBlockCommand(nfcDevice.getCurrentTag(), new byte[]{0x00,0x00});
			cpt ++;
			//Used for DEBUG : Log.v("CPT ", " CPT Read Block 0 ===> " + String.valueOf(cpt));
		}
		 */

		fullNdefMessage = null;

		//modified by DSKIM: unformatted tag로 변경 2014.04.23
		//2013-05-09 modified by DSKIM: Read Block 변경
		//		if(resultBlock0[0] == (byte)0x00 && resultBlock0[1] == (byte)0xE1) {
		//NDEF TAG Format valid
		cpt = 0;
		resultBlock1 = null;

		/* modified by DSKIM: unformatted tag로 변경 2014.04.23
			boolean boolMultipleReadCC3 = false;
			//CC3 bit0 analysis for Read Single /Read Multiple option
			if((resultBlock0[4] & (byte)0x01) == (byte)0x01) boolMultipleReadCC3 = true;
			else boolMultipleReadCC3 = false;
		 */		
		boolean boolMultipleReadCC3 = true;

		//LogUtils.LOGE(TAG, "resultBlock0: " + ByteUtil.ConvertHexByteArrayToString(resultBlock0));

		byte[] nCountBlock = new byte[2];
		byte[] SavePointBlock = new byte[2]; //Save Point 값 2bytes
		byte[] MaxCountBlock = new byte[2]; //Max n Count 값  2bytes

		byte[] GlucoseErrMsgBlock = new byte[2]; //Glucose Error Message 값  2bytes
		byte[] BgmMsgBlock = new byte[2]; //BGM Message 값  2bytes

		byte snChecksum = (byte)0x00; //Serial number Checksum

		//int numberOfBlockToRead = 0;


		while ((resultBlock1 == null || resultBlock1[0] == 1)&& cpt < 10) {
			resultBlock1 = SendReadSingleBlockCommand(nfcDevice.getCurrentTag(), new byte[]{0x00,0x0d});
			cpt ++;
			//Used for DEBUG : Log.v("CPT ", " CPT Read Block 0 ===> " + String.valueOf(cpt));
		}
		//BY DSKIM
		nCountBlock[0]  += (byte)resultBlock1[1];
		nCountBlock[1]  += (byte)resultBlock1[2];
		//Save Point READ
		SavePointBlock[0] += (byte)resultBlock1[3];
		SavePointBlock[1] += (byte)resultBlock1[4];
		LogUtils.LOGE(TAG, "now_i_want_information " + ByteUtil.ConvertHexByteArrayToString(resultBlock1));
		cpt = 0;
		resultBlock1 = null;
		while ((resultBlock1 == null || resultBlock1[0] == 1) && cpt < 10) {
			resultBlock1 = SendReadSingleBlockCommand(nfcDevice.getCurrentTag(), new byte[]{0x00,0x0e});
			cpt ++;
			//Used for DEBUG : Log.v("CPT ", " CPT Read Block 0 ===> " + String.valueOf(cpt));
		}

        LogUtils.LOGE(TAG, "resultBlock1: " + ByteUtil.ConvertHexByteArrayToString(resultBlock1));

		//Max n Count READ
		MaxCountBlock[0] += (byte)resultBlock1[1];
		MaxCountBlock[1] += (byte)resultBlock1[2];
		/*
			byte tempChecksum = (byte)(nfcSerialNumber[1] ^ nfcSerialNumber[2] ^ nfcSerialNumber[3] ^ nfcSerialNumber[4] ^
									   nfcSerialNumber[5] ^ nfcSerialNumber[6] ^ nfcSerialNumber[7] ^ nfcSerialNumber[8] ^
									   nfcSerialNumber[9] ^ nfcSerialNumber[10] ^ nfcSerialNumber[11] ^ nfcSerialNumber[12]);

			LogUtils.LOGE(TAG, "Serial Number :" + ByteUtil.ConvertHexByteArrayToString(nfcSerialNumber));
			LogUtils.LOGE(TAG, "real CHK :" + ByteUtil.ConvertHexByteToString(resultBlock1[4]));
			LogUtils.LOGE(TAG, "temp CHK:" + ByteUtil.ConvertHexByteToString(tempChecksum));

			asciiSerialNumber.setLength(0);

			for(int k = 1; k < 13; k++) {
				asciiSerialNumber.append(ByteUtil.convertHexToString(ByteUtil.ConvertHexByteToString(nfcSerialNumber[k])));
			}


			if(resultBlock1[4] == tempChecksum){
				LogUtils.LOGE(TAG, "시리얼번호를 확인하였습니다.");
			} else {
				LogUtils.LOGE(TAG, "시리얼번호를 확인하지 못하였습니다.");
				errCtlFlag = 7;
				fullNdefMessage = null;
				return false;
			}
			LogUtils.LOGE(TAG, "Serial Number :" + asciiSerialNumber);
		 */
		cpt = 0;
		resultBlock1 = null;
		while ((resultBlock1 == null || resultBlock1[0] == 1)&& cpt < 10) {
			resultBlock1 = SendReadSingleBlockCommand(nfcDevice.getCurrentTag(), new byte[]{0x00,0x10});
			cpt ++;
			//Used for DEBUG : Log.v("CPT ", " CPT Read Block 0 ===> " + String.valueOf(cpt));
		}

		GlucoseErrMsgBlock[0] += (byte)resultBlock1[1];
		GlucoseErrMsgBlock[1] += (byte)resultBlock1[2];
		BgmMsgBlock[0] += (byte)resultBlock1[3];
		BgmMsgBlock[1] += (byte)resultBlock1[4];

        LogUtils.LOGE(TAG, "nCountBlock: " + ByteUtil.ConvertHexByteArrayToString(nCountBlock));
        LogUtils.LOGE(TAG, "SavePointBlock: " + ByteUtil.ConvertHexByteArrayToString(SavePointBlock));
        LogUtils.LOGE(TAG, "MaxCountBlock: " + String.valueOf(ByteUtil.Convert2bytesHexaFormatToInt(MaxCountBlock)));
        LogUtils.LOGE(TAG, "GlucoseErrMsgBlock: " + String.valueOf(ByteUtil.Convert2bytesHexaFormatToInt(GlucoseErrMsgBlock)));
        LogUtils.LOGE(TAG, "BgmMsgBlock: " + String.valueOf(ByteUtil.Convert2bytesHexaFormatToInt(BgmMsgBlock)));


		MaxCount = ByteUtil.Convert2bytesHexaFormatToInt(MaxCountBlock);
		nCount = ByteUtil.Convert2bytesHexaFormatToInt(nCountBlock);

		WriteSingleBlockAnswer = null;


		if(ByteUtil.Convert2bytesHexaFormatToInt(BgmMsgBlock) == 1){
			errCtlFlag = 6;
			fullNdefMessage = null;
			return false;
		}

		if(ByteUtil.Convert2bytesHexaFormatToInt(GlucoseErrMsgBlock) > 0){
			errCtlFlag = ByteUtil.Convert2bytesHexaFormatToInt(GlucoseErrMsgBlock);
			fullNdefMessage = null;
			return false;
		}


		if(msgIsEmpty == false) { 
			cpt = 0;
			//if(numberOfBlockToRead <32 || boolMultipleReadCC3==false)
			if(boolMultipleReadCC3 == false){/*
					while ((fullNdefMessage == null || fullNdefMessage[0] == 1) && cpt < 10 && numberOfBlockToRead != 0){
						//fullNdefMessage = NFCCommand.Send_several_ReadSingleBlockCommands(dataDevice.getCurrentTag(),new byte[]{0x00,0x00}, resultBlockX, dataDevice);
						cpt++;
					}*/
			} else {
				//modified by DSKIM: Save Point를 참조하지 않고 nCount를 참조하도록 변경 2014.05.08
				//numberOfBlockToRead = ByteUtil.Convert2bytesHexaFormatToInt(SavePointBlock) * 3 + 17;
				if(ByteUtil.Convert2bytesHexaFormatToInt(nCountBlock) > 500) numberOfBlockToRead = 500 * 3 + 17;
				else numberOfBlockToRead = ByteUtil.Convert2bytesHexaFormatToInt(nCountBlock) * 3 + 17;

                LogUtils.LOGE(TAG, "numberOfBlockToRead: " + String.valueOf(numberOfBlockToRead));

				if(numberOfBlockToRead > 0) {
					fullNdefMessage = new byte[numberOfBlockToRead * 4];
					//0xFF로 초기화한다.
					for(int i = 0; i < numberOfBlockToRead * 4; i++){
						fullNdefMessage[i] = (byte)0xFF;
					}

				} else {
					fullNdefMessage = new byte[1];
					fullNdefMessage[0] = (byte)0x00;
				}
                Log.d(TAG, "xxxxxxxxxxxxxx");
				//while ((fullNdefMessage == null || fullNdefMessage[0] == 1) && cpt < 10 && numberOfBytesToRead != 0){
				//while ((fullNdefMessage == null || fullNdefMessage[0] == 1) && cpt < 10){
				while ((fullNdefMessage == null || fullNdefMessage[0] == (byte)0xFF) && cpt < 10){
					fullNdefMessage = SendReadMultipleBlockCommandCustom2(nfcDevice.getCurrentTag(), startDataAddress,
							ByteUtil.ConvertIntTo2bytesHexaFormat(numberOfBlockToRead), nfcDevice);
					/*
						if(fullNdefMessage[0] == (byte)0x0a && fullNdefMessage.length == 1){ //통신이 끊어지면, 1byte의 "0x0a" 값을 받는다.
							Log.d(TAG, ">>>>>>>>>>>>>>>> fullNdefMessage--->" + ByteUtil.ConvertHexByteArrayToString(fullNdefMessage));
							break;
						} 
					 */
					cpt++;
				}

				////SN CRC 확인
				byte tempChecksum = (byte)(fullNdefMessage[25] ^ fullNdefMessage[26] ^ fullNdefMessage[27] ^ fullNdefMessage[28] ^
						fullNdefMessage[29] ^ fullNdefMessage[30] ^ fullNdefMessage[31] ^ fullNdefMessage[32] ^
						fullNdefMessage[33] ^ fullNdefMessage[34] ^ fullNdefMessage[35] ^ fullNdefMessage[36]);

				//LogUtils.LOGE(TAG, "Serial Number :" + ByteUtil.ConvertHexByteArrayToString(nfcSerialNumber));
                LogUtils.LOGE(TAG, "Serial Number :" + ByteUtil.ConvertHexByteToString(fullNdefMessage[25]));
                LogUtils.LOGE(TAG, "real CHK :" + ByteUtil.ConvertHexByteToString(fullNdefMessage[60]));
                LogUtils.LOGE(TAG, "temp CHK:" + ByteUtil.ConvertHexByteToString(tempChecksum));

				asciiSerialNumber.setLength(0);

				for(int k = 25; k < 37; k++) {
					asciiSerialNumber.append(ByteUtil.convertHexToString(ByteUtil.ConvertHexByteToString(fullNdefMessage[k])));
				}

				if(fullNdefMessage[60] == tempChecksum){
                    LogUtils.LOGE(TAG, "시리얼번호를 확인하였습니다.");
				} else {
                    LogUtils.LOGE(TAG, "시리얼번호를 확인하지 못하였습니다.");
					errCtlFlag = 7;
					fullNdefMessage = null;
					return false;
				}
				////
			} 
		}
		//		}/* //modified by DSKIM: unformatted tag로 변경 2014.04.23
		/*	
		} else {
			fullNdefMessage = new byte[1];
			fullNdefMessage[0] = (byte)0x00;
		}*/
        Log.d(TAG, "unformatted tag로 변경하였음!!!");

		return true;
	}

	public boolean chkReadData() {
		boolean succeed = false;
		if(fullNdefMessage == null){ //NDEF 메시지를 읽지 못함
            Log.d(TAG, ">>>>>>>>>>>>>>>>>>>> NFC : fullNdefMessage is null");
			/*
			Toast toast = Toast.makeText(getApplicationContext(), "미터를 폰과 다시 접촉하여 주세요.", Toast.LENGTH_SHORT);
			toast.show();
			 */
			if(errCtlFlag == 1){
                Log.d(TAG, ">>>>>>>>>>>>>>>>>>>> NFC : 이미 사용한 시험지를 재사용했을 때의 에러 메시지");
				//				context.dispatchStatusEventAsync("nfc_msg_d001", "nfc_receive_error");
			} else if(errCtlFlag == 2){
                Log.d(TAG, ">>>>>>>>>>>>>>>>>>>> NFC : 혈액 주입 표시가 나타나기 전에 혈액이 주입되었을 때의 에러 메시지");
				//				context.dispatchStatusEventAsync("nfc_msg_d002", "nfc_receive_error");
			} else if(errCtlFlag == 3){
                Log.d(TAG, ">>>>>>>>>>>>>>>>>>>> NFC : 측정 가능 온도 범위를 벗어났을 때의 에러 메시지");
				//				context.dispatchStatusEventAsync("nfc_msg_d003", "nfc_receive_error");
			} else if(errCtlFlag == 4){
                Log.d(TAG, ">>>>>>>>>>>>>>>>>>>> NFC : 혈액의 점도가 지나치게 높거나 혈액량이 충분하지 않았을 때의 에러 메시지");
				//				context.dispatchStatusEventAsync("nfc_msg_d004", "nfc_receive_error");
			} else if(errCtlFlag == 5){
                Log.d(TAG, ">>>>>>>>>>>>>>>>>>>> NFC : CareSens N 혈당시험지가 아닌 다른 혈당시험지를 사용 하였거나 시험지가 잘못 삽입되었을 때 또는 완전히 삽입되지 않았을 때의 에러 메시지");
				//				context.dispatchStatusEventAsync("nfc_msg_d005", "nfc_receive_error");
			} else if(errCtlFlag == 6){
                Log.d(TAG, ">>>>>>>>>>>>>>>>>>>> NFC : 측정기에 문제가 있을 때 나타나는 에러 메시지");
				//				context.dispatchStatusEventAsync("nfc_msg_d006", "nfc_receive_error");
			} else if(errCtlFlag == 7){
                Log.d(TAG, ">>>>>>>>>>>>>>>>>>>> NFC : 미터를 다시 접촉해 주세요");
				//				context.dispatchStatusEventAsync("nfc_msg_d009", "nfc_receive_error");
			} else {
                Log.d(TAG, ">>>>>>>>>>>>>>>>>>>> NFC : fullNdefMessage is null");
				//				context.dispatchStatusEventAsync("nfc_msg_d007", "nfc_receive_error");
			}
			succeed = false;
		} else if (fullNdefMessage.length == 1 && fullNdefMessage[0] == (byte)0x00){ //NDEF 메시지가 없음
            Log.d(TAG, ">>>>>>>>>>>>>>>>>>>> NFC : 수신할 데이터가 없습니다!!!");
			//			context.dispatchStatusEventAsync("nfc_msg_d008", "nfc_receive_error");
			succeed = false;
		} else if (fullNdefMessage.length < (numberOfBlockToRead * 4)){ //읽어야 할 총 데이터 개수가 부족함 -> 데이터 수신 중, NFC 통신 해제됨
            Log.d(TAG, ">>>>>>>>>>>>>>>>>>>> NFC : 미터를 폰과 다시 접촉하여 주세요.");
			//			context.dispatchStatusEventAsync("nfc_msg_d009", "nfc_receive_error");
			succeed = false;		
			//		} else if (fullNdefMessage.length == (numberOfBlockToRead * 4)){ //NDEF 메시지가 있음 */
		} else if (fullNdefMessage.length != 0 && fullNdefMessage.length >= 12){ //NDEF 메시지가 있음
			//Log.d(TAG, "fullNdefMessage--------------- 1: " + ByteUtil.ConvertHexByteArrayToString(fullNdefMessage));
			//			sNDEFMessage = NDEFMessages.ConvertNDEF_ByteArrayToString_Adapted(fullNdefMessage);

			if(!sNDEFMessage.equals("No Ndef Message Found")){
                Log.d(TAG, ">>>>>>>>>>>>>>>>>>>> NFC : 데이터 수신 완료!!!");
				//				context.dispatchStatusEventAsync("데이터 수신 완료.", "nfc_receive_error");

				finalMessage = new byte[fullNdefMessage.length - 69];
				System.arraycopy(fullNdefMessage, 69, finalMessage, 0, finalMessage.length);
				//				Log.d(TAG, "===========" + ByteUtil.ConvertHexByteToString(finalMessage[0]));
			
//				int [] byte2Int = new int[7];
				
				
//				for(int i = 0; i < finalMessage.length; i += 12) {
//					byte2Int[0] = 2000 + (finalMessage[3 + i] & 0xff);
//					byte2Int[1] = (finalMessage[4 + i] & 0xff);
//					byte2Int[2] = (finalMessage[5 + i] & 0xff);
//					byte2Int[3] = (finalMessage[6 + i] & 0xff);
//					byte2Int[4] = (finalMessage[7 + i] & 0xff);
//					byte2Int[5] = (finalMessage[8 + i] & 0xff);
//					byte2Int[6] = ((finalMessage[10 + i] & 0xff) * 256) + ((finalMessage[9 + i] & 0xff));
//					Log.v(TAG, byte2Int[0]+"-"+byte2Int[1]+"-"+byte2Int[2]+" "+
//							byte2Int[3]+":"+byte2Int[4]+":"+byte2Int[5]+"     [" + byte2Int[6]+"]");
//				}
				
				succeed = true;
				/*
				for(int i = 0; i < fullNdefMessage.length; i += 12) {
					byte2Int[0] = 2000 + (fullNdefMessage[3 + i] & 0xff);
					byte2Int[1] = (fullNdefMessage[4 + i] & 0xff);
					byte2Int[2] = (fullNdefMessage[5 + i] & 0xff);
					byte2Int[3] = (fullNdefMessage[6 + i] & 0xff);
					byte2Int[4] = (fullNdefMessage[7 + i] & 0xff);
					byte2Int[5] = (fullNdefMessage[8 + i] & 0xff);
					byte2Int[6] = ((fullNdefMessage[10 + i] & 0xff) * 256) + ((fullNdefMessage[9 + i] & 0xff));

				}
				 */

				//modified by DSKIM: 데이터 전송 완료 시, Serial number 데이터도 함께 넘김
				//context.dispatchStatusEventAsync(nfcDevice.getUid() , "nfc_receive");
				//				context.dispatchStatusEventAsync(nfcDevice.getUid() + "/" +  asciiSerialNumber, "nfc_receive");
			} else {
                Log.d(TAG, ">>>>>>>>>>>>>>> NFC No Tag?");
			}

		} else {
			sNDEFMessage = "No NDEF message";
		}
        Log.d(TAG, ">>>>>>>>>>>>>>>>>>>> NFC sNDEFMessage Result : " + sNDEFMessage);
		
		return succeed;
	}

	public void doTimeSync() {
		cpt = 0;
		int mCurrYear, mCurrMonth, mCurrDay, mCurrHour, mCurrMin, mCurrSec;
		Calendar currCal = new GregorianCalendar();

		mCurrYear = currCal.get(Calendar.YEAR);
		mCurrMonth = currCal.get(Calendar.MONTH);
		mCurrDay = currCal.get(Calendar.DAY_OF_MONTH);
		mCurrHour = currCal.get(Calendar.HOUR_OF_DAY);
		mCurrMin = currCal.get(Calendar.MINUTE);
		mCurrSec = currCal.get(Calendar.SECOND);

		if(!nfcDevice.getCurrentTag().isConnected()) {
			try {
				nfcDevice.getCurrentTag().connect();
			} catch (IOException e) {
                LogUtils.LOGE(TAG, ">>>>>>> nfc tag connect error", e);
			}
		}

		byte[] GetSystemInfoAnswer = SendGetSystemInfoCommandCustom(nfcDevice.getCurrentTag());

		if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
		{

			////String startAddressString = valueBlock.getText().toString(); 
			String startAddressString = "0003";
			startAddressString = ByteUtil.castHexKeyboard(startAddressString);
			startAddressString = ByteUtil.FormatStringAddressStart(startAddressString, nfcDevice.getMemorySize());
            Log.d(TAG, ">>>>>>>>>>>> nfc Memory size : " + nfcDevice.getMemorySize());
			////valueBlock.setText(startAddressString.toUpperCase());
			addressStart = ByteUtil.ConvertStringToHexBytes(startAddressString);

            Log.d(TAG, ">>>>>>>>>>>> startAddressString : " + startAddressString);

			String valueBlock1 = Integer.toString(mCurrYear - 2000, 16);//1byte 단위로 저장하기 때문에, 2000이라는 값을 뺀다.
			String valueBlock2 = Integer.toString(mCurrMonth + 1, 16); //Calendar의 month 값에 1을 더해야 원래의 month 값이다.
			String valueBlock3 = Integer.toString(mCurrDay, 16);
			String valueBlock4 = Integer.toString(mCurrHour, 16);

			String valueBlock5 = Integer.toString(mCurrMin, 16);
			String valueBlock6 = Integer.toString(mCurrSec, 16);
			String valueBlock7 = Integer.toString(Integer.parseInt(valueBlock1, 16)^ Integer.parseInt(valueBlock2, 16)^ Integer.parseInt(valueBlock3, 16)^
					Integer.parseInt(valueBlock4, 16)^ Integer.parseInt(valueBlock5, 16)^ Integer.parseInt(valueBlock6, 16), 16);
			String valueBlock8 = Integer.toString(1, 16);

			//자릿 수가 2자리가 아니면, 2자리로 맞춰준다.
			if(valueBlock1.length() < 2)
				valueBlock1 = "0" + valueBlock1;
			if(valueBlock2.length() < 2)
				valueBlock2 = "0" + valueBlock2;
			if(valueBlock3.length() < 2)
				valueBlock3 = "0" + valueBlock3;
			if(valueBlock4.length() < 2)
				valueBlock4 = "0" + valueBlock4;

            Log.d(TAG, ">>>>>>>> YEAR " + valueBlock1);
            Log.d(TAG, ">>>>>>>> MONTH " + valueBlock2);
            Log.d(TAG, ">>>>>>>> DAY " + valueBlock3);
            Log.d(TAG, ">>>>>>>> HOUR " + valueBlock4);

			if(valueBlock5.length() < 2)
				valueBlock5 = "0" + valueBlock5;
			if(valueBlock6.length() < 2)
				valueBlock6 = "0" + valueBlock6;
			if(valueBlock7.length() < 2)
				valueBlock7 = "0" + valueBlock7;
			if(valueBlock8.length() < 2)
				valueBlock8 = "0" + valueBlock8;

            Log.d(TAG, ">>>>>>>> MINUTE " + valueBlock5);
            Log.d(TAG, ">>>>>>>> SECOND " + valueBlock6);
            Log.d(TAG, ">>>>>>>> CHS " + valueBlock7);
            Log.d(TAG, ">>>>>>>> FLAG " + valueBlock8);


			String valueBlockTotal = "";
			valueBlockTotal += valueBlock1 + valueBlock2;
			byte[] valueBlockWrite = ByteUtil.ConvertStringToHexBytes(valueBlockTotal);

            Log.d(TAG, ">>>>>>>>> valueBlockTotal : " + valueBlockTotal);

			timeDataToWrite[0] = valueBlockWrite[0];
			timeDataToWrite[1] = valueBlockWrite[1];

			valueBlockTotal = "";
			valueBlockTotal += valueBlock3 + valueBlock4;
			valueBlockWrite = ByteUtil.ConvertStringToHexBytes(valueBlockTotal);

            Log.d(TAG, ">>>>>>>>> valueBlockTotal : " + valueBlockTotal);

			timeDataToWrite[2] = valueBlockWrite[0];
			timeDataToWrite[3] = valueBlockWrite[1];

			//
			valueBlockTotal = "";
			valueBlockTotal += valueBlock5 + valueBlock6;
			valueBlockWrite = ByteUtil.ConvertStringToHexBytes(valueBlockTotal);

            Log.d(TAG, ">>>>>>>>> valueBlockTotal : " + valueBlockTotal);

			timeDataToWrite1[0] = valueBlockWrite[0];
			timeDataToWrite1[1] = valueBlockWrite[1];

			valueBlockTotal = "";
			valueBlockTotal += valueBlock7 + valueBlock8;
			valueBlockWrite = ByteUtil.ConvertStringToHexBytes(valueBlockTotal);

            Log.d(TAG, ">>>>>>>>> valueBlockTotal : " + valueBlockTotal);

			timeDataToWrite1[2] = valueBlockWrite[0];
			timeDataToWrite1[3] = valueBlockWrite[1];
		}

		WriteSingleBlockAnswer = null;
        Log.d(TAG, ">>>>>> GetSystemInfoAnswer : " + ByteUtil.ConvertHexByteArrayToString(GetSystemInfoAnswer));
		if(DecodeGetSystemInfoResponse(GetSystemInfoAnswer))
		{
			cpt = 0;
			while ((WriteSingleBlockAnswer == null || WriteSingleBlockAnswer[0] == 1) && cpt <= 10)
			{
                Log.d(TAG, ">>>>>>>>>> while cpt : " + cpt);
				WriteSingleBlockAnswer = SendWriteSingleBlockCommand(nfcDevice.getCurrentTag(), addressStart, timeDataToWrite);

				WriteSingleBlockAnswer = SendWriteSingleBlockCommand(nfcDevice.getCurrentTag(), 
						ByteUtil.ConvertStringToHexBytes("0004"), timeDataToWrite1);
				cpt++;
			}
		}

		if (WriteSingleBlockAnswer==null)
		{
			//			Toast.makeText(getApplicationContext(), "ERROR Write (No tag answer) ", Toast.LENGTH_SHORT).show();
			//			Toast.makeText(getApplicationContext(), "Please, place your phone near the meter", Toast.LENGTH_SHORT).show();
			//			context.dispatchStatusEventAsync("nfc_msg_t002", "nfc_receive_error");
            Log.d(TAG, ">>>>>>>>>>> 미터를 가까이..!!!");
		}

		else if(WriteSingleBlockAnswer[0]==(byte)0x01)
		{
			//   			Toast.makeText(getApplicationContext(), "ERROR Write ", Toast.LENGTH_SHORT).show();
			//	buttonWrite.performClick();
            Log.d(TAG, ">>>>>>>>>>> WriteSingleBlockAnswer 0x01");
		}
		else if(WriteSingleBlockAnswer[0]==(byte)0xFF)
		{
			//			Toast.makeText(getApplicationContext(), "ERROR Write ", Toast.LENGTH_SHORT).show();
			//	buttonWrite.performClick();
            Log.d(TAG, ">>>>>>>>>>> WriteSingleBlockAnswer 0xff");
		}    		
		else if(WriteSingleBlockAnswer[0]==(byte)0x00)
		{
			/*
			try {
				Thread.sleep(1000); //1초대기

				byte[] resultflag0 = new byte[4];

				resultflag0 = SendReadSingleBlockCommand(nfcDevice.getCurrentTag(), new byte[]{0x00,0x04});
				Log.i("resultflag0", ByteUtil.ConvertHexByteArrayToString(resultflag0));

				if(resultflag0[4] == (byte)0x00) {
					g_timeSyncFlag = false;
					Log.d(TAG, ">>>>>>>>>>> nfc 시간동기화 완료");
					context.dispatchStatusEventAsync("nfc_msg_t003", "nfc_receive_error");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			 */
			g_timeSyncFlag = false;
            Log.d(TAG, ">>>>>>>>>>> nfc 시간동기화 완료");

			//context.dispatchStatusEventAsync("nfc_msg_t003", "nfc_receive_error");
			try {
				Thread.sleep(1000); //1초대기

				//				context.dispatchStatusEventAsync("nfc_msg_t003", "nfc_receive_error");

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//

		}
		else
		{
			//   			Toast.makeText(getApplicationContext(), "Write ERROR ", Toast.LENGTH_SHORT).show();
            Log.d(TAG, ">>>>>>>>>>> WriteSingleBlockAnswer else");
		} 
	}

	public byte[] getNDEFMessage() {
		//return fullNdefMessage;
		return finalMessage;
	}

	/**
	 * the function Decode the tag answer for the GetSystemInfo command
	 * the function fills the values (dsfid / afi / memory size / icRef /..)
	 * in the myApplication class. return true if everything is ok.
	 * @param GetSystemInfoResponse
	 * @return
	 */
	public boolean DecodeGetSystemInfoResponse(byte[] GetSystemInfoResponse){
		if(GetSystemInfoResponse[0] == (byte) 0x00 && GetSystemInfoResponse.length >= 12){
			NFCDevice ma = nfcDevice;
			String uidToString = "";
			byte[] uid = new byte[8];
			// change uid format from byteArray to a String
			for (int i = 1; i <= 8; i++){
				uid[i - 1] = GetSystemInfoResponse[10 - i];
				uidToString += ByteUtil.ConvertHexByteToString(uid[i - 1]);
			}

			// ***** TECHNO ******
			ma.setUid(uidToString);
			if(uid[0] == (byte) 0xE0) ma.setTechno("ISO 15693");
			else if(uid[0] == (byte) 0xE0) ma.setTechno("ISO 14443");
			else ma.setTechno("Unknown techno");

			// ***** MANUFACTURER ****
			if(uid[1] == (byte) 0x02) ma.setManufacturer("STMicroelectronics");
			else if(uid[1] == (byte) 0x04) ma.setManufacturer("NXP");
			else if(uid[1] == (byte) 0x07) ma.setManufacturer("Texas Instrument");
			else ma.setManufacturer("Unknown manufacturer");

			// **** PRODUCT NAME *****
			if(uid[2] >= (byte) 0x04 && uid[2] <= (byte) 0x07){
				ma.setProductName("LRI512");
				ma.setMultipleReadSupported(false);
				ma.setMemoryExceed2048bytesSize(false);
			} else if(uid[2] >= (byte) 0x14 && uid[2] <= (byte) 0x17){
				ma.setProductName("LRI64");
				ma.setMultipleReadSupported(false);
				ma.setMemoryExceed2048bytesSize(false);
			} else if(uid[2] >= (byte) 0x20 && uid[2] <= (byte) 0x23){
				ma.setProductName("LRI2K");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(false);
			} else if(uid[2] >= (byte) 0x28 && uid[2] <= (byte) 0x2B){
				ma.setProductName("LRIS2K");
				ma.setMultipleReadSupported(false);
				ma.setMemoryExceed2048bytesSize(false);
			} else if(uid[2] >= (byte) 0x2C && uid[2] <= (byte) 0x2F){
				ma.setProductName("M24LR64");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if(uid[2] >= (byte) 0x40 && uid[2] <= (byte) 0x43){
				ma.setProductName("LRI1K");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(false);
			} else if(uid[2] >= (byte) 0x44 && uid[2] <= (byte) 0x47){
				ma.setProductName("LRIS64K");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if(uid[2] >= (byte) 0x48 && uid[2] <= (byte) 0x4B){
				ma.setProductName("M24LR01E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(false);
			} else if(uid[2] >= (byte) 0x4C && uid[2] <= (byte) 0x4F){
				ma.setProductName("M24LR16E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if(uid[2] >= (byte) 0x50 && uid[2] <= (byte) 0x53){
				ma.setProductName("M24LR02E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(false);
			} else if(uid[2] >= (byte) 0x54 && uid[2] <= (byte) 0x57){
				ma.setProductName("M24LR32E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if(uid[2] >= (byte) 0x58 && uid[2] <= (byte) 0x5B){
				ma.setProductName("M24LR04E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if(uid[2] >= (byte) 0x5C && uid[2] <= (byte) 0x5F){
				ma.setProductName("M24LR64E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if(uid[2] >= (byte) 0x60 && uid[2] <= (byte) 0x63){
				ma.setProductName("M24LR08E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if(uid[2] >= (byte) 0x64 && uid[2] <= (byte) 0x67){
				ma.setProductName("M24LR128E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if(uid[2] >= (byte) 0x6C && uid[2] <= (byte) 0x6F){
				ma.setProductName("M24LR256E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if(uid[2] >= (byte) 0xF8 && uid[2] <= (byte) 0xFB){
				ma.setProductName("detected product");
				ma.setBasedOnTwoBytesAddress(true);
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else{
				ma.setProductName("Unknown product");
				ma.setMultipleReadSupported(false);
				ma.setMemoryExceed2048bytesSize(false);
			}

			// *** DSFID ***
			ma.setDsfid(ByteUtil.ConvertHexByteToString(GetSystemInfoResponse[10]));

			// *** AFI ***
			ma.setAfi(ByteUtil.ConvertHexByteToString(GetSystemInfoResponse[11]));

			// *** MEMORY SIZE ***
			if(ma.isBasedOnTwoBytesAddress()){
				String temp = new String();
				temp += ByteUtil.ConvertHexByteToString(GetSystemInfoResponse[13]);
				temp += ByteUtil.ConvertHexByteToString(GetSystemInfoResponse[12]);
				ma.setMemorySize(temp);
			} else ma.setMemorySize(ByteUtil.ConvertHexByteToString(GetSystemInfoResponse[12]));

			// *** BLOCK SIZE ***
			if(ma.isBasedOnTwoBytesAddress())ma.setBlockSize(ByteUtil.ConvertHexByteToString(GetSystemInfoResponse[14]));
			else ma.setBlockSize(ByteUtil.ConvertHexByteToString(GetSystemInfoResponse[13]));

			// *** IC REFERENCE ***
			if(ma.isBasedOnTwoBytesAddress())ma.setIcReference(ByteUtil.ConvertHexByteToString(GetSystemInfoResponse[15]));
			else ma.setIcReference(ByteUtil.ConvertHexByteToString(GetSystemInfoResponse[14]));

			return true;
		} else
			return false;
	}

	//***********************************************************************/
	//* the function send an ReadSingle command (0x0A 0x20) || (0x02 0x20) 
	//* the argument myTag is the intent triggered with the TAG_DISCOVERED
	//* example : StartAddress {0x00, 0x02}  NbOfBlockToRead : {0x04}
	//* the function will return 04 blocks read from address 0002
	//* According to the ISO-15693 maximum block read is 32 for the same sector
	//***********************************************************************/
	private byte[] SendReadSingleBlockCommand (NfcV nfcvTag, byte[] StartAddress)
	{
		byte[] response = new byte[] {(byte) 0x0A}; 
		byte[] ReadSingleBlockFrame;

		if(nfcDevice.isBasedOnTwoBytesAddress()) {
			//			Log.d(TAG, ">>>>>>>>>>>>>>> nfc : isBasedOnTwoBytesAddress is true");
			ReadSingleBlockFrame = new byte[]{(byte) 0x0A, (byte) 0x20, StartAddress[1], StartAddress[0]};
		} else {
			//			Log.d(TAG, ">>>>>>>>>>>>>>> nfc : isBasedOnTwoBytesAddress is false");
			ReadSingleBlockFrame = new byte[]{(byte) 0x02, (byte) 0x20, StartAddress[1]};
		}

//        Log.d(TAG, "isBasedOnTwoBytesAddress : " + nfcDevice.isBasedOnTwoBytesAddress());

		int errorOccured = 1;
		while(errorOccured != 0)
		{
			try
			{
				response = nfcvTag.transceive(ReadSingleBlockFrame);
				if(response[0] == (byte) 0x00)
				{
					errorOccured = 0;
					//Used for DEBUG : //Log.i("NFCCOmmand", "SENDED Frame : " + ByteUtil.ConvertHexByteArrayToString(ReadSingleBlockFrame));
				}
			}
			catch(Exception e)
			{
                Log.d(TAG, ">>>>>>>>>>>>>> " + e.getMessage());
				errorOccured++;
				//Used for DEBUG : //Log.i("NFCCOmmand", "Response Read Single Block" + ByteUtil.ConvertHexByteArrayToString(response));
				if(errorOccured == 2)
				{
					//Used for DEBUG : //Log.i("Exception","Exception " + e.getMessage());
					return response;
				}
			}
		}
		//Used for DEBUG : //Log.i("NFCCOmmand", "Response Read Sigle Block" + ByteUtil.ConvertHexByteArrayToString(response));
		return response;
	}

	private byte[] Send_several_ReadSingleBlockCommands (NfcV myTag, byte[] StartAddress, byte[] bytNbBytesToRead)
	{
		long cpt =0;

		int NbBytesToRead = ByteUtil.Convert2bytesHexaFormatToInt(bytNbBytesToRead);		
		int iNbOfBlockToRead = (NbBytesToRead / 4);
		byte[] FinalResponse = new byte[iNbOfBlockToRead*4 + 1];		

		byte[] bytAddress = new byte[2];

		//int intAddress = 0;
		int intAddress = ByteUtil.Convert2bytesHexaFormatToInt(StartAddress);

		int index = 0;

		byte[] temp = new byte[5];

		//boucle for(int i=0;i<iNbOfBlockToRead; i++)
		do
		{			
			bytAddress = ByteUtil.ConvertIntTo2bytesHexaFormat(intAddress);

			temp = null;
			while (temp == null || temp[0] == 1 && cpt <= 10)
			{
				temp = SendReadSingleBlockCommand (myTag, new byte[]{(byte)bytAddress[0],(byte)bytAddress[1]});
				cpt ++;
			}
			cpt =0;				

			if (temp[0] == 0)
			{
				if(index==0)
				{
					for(int j=0;j<=4;j++)
						FinalResponse[j] = temp[j];
				}
				else 
				{
					for(int j=1;j<=4;j++)
						FinalResponse[(index*4)+j] = temp[j];
				}
			}
			else
			{
				if(index==0)
				{
					for(int j=0;j<=4;j++)
						FinalResponse[j] = (byte)0xFF;
				}
				else 
				{
					for(int j=1;j<=4;j++)
						FinalResponse[(index*4)+j] = (byte)0xFF;
				}
			}

			intAddress++;
			index++;

		} while(index < iNbOfBlockToRead);

		return FinalResponse;
	}

	//***********************************************************************/
	//* the function send an WriteSingle command (0x0A 0x21) || (0x02 0x21) 
	//* the argument myTag is the intent triggered with the TAG_DISCOVERED
	//* example : StartAddress {0x00, 0x02}  DataToWrite : {0x04 0x14 0xFF 0xB2}
	//* the function will write {0x04 0x14 0xFF 0xB2} at the address 0002
	//***********************************************************************/
	private byte[] SendWriteSingleBlockCommand (NfcV nfcvTag, byte[] StartAddress, byte[] DataToWrite)
	{
		byte[] response = new byte[] {(byte) 0xFF}; 
		byte[] WriteSingleBlockFrame;

		if(nfcDevice.isBasedOnTwoBytesAddress())
			WriteSingleBlockFrame = new byte[]{(byte) 0x0A, (byte) 0x21, StartAddress[1], StartAddress[0], DataToWrite[0], DataToWrite[1], DataToWrite[2], DataToWrite[3]};
		else
			WriteSingleBlockFrame = new byte[]{(byte) 0x02, (byte) 0x21, StartAddress[1], DataToWrite[0], DataToWrite[1], DataToWrite[2], DataToWrite[3]};

		int errorOccured = 1;
		while(errorOccured != 0)
		{
			try
			{
				nfcvTag.close();
				nfcvTag.connect();
				response = nfcvTag.transceive(WriteSingleBlockFrame);
				if(response[0] == (byte) 0x00)
				{
					errorOccured = 0;						 
					//Used for DEBUG : //Log.i("*******", "**SUCCESS** Write Data " + DataToWrite[0] +" "+ DataToWrite[1] +" "+ DataToWrite[2] +" "+ DataToWrite[3] + " at address " +  (byte)StartAddress[0] +" "+ (byte)StartAddress[1]);
				}
			}
			catch(Exception e)
			{
                LogUtils.LOGE(TAG, ">>>>>>>> error", e);
				errorOccured++;
				//Used for DEBUG : //Log.i("NFCCOmmand", "Write Single block command  " + errorOccured);
				if(errorOccured == 2)
				{
					//Used for DEBUG : //Log.i("Exception","Exception " + e.getMessage());
					//Used for DEBUG : //Log.i("WRITE", "**ERROR WRITE SINGLE** at address " +  ByteUtil.ConvertHexByteArrayToString(StartAddress));
					return response;
				}
			}
		}
		return response;
	}

	//***********************************************************************/
	//* the function send an Get System Info command (0x02 0x2B) 
	//* the argument myTag is the intent triggered with the TAG_DISCOVERED
	//***********************************************************************/
	private byte[] SendGetSystemInfoCommandCustom (NfcV nfcvTag)
	{
		byte[] response = new byte[] { (byte) 0x01 };
		byte[] GetSystemInfoFrame = new byte[2]; 

		// to know if tag's addresses are coded on 1 or 2 byte we consider 2  
		// then we wait the response if it's not good we trying with 1
		nfcDevice.setBasedOnTwoBytesAddress(true);	 

		GetSystemInfoFrame = new byte[] { (byte) 0x0A, (byte) 0x2B };	//1st flag=1

		for(int h=0; h<=1;h++)
		{
			try 
			{
				nfcvTag.close();
				nfcvTag.connect();
				response = nfcvTag.transceive(GetSystemInfoFrame);
				nfcvTag.close();
				if (response[0] == (byte) 0x00) 
				{
					//Used for DEBUG : //Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(GetSystemInfoFrame));
					if (h == 0)
						nfcDevice.setBasedOnTwoBytesAddress(true);	//1st (flag=1) = 2 add bytes (M24LR64 FREEDOM2)
					else
						nfcDevice.setBasedOnTwoBytesAddress(false);	//2nd (flag=0) = 1 add bytes (LRI M24LR04 FREEDOM1 !)
					h = 2;// to get out of the loop
				}
			}
			catch (Exception e)
			{
				//Used for DEBUG : //Log.i("Exception","Get System Info Exception " + e.getMessage());
				//Used for DEBUG : //Log.i("NFCCOmmand", "Response Get System Info " + Helper.ConvertHexByteArrayToString(response));
				nfcDevice.setBasedOnTwoBytesAddress(false);
			}

			//Used for DEBUG : //Log.i("NFCCOmmand", "Response Get System Info " + Helper.ConvertHexByteArrayToString(response));
			GetSystemInfoFrame = new byte[] { (byte) 0x02, (byte) 0x2B }; //2nd flag=0
		}
		return response;
	}

	//***********************************************************************/
	//* the function send an ReadSingle Custom command (0x0A 0x20) || (0x02 0x20) 
	//* the argument myTag is the intent triggered with the TAG_DISCOVERED
	//* example : StartAddress {0x00, 0x02}  NbOfBlockToRead : {0x04}
	//* the function will return 04 blocks read from address 0002
	//* According to the ISO-15693 maximum block read is 32 for the same sector
	//***********************************************************************/

	//public static byte[] SendReadMultipleBlockCommandCustom2 (Tag myTag, byte[] StartAddress, byte[] bNbOfBlockToRead, NFCDevice ma)
	private static byte[] SendReadMultipleBlockCommandCustom2 (NfcV nfcvTag, byte[] StartAddress, byte[] bNbOfBlockToRead, NFCDevice ma)
	{

		boolean checkCorrectAnswer = true;

		int iNbOfBlockToRead = ByteUtil.Convert2bytesHexaFormatToInt(bNbOfBlockToRead);
		int iNumberOfSectorToRead;
		int iStartAddress = ByteUtil.Convert2bytesHexaFormatToInt(StartAddress);
		int iAddressStartRead = (iStartAddress/32)*32;
		if(iNbOfBlockToRead%32 == 0)
		{
			iNumberOfSectorToRead = (iNbOfBlockToRead/32);
		}
		else
		{
			iNumberOfSectorToRead = (iNbOfBlockToRead/32)+1;
		}
		byte[] bAddressStartRead = ByteUtil.ConvertIntTo2bytesHexaFormat(iAddressStartRead);

		byte[] AllReadDatas = new byte[((iNumberOfSectorToRead*128)+1)];
		byte[] FinalResponse = new byte[(iNbOfBlockToRead*4)+1] ;

		String sMemorySize = ma.getMemorySize();
		sMemorySize = ByteUtil.StringForceDigit(sMemorySize,4);
		byte[] bLastMemoryAddress = ByteUtil.ConvertStringToHexBytes(sMemorySize);

		//Loop needed for number of sector o read
		for(int i=0; i<iNumberOfSectorToRead;i++)
		{
			byte[] temp = new byte[33]; 

			int incrementAddressStart0 = (bAddressStartRead[0]+i/8)  ;									//Most Important Byte
			int incrementAddressStart1 = (bAddressStartRead[1]+i*32) - (incrementAddressStart0*256);	//Less Important Byte


			if(bAddressStartRead[0]<0)
				incrementAddressStart0 = ((bAddressStartRead[0]+256)+i/8);	

			if(bAddressStartRead[1]<0)
				incrementAddressStart1 = ((bAddressStartRead[1]+256)+i*32) - (incrementAddressStart0*256);


			if(incrementAddressStart1 > bLastMemoryAddress[1] && incrementAddressStart0 > bLastMemoryAddress[0])
			{


			}
			else
			{
				temp = null;	
				temp = SendReadMultipleBlockCommand (nfcvTag, new byte[]{(byte)incrementAddressStart0,(byte)incrementAddressStart1},(byte)0x1F, ma);

				if (temp[0] != 0x00)
					checkCorrectAnswer = false;

				// if any error occurs during 
				if(temp[0] == (byte)0x01)
				{
					return temp;
				}
				else
				{
					// to construct a response with first byte = 0x00
					if(i==0)
					{
						for(int j=0;j<=128;j++)
						{
							AllReadDatas[j] = temp[j];
						}
					}
					else 
					{
						for(int j=1;j<=128;j++)
						{
							AllReadDatas[(i*128)+j] = temp[j];
						}
					}
				}
			}
		}

		int iNbBlockToCopyInFinalReponse = ByteUtil.Convert2bytesHexaFormatToInt(bNbOfBlockToRead);		 
		int iNumberOfBlockToIgnoreInAllReadData = 4*(ByteUtil.Convert2bytesHexaFormatToInt(StartAddress)%32);

		for(int h=1; h <= iNbBlockToCopyInFinalReponse*4 ; h++)
		{
			FinalResponse[h] = AllReadDatas[h + iNumberOfBlockToIgnoreInAllReadData];
		}

		if (checkCorrectAnswer == true)
			FinalResponse[0] = AllReadDatas[0];
		else
			FinalResponse[0] = (byte)0xAF;

		return FinalResponse;
	}

	private static byte[] SendReadMultipleBlockCommand (NfcV nfcvTag, byte[] StartAddress, byte NbOfBlockToRead, NFCDevice ma)
	{
		byte[] response = new byte[] {(byte) 0x01}; 
		byte[] ReadMultipleBlockFrame;

		if(ma.isBasedOnTwoBytesAddress())
			ReadMultipleBlockFrame = new byte[]{(byte) 0x0A, (byte) 0x23, StartAddress[1], StartAddress[0], NbOfBlockToRead};
		else
			ReadMultipleBlockFrame = new byte[]{(byte) 0x02, (byte) 0x23, StartAddress[1], NbOfBlockToRead};

		//Used for DEBUG : Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(ReadMultipleBlockFrame));

		int errorOccured = 1;
		while(errorOccured != 0)
		{
			try
			{
				//NfcV nfcvTag = NfcV.get(myTag);
				nfcvTag.close();
				nfcvTag.connect();
				response = nfcvTag.transceive(ReadMultipleBlockFrame);
				if(response[0] == (byte) 0x00 || response[0] == (byte) 0x01)//response 01 = error sent back by tag (new Android 4.2.2) or BC
				{
					errorOccured = 0;
					//Used for DEBUG : Log.i("NFCCOmmand", "SENDED Frame : " + Helper.ConvertHexByteArrayToString(ReadMultipleBlockFrame));

				}
			}
			catch(Exception e)
			{
				errorOccured++;
				//Used for DEBUG : Log.i("NFCCOmmand", "SendReadMultipleBlockCommand errorOccured " + errorOccured);
				if(errorOccured == 3)
				{
					//Used for DEBUG : Log.i("Exception","Exception " + e.getMessage());
					//Used for DEBUG : Log.i("NFCCOmmand", "Error when try to read from address  " +  (byte)StartAddress[0] +" "+ (byte)StartAddress[1]);
					return response;
				}
			}
		}
		//Used for DEBUG : Log.i("NFCCOmmand", "Response Read Multiple Block" + Helper.ConvertHexByteArrayToString(response));	
		return response;
	}

}

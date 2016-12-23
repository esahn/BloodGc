package net.huray.bloodgc.nfc;

import android.nfc.tech.NfcV;

public class NFCDevice {
	private NfcV currentTag;
	private String uid;
	private String techno;
	private String manufacturer;
	private String productName;
	private String dsfid;
	private String afi;
	private String memorySize;
	private String blockSize;
	private String icReference;
	private boolean basedOnTwoBytesAddress;
	private boolean MultipleReadSupported;
	private boolean MemoryExceed2048bytesSize;
	
	public NfcV getCurrentTag() {
		return currentTag;
	}
	public void setCurrentTag(NfcV currentTag) {
		this.currentTag = currentTag;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getTechno() {
		return techno;
	}
	public void setTechno(String techno) {
		this.techno = techno;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getDsfid() {
		return dsfid;
	}
	public void setDsfid(String dsfid) {
		this.dsfid = dsfid;
	}
	public String getAfi() {
		return afi;
	}
	public void setAfi(String afi) {
		this.afi = afi;
	}
	public String getMemorySize() {
		return memorySize;
	}
	public void setMemorySize(String memorySize) {
		this.memorySize = memorySize;
	}
	public String getBlockSize() {
		return blockSize;
	}
	public void setBlockSize(String blockSize) {
		this.blockSize = blockSize;
	}
	public String getIcReference() {
		return icReference;
	}
	public void setIcReference(String icReference) {
		this.icReference = icReference;
	}
	public boolean isBasedOnTwoBytesAddress() {
		return basedOnTwoBytesAddress;
	}
	public void setBasedOnTwoBytesAddress(boolean basedOnTwoBytesAddress) {
		this.basedOnTwoBytesAddress = basedOnTwoBytesAddress;
	}
	public boolean isMultipleReadSupported() {
		return MultipleReadSupported;
	}
	public void setMultipleReadSupported(boolean multipleReadSupported) {
		MultipleReadSupported = multipleReadSupported;
	}
	public boolean isMemoryExceed2048bytesSize() {
		return MemoryExceed2048bytesSize;
	}
	public void setMemoryExceed2048bytesSize(boolean memoryExceed2048bytesSize) {
		MemoryExceed2048bytesSize = memoryExceed2048bytesSize;
	}
	
	@Override
	public String toString() {
		return "uid : " + uid + ", techno : " + techno + ", manufacturer : " + manufacturer
				+ ", productName : " + productName + ", dsfid: " + dsfid + ", afi : " + afi
				+ ", memorySize : " + memorySize + ", blockSize : " + blockSize + ", icReference : " + icReference
				+ ", basedOnTwoBytesAddress : " + basedOnTwoBytesAddress
				+ ", MultipleReadSupported : " + MultipleReadSupported
				+ ", MemoryExceed2048bytesSize : " + MemoryExceed2048bytesSize;
	}
	
}

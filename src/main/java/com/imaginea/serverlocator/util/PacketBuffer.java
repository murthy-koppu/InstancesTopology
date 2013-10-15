package com.imaginea.serverlocator.util;


public class PacketBuffer {
	private int packetPosition = -1;
	private byte[] packet = null;
	
	public byte[] getPacket() {
		return packet;
	}

	private int getPacketPosition() throws Exception{
		if(packetPosition >= packet.length)
			throw new Exception();
		return packetPosition++;
	}
	
	public PacketBuffer(byte[] data){
		packet = data;
		packetPosition = 0;
	}
	
	public static boolean isInvalidInt(final byte[] inDatas){
		boolean isInputInt = true;
		for(int i=0; i<inDatas.length && isInputInt ; i++)
			isInputInt = (inDatas[i] < 58 && inDatas[i] > 47) ? true:false;
		
		return isInputInt;
	}	
	
	public int readInt() throws Exception{
		byte dataAsInt =  packet[getPacketPosition()];
		byte dataAsInt1 = packet[getPacketPosition()];
		if(!isInvalidInt(new byte[]{dataAsInt,dataAsInt1}))
			throw new Exception();	
		
		return (dataAsInt & 0xff | ((dataAsInt1 & 0xff)<<8));
	}
	
	public String readString() throws Exception{
		
		byte byteData = 0;
		String strOPData = "";
		while((byteData = packet[getPacketPosition()]) != 0)
			strOPData += (char)byteData;
		
		return strOPData;
	}
	
	public void skipByte(int len){
		packetPosition += len;
	}
	
	public byte readByte() throws Exception{
		return packet[getPacketPosition()];
	}
	
	public void skipString() throws Exception{
		while(packet[getPacketPosition()] != 0)
			;
	}
	
	
	
}

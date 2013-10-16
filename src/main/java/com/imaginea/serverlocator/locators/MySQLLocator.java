package com.imaginea.serverlocator.locators;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.imaginea.serverlocator.util.ApplicationConstants;
import com.imaginea.serverlocator.util.PacketBuffer;
import com.imaginea.serverlocator.util.ServersEnum;
import com.imaginea.serverlocator.util.Utils;

public class MySQLLocator implements ServerLocator,ApplicationConstants{

	PacketBuffer bufferPacket = null;
	static Logger log = Logger.getLogger(MySQLLocator.class);
	
	@Override
	public ServerProperties parseToServerProp(InetAddress iNetAddr, int port, boolean isLimitedTimeOut) {
			log.debug("********** Entered into MySQLLocator --> parseToServerProp() ********");
			try {
				Socket socket = null;
				int connectionTimeOut = isLimitedTimeOut ? MYSQL_SERVER_MIN_TIME_OUT_PERIOD : MYSQL_SERVER_MAX_TIME_OUT_PERIOD;
				try{
					socket = Utils.getClientSocket(iNetAddr, port, connectionTimeOut);
					DataInputStream dInStream = Utils.getDataInStreamFromServer(socket);
					bufferPacket = getDataPacket(dInStream);
					if(bufferPacket != null){
						log.debug("MYSql DB Response "+Arrays.toString(bufferPacket.getPacket()));						
					}else{
						return null;
					}
				}catch(IOException e){
					log.debug("Unable to get DataInputStream",e);
					return null;
				}finally{
					if(socket != null){
						socket.close();
					}						
				}
				
				bufferPacket.skipByte(1);
				byte protocolVersion = bufferPacket.readByte();
				if(protocolVersion < 0){
					log.error("Parsed data identifies error result");
					return null;						 
				}
				String version = bufferPacket.readString();
				log.debug("Parsed version of MySQL is"+version);
				if(!isValidVersion(version)){
					log.debug("Version data from DataStream validation failed");
					return null;
				}
				
				ServerProperties serverProp = new ServerProperties();
				serverProp.setServerName(ServersEnum.MY_SQL.toString());
				serverProp.setVersion(version);
				
				return serverProp;
			} catch (Exception e) {
				log.error("Unable to parse socket output as MySQL response"+e);
				return null;
			}
	}
	
		
	private boolean isValidVersion(String version){
		log.debug("********** Entered into MySQLLocator --> isValidVersion() ********");
		byte[] versionBytes = version.getBytes();
		for(byte versionByte: versionBytes){
			if((!(versionByte > 47 && versionByte < 58) && versionByte != 46))
				return false;
		}
		return true;
	}
	
	private PacketBuffer getDataPacket(DataInputStream dInStream) throws IOException {
		log.debug("********** Entered into MySQLLocator --> getDataPacket() ********");
		byte[] packetLengthInBytes = new byte[3];
		try {
			Utils.readBytesFromStream(dInStream, packetLengthInBytes, 0, 3);
		} catch (Exception e) {
			return null;
		}
		if(PacketBuffer.isInvalidInt(packetLengthInBytes)){
			log.debug("Unable to parse socket output as MySQL response");
			return null;
		}
		
		int packetLength = (packetLengthInBytes[0] & 0xff) |
		((packetLengthInBytes[1] & 0xff) << 8) |
		((packetLengthInBytes[2] & 0xff) << 16);
		byte[] bytePacket = new byte[packetLength+1];
		log.debug("Packet data read from stream is "+ packetLength);
		int noOfPacktetBytes = dInStream.read(bytePacket, 0, packetLength);
		log.debug("Byte data from DataStream is "+ bytePacket);
		if(noOfPacktetBytes != packetLength){
			log.debug("Unable to parse socket output as MySQL response");
			return null;
		}			
		return  new PacketBuffer(bytePacket);
	}
	
}

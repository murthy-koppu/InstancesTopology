package com.imaginea.serverlocator.locators;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.imaginea.serverlocator.util.ApplicationConstants;
import com.imaginea.serverlocator.util.PacketBuffer;
import com.imaginea.serverlocator.util.ServersEnum;
import com.imaginea.serverlocator.util.Utils;

public class OracleDBLocator implements ServerLocator,ApplicationConstants{
	static Logger log = Logger.getLogger(OracleDBLocator.class);
	private final static byte[] inBufferHdr = new byte[34];
	private final static int HEADER_LENGTH = 34;
	private byte[] addressData;
	private byte[] bufferData;
	
	static{
		 int sduOrtdu = 32767;
		 inBufferHdr[4] = 1;
		 inBufferHdr[8] = 1;
		 inBufferHdr[9] = 52;
		 inBufferHdr[10] = 1;
		 inBufferHdr[11] = 44;
		 inBufferHdr[12] = 12;
		 inBufferHdr[13] = 65;
		 
		 inBufferHdr[14] = ((byte)(sduOrtdu / 256));
		 inBufferHdr[15] = ((byte)(sduOrtdu % 256));
		 inBufferHdr[16] = ((byte)(sduOrtdu / 256));
		 inBufferHdr[17] = ((byte)(sduOrtdu % 256));
		 
		 inBufferHdr[18] = 79;
		 inBufferHdr[19] = -104;
		 inBufferHdr[22] = 0;
		 inBufferHdr[23] = 1;
		 inBufferHdr[27] = 34;
		 
		 inBufferHdr[33] = -127;
		 inBufferHdr[32] = -127;		 		 
	}
	
	
	@Override
	public ServerProperties parseToServerProp(InetAddress iNetAddr, int port,
			boolean isLimitedTimeOut) {
		log.debug("********** Entered into OracleDBLocator --> parseToServerProp() ********");
		Socket socket = null;
		try {
			int connectionTimeOut = isLimitedTimeOut ? ORACLE_DB_MIN_TIME_OUT_PERIOD : ORACLE_DB_MAX_TIME_OUT_PERIOD;
			socket = Utils.getClientSocket(iNetAddr, port, connectionTimeOut);
			DataOutputStream dataOStream = Utils.getDataOutStreamFromServer(socket);
			DataInputStream dataInStream = Utils.getDataInStreamFromServer(socket);
			fillBufferData(iNetAddr, port);
			synchronized (dataOStream) {
				dataOStream.write(bufferData, 0, bufferData.length);				
				if(addressData.length > 230){
					dataOStream.write(addressData, 0, addressData.length);
				}
				dataOStream.flush();
			}
			if(isValidResponseFromDB(dataInStream)){
				ServerProperties serverProp = new ServerProperties();
				serverProp.setServerName(ServersEnum.ORACLE_DB.toString());
				log.debug("Address details are identified to have Oracle DB running");
				return serverProp;
			}
		} catch (SocketException e) {
			log.error("Unable to connect to server",e);
			return null;
		} catch (IOException e) {
			log.debug("Unable to communicate with server",e);
			return null;
		}finally{
			if(socket != null){
				try {
					socket.close();
				} catch (IOException e) {
					log.error("Unable to close socket");
				}
			}
		}
		
		return null;
	}
	
	private boolean isValidResponseFromDB(DataInputStream dataInStream){
		log.debug("********** Entered into OracleDBLocator --> isValidResponseFromDB() ********");
		byte[] lengthInBytes = new byte[2];
		try {
			Utils.readBytesFromStream(dataInStream, lengthInBytes, 0, 2);
			log.debug("Bytes read as response length "+Arrays.toString(lengthInBytes));
			if(PacketBuffer.isInvalidInt(lengthInBytes)){
				log.debug("Invalid message from DB Server");
				return false;
			}
			int responseLength = (lengthInBytes[0] << 8 & 0xff) | (lengthInBytes[1]  & 0xff);
			if(responseLength < 12){
				log.debug("Invalid message from DB Server");
				return false;
			}
			byte[] headerInBytes = new byte[10];
			Utils.readBytesFromStream(dataInStream, headerInBytes, 0, 10);
			log.debug("Bytes read as header  "+Arrays.toString(headerInBytes));
			if(headerInBytes[2] != (byte)4 && headerInBytes[2] != (byte)5){
				log.debug("Invalid message from DB Server expected error index since input passed without SID");
				return false;
			}	
		} catch (IOException e) {
			log.debug("Unable to get message from DB Server"+e);
			return false;
		}catch (Exception e) {
			log.debug("Unable to parse exception from DB Server"+e);
			return false;
		}
		return true;
		
	}
	
	private void fillBufferData(InetAddress iNetAddr, int port){
		log.debug("********** Entered into OracleDBLocator --> fillBufferData() ********");
		getAddressData(iNetAddr, port);
		int addressDataLen = this.addressData.length;
		int bufferLength = addressDataLen > 230 ? HEADER_LENGTH : addressDataLen + HEADER_LENGTH;
		bufferData = new byte[bufferLength];
		for(int i=0; i < HEADER_LENGTH; i++){
			bufferData[i] = inBufferHdr[i];
		}
		bufferData[24] = ((byte)(addressDataLen / 256));
		bufferData[25] = ((byte)(addressDataLen % 256));
		bufferData[0] = ((byte)(bufferLength / 256));
		bufferData[1] = ((byte)(bufferLength % 256));
		if(addressDataLen <= 230){
			for(int k=0; k < addressDataLen ; k++){
				bufferData[HEADER_LENGTH + k] = addressData[k];
			}
		}
		log.debug("Buffer Data to be passed to Server "+ Arrays.toString(bufferData));
	}
	
	private void getAddressData(InetAddress iNetAddr, int port){
		log.debug("********** Entered into OracleDBLocator --> getAddressData() ********");
		String hostAddress = iNetAddr.getHostAddress();
		StringBuilder netPropData = new StringBuilder("(DESCRIPTION=(CONNECT_DATA=(SID=)(CID=(PROGRAM=JDBC Thin Client)(HOST=__jdbc__)(USER=murthykoppu)))(ADDRESS=(PROTOCOL=tcp)(HOST="+hostAddress+")(PORT="+port+")))");		
		addressData = new byte[netPropData.length()+24];
		netPropData.toString().getBytes(0, netPropData.length(), addressData, 24);
		log.debug("Address Data to be passed to Server "+ Arrays.toString(addressData));
	}
	
}

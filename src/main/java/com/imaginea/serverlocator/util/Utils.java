package com.imaginea.serverlocator.util;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class Utils {

	static Logger log = Logger.getLogger(Utils.class);
	private static final Set<String> defaultLocalIps = new HashSet<String>();
	static {
		defaultLocalIps.add("127.0.0.1");
		defaultLocalIps.add("0.0.0.0");
		defaultLocalIps.add("localhost");
		defaultLocalIps.add("::");
		defaultLocalIps.add("::1");
	}

	public static DataInputStream getDataInStreamFromServer(Socket socket)
			throws IOException {
		InputStream inStream = socket.getInputStream();
		if (inStream == null) {
			log.error("Unable to get InputStream from socket");
			throw new IOException("Unable to get InputStream from socket");
		}
		DataInputStream dInStream = new DataInputStream(inStream);
		return dInStream;
	}

	public static DataOutputStream getDataOutStreamFromServer(Socket socket)
			throws IOException {
		OutputStream outStream = socket.getOutputStream();
		if (outStream == null) {
			log.error("Unable to get OutputStream from socket");
			throw new IOException("Unable to get OutputStream from socket");
		}
		DataOutputStream dOutStream = new DataOutputStream(outStream);
		return dOutStream;
	}

	public static DataInputStream getDataInStreamFromServer(
			InetAddress iNetAddr, int port, int connectionTimeOut)
			throws IOException {
		Socket clientSocket = getClientSocket(iNetAddr, port, connectionTimeOut);
		return getDataInStreamFromServer(clientSocket);
	}

	public static Socket getClientSocket(InetAddress iNetAddr, int port,
			int connectionTimeOut) throws IOException, SocketException {
		Socket clientSocket = new Socket(iNetAddr, port);
		clientSocket.setSoTimeout(connectionTimeOut);
		return clientSocket;
	}

	public static void readBytesFromStream(DataInputStream dInStream,
			byte[] destination, int off, int len) throws Exception {
		int totalBytesRead = 0;
		synchronized (dInStream) {
			while (totalBytesRead < len) {
				try {
					int tempBytesRead = dInStream.read(destination, off
							+ totalBytesRead, len - totalBytesRead);
					if (tempBytesRead < 0) {
						log.debug("Unable to read required number of bytes");
						throw new Exception();
					}
					totalBytesRead += tempBytesRead;
				} catch (IOException e) {
					log.debug("Unable to read Required bytes from Socket");
					throw e;
				}
			}
		}

	}

	public static String downToIpV4(String ipAddress) {
		int ipV4Index = ipAddress.indexOf("::ffff:");
		return ipV4Index == 0 ? ipAddress.substring(7) : ipAddress;
	}

	public static Set<String> getDefaultLocalIps() {
		return defaultLocalIps;
	}

	public static String getResourcesLocationPath() {
	/*	String path = Utils.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath()
				+ "/../";*/
		String path = "./";
		try {
			return URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return path;
	}
	
	public static void writeJsonToDeployment(String jsonTopologyGenericData,String fileLocation){
		try {
			BufferedWriter jsonFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileLocation)));
			jsonFileWriter.write(jsonTopologyGenericData, 0, jsonTopologyGenericData.length());
			jsonFileWriter.flush();
			jsonFileWriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}

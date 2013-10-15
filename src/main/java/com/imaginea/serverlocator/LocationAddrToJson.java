package com.imaginea.serverlocator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.omg.CosNaming.NamingContextExtPackage.AddressHelper;

public class LocationAddrToJson {
	public static void main(String[] args) {
		/*String input = "0.0.0.0,22,0.0.0.0,*,1108/sshd,127.0.0.1,25,0.0.0.0,*,1141/sendmail,10.203.70.251,22,182.73.145.150,33081,20778/sshd,10.203.70.251,22,182.73.145.150,33012,20719/sshd,10.203.70.251,22,182.73.145.150,32996,20701/sshd,,,,22,,";
		new LocationAddrToJson().getJsonFromAddresses(args[0].split(","));*/
	String input1 = "0.0.0.0:22,0.0.0.0:*,LISTEN,1145/sshd;127.0.0.1:25,0.0.0.0:*,LISTEN,1178/sendmail;10.179.9.248:22,14.141.6.170:32940,ESTABLISHED,1277/sshd;10.179.9.248:22,14.141.6.170:32812,ESTABLISHED,1252/sshd;:::22,:::*,LISTEN,1145/sshd;::ffff:10.179.9.248:39444,::ffff:10.203.70.251:9005,ESTABLISHED,1361/java;";
	//	String input ="0.0.0.0:22,0.0.0.0:*,LISTEN,1108/sshd;127.0.0.1:25,0.0.0.0:*,LISTEN,1141/sendmail;10.203.70.251:22,182.73.145.150:32937,ESTABLISHED,20872/sshd;10.203.70.251:22,182.73.145.150:33142,ESTABLISHED,20917/sshd;:::9005,:::*,LISTEN,21227/java;:::22,:::*,LISTEN,1108/sshd;::ffff:10.203.70.251:9005,::ffff:10.179.9.248:39442,CLOSE_WAIT,21227/java;::ffff:10.203.70.251:9005,::ffff:10.179.9.248:39444,ESTABLISHED,21227/java";
		LocationAddrToJson obj = new LocationAddrToJson();
		obj.getJsonFromAddrSplitOn(args[0]);
		System.out.println(obj.jsonRootEle);	
	}
	
	JSONObject jsonRootEle = new JSONObject();
	Set<String> listeningPorts = new HashSet<String>();
	Map<String,String> communicatingPorts = new HashMap<String,String>();
	Set<String> localIps = new HashSet<String>();/*["127.0.0.1","0.0.0.0","localhost"]*/
	{
		localIps.add("127.0.0.1");
		localIps.add("0.0.0.0");
		localIps.add("localhost");
		localIps.add(":::");
		try {
			localIps.add(InetAddress.getLocalHost().toString().split("/")[0]);
			localIps.add(InetAddress.getLocalHost().toString().split("/")[1]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void getJsonFromAddrSplitOn(String addrArgsSplit){
		String[] communicationPointArr = addrArgsSplit.split(";");
		for(String communicationPoint : communicationPointArr)
			if(!communicationPoint.equals(""))
				addToJsonCommunication(communicationPoint);
		Set<Map.Entry<String, String>> entrySetOfComm = communicatingPorts.entrySet();
		for(Map.Entry<String, String> communicationEntry: entrySetOfComm){
			if(!listeningPorts.contains(communicationEntry.getValue())){
				JSONObject externalCommDtlsToVerify = new JSONObject();				
				try {
					externalCommDtlsToVerify.put("ServerIPAddr", communicationEntry.getKey());
					externalCommDtlsToVerify.put("ServerPort", communicationEntry.getValue());
					jsonRootEle.accumulate("ExternalServer", externalCommDtlsToVerify);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		for(String listeningPort: listeningPorts){
			try{
				if(Integer.parseInt(listeningPort) > 1024){
					JSONObject internalServerPort = new JSONObject();
					JSONObject internalCommDtlsToVerify = new JSONObject();				
					try {
						internalCommDtlsToVerify.put("ServerPort", listeningPort);
						jsonRootEle.accumulate("InternalServer", internalCommDtlsToVerify);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}catch(Exception e){
				
			}
						
		}
			
		
	}
	
	public void addToJsonCommunication(String communicationPoint){
		String[] communicationDetails = communicationPoint.split(",");
		String toSkt = communicationDetails[0];
		String fromSkt = communicationDetails[1];
		String state = communicationDetails[2];
		String[] processDtls = communicationDetails[3].split("/");
		//String[] toSktArr =
		int ipAddrBound = toSkt.lastIndexOf(":");
		String toPort = toSkt.substring(ipAddrBound+1);
		String toIPAddr = toSkt.substring(0, ipAddrBound);
		
		int fromAddrBound = fromSkt.lastIndexOf(":");
		String fromPort = fromSkt.substring(fromAddrBound+1);
		String fromIPAddr = fromSkt.substring(0, fromAddrBound);
		
		String processId = processDtls[0];
		String processName = processDtls[1];
		if(!isInternalCommunication(fromIPAddr, toIPAddr,fromPort,toPort,state)){
			JSONObject jsonCommunication = new JSONObject();
			listeningPorts.add(toPort);
			communicatingPorts.put(fromIPAddr,fromPort);
			try {
				jsonCommunication.put("ToIpAddr", toIPAddr);
				jsonCommunication.put("ToPort", toPort);
				jsonCommunication.put("FromIpAddr", fromIPAddr);
				jsonCommunication.put("FromPort", fromPort);
				jsonCommunication.put("ProcessId", processId);
				jsonCommunication.put("ProcessName", processName);
				jsonCommunication.put("ConnectionState", state);				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			try {
				jsonRootEle.accumulate("CommunicationElement", jsonCommunication);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		/*for(int k=0; k < communicationDetails.length; k++){
			String toCommSkt = communicationDetails[k];
			String fromIp = addrArgs[i+2];
			String toPort = (addrArgs[i+1]);
			String fromPort = (addrArgs[i+3]);
		}*/
	}
	public void getJsonFromAddresses(String... addrArgs){
		JSONObject jsonRootEle = new JSONObject();
		for(int i=0; i+5 < addrArgs.length; i += 5){
			String toIp = addrArgs[i];
			String fromIp = addrArgs[i+2];
			String toPort = (addrArgs[i+1]);
			String fromPort = (addrArgs[i+3]);
			String[] processIdAndName = addrArgs[i+4].split("/");
			if(!isInternalCommunication(fromIp, toIp)){
				JSONObject jsonCommunication = new JSONObject();				
				try {
					jsonCommunication.put("ToIpAddr", toIp);
					jsonCommunication.put("ToPort", toPort);
					jsonCommunication.put("FromIpAddr", fromIp);
					jsonCommunication.put("FromPort", fromPort);
					jsonCommunication.put("ProcessId", processIdAndName[0]);
					jsonCommunication.put("ProcessName", processIdAndName[1]);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				try {
					jsonRootEle.accumulate("CommunicationElement", jsonCommunication);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		}
		System.out.println(jsonRootEle.toString());
	}
	
	
	private boolean isInternalCommunication(InetAddress fromInetAddress, InetAddress toInetAddress){
		return fromInetAddress.equals(toInetAddress);
	}
	private boolean isInternalCommunication(String fromHost, String toHost){
		return false;
	}
	private boolean isInternalCommunication(String fromHost, String toHost,String fromPort, String toPort,String state){			
			try {
				if(localIps.contains(toHost) && (toPort.equals("*") || (Integer.parseInt(toPort) < 1025)) && state.equals("LISTEN"))
					return true;
				/*InetAddress fromInetAddress = InetAddress.getByName(fromHost);
				InetAddress toInetAddress = InetAddress.getByName(toHost);
				return isInternalCommunication(fromInetAddress, toInetAddress) ? true : false;*/
			} catch (Exception e) {
				return false;
			}
			return false;
	}
	
}

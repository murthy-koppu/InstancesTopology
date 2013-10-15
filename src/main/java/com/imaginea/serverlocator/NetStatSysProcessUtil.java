package com.imaginea.serverlocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.serverlocator.factory.ServerLocator;
import com.imaginea.serverlocator.model.NetStatProcessModel;
import com.imaginea.serverlocator.model.NetStatSocketModel;
import com.imaginea.serverlocator.util.AWSInstanceUtil;
import com.imaginea.serverlocator.util.ApplicationConstants;
import com.imaginea.serverlocator.util.Utils;

public class NetStatSysProcessUtil implements ApplicationConstants {
	Set<Integer> listeningPorts = new HashSet<Integer>();
	Map<String, String> foreignSockets = new HashMap<String, String>();
	Set<NetStatProcessModel> communicatingProcess = new HashSet<NetStatProcessModel>();

	public static void main(String[] args) throws Exception {
		String serverSampleData = "0.0.0.0:22,0.0.0.0:*,LISTEN,1136/sshd;127.0.0.1:25,0.0.0.0:*,LISTEN,1169/sendmail;10.178.8.73:22,182.73.145.150:33092,ESTABLISHED,3845/sshd;10.178.8.73:22,182.73.145.150:32872,ESTABLISHED,3128/sshd;10.178.8.73:22,182.73.145.150:33174,ESTABLISHED,2571/sshd;:::9005,:::*,LISTEN,3835/java;:::22,:::*,LISTEN,1136/sshd;::1:52257,::1:53357,TIME_WAIT,23/asd;";
		String sampleData = "0.0.0.0:22,0.0.0.0:*,LISTEN,1128/sshd;127.0.0.1:25,0.0.0.0:*,LISTEN,1161/sendmail;10.204.97.158:22,182.73.145.150:33084,ESTABLISHED,4572/sshd;10.204.97.158:22,182.73.145.150:33003,ESTABLISHED,4588/sshd;:::22,:::*,LISTEN,1128/sshd;::ffff:10.204.97.158:60959,::ffff:10.243.123.119:9005,ESTABLISHED,2484/java;";
		new NetStatSysProcessUtil().processNetStatRecs(serverSampleData,
				"10.178.8.73;127.0.0.1;");
		// System.out.println(new
		// AWSInstanceUtil().getInstanceRelationsInJson());
	}

	private void processNetStatRecs(String netStatIn, String systemIps) {
		List<String> standardLocalIps = getStandardSystemIP(systemIps);
		if (standardLocalIps.isEmpty()) {
			try {
				throw new Exception("Unable to find Machine IP Address");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		NetStatLinuxParser linuxParser = new NetStatLinuxParser();
		
		try {
			List<NetStatProcessModel>  netStatRecords = linuxParser
					.getValidNetStatRecords(netStatIn, standardLocalIps);
			for (NetStatProcessModel netStatRec : netStatRecords) {
				String communicationState = netStatRec.getState();
				if (communicationState.equals(NET_STAT_PROCESS_STATE_LISTEN)) {
					listeningPorts.add(netStatRec.getLocalSkt().getPort());
				} else {
					communicatingProcess.add(netStatRec);
				}
			}
			for (NetStatProcessModel netStatRec : communicatingProcess) {
				if (listeningPorts.contains(netStatRec.getLocalSkt().getPort())) {
					
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void processNetStatRecs1(String netStatIn, String systemIps)
			throws Exception {
		JSONObject systemLvlRootJSON = new JSONObject();
		JSONObject rootTopologyJson = new JSONObject();
		JSONObject systemIPsJSON = new JSONObject();

		List<String> standardLocalIps = getStandardSystemIP(systemIps);
		if (standardLocalIps.isEmpty()) {
			throw new Exception("Unable to find Machine IP Address");
		} else {
			for (String systemIP : standardLocalIps) {
				systemIPsJSON.accumulate("PrivateInetAddresses", systemIP);
			}
			systemLvlRootJSON.put("SystemInetAddrs", systemIPsJSON);
		}

		NetStatLinuxParser linuxParser = new NetStatLinuxParser();
		List<NetStatProcessModel> netStatRecords = linuxParser
				.getValidNetStatRecords(netStatIn, standardLocalIps);
		for (NetStatProcessModel netStatRec : netStatRecords) {
			String communicationState = netStatRec.getState();
			if (communicationState.equals(NET_STAT_PROCESS_STATE_LISTEN)) {
				listeningPorts.add(netStatRec.getLocalSkt().getPort());
			} else {
				communicatingProcess.add(netStatRec);
			}
		}
		JSONObject systemLvlNetStatJson = parseNetStatRecsToJson(netStatRecords);
		systemLvlRootJSON.put("SystemServersTopology", systemLvlNetStatJson);
		rootTopologyJson.accumulate("Instances", systemLvlRootJSON);
		System.out.println(rootTopologyJson.toString());
		// rootTopologyJson.write(arg0)
	}

	public List<String> getStandardSystemIP(String systemIps) {
		String[] systemIpsArr = systemIps.split(";");
		List<String> standardIps = new ArrayList<String>(systemIpsArr.length);
		for (String systemIp : systemIpsArr) {
			if (Utils.getDefaultLocalIps().contains(systemIp)) {
				continue;
			} else if (systemIp
					.startsWith(IPV6_REPRESENTATION_START_SUB_STRING)) {
				standardIps.add(Utils.downToIpV4(systemIp));
			} else {
				standardIps.add(systemIp);
			}
		}
		return standardIps;
	}

	private JSONObject parseNetStatRecsToJson(
			List<NetStatProcessModel> netStatRecords) {
		JSONObject systemLvlNetStatJson = new JSONObject();
		Set<String> foreignConnChannelIPPort = new HashSet<String>();
		for (NetStatProcessModel netStatRec : communicatingProcess) {
			JSONObject jsonNetStatRec = new JSONObject();
			if (listeningPorts.contains(netStatRec.getLocalSkt().getPort())) {
				setToAndFromSktOnJson(jsonNetStatRec, netStatRec.getLocalSkt(),
						netStatRec.getForeignSkt());
			} else {
				setToAndFromSktOnJson(jsonNetStatRec,
						netStatRec.getForeignSkt(), netStatRec.getLocalSkt());
				// TODO Handle duplication of Foreign Server Addr if required
				JSONObject jsonForeignServer = new JSONObject();
				try {
					String foreignIpV4 = Utils.downToIpV4(netStatRec
							.getForeignSkt().getIpAddress());
					String strIpPortWithSemiColon = foreignIpV4 + ':'
							+ netStatRec.getForeignSkt().getPort();
					if (!foreignConnChannelIPPort
							.contains(strIpPortWithSemiColon)) {
						jsonForeignServer.put("ForeignServerIPAddr",
								foreignIpV4);
						jsonForeignServer.put("ForeignServerPort", netStatRec
								.getForeignSkt().getPort());
						foreignConnChannelIPPort.add(strIpPortWithSemiColon);
					}
					systemLvlNetStatJson.accumulate("ForeignServers",
							jsonForeignServer);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			try {
				jsonNetStatRec.put("ProcessId", netStatRec.getProcessId());
				jsonNetStatRec.put("ProcessName", netStatRec.getProcessName());
				jsonNetStatRec.put("ConnectionState", netStatRec.getState());
				systemLvlNetStatJson.accumulate("CommunicatingProcess",
						jsonNetStatRec);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		for (Integer localServerPort : listeningPorts) {
			JSONObject jsonInternalServer = new JSONObject();
			try {
				jsonInternalServer.put("LocalServerPort", localServerPort);
				systemLvlNetStatJson.accumulate("LocalServers",
						jsonInternalServer);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return systemLvlNetStatJson;
		// System.out.println(rootNetStatJson.toString());
		// new ServerLocator().findServerDetailsOnNetStats(rootNetStatJson);
	}

	private void setToAndFromSktOnJson(JSONObject jsonNetStatRec,
			NetStatSocketModel toSktMdl, NetStatSocketModel fromSktMdl) {
		try {
			jsonNetStatRec.put("ToIpAddr", toSktMdl.getIpAddress());
			jsonNetStatRec.put("ToPort", toSktMdl.getPort());
			jsonNetStatRec.put("FromIpAddr", fromSktMdl.getIpAddress());
			jsonNetStatRec.put("FromPort", fromSktMdl.getPort());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}

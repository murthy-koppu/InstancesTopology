package com.imaginea.serverlocator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.serverlocator.model.NetStatProcessModel;
import com.imaginea.serverlocator.util.AWSInstanceUtil;
import com.imaginea.serverlocator.util.ApplicationConstants;
import com.imaginea.serverlocator.util.Utils;

public class ConcreteTopologyPublisher implements ApplicationConstants {
	private JSONObject genericTopologyJson;
	private Map<String, JSONObject> privateIpToInstanceJsonProp = new HashMap<String, JSONObject>();
	JSONArray newInstanceNodesDrvdFmNetstat = new JSONArray();
	private volatile int jsonNodesLength = 0;
	List<JSONObject> lsNewDerivedNodesFmNetStat = new ArrayList<JSONObject>();

	public void loadGenericTopologyFromFile(String filePath) throws JSONException, IOException {
		BufferedReader jsonFileReader;
		try {
			jsonFileReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(filePath)));
			StringBuilder strGenericTopologyJson = new StringBuilder();
			String topologyJsonLine = "";
			while ((topologyJsonLine = jsonFileReader.readLine()) != null) {
				strGenericTopologyJson.append(topologyJsonLine);
			}
			strGenericTopologyJson.delete(0,
					strGenericTopologyJson.indexOf("data =") + 6);
			int lastIndexOfData = strGenericTopologyJson.lastIndexOf("}",
					strGenericTopologyJson.lastIndexOf("}"));
			strGenericTopologyJson.deleteCharAt(lastIndexOfData + 1);
			loadGenericTopologyFromJson(new JSONObject(
					strGenericTopologyJson.toString()));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			throw e1;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} catch (JSONException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public JSONObject loadGenericTopologyFromJson(JSONObject genericParamJson)
			throws JSONException {
		genericTopologyJson = new JSONObject(genericParamJson.toString());
		genericTopologyJson.remove("links");
		loadJsonInstNodesToMap();
		publishAbsoluteConcreteTopologyJson();
		return genericTopologyJson;
	}

	private void loadJsonInstNodesToMap() {
		JSONArray instNodes;
		try {
			instNodes = genericTopologyJson.getJSONArray("nodes");
			jsonNodesLength = instNodes.length();
			if (instNodes != null) {
				for (int i = 0; i < jsonNodesLength; i++) {
					JSONObject instanceNode = instNodes.getJSONObject(i);
					privateIpToInstanceJsonProp.put((String) instanceNode
							.get(TOPOLOGY_INSTANCE_PRIVATE_IP_PROP_NAME),
							instanceNode);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void publishAbsoluteConcreteTopologyJson() {
		String[] InstancePrivateIpsArr = (String[]) privateIpToInstanceJsonProp
				.keySet().toArray(new String[1]);
		List<Thread> lsInitializedThreads = new ArrayList<Thread>();
		
		for (String instanceIp : InstancePrivateIpsArr) {
			try{
				JSONObject instanceJsonProperties = privateIpToInstanceJsonProp
						.get(instanceIp);
				RemoteSshProcessor remoteSshProcessor = new RemoteSshProcessor(
						this, instanceJsonProperties);
				Thread remoteSshThread = new Thread(remoteSshProcessor);
				lsInitializedThreads.add(remoteSshThread);
				remoteSshThread.start();
			}catch(Exception e){
				e.printStackTrace();
			}			
		}
		for (Thread remoteSshThread : lsInitializedThreads) {
			try {
				remoteSshThread.join(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	private void appendLinksToTopologyJson(String localIp, String foreignIp,
			boolean isLocalServer) {
		JSONObject localInstJsonProp = privateIpToInstanceJsonProp.get(localIp);
		JSONObject foreignInstJsonProp = privateIpToInstanceJsonProp
				.get(foreignIp);

		if (foreignInstJsonProp == null) {
			synchronized (this) {
				foreignInstJsonProp = privateIpToInstanceJsonProp
						.get(foreignIp);
				if (foreignInstJsonProp == null) {
					foreignInstJsonProp = new JSONObject();
					try {
						foreignInstJsonProp.put(
								TOPOLOGY_INSTANCE_PRIVATE_IP_PROP_NAME,
								foreignIp);
						foreignInstJsonProp.put(
								TOPOLOGY_INSTANCE_IS_START_POINT, false);
						foreignInstJsonProp.put(
								TOPOLOGY_INSTANCE_NODE_SERIAL_NO,
								++jsonNodesLength);
						privateIpToInstanceJsonProp.put(foreignIp,
								foreignInstJsonProp);
						genericTopologyJson.append(
								TOPOLOGY_INSTANCES_NODES_PARENT,
								foreignInstJsonProp);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		}
		try {
			int localNodeSeqNo = localInstJsonProp
					.getInt(TOPOLOGY_INSTANCE_NODE_SERIAL_NO);
			int foreignNodeSeqNo = foreignInstJsonProp
					.getInt(TOPOLOGY_INSTANCE_NODE_SERIAL_NO);

			JSONObject linkJson = new JSONObject();
			if (isLocalServer) {
				linkJson.put(TOPOLOGY_INSTANCE_LINK_SOURCE, foreignNodeSeqNo);
				linkJson.put(TOPOLOGY_INSTANCE_LINK_TARGET, localNodeSeqNo);
			} else {
				linkJson.put(TOPOLOGY_INSTANCE_LINK_SOURCE, localNodeSeqNo);
				linkJson.put(TOPOLOGY_INSTANCE_LINK_TARGET, foreignNodeSeqNo);
			}
			linkJson.put(TOPOLOGY_INSTANCE_LINK_RELATIONSHIP, "rel");
			synchronized (genericTopologyJson) {
				genericTopologyJson.append(TOPOLOGY_INSTANCE_LINKS_PARENT,
						linkJson);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * public static void main(String[] args) { new AWSInstanceUtil(); new
	 * ConcreteTopologyPublisher().publishAbsoluteConcreteTopologyJson();
	 * 
	 * }
	 */

	public void processNetStatRecs(String netStatIn, String systemIps) {
		Set<Integer> listeningPorts = new HashSet<Integer>();
		Set<NetStatProcessModel> communicatingProcess = new HashSet<NetStatProcessModel>();
		NetStatSysProcessUtil netstatUtil = new NetStatSysProcessUtil();
		List<String> standardLocalIps = netstatUtil
				.getStandardSystemIP(systemIps);
		if (standardLocalIps.isEmpty()) {
			try {
				throw new Exception("Unable to find Machine IP Address");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		NetStatLinuxParser linuxParser = new NetStatLinuxParser();
		try {
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
			for (NetStatProcessModel netStatRec : communicatingProcess) {
				appendLinksToTopologyJson(netStatRec.getLocalSkt()
						.getIpAddress(), netStatRec.getForeignSkt()
						.getIpAddress(), listeningPorts.contains(netStatRec
						.getLocalSkt().getPort()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

package com.imaginea.serverlocator;

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
import com.imaginea.serverlocator.util.ApplicationConstants;

public class InstancesTalkingTopology implements ApplicationConstants {
	private JSONObject topologyJson;
	private Map<String, JSONObject> privateIpToInstanceJsonProp = new HashMap<String, JSONObject>();
	JSONArray newInstanceNodesDrvdFmNetstat = new JSONArray();
	private volatile int jsonNodesLength = 0;
	List<JSONObject> lsNewDerivedNodesFmNetStat = new ArrayList<JSONObject>();

	public JSONObject getIsTalkingOn(JSONObject canTalkJson)
			throws JSONException {
		topologyJson = new JSONObject(canTalkJson.toString());
		topologyJson.remove("links");
		loadJsonInstNodesToMap();
		publishTalkingTopologyJson();
		JSONArray jsonLinksArr = null;
		try {
			jsonLinksArr = topologyJson
					.getJSONArray(TOPOLOGY_INSTANCE_LINKS_PARENT);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		if (jsonLinksArr == null || jsonLinksArr.length() == 0) {
			JSONObject linkJson = createLinkJson(true, 0, 0);
			try {
				topologyJson.append(TOPOLOGY_INSTANCE_LINKS_PARENT, linkJson);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return topologyJson;
	}

	private void loadJsonInstNodesToMap() {
		JSONArray instNodes;
		try {
			instNodes = topologyJson.getJSONArray("nodes");
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

	private void publishTalkingTopologyJson() {
		String[] InstancePrivateIpsArr = (String[]) privateIpToInstanceJsonProp
				.keySet().toArray(new String[1]);
		List<Thread> lsInitializedThreads = new ArrayList<Thread>();

		for (String instanceIp : InstancePrivateIpsArr) {
			try {
				JSONObject instanceJsonProperties = privateIpToInstanceJsonProp
						.get(instanceIp);
				RemoteSshProcessor remoteSshProcessor = new RemoteSshProcessor(
						this, instanceJsonProperties);
				Thread remoteSshThread = new Thread(remoteSshProcessor);
				lsInitializedThreads.add(remoteSshThread);
				remoteSshThread.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (Thread remoteSshThread : lsInitializedThreads) {
			try {
				remoteSshThread.join();
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
			foreignInstJsonProp = appendNewForeignNodeToTopology(foreignIp);
		}
		try {
			int localNodeSeqNo = localInstJsonProp
					.getInt(TOPOLOGY_INSTANCE_NODE_SERIAL_NO);
			int foreignNodeSeqNo = foreignInstJsonProp
					.getInt(TOPOLOGY_INSTANCE_NODE_SERIAL_NO);
			JSONObject linkJson = createLinkJson(isLocalServer, localNodeSeqNo,
					foreignNodeSeqNo);
			synchronized (topologyJson) {
				topologyJson.append(TOPOLOGY_INSTANCE_LINKS_PARENT, linkJson);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private JSONObject appendNewForeignNodeToTopology(String foreignIp) {
		JSONObject foreignInstJsonProp;
		synchronized (this) {
			foreignInstJsonProp = privateIpToInstanceJsonProp.get(foreignIp);
			if (foreignInstJsonProp == null) {
				foreignInstJsonProp = new JSONObject();
				try {
					foreignInstJsonProp.put(
							TOPOLOGY_INSTANCE_PRIVATE_IP_PROP_NAME, foreignIp);
					foreignInstJsonProp.put(TOPOLOGY_INSTANCE_IS_START_POINT,
							false);
					foreignInstJsonProp.put(TOPOLOGY_INSTANCE_NODE_SERIAL_NO,
							jsonNodesLength++);					
					synchronized(this) {
						privateIpToInstanceJsonProp.put(foreignIp,
								foreignInstJsonProp);
						topologyJson.append(TOPOLOGY_INSTANCES_NODES_PARENT,
								foreignInstJsonProp);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return foreignInstJsonProp;
	}

	private JSONObject createLinkJson(boolean isLocalServer,
			int localNodeSeqNo, int foreignNodeSeqNo) throws JSONException {
		JSONObject linkJson = new JSONObject();
		linkJson.put(TOPOLOGY_INSTANCE_LINK_SOURCE,
				isLocalServer ? foreignNodeSeqNo : localNodeSeqNo);
		linkJson.put(TOPOLOGY_INSTANCE_LINK_TARGET,
				isLocalServer ? localNodeSeqNo : foreignNodeSeqNo);
		linkJson.put(TOPOLOGY_INSTANCE_LINK_RELATIONSHIP, "rel");
		return linkJson;
	}

	public void processNetStatRecs(String netStatIn, String systemIps)
			throws Exception {
		Set<Integer> listeningPorts = new HashSet<Integer>();
		Set<NetStatProcessModel> talkingProcesses = new HashSet<NetStatProcessModel>();
		NetStatLinuxParser linuxParser = new NetStatLinuxParser();
		List<String> standardLocalIps = linuxParser
				.getStandardSystemIP(systemIps);
		if (standardLocalIps.isEmpty()) {
			throw new Exception("Unable to find Machine IP Address");
		}
		try {
			List<NetStatProcessModel> netStatRecords = linuxParser
					.getValidNetStatRecords(netStatIn, standardLocalIps);
			for (NetStatProcessModel netStatRec : netStatRecords) {
				String communicationState = netStatRec.getState();
				if (communicationState.equals(NET_STAT_PROCESS_STATE_LISTEN)) {
					listeningPorts.add(netStatRec.getLocalSkt().getPort());
				} else {
					talkingProcesses.add(netStatRec);
				}
			}
			for (NetStatProcessModel netStatRec : talkingProcesses) {
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

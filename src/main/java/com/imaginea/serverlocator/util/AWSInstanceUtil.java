package com.imaginea.serverlocator.util;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.imaginea.serverlocator.model.OptimizedIpPerms;
import com.imaginea.serverlocator.model.PermissibleSocketModel;

public class AWSInstanceUtil implements ApplicationConstants{
	Map<String, SecurityGroup> mapSGroupsWithId = new HashMap<String, SecurityGroup>();
	Map<Instance, OptimizedIpPerms> ipPermsToEachInstance = new HashMap<Instance, OptimizedIpPerms>();
	List<Instance> lsInstances = new ArrayList<Instance>();
	static final int START_POINT_ACCESS_PORT = 80;

	public static void main(String[] args) {
		AWSInstanceUtil obj = new AWSInstanceUtil();
		obj.loadEC2Instances();		
		//ApplicationConstants.DEPLOYMENT_JSON_DATA_LOCATION
	}	
	
	public AWSInstanceUtil(){
		/*loadEC2Instances();
		Utils.writeJsonToDeployment("$('document').ready(function () { \n data = "
				+ this.publishInstanceConnectionsToJson().toString()
				+ "; \n });",ApplicationConstants.GENERIC_DEPLOYMENT_JSON_DATA_LOCATION);
*/	}
	
	public JSONObject getInstanceRelationsInJson() {
		AWSInstanceUtil obj = new AWSInstanceUtil();
		obj.loadEC2Instances();
		/*return "$('document').ready(function () { \n data = "
				+ obj.publishInstanceConnectionsToJson().toString()
				+ "; \n });";*/
		return obj.publishInstanceConnectionsToJson();
	}

	public JSONObject publishInstanceConnectionsToJson() {
		List<Instance> startPointInstances = findStartPointInstances();
		JSONObject rootInstanceRel = new JSONObject();
		Instance instance = null;
		for (int k = 0; k < lsInstances.size(); k++) {
			instance = lsInstances.get(k);
			JSONObject jsonInstances = new JSONObject();
			try {
				jsonInstances.put(TOPOLOGY_INSTANCE_PRIVATE_IP_PROP_NAME, instance.getPrivateIpAddress());
				jsonInstances.put(TOPOLOGY_INSTANCE_NODE_PUBLIC_DNS, instance.getPublicDnsName());
				jsonInstances.put(TOPOLOGY_INSTANCE_NODE_SERIAL_NO, k);
				jsonInstances.put("instanceId", instance.getInstanceId());
				jsonInstances.put("instanceState", instance.getState()
						.getName());
				jsonInstances.put("isStartPoint",
						startPointInstances.contains(instance) ? true : false);
				rootInstanceRel.append(TOPOLOGY_INSTANCES_NODES_PARENT, jsonInstances);
				publishInstanceLinksToJson(rootInstanceRel, instance, k);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return rootInstanceRel;
	}

	private void publishInstanceLinksToJson(JSONObject rootInstanceRel,
			Instance instance, int k) throws JSONException {
		OptimizedIpPerms ipPermDtls = ipPermsToEachInstance.get(instance);
		for (int t = 0; t < lsInstances.size(); t++) {
			if (t == k) {
				continue;
			}
			Instance otherInstance = lsInstances.get(t);
			if (canTalkOn(ipPermDtls, otherInstance)) {
				JSONObject linkJson = new JSONObject();
				linkJson.put(TOPOLOGY_INSTANCE_LINK_SOURCE, k);
				linkJson.put(TOPOLOGY_INSTANCE_LINK_TARGET, t);
				linkJson.put(TOPOLOGY_INSTANCE_LINK_RELATIONSHIP, "rel");
				rootInstanceRel.append(TOPOLOGY_INSTANCE_LINKS_PARENT, linkJson);
			}
		}
	}

	public void loadEC2Instances() {
		loadSecurityGroups();
		DescribeInstancesResult instancesResult = AWSConfigLoader
				.getAmazonEC2().describeInstances();

		if (instancesResult != null
				&& instancesResult.getReservations() != null) {
			for (Reservation reservation : instancesResult.getReservations()) {
				for (Instance instanceFromReserv : reservation.getInstances()) {
					if (instanceFromReserv.getState().getName()
							.equals(InstanceStateName.Running.toString())) {
						lsInstances.add(instanceFromReserv);
					}
				}
			}
		}
		for (Instance instance : lsInstances) {
			ipPermsToEachInstance.put(instance,
					getPermissibleIpPermsForInstance(instance));
		}
	}

	public void loadSecurityGroups() {
		DescribeSecurityGroupsResult securityGroupRslt = AWSConfigLoader
				.getAmazonEC2().describeSecurityGroups();
		if (securityGroupRslt != null
				&& securityGroupRslt.getSecurityGroups() != null
				&& !securityGroupRslt.getSecurityGroups().isEmpty()) {
			for (SecurityGroup sGroup : securityGroupRslt.getSecurityGroups()) {
				mapSGroupsWithId.put(sGroup.getGroupId(), sGroup);
			}
		}
	}

	public OptimizedIpPerms getPermissibleIpPermsForInstance(Instance instance) {
		List<GroupIdentifier> instanceSGroupIdentifiers = instance
				.getSecurityGroups();
		OptimizedIpPerms instOptimizedIpPerms = new OptimizedIpPerms();
		for (GroupIdentifier sGroupIdentifier : instanceSGroupIdentifiers) {
			SecurityGroup instSGroup = mapSGroupsWithId.get(sGroupIdentifier
					.getGroupId());
			List<IpPermission> sGroupIpPerms = instSGroup.getIpPermissions();
			for (int k = 0; k < sGroupIpPerms.size(); k++) {
				instOptimizedIpPerms.merge(sGroupIpPerms.get(k));
			}
		}
		return instOptimizedIpPerms;
	}

	public List<Instance> findStartPointInstances() {
		return findInstancesWithPort(START_POINT_ACCESS_PORT);
	}

	public List<Instance> findInstancesWithPort(int port) {
		Set<Map.Entry<Instance, OptimizedIpPerms>> instanceWithIpPerms = ipPermsToEachInstance
				.entrySet();
		List<Instance> instancesOpenToPort = new ArrayList<Instance>();
		for (Map.Entry<Instance, OptimizedIpPerms> instanceIpPermsEntry : instanceWithIpPerms) {
			OptimizedIpPerms instanceIpPerms = instanceIpPermsEntry.getValue();
			List<PermissibleSocketModel> lsSkts = instanceIpPerms
					.getPermissibleSktsToProtocol().get("tcp");
			if (lsSkts != null && !lsSkts.isEmpty()) {
				for (PermissibleSocketModel permissibleSkt : lsSkts) {
					if (permissibleSkt.isAllIps()
							&& permissibleSkt.isAllowedPort(port)) {
						instancesOpenToPort.add(instanceIpPermsEntry.getKey());
						break;
					}
				}
			}
		}
		return instancesOpenToPort;
	}

	private boolean canTalkOn(OptimizedIpPerms ipPermDtls, Instance instance) {
		List<PermissibleSocketModel> lsSkts = ipPermDtls
				.getPermissibleSktsToProtocol().get("tcp");
		for (GroupIdentifier sGroupIdentifier : instance.getSecurityGroups()) {
			String sGroupId = sGroupIdentifier.getGroupId();
			if (ipPermDtls.getAssociatedSGroupIds().contains(sGroupId)) {
				return true;
			}
		}
		String privateIpOfOtherInst = instance.getPrivateIpAddress();
		if (lsSkts != null && !lsSkts.isEmpty()) {
			for (PermissibleSocketModel permissibleSkt : lsSkts) {
				if (permissibleSkt.isAllIps()
						|| isPermissibleIp(permissibleSkt.getIpAddress(),
								privateIpOfOtherInst)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isPermissibleIp(String sourcePermissibleIp, String inputIp) {
		if (sourcePermissibleIp == null || inputIp == null
				|| sourcePermissibleIp.equals("") || inputIp.equals(""))
			return false;
		String[] sourcePermisIPCIDRParts = sourcePermissibleIp.split("\\.");
		String[] inputIpCIDRParts = inputIp.split("\\.");
		for (int i = 0; i < 4; i++) {
			int sourcePermisIpPart = Integer
					.parseInt(sourcePermisIPCIDRParts[i]);
			int inputIpPart = Integer.parseInt(inputIpCIDRParts[i]);
			if ((sourcePermisIpPart & inputIpPart) != sourcePermisIpPart) {
				return false;
			}
		}
		return true;
	}

}

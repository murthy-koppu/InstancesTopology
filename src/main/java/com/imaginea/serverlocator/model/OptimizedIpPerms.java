package com.imaginea.serverlocator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.UserIdGroupPair;
import com.imaginea.serverlocator.util.AWSConfigLoader;

public class OptimizedIpPerms {

	private Set<String> associatedSGroupIdsToProtocol = new HashSet<String>();
	private Map<String, List<PermissibleSocketModel>> permissibleSktsToProtocol = new HashMap<String, List<PermissibleSocketModel>>();
	private boolean isAllIps = false;

	public boolean isAllIps() {
		return isAllIps;
	}

	public void setAllIps(boolean isAllIps) {
		this.isAllIps = isAllIps;
	}

	public Set<String> getAssociatedSGroupIds() {
		return associatedSGroupIdsToProtocol;
	}

	public Map<String, List<PermissibleSocketModel>> getPermissibleSktsToProtocol() {
		return permissibleSktsToProtocol;
	}

	public void merge(IpPermission inIpPermission) {
		List<UserIdGroupPair> lsAssocUserSGroups = inIpPermission
				.getUserIdGroupPairs();
		if (inIpPermission.getIpProtocol().equals("tcp")) {
			if (lsAssocUserSGroups != null && !lsAssocUserSGroups.isEmpty()) {
				for (UserIdGroupPair assocSGroup : lsAssocUserSGroups) {
					if (assocSGroup.getUserId().equals(
							AWSConfigLoader.getAccountId())) {
						associatedSGroupIdsToProtocol.add(assocSGroup
								.getGroupId());
					}
				}
			}
			List<PermissibleSocketModel> permissibleSockets = permissibleSktsToProtocol
					.get(inIpPermission.getIpProtocol());
			if (inIpPermission.getIpRanges() == null
					|| inIpPermission.getIpRanges().isEmpty())
				return;
			/*
			 * String inIpAddress = inIpPermission.getIpRanges().get(0); if
			 * (inIpAddress.equals("")) { return; }
			 */
			for (String inIpAddress : inIpPermission.getIpRanges()) {
				boolean isMerged = false;
				if (permissibleSockets != null) {
					for (int k = 0; k < permissibleSockets.size() && !isMerged; k++) {
						{
							PermissibleSocketModel permissibleIpSkt = permissibleSockets
									.get(k);
							if ((permissibleIpSkt.getIpAddress() + "/" + permissibleIpSkt
									.getSubNetMask()).equals(inIpAddress)) {
								permissibleIpSkt.addToPermissiblePorts(
										inIpPermission.getFromPort(),
										inIpPermission.getToPort());
								isMerged = true;
							}
						}
					}
				}
				if (permissibleSockets == null || !isMerged) {
					if (permissibleSockets == null) {
						permissibleSockets = new ArrayList<PermissibleSocketModel>();
						permissibleSktsToProtocol.put(
								inIpPermission.getIpProtocol(),
								permissibleSockets);
					}
					permissibleSockets.add(new PermissibleSocketModel(
							inIpPermission));
				}
			}
		}

	}

	/*
	 * private boolean mergeOnIPAddress(PermissibleSocketModel existingSkt,
	 * IpPermission newIpPerms) { String[] newIpParts =
	 * newIpPerms.getIpRanges().get(0).split("/"); int newSubNetMask =
	 * Integer.parseInt(newIpParts[1]); int subnetMaskDelta =
	 * existingSkt.getSubNetMask() - newSubNetMask; if (subnetMaskDelta == 0) {
	 * if (existingSkt.getIpAddress().equals(newIpParts[0])) { return true; }
	 * return false; } else { String[] newIpAddrCIDRParts =
	 * newIpParts[0].split("."); String[] existingIpAddrCIDRParts =
	 * existingSkt.getIpAddress() .split("."); int minimumMask = subnetMaskDelta
	 * < 0 ? existingSkt.getSubNetMask() : newSubNetMask; for (int k = 0; k < 4
	 * && minimumMask > 0; k++, minimumMask -= 8) { int newIpCIDRPart =
	 * Integer.parseInt(newIpAddrCIDRParts[k]); int existingIpCIDRPart = Integer
	 * .parseInt(existingIpAddrCIDRParts[k]); int subNetMaskResult =
	 * newIpCIDRPart & existingIpCIDRPart; if (subnetMaskDelta < 0) { if
	 * (subNetMaskResult != existingIpCIDRPart) return false; } else { if
	 * (subNetMaskResult != newIpCIDRPart) return false; } }
	 * existingSkt.setIpAddress(subnetMaskDelta < 0 ? existingSkt
	 * .getIpAddress() : newIpParts[0]); existingSkt.setSubNetMask(minimumMask);
	 * existingSkt.addToPermissiblePorts(newIpPerms.getFromPort(),
	 * newIpPerms.getToPort()); return true; } }
	 */
	/*
	 * private void mergeOnPorts(IpPermission newIpPerms, PermissibleSocketModel
	 * existingSkt){
	 * 
	 * } private void mergeOnPorts(IpPermission newIpPerms) { int newFromPort =
	 * newIpPerms.getFromPort(); int newToPort = newIpPerms.getToPort();
	 * PermissibleSocketModel otherObjIpPort = new
	 * PermissibleSocketModel(newIpPerms); if (permissibleSkts.isEmpty() ==
	 * true) { permissibleSkts.add(otherObjIpPort); }else if (newFromPort == -1)
	 * { permissibleSkts.clear(); permissibleSkts.add(otherObjIpPort); return;
	 * }else { PermissibleSocketModel existingPorts = null; existingPorts =
	 * permissibleSkts.get(0); if (existingPorts.isAllPorts()) { return; } for
	 * (int index = 0; index < permissibleSkts.size(); index++) { existingPorts
	 * = permissibleSkts.get(index); if(newFromPort <
	 * existingPorts.getFromPort()){ existingPorts.setFromPort(newFromPort);
	 * }else if(existingPorts.getFromPort() < newFromPort &&
	 * existingPorts.getToPort() > newToPort){ break; }
	 * if(existingPorts.getToPort() < newToPort){ while(index+1 <
	 * permissibleSkts.size() && permissibleSkts.get(index+1).getToPort() <
	 * newToPort){ permissibleSkts.remove(index+1); } if(index+1 ==
	 * permissibleSkts.size() || permissibleSkts.get(index+1).getFromPort() >
	 * newToPort){ existingPorts.setToPort(newToPort); }else
	 * if(permissibleSkts.get(index+1).getFromPort() <= newToPort){
	 * existingPorts.setToPort(permissibleSkts.get(index+1).getToPort());
	 * permissibleSkts.remove(index+1); } } } } if (newIpPerms.getFromPort() ==
	 * newIpPerms.getToPort()) { if (newIpPerms.getFromPort() == -1) {
	 * permissibleSkts.clear(); //ipAndPortPerms.add(new IpPortDetails(-1, -1));
	 * } else {
	 * 
	 * } } }
	 */
}

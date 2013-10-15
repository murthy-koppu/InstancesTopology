package com.imaginea.serverlocator.model;

import java.util.Arrays;

import com.amazonaws.services.ec2.model.IpPermission;

public class PermissibleSocketModel {
	private String ipAddress = "255.255.255.255";
	private int subNetMask = -1;
	private boolean isAllPorts;
	private boolean[] isAllowedPort = new boolean[65536];
	private boolean isAllIps;

	@Override
	public String toString() {
		return "PermissibleSocketModel [ipAddress=" + ipAddress
				+ ", subNetMask=" + subNetMask + ", isAllPorts=" + isAllPorts
				+ ", isAllowedPort=" + Arrays.toString(isAllowedPort)
				+ ", isAllIps=" + isAllIps + "]";
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getSubNetMask() {
		return subNetMask;
	}

	public void setSubNetMask(int subNetMask) {
		this.subNetMask = subNetMask;
	}

	public PermissibleSocketModel(IpPermission ipPermission) {
		super();
		initializeIpAddress(ipPermission);
		addToPermissiblePorts(ipPermission.getFromPort(),
				ipPermission.getToPort());
	}

	private void initializeIpAddress(IpPermission ipPermission) {
		String[] ipAndSubnetArr = ipPermission.getIpRanges().get(0).split("/");
		this.subNetMask = Integer.parseInt(ipAndSubnetArr[1]);
		this.ipAddress = ipAndSubnetArr[0];
		if(this.subNetMask == 0){
			isAllIps = true;
		}
	}

	public void addToPermissiblePorts(int fromPort, int toPort) {
		if (fromPort == -1) {
			isAllPorts = true;
		} else {
			for (int k = fromPort; k <= fromPort; k++) {
				isAllowedPort[k] = true;
			}
		}
	}

	public boolean isAllowedPort(int queriedPort) {
		return isAllowedPort[queriedPort];
	}

	public boolean isAllPorts() {
		return isAllPorts;
	}

	public boolean isAllIps() {
		return isAllIps;
	}

	public void setAllIps(boolean isAllIps) {
		this.isAllIps = isAllIps;
	}

}

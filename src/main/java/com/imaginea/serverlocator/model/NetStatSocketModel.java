package com.imaginea.serverlocator.model;

import java.util.Set;

import org.apache.log4j.Logger;

import com.imaginea.serverlocator.util.Utils;

public class NetStatSocketModel {
	private static Logger log = Logger.getLogger(NetStatSocketModel.class);
	private String ipAddress;
	private int port;
	private boolean isAllPorts;
	private boolean isAllIps;

	public NetStatSocketModel(String ipAddress, String port, boolean isLocalIp)
			throws Exception {
		super();
		setIpAddress(ipAddress, isLocalIp);
		setPort(port);
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress, boolean isLocalIp) {
		ipAddress = Utils.downToIpV4(ipAddress);
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setPort(String port) throws Exception {
		try {
			if (port.equals("*")) {
				this.port = Integer.MAX_VALUE;
				this.isAllPorts = true;
			} else
				this.port = Integer.parseInt(port);
		} catch (Exception e) {
			log.error("Invalid Port Number parsed " + e);
			throw e;
		}
	}

	public boolean isAllPorts() {
		return isAllPorts;
	}

	public void setAllPorts(boolean isAllPorts) {
		this.isAllPorts = isAllPorts;
	}

	public boolean isAllIps() {
		return isAllIps;
	}

	public void setAllIps(boolean isAllIps) {
		this.isAllIps = isAllIps;
	}

}

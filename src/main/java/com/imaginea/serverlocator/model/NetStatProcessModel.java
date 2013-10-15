package com.imaginea.serverlocator.model;

public class NetStatProcessModel {
/*	private String localIp;
	private String foreignIp;
*/	private String processName;
	private String state;
/*	private int localPort;
	private int foreignPort;
*/	private int processId;
	private NetStatSocketModel localSkt;
	private NetStatSocketModel foreignSkt;

	public NetStatProcessModel(String processName, String state, String processId,
			NetStatSocketModel localSkt, NetStatSocketModel foreignSkt) {
		super();
		this.processName = processName;
		this.state = state;
		setProcessId(processId);
		this.localSkt = localSkt;
		this.foreignSkt = foreignSkt;
	}

/*	public String getLocalIp() {
		return localIp;
	}

	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}

	public String getForeignIp() {
		return foreignIp;
	}

	public void setForeignIp(String foreignIp) {
		this.foreignIp = foreignIp;
	}
*/
	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

/*	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public int getForeignPort() {
		return foreignPort;
	}

	public void setForeignPort(int foreignPort) {
		this.foreignPort = foreignPort;
	}
*/
	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

	public void setProcessId(String processId) {
		try{
			this.processId = Integer.parseInt(processId);
		}catch(Exception e){
			this.processId = -1;
		}
	}
	
	public NetStatSocketModel getLocalSkt() {
		return localSkt;
	}

	public void setLocalSkt(NetStatSocketModel localSkt) {
		this.localSkt = localSkt;
	}

	public NetStatSocketModel getForeignSkt() {
		return foreignSkt;
	}

	public void setForeignSkt(NetStatSocketModel foreignSkt) {
		this.foreignSkt = foreignSkt;
	}

}

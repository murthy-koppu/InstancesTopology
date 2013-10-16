package com.imaginea.serverlocator.model;

public class NetStatProcessModel {
	private String processName;
	private String state;
	private int processId;
	private NetStatSocketModel localSkt;
	private NetStatSocketModel foreignSkt;

	public NetStatProcessModel(String processName, String state,
			String processId, NetStatSocketModel localSkt,
			NetStatSocketModel foreignSkt) {
		super();
		this.processName = processName;
		this.state = state;
		setProcessId(processId);
		this.localSkt = localSkt;
		this.foreignSkt = foreignSkt;
	}

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

	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

	public void setProcessId(String processId) {
		try {
			this.processId = Integer.parseInt(processId);
		} catch (Exception e) {
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

package com.imaginea.serverlocator.model;
public class KeyPairUserIdModel {
	private String keyPair;
	private String userId;

	public KeyPairUserIdModel(String keyPair, String userId) {
		super();
		this.keyPair = keyPair;
		this.userId = userId;
	}

	public String getKeyPair() {
		return keyPair;
	}

	public String getUserId() {
		return userId;
	}
}

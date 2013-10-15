package com.imaginea.serverlocator.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.imaginea.serverlocator.model.KeyPairUserIdModel;

public class AWSInstanceKeyPairLoader {
	private static Map<String, KeyPairUserIdModel> instanceIdToKeyPairNameMap = new HashMap<String, KeyPairUserIdModel>();
	private final static String KEY_PAIR_INSTANCES_META_FILE_LOCATION = ApplicationConstants.RESOURCES_LOCATION_PATH
			+ "AWSInstanceKeyPairs/InstancePrivateKeyFileMap/KeyPairInstanceIdsMap.properties";
	static{
		loadInstancesKeyPairMap();
	}
	
	public static KeyPairUserIdModel getKeyPairUserIdModel(String instanceId) {
		KeyPairUserIdModel keyPairFilePath = instanceIdToKeyPairNameMap
				.get(instanceId);
		return keyPairFilePath==null?null:keyPairFilePath;
	}

	private static void loadInstancesKeyPairMap() {
		BufferedReader keyPairInstanceIdFileReader;

		try {
			keyPairInstanceIdFileReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(
							KEY_PAIR_INSTANCES_META_FILE_LOCATION)));
			String strKeyPairInstances = null;
			while ((strKeyPairInstances = keyPairInstanceIdFileReader
					.readLine()) != null) {
				String[] keyPairInstances = strKeyPairInstances.split("=");
				String keyPairName = keyPairInstances[0];
				String[] instanceAuthProperties = keyPairInstances[1]
						.split(";");
				for (String instanceAuth : instanceAuthProperties) {
					String[] instanceIdAndUserId = instanceAuth.split(",");
					KeyPairUserIdModel instanceAuthObj = new KeyPairUserIdModel(keyPairName, instanceIdAndUserId[1]);
					instanceIdToKeyPairNameMap.put(instanceIdAndUserId[0], instanceAuthObj);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
}

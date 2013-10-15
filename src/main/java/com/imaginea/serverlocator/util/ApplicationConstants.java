package com.imaginea.serverlocator.util;

public interface ApplicationConstants {
	public static final int MILLIS_PER_SECOND = 1000;
	public static final String WEB_SERVER_HEADER_NAME = "Server";
	public static final int MYSQL_SERVER_MIN_TIME_OUT_PERIOD = 10 * MILLIS_PER_SECOND;
	public static final int MYSQL_SERVER_MAX_TIME_OUT_PERIOD = 20 * MILLIS_PER_SECOND;
	public static final int ORACLE_DB_MIN_TIME_OUT_PERIOD = 20 * MILLIS_PER_SECOND;
	public static final int ORACLE_DB_MAX_TIME_OUT_PERIOD = 40 * MILLIS_PER_SECOND;
	public static final int APP_SERVER_MIN_TIME_OUT_PERIOD = 5 * MILLIS_PER_SECOND;
	public static final int APP_SERVER_MAX_TIME_OUT_PERIOD = 10 * MILLIS_PER_SECOND;
	public static final String NET_STAT_PROCESS_STATE_LISTEN = "LISTEN";
	public static final String IPV6_REPRESENTATION_START_SUB_STRING = "::ffff:";
	public static final String RESOURCES_LOCATION_PATH = Utils
			.getResourcesLocationPath();
	public static final String GENERIC_DEPLOYMENT_JSON_DATA_LOCATION = "webapp/js/data_25.js";
	public static final String CONCRETE_DEPLOYMENT_JSON_DATA_LOCATION = "webapp/js/data_26.js";
	public static final String TOPOLOGY_INSTANCE_PRIVATE_IP_PROP_NAME = "name";
	public static final String TOPOLOGY_INSTANCE_NODE_SERIAL_NO = "serialNo";
	public static final String TOPOLOGY_INSTANCE_NODE_INSTANCE_ID = "instanceId";
	public static final String TOPOLOGY_INSTANCES_NODES_PARENT = "nodes";
	public static final String TOPOLOGY_INSTANCE_IS_START_POINT = "isStartPoint";
	public static final String TOPOLOGY_INSTANCE_LINK_SOURCE = "source";
	public static final String TOPOLOGY_INSTANCE_LINK_TARGET = "target";
	public static final String TOPOLOGY_INSTANCE_LINK_RELATIONSHIP = "relationship";
	public static final String TOPOLOGY_INSTANCE_LINKS_PARENT = "links";
	public static final String TOPOLOGY_INSTANCE_NODE_PUBLIC_DNS = "publicDNS";
	public static final int REMOTE_SSH_PORT_NO = 22;
	/*public static final String KEY_PAIR_INSTANCES_META_FILE_LOCATION = RESOURCES_LOCATION_PATH
			+ "AWSInstanceKeyPairs/InstancePrivateKeyFileMap/KeyPairInstanceIdsMap.properties";*/
	public static final String INSTANCE_KEY_PAIRS_LOCATION = RESOURCES_LOCATION_PATH
			+ "AWSInstanceKeyPairs/";
}

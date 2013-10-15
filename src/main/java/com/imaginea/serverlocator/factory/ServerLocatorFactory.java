package com.imaginea.serverlocator.factory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.imaginea.serverlocator.impl.AppServerLocator;
import com.imaginea.serverlocator.impl.MySQLLocator;
import com.imaginea.serverlocator.impl.OracleDBLocator;
import com.imaginea.serverlocator.impl.ServerProperties;
import com.imaginea.serverlocator.util.ConnectionProperties;
import com.imaginea.serverlocator.util.ServersEnum;

public class ServerLocatorFactory {
	static Logger log = Logger.getLogger(ServerLocatorFactory.class);

	public static ServerProperties getServerLocator(
			ServersEnum[] optimizedChoices, InetAddress iNetAddr, int port) {
		log.debug("*********** Inside ServerLocatorFactory -> getServerLocator( ) **********");
		log.info("Execution started for HostAddress is " + iNetAddr.toString()
				+ " and port number is " + port);
		log.debug("Optimized Choice provided " + optimizedChoices == null ? "Empty"
				: Arrays.toString(optimizedChoices));
		
		ServerProperties serverProp = new ServerProperties();
		Socket clientSocket = null;
		try {
			clientSocket = new Socket(iNetAddr, port);
		} catch (IOException e) {
			log.debug(
					"Unable to connect to server with provided host and port details",
					e);
			serverProp
					.setConnectionStatus(ConnectionProperties.SERVER_UNREACHABLE);
			return serverProp;
		} finally {
			try {
				if (clientSocket != null)
					clientSocket.close();
			} catch (IOException e) {
				log.warn("Unable to close the client socket", e);
			}
			log.info(" Exiting Execution For requested HostName " + serverProp.getHostName()
					+ " on port " + serverProp.getPortNo());
		}
		serverProp = chooseServerLocatorFmChoices(optimizedChoices, iNetAddr,
				port);
	
		log.debug("******** Exiting ServerLocatorFactory -> getServerLocator( ) **********");
		log.info(" Exiting Execution For requested HostName " + serverProp.getHostName()
				+ " on port " + serverProp.getPortNo());
		
		return serverProp;
	}

	private static ServerProperties chooseServerLocatorFmChoices(
			ServersEnum[] optimizedChoices, InetAddress iNetAddr, int port) {
		log.debug("*********** Inside ServerLocatorFactory -> chooseServerLocatorFmChoices( ) **********");
		ServerProperties serverProp = null;
		if (optimizedChoices == null) {
			optimizedChoices = ServersEnum.values();
		}
		log.debug("Optimized Choice selected "
				+ Arrays.toString(optimizedChoices));
		for (int i = 0; i < 2; i++) {
			for (ServersEnum inServerChoice : optimizedChoices) {
				serverProp = findServerDetails(iNetAddr, port, inServerChoice,
						i == 0 ? true : false);
				if (serverProp != null) {
					serverProp.setHostName(iNetAddr.getHostName());
					serverProp.setPortNo(port);
					serverProp
							.setConnectionStatus(ConnectionProperties.SERVER_LISTENING);
					log.debug("Found Server Details with Server Name "
							+ serverProp.getServerName() + " and version "
							+ serverProp.getVersion());
					log.debug("******** Exiting ServerLocatorFactory -> chooseServerLocatorFmChoices( ) **********");
					return serverProp;
				}
			}
		}

		log.debug("Unable to find from list of known servers");
		serverProp = new ServerProperties();
		serverProp
				.setConnectionStatus(ConnectionProperties.UNIDENTIFIED_SERVER_LISTENING);
		log.debug("******** Exiting ServerLocatorFactory -> chooseServerLocatorFmChoices( ) **********");
		return serverProp;
	}

	public static ServerProperties getServerLocator(InetAddress iNetAddr,
			int port) {
		return getServerLocator(null, iNetAddr, port);
	}

	public static ServerProperties getServerLocator(
			ServersEnum[] optimizedChoices, String hostName, int port) {
		InetAddress iNetAddr = null;
		try {
			iNetAddr = InetAddress.getByName(hostName);
		} catch (UnknownHostException e) {
			log.debug("Unable to identify provided hostname", e);
			return new ServerProperties();
		}
		return getServerLocator(optimizedChoices, iNetAddr, port);
	}

	public static ServerProperties getServerLocator(String hostName, int port) {
		return getServerLocator(null, hostName, port);
	}

	private static ServerProperties findServerDetails(InetAddress iNetAddr,
			int port, ServersEnum inServerChoice, boolean isLimitedTimeOut) {
		log.debug("*** Inside ServerLocatorFactory -> findServerDetails( ) with selected Choice "
				+ inServerChoice.toString()
				+ " and limitedTimeOut field as "
				+ isLimitedTimeOut + " ***");
		switch (inServerChoice) {

		case APP_SERVER: {
			return new AppServerLocator().parseToServerProp(iNetAddr, port,
					isLimitedTimeOut);
		}
		case MY_SQL: {
			return new MySQLLocator().parseToServerProp(iNetAddr, port,
					isLimitedTimeOut);
		}
		case ORACLE_DB: {
			return new OracleDBLocator().parseToServerProp(iNetAddr, port,
					isLimitedTimeOut);
		}

		}
		return null;
	}
}

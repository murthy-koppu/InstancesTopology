package com.imaginea.serverlocator.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.imaginea.serverlocator.util.ApplicationConstants;

public class AppServerLocator implements ServerLocator, ApplicationConstants {
	static Logger log = Logger.getLogger(AppServerLocator.class);
	private static Map<String, String> serverNameAliasMapDescription = new HashMap<String, String>();

	@Override
	public ServerProperties parseToServerProp(InetAddress iNetAddr, int portNo,
			boolean isLimitedTimeOut) {
		log.debug("********** Entered into ApacheServerLocator --> parseToServerProp() ********");
		String urlAddress = "http://" + iNetAddr.getHostAddress() + ":"
				+ portNo + "/";
		log.debug("URL Address " + urlAddress);
		URL url = null;
		URLConnection connection = null;
		try {
			url = new URL(urlAddress);
			connection = loadConnectionFromUrl(isLimitedTimeOut, url);
			String serverDetails = connection
					.getHeaderField(WEB_SERVER_HEADER_NAME);
			if (serverDetails != null && !serverDetails.isEmpty()) {
				ServerProperties serverProp = new ServerProperties();
				serverProp.setServerName(serverDetails.trim());
				log.debug("Host server name Identified is " + serverDetails);
				return serverProp;
			}
		} catch (MalformedURLException e) {
			log.error("Invalid server Address details " + e);
			return null;
		} catch (IOException e) {
			log.debug("Unable to connect to server " + e);
			return null;
		} finally {
			if (connection != null) {
				try {
					((HttpURLConnection) connection).disconnect();
				} catch (Exception e) {
					log.warn("Unable to close Url connection", e);
				}
			}
			log.debug("********** Exiting ApacheServerLocator --> parseToServerProp() ********");
		}
		log.debug("Current execution Server details failed for App Server Test");
		return null;
	}

	private URLConnection loadConnectionFromUrl(boolean isLimitedTimeOut,
			URL url) throws IOException {
		log.debug("*** Entered into ApacheServerLocator --> loadConnectionFromUrl() ***");
		URLConnection connection;
		connection = url.openConnection();
		int connectionTimeOut = isLimitedTimeOut ? APP_SERVER_MIN_TIME_OUT_PERIOD
				: APP_SERVER_MAX_TIME_OUT_PERIOD;
		connection.setConnectTimeout(connectionTimeOut);
		connection.setReadTimeout(connectionTimeOut);
		connection.setDoOutput(true);
		log.debug("*** Exiting ApacheServerLocator --> loadConnectionFromUrl() ***");
		return connection;
	}

}

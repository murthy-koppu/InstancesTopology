package com.imagine.serverlocator.factory;

import static junit.framework.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.imaginea.serverlocator.factory.ServerLocatorFactory;
import com.imaginea.serverlocator.locators.ServerProperties;
import com.imaginea.serverlocator.util.ConnectionProperties;
import com.imaginea.serverlocator.util.ServersEnum;

public class TestServerLocatorFactory {

	@Test
	public void testGetServerLocatorStringIntMySQL() {
		String hostAddress = "localhost";
		ServerProperties serverProp = ServerLocatorFactory.getServerLocator(hostAddress,3306);
		assertEquals(serverProp.getConnectionStatus(), ConnectionProperties.SERVER_LISTENING);
		assertEquals(serverProp.getHostName(), hostAddress);
		assertEquals(serverProp.getPortNo(), 3306);
		assertEquals(serverProp.getServerName(), ServersEnum.MY_SQL.toString());
		assertEquals(serverProp.getVersion(), "5.5.27");		
	}
	
	@Test
	public void testGetServerLocatorStringIntFailOnIp() {
		String hostAddress = "wronghost";
		ServerProperties serverProp = ServerLocatorFactory.getServerLocator(hostAddress,3306);
		assertEquals(serverProp.getConnectionStatus(), ConnectionProperties.HOST_UNREACHABLE);
	}
	
	@Test
	public void testGetServerLocatorStringIntAppServer() {
		String hostAddress = "localhost";
		int port = 8080;
		ServerProperties serverProp = ServerLocatorFactory.getServerLocator(hostAddress,port);
		assertEquals(serverProp.getConnectionStatus(), ConnectionProperties.SERVER_LISTENING);
		assertEquals(serverProp.getHostName(), hostAddress);
		assertEquals(serverProp.getPortNo(), port);
		assertEquals(serverProp.getServerName(), "Apache-Coyote/1.1");
	}
	
	@Test
	public void testGetServerLocatorStringIntOracle() {
		String hostAddress = "127.0.0.1";
		int port = 1521;
		ServerProperties serverProp = ServerLocatorFactory.getServerLocator(hostAddress,port);
		assertEquals(serverProp.getConnectionStatus(), ConnectionProperties.SERVER_LISTENING);
		assertEquals(serverProp.getHostName(), hostAddress);
		assertEquals(serverProp.getPortNo(), port);
		assertEquals(serverProp.getServerName(), ServersEnum.ORACLE_DB.toString());
	}


}

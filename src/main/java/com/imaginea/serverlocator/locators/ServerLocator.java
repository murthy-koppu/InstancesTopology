package com.imaginea.serverlocator.locators;

import java.net.InetAddress;

public interface ServerLocator {
	public ServerProperties parseToServerProp(InetAddress iNetAddr, int port, boolean isLimitedTimeOut);
}

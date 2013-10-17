package com.imaginea.serverlocator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.imaginea.serverlocator.model.NetStatProcessModel;
import com.imaginea.serverlocator.model.NetStatSocketModel;
import com.imaginea.serverlocator.util.ApplicationConstants;
import com.imaginea.serverlocator.util.Utils;

public class NetStatLinuxParser implements ApplicationConstants {
	private static Logger log = Logger.getLogger(NetStatLinuxParser.class);
	List<NetStatProcessModel> lsNetStatRecs = new ArrayList<NetStatProcessModel>();
	Set<String> localIps = new HashSet<String>();

	public NetStatLinuxParser() {
		localIps.addAll(Utils.getDefaultLocalIps());
	}

	public List<NetStatProcessModel> getValidNetStatRecords(
			String netStatInput, List<String> standardLocalIps)
			throws Exception {
		String[] netStatRecords = netStatInput.split(";");
		for (String netStatRecord : netStatRecords)
			parseNetStatRecord(netStatRecord);

		return lsNetStatRecs;
	}

	private void parseNetStatRecord(String netStatRecord) throws Exception {
		try {
			String[] netStatAttribs = netStatRecord.split(",");
			String strLocalSkt = netStatAttribs[0];
			String strForeignSocket = netStatAttribs[1];
			String connectionState = netStatAttribs[2];
			String[] processDtls = netStatAttribs[3].split("/");

			int ipAddrBound = strLocalSkt.lastIndexOf(":");

			NetStatSocketModel localSktMdl = new NetStatSocketModel(
					strLocalSkt.substring(0, ipAddrBound),
					strLocalSkt.substring(ipAddrBound + 1), true);

			if (!isInternalPort(localSktMdl)) {
				ipAddrBound = strForeignSocket.lastIndexOf(":");
				String foreignIpAddr = strForeignSocket.substring(0,
						ipAddrBound);
				if (foreignIpAddr
						.startsWith(IPV6_REPRESENTATION_START_SUB_STRING)) {
					foreignIpAddr = Utils.downToIpV4(foreignIpAddr);
				}
				NetStatSocketModel foreignSktMdl = new NetStatSocketModel(
						strForeignSocket.substring(0, ipAddrBound),
						strForeignSocket.substring(ipAddrBound + 1), false);

				if (NET_STAT_PROCESS_STATE_LISTEN
						.equals(connectionState)
						|| (!isInternalPort(foreignSktMdl) && !localIps
								.contains(foreignIpAddr))) {
					NetStatProcessModel netStatRec = new NetStatProcessModel(
							processDtls[1], connectionState, processDtls[0],
							localSktMdl, foreignSktMdl);
					lsNetStatRecs.add(netStatRec);
				}
			}

		} catch (Exception e) {
			log.error("Unable to parse NetStat Record " + netStatRecord);
			throw e;
		}

	}

	private boolean isInternalPort(NetStatSocketModel sktMdlIn) {
		return (sktMdlIn.getPort() < 1025 || sktMdlIn.isAllPorts()) ? true
				: false;
	}
	
	public List<String> getStandardSystemIP(String systemIps) {
		String[] systemIpsArr = systemIps.split(";");
		List<String> standardIps = new ArrayList<String>(systemIpsArr.length);
		for (String systemIp : systemIpsArr) {
			if (Utils.getDefaultLocalIps().contains(systemIp)) {
				continue;
			} else if (systemIp
					.startsWith(IPV6_REPRESENTATION_START_SUB_STRING)) {
				standardIps.add(Utils.downToIpV4(systemIp));
			} else {
				standardIps.add(systemIp);
			}
		}
		return standardIps;
	}

}

package com.imaginea.serverlocator.factory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerLocator {
	public void findServerDetailsOnNetStats(JSONObject netStatJson){
		try {
			JSONArray jsonLocalServers = netStatJson.getJSONArray("LocalServers");
			for(int k=0; k <jsonLocalServers.length() ; k++){
				JSONObject jsonLocalServ = (JSONObject) jsonLocalServers.get(k);
				String internalServerPort = jsonLocalServ.get("LocalServerPort").toString();
				System.out.println("Server Running is "+ServerLocatorFactory.getServerLocator("localhost", Integer.parseInt(internalServerPort)));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

package com.imaginea.serverlocator.controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.serverlocator.InstancesCanTalkTopology;
import com.imaginea.serverlocator.InstancesTalkingTopology;

public class SpecificTopology extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		JSONObject genericTopologyData = SpecificInstancesTopology.specificTopologyJson;
		if (genericTopologyData == null) {
			try {
				if (InstancesTopology.genericTopologyData != null) {
					genericTopologyData = new JSONObject(
							InstancesTopology.genericTopologyData.toString());
				}
				if (genericTopologyData == null) {
					genericTopologyData = new InstancesCanTalkTopology()
							.getCanTalkOnTopology();
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			InstancesTalkingTopology concreateTopologyPublisher = new InstancesTalkingTopology();
			try {
				genericTopologyData = concreateTopologyPublisher
						.getIsTalkingOn(genericTopologyData);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		System.out
				.println("Entered talkingTo" + genericTopologyData.toString());
		response.getWriter().write(genericTopologyData.toString());
	}
}

package com.imaginea.serverlocator;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.imaginea.serverlocator.util.AWSInstanceUtil;

public class SpecificTopology extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		System.out.println("Entered speicific");
		JSONObject genericTopologyData =SpecificInstancesTopology.specificTopologyJson;
		System.out.println("final data is for specific "+genericTopologyData.toString());
		response.getWriter().write(genericTopologyData.toString());
		// response.setHeader("topologyData", genericTopologyData);

	}
}

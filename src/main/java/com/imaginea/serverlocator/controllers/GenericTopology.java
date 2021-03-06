package com.imaginea.serverlocator.controllers;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.imaginea.serverlocator.InstancesCanTalkTopology;

public class GenericTopology extends HttpServlet {
	protected void doGet(HttpServletRequest request,
		HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		JSONObject genericTopologyData =InstancesTopology.genericTopologyData;
		if(genericTopologyData == null){
			genericTopologyData = new InstancesCanTalkTopology().getCanTalkOnTopology();
		}
		System.out.println("Entered Generic"+genericTopologyData.toString());
		response.getWriter().write(genericTopologyData.toString());

	}
}

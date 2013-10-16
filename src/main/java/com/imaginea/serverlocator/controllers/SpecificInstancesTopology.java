package com.imaginea.serverlocator.controllers;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;
import com.imaginea.serverlocator.InstancesCanTalkTopology;
import com.imaginea.serverlocator.InstancesTalkingTopology;
 
public class SpecificInstancesTopology extends HttpServlet
{
	public static JSONObject specificTopologyJson = null;
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	try {
    		if(InstancesTopology.genericTopologyData != null){
    			specificTopologyJson = new JSONObject(InstancesTopology.genericTopologyData.toString());
    		}
    		if(specificTopologyJson == null){
				specificTopologyJson = new InstancesCanTalkTopology().getCanTalkOnTopology();
			}
    	} catch (Exception e1) {
			e1.printStackTrace();
		}
    	InstancesTalkingTopology concreateTopologyPublisher =new InstancesTalkingTopology();
    	try {
			specificTopologyJson = concreateTopologyPublisher.getIsTalkingOn(specificTopologyJson);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        
        RequestDispatcher view = request.getRequestDispatcher("specific.html");
        view.forward(request, response);
        
    }
}
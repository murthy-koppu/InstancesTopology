package com.imaginea.serverlocator;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;
import com.imaginea.serverlocator.util.AWSInstanceUtil;
 
public class SpecificInstancesTopology extends HttpServlet
{
	public static JSONObject specificTopologyJson = null;
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	try {
			specificTopologyJson = new JSONObject(InstancesTopology.genericTopologyData.toString());
			if(specificTopologyJson == null){
				specificTopologyJson = new AWSInstanceUtil().getCanTalkOnTopology();
			}
    	} catch (JSONException e1) {
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
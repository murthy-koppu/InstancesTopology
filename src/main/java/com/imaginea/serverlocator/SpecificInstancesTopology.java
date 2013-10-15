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
    	System.out.println("Entered");
    	try {
			specificTopologyJson = new JSONObject(InstancesTopology.genericTopologyData.toString());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	ConcreteTopologyPublisher concreateTopologyPublisher =new ConcreteTopologyPublisher();
    	try {
			specificTopologyJson = concreateTopologyPublisher.loadGenericTopologyFromJson(specificTopologyJson);
			System.out.println("final ouptup fo specific is "+specificTopologyJson.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
       // request.setAttribute("topologyData", genericTopologyData);
       // response.setHeader("topologyData", genericTopologyData);
        
        RequestDispatcher view = request.getRequestDispatcher("specific.html");
        view.forward(request, response);
        
    }
}
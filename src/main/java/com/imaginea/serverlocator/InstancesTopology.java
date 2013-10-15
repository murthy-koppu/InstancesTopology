package com.imaginea.serverlocator;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.amazonaws.services.simpleworkflow.flow.JsonDataConverter;
import com.imaginea.serverlocator.util.AWSInstanceUtil;
 
public class InstancesTopology extends HttpServlet
{
	public static JSONObject genericTopologyData = null;
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	System.out.println("Entered");
    	genericTopologyData = new AWSInstanceUtil().getInstanceRelationsInJson();
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
       // request.setAttribute("topologyData", genericTopologyData);
       // response.setHeader("topologyData", genericTopologyData);
        
        RequestDispatcher view = request.getRequestDispatcher("index.html");
        view.forward(request, response);
        
    }
}
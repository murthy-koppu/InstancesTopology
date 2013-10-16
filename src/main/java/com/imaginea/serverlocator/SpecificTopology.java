package com.imaginea.serverlocator;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.imaginea.serverlocator.util.AWSInstanceUtil;

public class SpecificTopology extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		JSONObject genericTopologyData =SpecificInstancesTopology.specificTopologyJson;
		if(genericTopologyData == null){
			JSONObject specificTopologyJson = null;
			try {
				if(InstancesTopology.genericTopologyData != null){
					specificTopologyJson = new JSONObject(InstancesTopology.genericTopologyData.toString());
				}				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
			if(specificTopologyJson == null){
				specificTopologyJson = new AWSInstanceUtil().getCanTalkOnTopology();
			}
			InstancesTalkingTopology concreateTopologyPublisher =new InstancesTalkingTopology();
			try {
				
				genericTopologyData = concreateTopologyPublisher.getIsTalkingOn(specificTopologyJson);
				System.out.println(genericTopologyData.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(genericTopologyData.toString());
		response.getWriter().write(genericTopologyData.toString());
	}
}

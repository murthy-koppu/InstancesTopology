package com.imaginea.serverlocator.retrials;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class UnderstandingSockets {
public static void main(String[] args) {
	Socket skt = null;
	try {
		skt = new Socket("localhost", 9006);
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		System.out.println("Socket value"+skt);
		skt.close();
		System.out.println("Socket closed");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}

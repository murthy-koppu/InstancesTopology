package com.imaginea.serverlocator.retrials;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class VerifyOnSockets {
	public static void main(String[] args) {
		try {
			Socket skt = new Socket("localhost/as", 8005);
			/*if(!skt.isBound()){
				throw new IOException();
			}*/
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

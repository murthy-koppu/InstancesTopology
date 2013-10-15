package com.imaginea.serverlocator.retrials;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class PingSystemContinuous {

	
	public static void main(String[] args) throws UnknownHostException, IOException {
		//getMessage();
		//pingMessage();
		pingMessageContent();
	}
	
	private static void pingMessage() throws IOException{
		Socket client = new Socket("localhost", 80);
		
		/*DataOutputStream dos = new DataOutputStream(client.getOutputStream());
		dos.writeBytes("SHUTDOWN");
		dos.flush();*/
		InputStream s1In = client.getInputStream();
		
		 DataInputStream dis = new DataInputStream(s1In);
		 Byte byteMsg = null;
		  long l =0;
		  System.out.println("Entering to print output");
		 while(l < 1000){
			 if((byteMsg = dis.readByte()) != null)
				 System.out.print((char)(byteMsg.byteValue()));
			 l++;
		 }
	}
	
	private static void pingMessageContent() throws IOException{
		Socket s = new Socket("localhost", 8080);

        BufferedReader in = new BufferedReader(new 
        InputStreamReader(s.getInputStream()));
          /*PrintWriter socketOut = new PrintWriter(s.getOutputStream());

        socketOut.print(":8080");
        socketOut.flush();
*/
        String line;

        while ((line = in.readLine()) != null){
            System.out.println(line);
        }
	}

	private static void getMessage() throws UnknownHostException, IOException,
			SocketException {
		Socket client = new Socket("localhost", 8080);
		 client.setKeepAlive(true);
		System.out.println("Inet address is "+client.getInetAddress());
		InputStream s1In = client.getInputStream();
		
		 DataInputStream dis = new DataInputStream(s1In);
		 Byte byteMsg = null;
		  long l =0;
		 while(l < 77){
			 if((byteMsg = dis.readByte()) != null)
				 System.out.print((char)(byteMsg.byteValue()));
			 l++;
		 }
		 SocketAddress serverAddr = client.getRemoteSocketAddress();
		 //client.bind(serverAddr);
		 DataOutputStream dos = new DataOutputStream(client.getOutputStream());
		 
		 dos.writeChars("Hello");
		 dos.flush();
		 while(true){
			 try{
				 if((byteMsg = dis.readByte()) != null)
					 System.out.print((char)(byteMsg.byteValue()));
				 			 
				 
			 }catch(Exception e){
				// e.printStackTrace();
				 break;
			 }
			 
		 }
	}
}

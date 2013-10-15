package com.imaginea.serverlocator.retrials;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;


public class MYSQLDBConnect {
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	   static final String DB_URL = "jdbc:mysql://localhost:3306/co_learning";

	   //  Database credentials
	   static final String USER = "root";
	   static final String PASS = "root";
	
	public static void main(String[] args) {
		 Connection conn = null;
		   Statement stmt = null;
		   try{
		      //STEP 2: Register JDBC driver
		      Class.forName("com.mysql.jdbc.Driver");
		      
		      //STEP 3: Open a connection
		      System.out.println("Connecting to database...");
		      conn = DriverManager.getConnection(DB_URL,USER,PASS);
		   }catch(Exception e){
			   e.printStackTrace();
		   }
	}
}

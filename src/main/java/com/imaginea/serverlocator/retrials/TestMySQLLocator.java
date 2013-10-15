package com.imaginea.serverlocator.retrials;
import com.imaginea.serverlocator.factory.ServerLocatorFactory;


public class TestMySQLLocator {
public static void main(String[] args) {
	ServerLocatorFactory.getServerLocator("localhost", 3306);
}
}

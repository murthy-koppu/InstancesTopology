package com.imaginea.serverlocator.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Properties;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;

public class AWSConfigLoader {
	private static Properties properties = new Properties();
	private static final String AWS_CREDENTITALS_PROP_FILE_NAME = "AwsCredentials.properties";
	private static AWSCredentialsLoader awsCredentialsLoader = new AWSCredentialsLoader();
	private static AmazonEC2 amazonEC2 = new AmazonEC2Client(
			awsCredentialsLoader);
	private static AmazonIdentityManagement amazonIM = new AmazonIdentityManagementClient(
			awsCredentialsLoader);
	private static Region region;
	private static String accountId;

	static {
		try {
			InputStream propFileInputStream = AWSConfigLoader.class
					.getClassLoader().getResourceAsStream("config.properties");
			properties.load(propFileInputStream);
			region = RegionUtils.getRegion(properties
					.getProperty("request.region"));
			amazonEC2.setRegion(region);
			String awsArn = amazonIM.getUser().getUser().getArn();
			int accountIdStartPos = awsArn.indexOf("::") + 2;
			accountId = awsArn.substring(accountIdStartPos,
					awsArn.indexOf(":user/", accountIdStartPos));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Properties getProperties() {
		return properties;
	}

	public static AmazonEC2 getAmazonEC2() {
		return amazonEC2;
	}

	public static Region getRegion() {
		return region;
	}

	public static String getAccountId() {
		return accountId;
	}

	private static class AWSCredentialsLoader implements AWSCredentials {
		private static Properties awsCredentialProperties = new Properties();
		{
			try {
				FileInputStream file = new FileInputStream(
						ApplicationConstants.RESOURCES_LOCATION_PATH
								+ AWS_CREDENTITALS_PROP_FILE_NAME);
				awsCredentialProperties.load(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public String getAWSAccessKeyId() {
			return awsCredentialProperties.get("accessKey").toString();
		}

		@Override
		public String getAWSSecretKey() {
			return awsCredentialProperties.get("secretKey").toString();

		}
	}

}

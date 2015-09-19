package com.betahikaru.aws.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

public class ConfigProvider {

	private static Properties credentialsProperty = null;

	public static void loadConfigure(InputStream inputStream) {
		credentialsProperty = new Properties();
		try {
			credentialsProperty.load(inputStream);
		} catch (IOException e) {
			System.err.println("load property");
		}
	}

	private static Region defaultRegion = Region.getRegion(Regions.AP_NORTHEAST_1);

	public static Region getDefaultRegion() {
		if (defaultRegion == null && credentialsProperty != null) {
			String regionKey = credentialsProperty.getProperty("aws.region");
			if (regionKey != null) {
				defaultRegion = Region.getRegion(Regions.valueOf(regionKey));
			}
		}
		return defaultRegion;
	}

	private static AWSCredentials credentials = null;

	public static AWSCredentials getCredential() {
		if (credentials == null && credentialsProperty != null) {
			String accessKey = credentialsProperty.getProperty("aws.access_key_id");
			String secretKey = credentialsProperty.getProperty("aws.secret_access_key");
			credentials = new BasicAWSCredentials(accessKey, secretKey);
		}
		return credentials;
	}

}

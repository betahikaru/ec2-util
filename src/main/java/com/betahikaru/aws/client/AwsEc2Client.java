package com.betahikaru.aws.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.AllocateAddressRequest;
import com.amazonaws.services.ec2.model.AllocateAddressResult;
import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.AssociateAddressResult;
import com.amazonaws.services.ec2.model.DescribeAddressesRequest;
import com.amazonaws.services.ec2.model.DescribeAddressesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DisassociateAddressRequest;
import com.amazonaws.services.ec2.model.DomainType;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.ReleaseAddressRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.Tag;

public class AwsEc2Client {

	public static AmazonEC2 getEc2(InputStream inputStream) {
		Properties credentialsProperty = new Properties();
		try {
			credentialsProperty.load(inputStream);
		} catch (IOException e) {
			System.err.println("load property");
		}
		String region = credentialsProperty.getProperty("aws.region");
		String accessKey = credentialsProperty.getProperty("aws.access_key_id");
		String secretKey = credentialsProperty.getProperty("aws.secret_access_key");

		BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		AmazonEC2 ec2 = new AmazonEC2Client(credentials);
		ec2.setRegion(Region.getRegion(Regions.valueOf(region)));

		return ec2;
	}

	public static Instance findInstanceByName(AmazonEC2 ec2, String targetName) {
		DescribeInstancesResult instanceResult = ec2.describeInstances();
		List<Reservation> reservations = instanceResult.getReservations();
		for (Reservation reservation : reservations) {
			List<Instance> instances = reservation.getInstances();
			for (Instance instance : instances) {
				List<Tag> tagList = instance.getTags();
				String name = "";
				for (Tag tag : tagList) {
					String tagKey = tag.getKey();
					String tagValue = tag.getValue();
					if (tagKey.contains("Name")) {
						name = tagValue;
						if (targetName.equals(name)) {
							return instance;
						}
						break;
					}
				}
			}
		}
		return null;
	}

	public static Address checkExistsAddress(AmazonEC2 ec2, String targetIp) {
		DescribeAddressesRequest addressRequest = new DescribeAddressesRequest().withPublicIps(targetIp);
		DescribeAddressesResult addressResult = ec2.describeAddresses(addressRequest);
		List<Address> addresses = addressResult.getAddresses();
		for (Address address : addresses) {
			String publicIp = address.getPublicIp();
			if (targetIp.equals(publicIp)) {
				return address;
			}
			break;
		}
		return null;
	}

	public static InstanceStateChange stopInstance(AmazonEC2 ec2, String instanceId) {
		StopInstancesRequest stopInstancesRequest = new StopInstancesRequest().withInstanceIds(instanceId);
		StopInstancesResult stopInstancesResult = ec2.stopInstances(stopInstancesRequest);
		List<InstanceStateChange> instanceStateChange = stopInstancesResult.getStoppingInstances();
		for (InstanceStateChange stateChange : instanceStateChange) {
			return stateChange;
		}
		return null;
	}

	public static InstanceStateChange startInstance(AmazonEC2 ec2, String instanceId) {
		StartInstancesRequest startInstancesRequest = new StartInstancesRequest().withInstanceIds(instanceId);
		StartInstancesResult startInstancesResult = ec2.startInstances(startInstancesRequest);
		List<InstanceStateChange> instanceStateChange = startInstancesResult.getStartingInstances();
		for (InstanceStateChange stateChange : instanceStateChange) {
			return stateChange;
		}
		return null;
	}

	public static void showStateChange(InstanceStateChange stateChange, String description) {
		String changedInstanceId = stateChange.getInstanceId();
		InstanceState previousState = stateChange.getPreviousState();
		InstanceState currentState = stateChange.getCurrentState();
		System.out.println(description + " (" + changedInstanceId + ": " + previousState.getName() + "=>"
				+ currentState.getName() + ")");
	}

	public static Address allocateAddress(AmazonEC2 ec2, DomainType domainType) {
		AllocateAddressRequest addressRequest = new AllocateAddressRequest().withDomain(domainType);
		AllocateAddressResult addressResult = ec2.allocateAddress(addressRequest);
		Address address = new Address().withAllocationId(addressResult.getAllocationId())
				.withDomain(addressResult.getDomain()).withPublicIp(addressResult.getPublicIp());
		return address;
	}

	public static void releaseAddress(AmazonEC2 ec2, Address address) {
		String allocationId = address.getAllocationId();
		ReleaseAddressRequest releaseAddressRequest = new ReleaseAddressRequest();
		releaseAddressRequest.setAllocationId(allocationId);
		ec2.releaseAddress(releaseAddressRequest);
	}

	public static String associateAddress(AmazonEC2 ec2, Address address, String instanceId) {
		AssociateAddressRequest addressRequest = new AssociateAddressRequest()
				.withAllocationId(address.getAllocationId()).withInstanceId(instanceId);
		AssociateAddressResult addressResult = ec2.associateAddress(addressRequest);
		String associationId = addressResult.getAssociationId();
		return associationId;
	}

	public static void disassociateAddress(AmazonEC2 ec2, Address address) {
		String associationId = address.getAssociationId();
		DisassociateAddressRequest disassociateAddressRequest = new DisassociateAddressRequest();
		disassociateAddressRequest.setAssociationId(associationId);
		ec2.disassociateAddress(disassociateAddressRequest);
	}
}

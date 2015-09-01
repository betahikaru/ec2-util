package com.betahikaru.aws.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.DomainType;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.betahikaru.aws.client.AwsEc2Client;
import com.betahikaru.aws.command.option.Ec2CommandOptions;

public class Ec2StartCommand implements Ec2SubCommand {
	/**
	 * Start Ec2 Instance. Realease EIP for Ec2 Instance. Disassociate EIP.
	 */
	public int execute(Ec2CommandOptions options) throws FileNotFoundException {
		System.out.println(getClass().getName());
		String name = options.getName();
		if (name != null) {
			int result = startByName(options);
			return result;
		} else {
			System.err.println("Less options.");
			return 1;
		}
	}

	private int startByName(Ec2CommandOptions options) throws FileNotFoundException {
		String name = options.getName();
		InputStream inputStream = new FileInputStream(new File(options.getCredentialsPath()));
		AmazonEC2 ec2 = AwsEc2Client.getEc2(inputStream);

		// Check Exists Instance
		Instance instance = AwsEc2Client.findInstanceByName(ec2, name);
		String instanceId = instance.getInstanceId();
		if (instanceId == null) {
			System.err.println("Not exists instance (name = " + name + ").");
			return 2;
		} else {
			System.out.println("Exists instance (id = " + instanceId + ")");
		}

		// Start Ec2 Instance
		InstanceStateChange stateChange = AwsEc2Client.startInstance(ec2, instanceId);
		AwsEc2Client.showStateChange(stateChange, "Starting Instance");

		// Allocate and Associate Address
		DomainType domainType = (instance.getVpcId() == null) ? DomainType.Standard : DomainType.Vpc;
		Address address = AwsEc2Client.allocateAddress(ec2, domainType);
		String publicIp = address.getPublicIp();
		System.out.println("Allocated Address(" + publicIp + ", " + address.getAllocationId() + ")");
		if (address != null) {
			// TODO: Wait for Starting Instance.
			waitForStartingInstance();

			try {
				String associateAddress = AwsEc2Client.associateAddress(ec2, address, instanceId);
				System.out.println("Associated Address(" + publicIp + ", " + associateAddress + ")");
			} catch (AmazonServiceException e) {
				AwsEc2Client.releaseAddress(ec2, address);
				System.out.println("Released Address (" + publicIp + ")");
				return 2;
			}
		}
		return 0;
	}

	private static void waitForStartingInstance() {
		try {
			Thread.sleep(2 * 60 * 1000);
		} catch (InterruptedException e) {
		}
	}
}

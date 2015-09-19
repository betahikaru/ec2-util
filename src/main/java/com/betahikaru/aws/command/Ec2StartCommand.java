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
import com.amazonaws.services.route53.AmazonRoute53;
import com.betahikaru.aws.client.AwsEc2Client;
import com.betahikaru.aws.client.AwsRoute53Client;
import com.betahikaru.aws.command.option.Ec2CommandOptions;
import com.betahikaru.aws.util.ConfigProvider;

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
		ConfigProvider.loadConfigure(inputStream);
		AmazonEC2 ec2 = AwsEc2Client.getEc2();

		// Check Exists Instance
		Instance instance = AwsEc2Client.findInstanceByName(ec2, name);
		if (instance == null) {
			System.err.println("Not exists instance (name = " + name + ").");
			return 2;
		}
		String instanceId = instance.getInstanceId();
		System.out.println("Exists instance (id = " + instanceId + ")");

		// Start Ec2 Instance
		InstanceStateChange stateChange = AwsEc2Client.startInstance(ec2, instanceId);
		AwsEc2Client.showStateChange(stateChange, "Starting Instance");

		// Allocate Address
		DomainType domainType = (instance.getVpcId() == null) ? DomainType.Standard : DomainType.Vpc;
		Address address = AwsEc2Client.allocateAddress(ec2, domainType);
		String publicIp = address.getPublicIp();
		System.out.println("Allocated Address(" + publicIp + ", " + address.getAllocationId() + ")");
		if (address != null) {
			// TODO: Wait for Starting Instance.
			waitForStartingInstance();

			try {
				// Associate Address
				String associateAddress = AwsEc2Client.associateAddress(ec2, address, instanceId);
				System.out.println("Associated Address(" + publicIp + ", " + associateAddress + ")");

				String domain = options.getDomain();
				if (domain != null) {
					// Attach Domain to EIP
					AmazonRoute53 route53 = AwsRoute53Client.getRoute53();
					String attachedDomain = AwsRoute53Client.attachDomainToEip(route53, publicIp, domain);
					if (attachedDomain != null) {
						System.out.println("Attach domain : " + attachedDomain);
					} else {
						System.err.println("Not Found Available Hosted Zone for specified Domain(" + domain + ")");
					}
				}
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

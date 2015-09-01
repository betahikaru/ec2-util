package com.betahikaru.aws.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Address;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.betahikaru.aws.client.AwsEc2Client;
import com.betahikaru.aws.command.option.Ec2CommandOptions;

public class Ec2StopCommand implements Ec2SubCommand {

	/**
	 * Stop Ec2 Instance. Realease EIP for Ec2 Instance. Disassociate EIP.
	 */
	public int execute(Ec2CommandOptions options) throws FileNotFoundException {
		System.out.println(getClass().getName());
		String name = options.getName();
		InputStream inputStream = new FileInputStream(new File(options.getCredentialsPath()));
		AmazonEC2 ec2 = AwsEc2Client.getEc2(inputStream);

		// Check Exists Instance
		Instance instance = AwsEc2Client.findInstanceByName(ec2, name);
		if (instance == null) {
			System.err.println("Not exists instance (name = " + name + ").");
			return 2;
		}
		String instanceId = instance.getInstanceId();
		String publicIp = instance.getPublicIpAddress();
		System.out.println("Exists instance (id = " + instanceId + ")");

		// Stop Ec2 Instance
		InstanceStateChange stateChange = AwsEc2Client.stopInstance(ec2, instanceId);
		AwsEc2Client.showStateChange(stateChange, "Stopping Instance");

		// Disassociate and Release Address
		if (publicIp != null) {
			Address address = AwsEc2Client.checkExistsAddress(ec2, publicIp);
			if (address != null) {
				AwsEc2Client.disassociateAddress(ec2, address);
				System.out.println("Disassociated Address (" + publicIp + ")");
				AwsEc2Client.releaseAddress(ec2, address);
				System.out.println("Released Address (" + publicIp + ")");
			}
		} else {
			System.out.println("No EIP.");
		}
		return 0;
	}

}

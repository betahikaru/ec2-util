package com.betahikaru.aws.command.option;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;

import com.betahikaru.aws.command.Ec2StartCommand;
import com.betahikaru.aws.command.Ec2StopCommand;
import com.betahikaru.aws.command.Ec2SubCommand;

public class Ec2CommandOptions {
	@Argument(required = true, index = 0, usage = "start, stop", handler = SubCommandHandler.class)
	@SubCommands({ @SubCommand(name = "start", impl = Ec2StartCommand.class),
			@SubCommand(name = "stop", impl = Ec2StopCommand.class) })
	private Ec2SubCommand command;

	public Ec2SubCommand getCommand() {
		return command;
	}

	@Option(name = "-n", aliases = "--name", usage = "Name of Instance")
	private String name;

	public String getName() {
		return name;
	}

	@Option(name = "-c", aliases = "--credentials-path", usage = "Path to Credentials File")
	private String credentialsPath = "conf/credentials.properties";

	public String getCredentialsPath() {
		return credentialsPath;
	}

	@Option(name = "-d", aliases = "--domain", usage = "Sub Domain to attach")
	private String domain;

	public String getDomain() {
		return domain;
	}
}

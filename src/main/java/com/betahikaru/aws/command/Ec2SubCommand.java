package com.betahikaru.aws.command;

import java.io.FileNotFoundException;

import com.betahikaru.aws.command.option.Ec2CommandOptions;

public interface Ec2SubCommand {
	public int execute(Ec2CommandOptions options) throws FileNotFoundException;
}

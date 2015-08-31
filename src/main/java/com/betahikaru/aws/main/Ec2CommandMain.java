package com.betahikaru.aws.main;

import java.io.FileNotFoundException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.betahikaru.aws.command.option.Ec2CommandOptions;

public class Ec2CommandMain {

	public static void main(String[] args)  throws FileNotFoundException {
		Ec2CommandOptions options = new Ec2CommandOptions();
		CmdLineParser parser = new CmdLineParser(options);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			parser.printUsage(System.out);
			System.exit(1);
		}
		options.getCommand().execute(options);
		System.exit(0);
	}
}

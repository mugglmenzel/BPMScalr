package de.eorganization.go8.awsscaling;

import java.io.IOException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.DeleteAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.DeleteLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.UpdateAutoScalingGroupRequest;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.sun.xml.messaging.saaj.util.Base64;

public class AutoScale {

	private static AmazonEC2 ec2;
	private static String ami = "ami-6ab40a03";
	private static AmazonAutoScalingClient as;
	private static String launchConfigurationName = "BPMScalingLaunchConfig";
	private static String autoScalingGroupName = "BPMScaling";
	private static String[] az = new String[] { "us-east-1a" };
	// private static String[] az = new String[]{"us-east-1a","us-east-1b"};

	private static String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
	private static String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
	private static String userDataNotEncoded = "#!/bin/sh -ex\n"
			+ "export DEBIAN_FRONTEND=noninteractive\n"
			+ "cat >/etc/awscredential.properties <<END_OF_FILE\n"
			+ "AWSACCESSID=" + accessKey + "\n" + "AWSKEY=" + secretKey + "\n"
			+ "END_OF_FILE\n\n" + "(cd /home/ubuntu/cassandra_ami; git pull)\n"
			+ "sudo sh /home/ubuntu/cassandra_ami/configure1.sh\n";
	private static String userData = new String(
			new Base64().encode(userDataNotEncoded.getBytes()));

	static {
		AWSCredentials credentials;
		try {
			credentials = new PropertiesCredentials(
					AutoScale.class
							.getResourceAsStream("AwsCredentials.properties"));
			as = new AmazonAutoScalingClient(credentials);
			ec2 = new AmazonEC2Client(credentials);

		} catch (IOException e) {
			System.err
					.println("Credentials were not properly entered into AwsCredentials.properties.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		destroyASG();
		//launchEC2Instance();
		launchASG();
	}

	public static void launchASG() throws InterruptedException {

		CreateLaunchConfigurationRequest createLaunchConfigurationRequest = new CreateLaunchConfigurationRequest()
				.withLaunchConfigurationName(launchConfigurationName)
				.withImageId(ami).withInstanceType("m1.small")
				.withSecurityGroups("Default").withKeyName("use2");

		as.createLaunchConfiguration(createLaunchConfigurationRequest);

		Thread.sleep(5000);

		CreateAutoScalingGroupRequest createAutoScalingGroupRequest = new CreateAutoScalingGroupRequest()
				.withAutoScalingGroupName(autoScalingGroupName)
				.withAvailabilityZones(az).withMinSize(1).withMaxSize(3)
				.withLaunchConfigurationName(launchConfigurationName);
		as.createAutoScalingGroup(createAutoScalingGroupRequest);

		System.out.println(as.describeLaunchConfigurations());
		System.out.println(as.describeAutoScalingGroups());
	}

	public static void destroyASG() throws InterruptedException {
		try {
		UpdateAutoScalingGroupRequest updateAutoScalingGroupRequest = new UpdateAutoScalingGroupRequest()
				.withMinSize(0).withMaxSize(0)
				.withAutoScalingGroupName(autoScalingGroupName);
		as.updateAutoScalingGroup(updateAutoScalingGroupRequest);

		Thread.sleep(90000);
		DeleteAutoScalingGroupRequest deleteAutoScalingGroupRequest = new DeleteAutoScalingGroupRequest()
				.withAutoScalingGroupName(autoScalingGroupName);
		as.deleteAutoScalingGroup(deleteAutoScalingGroupRequest);

		DeleteLaunchConfigurationRequest deleteLaunchConfigurationRequest = new DeleteLaunchConfigurationRequest()
				.withLaunchConfigurationName(launchConfigurationName);
		as.deleteLaunchConfiguration(deleteLaunchConfigurationRequest);
		} catch (Exception e) {
			System.err.println("Error during deletion of autoscaling group " + autoScalingGroupName + ". " + e.getLocalizedMessage());
			e.printStackTrace(System.err);
		}
	}

	public static void launchEC2Instance() {
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
				.withImageId(ami).withInstanceType("m1.small")
				.withSecurityGroups("Default").withMinCount(1).withMaxCount(1)
				.withUserData(userData).withKeyName("use2");

		// Do it!
		RunInstancesResult runInstancesResult = ec2
				.runInstances(runInstancesRequest);
	}

}

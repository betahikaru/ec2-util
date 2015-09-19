package com.betahikaru.aws.client;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.Change;
import com.amazonaws.services.route53.model.ChangeAction;
import com.amazonaws.services.route53.model.ChangeBatch;
import com.amazonaws.services.route53.model.ChangeInfo;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsRequest;
import com.amazonaws.services.route53.model.ChangeResourceRecordSetsResult;
import com.amazonaws.services.route53.model.HostedZone;
import com.amazonaws.services.route53.model.ListHostedZonesResult;
import com.amazonaws.services.route53.model.RRType;
import com.amazonaws.services.route53.model.ResourceRecord;
import com.amazonaws.services.route53.model.ResourceRecordSet;
import com.betahikaru.aws.util.ConfigProvider;

public class AwsRoute53Client {

	public static AmazonRoute53 getRoute53() {
		AmazonRoute53 route53 = new AmazonRoute53Client(ConfigProvider.getCredential());
		route53.setRegion(ConfigProvider.getDefaultRegion());
		return route53;
	}

	public static ChangeInfo attachDomainToEip(AmazonRoute53 route53, String publicIp, String domain) {
		String hostedZoneId = findHostedZoneForDomain(route53, domain);
		if (hostedZoneId != null) {
			System.out.println("Found HostedZone's Id(" + hostedZoneId + ")");
		} else {
			return null;
		}

		ResourceRecordSet resourceRecordSet = generateResourceRecordSetForARecord(publicIp, domain);
		ChangeInfo changeinfo = AwsRoute53Client.changeResourceRecordSet(route53, hostedZoneId, resourceRecordSet,
				ChangeAction.CREATE);
		return changeinfo;
	}

	public static String findHostedZoneForDomain(AmazonRoute53 route53, String domain) {
		ListHostedZonesResult listHostedZonesResult = route53.listHostedZones();
		List<HostedZone> hostedZones = listHostedZonesResult.getHostedZones();
		String hostedZoneId = null;
		for (HostedZone hostedZone : hostedZones) {
			if (isRootDomain(domain, hostedZone)) {
				hostedZoneId = hostedZone.getId();
				break;
			}
		}
		return hostedZoneId;
	}

	public static boolean isRootDomain(String subDomain, HostedZone hostedZone) {
		if (subDomain == null || hostedZone == null) {
			return false;
		}
		String rootDomain = hostedZone.getName();
		if (rootDomain == null) {
			return false;
		}
		String subDomainEndWithDot = subDomain + ".";
		if (subDomainEndWithDot.endsWith(rootDomain)) {
			return true;
		} else {
			return false;
		}
	}

	public static ResourceRecordSet generateResourceRecordSetForARecord(String publicIp, String domain) {
		ArrayList<ResourceRecord> resourceRecords = new ArrayList<>();
		ResourceRecord resourceRecord = new ResourceRecord().withValue(publicIp);
		resourceRecords.add(resourceRecord);

		ResourceRecordSet resourceRecordSet = new ResourceRecordSet();
		resourceRecordSet.setTTL(300L);
		resourceRecordSet.setName(domain);
		resourceRecordSet.setType(RRType.A);
		resourceRecordSet.setResourceRecords(resourceRecords);
		return resourceRecordSet;
	}

	public static ChangeInfo changeResourceRecordSet(AmazonRoute53 route53, String hostedZoneId,
			ResourceRecordSet resourceRecordSet, ChangeAction changeAction) {
		Change change = new Change(changeAction, resourceRecordSet);
		ChangeBatch changeBatch = new ChangeBatch().withChanges(change);
		ChangeResourceRecordSetsRequest changeResourceRecordSetsRequest = new ChangeResourceRecordSetsRequest(
				hostedZoneId, changeBatch);
		ChangeResourceRecordSetsResult changeResourceRecordSetsResult = route53
				.changeResourceRecordSets(changeResourceRecordSetsRequest);
		ChangeInfo changeinfo = changeResourceRecordSetsResult.getChangeInfo();
		return changeinfo;
	}

}

package util;

import java.util.HashSet;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.XmlObject;

/**
 * This class gets case and account
 * referrals by processing them
 * separately
 * 
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
public class Referred {
	int accountReferrals;
	int caseReferrals;
	int numReferrals;

	/**
	 * Gets case and account referrals
	 * 
	 * @param pc Partner Connection
	 * @param qrSpent Query Result for spent
	 * @param qrReferred Query Result for referred
	 * @param programName Program Name
	 * @param begDate Beginning Date
	 * @param endDate Ending Date
	 */
	public Referred(PartnerConnection pc, QueryResult qrSpent, QueryResult qrReferred, String programName, String begDate, String endDate) {
		caseReferrals(pc, qrSpent, programName, begDate, endDate);
		accountReferrals(pc, qrReferred, programName, begDate, endDate);
		
	}

	/**
	 * This method TODO
	 *
	 * @param pc Partner Connection
	 * @param qr Query Result
	 * @param programName Program Name
	 * @param begDate Beginning Date
	 * @param endDate Ending Date
	 */
	public void caseReferrals(PartnerConnection pc, QueryResult qr, String programName, String begDate, String endDate) {
		try {
			boolean done = false;
			HashSet<String> caseReferralsSet = new HashSet<String>();
			while (!done) {
				SObject[] records = qr.getRecords();
				for (int i = 0; i < records.length; i++) {
					SObject info = records[i];
					XmlObject account = (XmlObject) records[i].getField("Account");
					String status = info.getField("Status").toString();
					Object program = info.getField("Program__c");

					if (program != null
							&& program.toString().equals(programName)
							&& status.toString().equals("Closed - Referred")) {
						String familyFirstName = (String) account.getChild("Family_First_Name__c").getValue();
						String familyLastName = (String) account.getChild("Family_Last_Name__c").getValue();
						caseReferralsSet.add(familyFirstName + familyLastName);
					}

				}

				if (qr.isDone()) {
					done = true;
				} else {
					qr = pc.queryMore(qr.getQueryLocator());
				}
			}
			this.caseReferrals = caseReferralsSet.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method gets number of referrals
	 * from Accounts
	 *
	 * @param pc Partner Connection
	 * @param qr Query Results
	 * @param programName Program Name
	 * @param begDate Beginning Date
	 * @param endDate Ending Date
	 */
	public void accountReferrals(PartnerConnection pc, QueryResult qr, String programName, String begDate, String endDate) {
		try {
			boolean done = false;
			int accountReferralsCount = 0;
			while (!done) {
				SObject[] records = qr.getRecords();
				for (int i = 0; i < records.length; i++) {
					SObject info = records[i];
					XmlObject recordType = (XmlObject) records[i].getField("RecordType");
					String type = (String) recordType.getChild("DeveloperName").getValue();
					Object program = info.getField("Program__c");
					if (program != null
							&& program.toString().equals(programName)
							&& type.toString().equals("Referral")) {
						accountReferralsCount++;
					}

				}

				if (qr.isDone()) {
					done = true;
				} else {
					qr = pc.queryMore(qr.getQueryLocator());
				}
			}
			this.accountReferrals = accountReferralsCount;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * To @return total number of referrals
	 */
	public int getNumReferrals() {
		return caseReferrals + accountReferrals;
	}
}

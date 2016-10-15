package util;

import java.util.HashSet;

import javax.xml.bind.DatatypeConverter;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

/**
 * Calculates Management Team Statistics
 * 
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
public class MTStats {
	int numMembersWithNonMTActivities;
	double numMTMembers;
	double numMTFamMembers;

	/**
	 * Constructor, calculates MT Stats
	 * 
	 * @param pc
	 *            PartnerConnection
	 * @param qr
	 *            QueryResult
	 * @param programName
	 *            Program Name
	 * @param begDate
	 *            Beginning Date
	 * @param endDate
	 *            Ending Date
	 */
	public MTStats(PartnerConnection pc, QueryResult qr, String programName, String begDate, String endDate) {

		try {
			boolean done = false;
			HashSet<String> mTMembers = new HashSet<String>();
			HashSet<String> membersWithNonMTActivities = new HashSet<String>();
			HashSet<String> mTFamMembers = new HashSet<String>();

			while (!done) {
				SObject[] records = qr.getRecords();
				for (int k = 0; k < records.length; k++) {
					SObject info = records[k];
					XmlObject individualName = (XmlObject) records[k].getField("Individual_Name__r");
					boolean isMTMem = DatatypeConverter.parseBoolean((String) individualName.getChild("Management_Team_Member__c").getValue());
					boolean isFamMem = DatatypeConverter.parseBoolean((String) individualName.getChild("Family_Member_SelfAdvocate__c").getValue());
					String program = (String) individualName.getChild("Program__c").getValue();
					String firstName = (String) individualName.getChild("FirstName").getValue();
					String lastName = (String) individualName.getChild("LastName").getValue();
					Object volunteerTask = info.getField("Volunteer_Task__c");
					String task = (String) volunteerTask.toString();
					if (program != null && program.toString().equals(programName)) {
						// If they are a MT member, add them to MT member set
						if (isMTMem) {
							mTMembers.add(firstName + lastName);
							if (isFamMem) {
								mTFamMembers.add(firstName + lastName + program);
							}
							if (!task.contains("Management")) {
								membersWithNonMTActivities.add(firstName + lastName + program);
							} else if(task.length() > 24) {
								membersWithNonMTActivities.add(firstName + lastName + program);
							}
						}
					}
				}
				if (qr.isDone()) {
					done = true;
				} else {
					qr = pc.queryMore(qr.getQueryLocator());
				}
			} // End while loop
			this.numMTFamMembers = mTFamMembers.size();
			this.numMTMembers = mTMembers.size();
			this.numMembersWithNonMTActivities = membersWithNonMTActivities.size();
		} catch (ConnectionException ce) {
			ce.printStackTrace();
		}
	}

	/**
	 * This method TODO
	 *
	 * @return number of members with non management team activities
	 */
	public double getNumMembersWithNonMTActivities() {
		return numMembersWithNonMTActivities;
	}

	/**
	 * This method TODO
	 *
	 * @return number of management team members
	 */
	public double getNumMTMembers() {
		return numMTMembers;
	}

	/**
	 * This method TODO
	 *
	 * @return number of MT members who are family members
	 */
	public double getNumMTFamMembers() {
		return numMTFamMembers;
	}

	/**
	 * This method TODO
	 *
	 * @return percent of activities
	 */
	public double getTotalMTActPercent() {
		if (numMTMembers == 0) {
			return 0;
		} else {
			return this.numMembersWithNonMTActivities / this.numMTMembers;
		}
	}

	/**
	 * This method TODO
	 *
	 * @return percent who are family members
	 */
	public double getTotalMTFamPercent() {
		if (numMTMembers == 0) {
			return 0;
		} else {
			return this.numMTFamMembers / this.numMTMembers;
		}
	}
}

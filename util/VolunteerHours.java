package util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

/**
 * Gets volunteer hours and number of families giving back through volunteering
 * 
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
public class VolunteerHours {
	private double numVolunteerHours;
	private int numGiveBack;
	/**
	 * Gets volunteer hours and number of families giving back
	 * 
	 * @param pc
	 *            Partner Connection
	 * @param qr
	 *            Query Result
	 * @param programName
	 *            Program Name
	 * @param begDate
	 *            Beginning Date
	 * @param endDate
	 *            Ending Date
	 * @throws ParseException
	 */
	public VolunteerHours(PartnerConnection pc, QueryResult qr,
			String programName, String begDate, String endDate)
			throws ParseException {
		try {
			boolean done = false;
			double numVolunteerHours = 0;
			HashSet<String> giveBack = new HashSet<String>();
			HashSet<String> volunteers = new HashSet<String>();
			while (!done) {
				SObject[] records = qr.getRecords();
				for (int i = 0; i < records.length; i++) {
					SObject info = records[i];
					XmlObject individualName = (XmlObject) records[i].getField("Individual_Name__r");
					XmlObject account = (XmlObject) individualName.getField("Account");
					String program = (String) individualName.getChild("Program__c").getValue();
					String firstName = (String) individualName.getChild("FirstName").getValue();
					String lastName = (String) individualName.getChild("LastName").getValue();
					if (account == null) {
						continue;
					}

					Object date = account.getField("Date_of_Last_Request__c");
					Object volunteerHours = info.getField("Volunteer_Hours__c");
					Double volunteerHoursValue = new Double(volunteerHours.toString());

					if (program != null && program.toString().equals(programName)) {
						volunteers.add(firstName + lastName + program);
						numVolunteerHours += volunteerHoursValue;
						
						if (date != null) {
							// Format dates for comparison
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
							Date dateLastRequest = df.parse(date.toString());
							Date begOf2011 = df.parse("2011-01-01");
							if (dateLastRequest.compareTo(begOf2011) >= 0) {
								giveBack.add(firstName + lastName + dateLastRequest);
							}
						}
					}
				}
				if (qr.isDone()) {
					done = true;
				} else {
					qr = pc.queryMore(qr.getQueryLocator());
				}
				
			}
			this.numVolunteerHours = numVolunteerHours;
			this.numGiveBack = giveBack.size();
			volunteers.size();
		} catch (ConnectionException | NullPointerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * To @return number of volunteer hours
	 */
	public double getNumVolunteerHours() {
		return numVolunteerHours;
	}

	/**
	 * To @return number of families giving back
	 */
	public int getNumGiveBack() {
		return numGiveBack;
	}

}

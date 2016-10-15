package util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

/**
 * Counts number of families served and calculates percent of new families
 * served
 * 
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
public class NewFamilies {
	/** Int representation of January */
	public static final int JANUARY = 1;
	/** Int representation of June */
	public static final int JUNE = 6;
	/** Comma */
	public static final String C = ",";
	/** Program name */

	private String programName;
	private PartnerConnection pc;
	private QueryResult qr;
	private String begDateParam;
	private String endDateParam;
	private int begDateMonth;
	private int begDateYear;
	// New families served year to date
	private HashSet<String> newFamYTD;
	// Families served year to date
	private HashSet<String> famYTD;

	/**
	 * Processes data for percent new families served
	 * 
	 * @param pc Part
	 * @param qr Query Result
	 * @param programName Program Name
	 * @param begDateParam Beginning Date
	 * @param endDateParam Ending Date
	 */
	public NewFamilies(PartnerConnection pc, QueryResult qr, String programName, String begDateParam, String endDateParam) {
		this.pc = pc;
		this.qr = qr;
		this.programName = programName;
		this.begDateParam = begDateParam;
		this.endDateParam = endDateParam;
		process();

	}

	/**
	 * Process data
	 */
	public void process() {
		try {
			// Empty sets are created to store families and new families served
			newFamYTD = new HashSet<String>();
			famYTD = new HashSet<String>();
			// Trims month and year off of date
			begDateMonth = Integer.parseInt(begDateParam.substring(5, 7));
			begDateYear = Integer.parseInt(begDateParam.substring(0, 4));
			// Set beginning date to July 1 of current fiscal year
			setDate();
			boolean done = false;
			while (!done) {
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
				SObject[] records = qr.getRecords();
				// Process family data
				for (int i = 0; i < records.length; i++) {
					SObject info = records[i];
					Object program = info.getField("Program__c");
					Object status = info.getField("Status");
					XmlObject account = (XmlObject) records[i].getField("Account");
					if(validateRecord(program, status)){
						String caseStartDate = (String) account.getChild("StartDate__c").getValue();
						String familyFirstName = (String) account.getChild("Family_First_Name__c").getValue();
						String familyLastName = (String) account.getChild("Family_Last_Name__c").getValue();
						String county = (String) account.getChild("County__c").getValue();
						// Date when case was created
						Date startDate = format.parse(caseStartDate);
						Date begDate = format.parse(begDateParam);
						Date endDate = format.parse(endDateParam);
						try {
							// String entry to be added to the set
							String setEntry = familyFirstName + C + familyLastName + C + program + C + county;
								/** Set of records in above array contain all families served from
								 beginning of fiscal year up to and inclusive of the current report 
								 quarter. Of all these families, this condition looks at which family had
								 their creation date within the begin and end dates, i.e., this quarter.
								 */
								if (startDate.compareTo(begDate) >= 0 && startDate.compareTo(endDate) <= 0) {
									// If creation date is within data params
									// and entry is not in the set, add it
									if (!newFamYTD.contains(setEntry)) {
										newFamYTD.add(setEntry);
									}
								}
								if (!famYTD.contains(setEntry)) {
									famYTD.add(setEntry);
								}
						} catch (NullPointerException e) {
							continue;
						}
					}
				}
				if (qr.isDone()) {
					done = true;
				} else {
					qr = pc.queryMore(qr.getQueryLocator());
				}
			}
			setNewFamYTD(newFamYTD);
			setFamYTD(famYTD);
		} catch (ConnectionException | ParseException ce) {
			System.out.println("Error!!!!!!!!");
		}

	}

	private boolean validateRecord(Object program, Object status) {
		if (program != null && program.toString().equals(programName) 
				&& !status.toString().equals("Closed - Referred")
				&& !status.toString().equals("Closed - Suspended")
				&& !status.toString().equals("Resourcing")) {
			return true;
		}
		return false;
	}

	private void setDate() {
		if ((begDateMonth >= JANUARY && begDateMonth <= JUNE)) {
			this.begDateParam = (begDateYear - 1) + "-07-01";
		} else {
			this.begDateParam = (begDateYear) + "-07-01";
		}

	}

	/**Returns new families served set
	 * @return the newFamYTD
	 */
	public HashSet<String> getNewFamYTD() {
		return newFamYTD;
	}

	/**Sets new families served to set
	 * @param newFamYTD the newFamYTD to set
	 */
	public void setNewFamYTD(HashSet<String> newFamYTD) {
		this.newFamYTD = newFamYTD;
	}

	/**Returns families served set
	 * @return the famYTD
	 */
	public HashSet<String> getFamYTD() {
		return famYTD;
	}

	/**Sets families served to set
	 * @param famYTD the famYTD to set
	 */
	public void setFamYTD(HashSet<String> famYTD) {
		this.famYTD = famYTD;
	}
}

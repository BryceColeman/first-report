package util;

import java.util.HashSet;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

/**
 * Processes amount spent, number of families served, number of requests,
 * leveraging, and total value of support
 * 
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
public class Spent {
	PartnerConnection pc;
	QueryResult qr;
	String programName;
	String begDate;
	String endDate;
	double totalSpent;
	double totalLeverageAmt;
	double totalValueOfSupport;
	int numRequests;
	int numFamily;
	int leveragedCases;
	private HashSet<String> familiesServedSet = new HashSet<String>();

	/**
	 * Processes amount spent for a program
	 * 
	 * @param pc Partner Connection
	 * @param qr Query Result
	 * @param programName Program Name
	 * @param begDate Beginning Date
	 * @param endDate Ending Date
	 */
	public Spent(PartnerConnection pc, QueryResult qr, String programName, String begDate, String endDate) {
		this.pc = pc;
		this.qr = qr;
		this.programName = programName;
		this.begDate = begDate;
		this.endDate = endDate;
		this.totalSpent = 0;
		this.totalLeverageAmt = 0;
		this.totalValueOfSupport = 0;
		this.numRequests = 0;
		this.numFamily = 0;
		this.leveragedCases = 0;
		process();
	}

	/**
	 * Process data 
	 */
	public void process() {
		try {
			boolean done = false;
			HashSet<String> leveragedCasesSet = new HashSet<String>();
			while (!done) {
				SObject[] records = qr.getRecords();
				// Process the query results
				for (int i = 0; i < records.length; i++) {
					try {
						SObject info = records[i];
						XmlObject account = (XmlObject) records[i].getField("Account");
						Object program = info.getField("Program__c");
						Object fifAmount = info.getField("FIF__c");
						Object caseStatus = info.getField("Status");
						Object leverageAmount = info.getField("Total_Leverage__c");
						Object totalSupportAmount = info.getField("Total_Value_of_Support__c");
						Object caseNum = info.getField("CaseNumber");
						String status = (String) caseStatus.toString();
						if (validateRecord(program, status)) {
							String familyFirstName = (String) account.getChild("Family_First_Name__c").getValue();
							String familyLastName = (String) account.getChild("Family_Last_Name__c").getValue();
							String setEntry = familyFirstName + familyLastName + program;
							double leverageAmt = new Double(leverageAmount.toString());
							double fifAmt = new Double(fifAmount.toString());
							double totalSupportAmt = new Double(totalSupportAmount.toString());
							numRequests++;
							// Add family to family served set if they have not
							// already been added
							if (!familiesServedSet.contains(setEntry)) {
								familiesServedSet.add(setEntry);
							}
							totalSpent += fifAmt;
							if (leverageAmt > 0) {
								leveragedCasesSet.add(caseNum.toString());
							}
							totalLeverageAmt += leverageAmt;
							totalValueOfSupport += totalSupportAmt;
						}
					} catch (NullPointerException e) {
						continue;
					}
				}

				if (qr.isDone()) {
					done = true;
				} else {
					qr = pc.queryMore(qr.getQueryLocator());
				}
			}
			setLeveragedCases(leveragedCasesSet.size());
			setNumFamily(familiesServedSet.size());
		} catch (ConnectionException ce) {
			ce.printStackTrace();
		}
	}

	private boolean validateRecord(Object program, String status) {
		if (program != null
				&& program.toString().equals(programName)
				&& !status.toString().equals("Closed - Referred")
				&& !status.toString().equals("Closed - Suspended")
				&& !status.toString().equals("Resourcing")) {
			return true;
		}
		return false;
	}

	/**
	 * Returns number of families served
	 * @return the numFamily
	 */
	public int getNumFamily() {
		return numFamily;
	}

	/**
	 * Sets number of family served
	 * @param numFamily the numFamily to set
	 */
	public void setNumFamily(int numFamily) {
		this.numFamily = numFamily;
	}

	/**
	 * Returns number of leveraged cases
	 * @return the leveragedCases
	 */
	public int getLeveragedCases() {
		return leveragedCases;
	}

	/**
	 * Sets number of leveraged cases
	 * @param leveragedCases the leveragedCases to set
	 */
	public void setLeveragedCases(int leveragedCases) {
		this.leveragedCases = leveragedCases;
	}

	/**
	 * Returns program name
	 * @return the programName
	 */
	public String getProgramName() {
		return programName;
	}

	/**
	 * Returns total money spent
	 * @return the totalSpent
	 */
	public double getTotalSpent() {
		return totalSpent;
	}

	/**
	 * Returns total amount leveraged
	 * @return the totalLeverageAmt
	 */
	public double getTotalLeverageAmt() {
		return totalLeverageAmt;
	}

	/**
	 * Returns total value of support
	 * @return the totalValueOfSupport
	 */
	public double getTotalValueOfSupport() {
		return totalValueOfSupport;
	}

	/**
	 * Returns number of requests
	 * @return the numRequests
	 */
	public int getNumRequests() {
		return numRequests;
	}

	/**
	 * Returns set of families served
	 * @return the familiesServedSet
	 */
	public HashSet<String> getFamiliesServedSet() {
		return familiesServedSet;
	}
}

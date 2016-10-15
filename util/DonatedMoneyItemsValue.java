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
 * 
 * Gets value of donated monies and items
 * 
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
public class DonatedMoneyItemsValue {

	private double totalItemsValue;
	private double totalDonatedMoney;
	private int numGiveBack;

	/**
	 * Constructor
	 * 
	 * @param pc Partner Connection
	 * @param qrItems Query Results for Items
	 * @param qrDonated Query Results for Donated Money
	 * @param programName Program Name
	 * 
	 * @throws ParseException
	 * @throws ConnectionException
	 */
	public DonatedMoneyItemsValue(PartnerConnection pc, QueryResult qrItems, QueryResult qrDonated, String programName) throws ParseException, ConnectionException {
		this.totalItemsValue = processItemsValue(pc, qrItems, 0, programName);
		this.totalDonatedMoney = processDonatedMoney(pc, qrDonated, 0, programName);
		this.numGiveBack = processGiveBack(pc, qrItems, 0, programName);
	}

	/**
	 * This method processes the value of items
	 *
	 * @param pc Partner Connection
	 * @param qr Query Result
	 * @param totalItemsValue Total Items Value
	 * @param programName Program Name
	 * @return Value of Items
	 * 
	 * @throws ParseException
	 */
	public double processItemsValue(PartnerConnection pc, QueryResult qr, double totalItemsValue, String programName) throws ParseException {
		try {
			boolean done = false;
			HashSet<String> itemsValue = new HashSet<String>();
			while (!done) {
				SObject[] records = qr.getRecords();
				// Process the query results
				for (int i = 0; i < records.length; i++) {
					SObject info = records[i];

					// Gets program name, item request value, and item request
					// number
					Object program = info.getField("Program__c");
					Object itemRequestVal = info.getField("Item_Request_Value__c");
					Object name = info.getField("Name");

					if (program == null || !program.equals(programName)) {
						continue;
					}

					// Gets request record type, must be "New Request"
					XmlObject requestObj = (XmlObject) info.getField("Request__r");
					XmlObject recordType = (XmlObject) requestObj.getField("RecordType");
					String developerName = (String) recordType.getChild("DeveloperName").getValue();

					// Gets status of request (i.e., closed, resourcing, etc.)
					String status = (String) requestObj.getChild("Status").getValue();

					if (validate(itemRequestVal, developerName, status)) {

						double requestValue = new Double(itemRequestVal.toString());
						// If the hash set contains item request, skip
						if (!itemsValue.contains(name)) {
							totalItemsValue += requestValue;
						}
						itemsValue.add(name.toString());
					}
				}
				if (qr.isDone()) {
					done = true;
				} else {
					qr = pc.queryMore(qr.getQueryLocator());
				}
				this.totalItemsValue = totalItemsValue;

			}
		} catch (ConnectionException ce) {
			ce.printStackTrace();
		}
		return totalItemsValue;
	}

	private boolean validate(Object itemRequestVal, String developerName, String status) {
		if (itemRequestVal != null 
				&& developerName.toString().equals("New_Request") 
				&& !status.toString().equals("Closed - Referred") 
				&& !status.toString().equals("Closed - Suspended")
				&& !status.toString().equals("Resourcing")) {
			return true;
		}
		return false;
	}

	/**
	 * This method gets num families that gave back
	 *
	 * @param pc Partner Connect
	 * @param qr Query Result
	 * @param numGiveBack Number that gave back
	 * @param programName Program
	 * @return number that gave back
	 * @throws ConnectionException
	 * @throws ParseException
	 */
	public int processGiveBack(PartnerConnection pc, QueryResult qr, double numGiveBack, String programName) throws ConnectionException, ParseException {
		boolean done = false;
		HashSet<String> giveBack = new HashSet<String>();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		while (!done) {
			SObject[] records = qr.getRecords();
			// Process the query results
			for (int i = 0; i < records.length; i++) {
				SObject info = records[i];

				try {
					// Get item
					XmlObject itemObj = (XmlObject) info.getField("Item__r");

					// Get org associated w/ item
					XmlObject itemOrgObj = (XmlObject) itemObj.getField("Organization__r");

					// Get org name (testing only)
					String accountName = (String) itemOrgObj.getChild("Name").getValue();

					String accountProgram = (String) itemOrgObj.getChild("Program__c").getValue();

					// Get date of last request
					Object date = itemOrgObj.getField("Date_of_Last_Request__c");

					// Format date for comparison
					Date dateLastRequest = df.parse(date.toString());
					Date begOf2011 = df.parse("2011-01-01");
					if (dateLastRequest.compareTo(begOf2011) >= 0 && !accountName.contains("anonymous") && accountProgram.toString().equals(programName)) {
						giveBack.add(accountName + dateLastRequest);
					}
					if (qr.isDone()) {
						done = true;
					} else {
						qr = pc.queryMore(qr.getQueryLocator());
					}
					// If any are null, continue
				} catch (NullPointerException e) {
					continue;
				}
			}
		}

		this.numGiveBack = giveBack.size();
		return giveBack.size();

	}

	/**
	 * This method test
	 *
	 * @param pc Partner Connection
	 * @param qr Query Results
	 * @param totalDonatedMoney Total Donate Money
	 * @param programName Program Name
	 * 
	 * @return Total Donated Money
	 */
	public double processDonatedMoney(PartnerConnection pc, QueryResult qr, double totalDonatedMoney, String programName) {
		try {
			boolean done = false;
			while (!done) {
				SObject[] records = qr.getRecords();
				for (int i = 0; i < records.length; i++) {
					SObject info = records[i];
					XmlObject request = (XmlObject) records[i].getField("Request__r");
					String requestProgram = (String) request.getChild("Program__c").getValue();
					String status = (String) request.getChild("Status").getValue();
					Object program = info.getField("Program__c");
					Object donationAmt = info.getField("Donation_Amount__c");

					if (program != null && donationAmt != null && program.toString().equals(programName) && requestProgram.toString().equals(programName)
							&& !status.toString().equals("Closed - Referred") && !status.toString().equals("Closed - Suspended") && !status.toString().equals("Resourcing")) {
						double donationValue = new Double(donationAmt.toString());
						totalDonatedMoney += donationValue;
					}
				}
				if (qr.isDone()) {
					done = true;
				} else {
					qr = pc.queryMore(qr.getQueryLocator());
				}
				this.totalDonatedMoney = totalDonatedMoney;
			} // End while loop
		} catch (ConnectionException ce) {
			ce.printStackTrace();
		}
		return totalDonatedMoney;
	}

	/**
	 * Returns total value of items
	 * 
	 * @return the totalValue
	 */
	public double getTotalItemValue() {
		return totalItemsValue;
	}

	/**
	 * This method @return Total of Donated Money
	 */
	public double getTotalDonatedMoney() {
		return totalDonatedMoney;
	}

	/**
	 * This method @return Number of Families who gave back
	 */
	public int getNumGiveBack() {
		return numGiveBack;
	}

	/**
	 * This method @return Total of items and donated money
	 */
	public double getTotal() {
		return (getTotalItemValue() + getTotalDonatedMoney());
	}
}
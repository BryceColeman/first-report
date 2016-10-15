package query;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.ws.ConnectionException;

/**
 * Fetches data from Salesforce.com for the given program, quarter, and fiscal
 * year.
 * 
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
public class SFQuery {
	/** Integer representation of January */
	public static final int JANUARY = 1;
	/** Integer representation of June */
	public static final int JUNE = 6;
	/** Moving progress bar ahead by 1 */
	public static final int MOVE = 1;
	private static final String PULLING = "Pulling Data";
	private String begDate;
	private String endDate;
	private int quarter;
	private int value;
	private QueryResult spentQuery;
	private QueryResult itemsValueQuery;
	private QueryResult businessLeverageAmountQuery;
	private QueryResult donatedMoneyQuery;
	private QueryResult chapterNarrativeQuery;
	private QueryResult newFamiliesServedQuery;
	private QueryResult volunteerHoursQuery;
	private QueryResult referredQuery;

	/**
	 * Queries data and stores in SFQuery Object
	 * 
	 * @param pc Salesforce connection
	 * @param endFY ending fy num
	 * @param quarter quarter num
	 * @param pBar progress bar
	 * @param statusLabel action status
	 * @throws ConnectionException
	 */
	public SFQuery(PartnerConnection pc, int endFY, int quarter, JProgressBar pBar, JLabel statusLabel) throws ConnectionException {
		/*
		 * Determine dates for SOQL queries
		 */
		processDates(endFY, quarter);
		moveBar(pBar);
		statusLabel.setText(PULLING + "-Spent");
		this.spentQuery = pc.query(spentQuery());
		moveBar(pBar);
		statusLabel.setText(PULLING + "-Business Leveraging");
		this.businessLeverageAmountQuery = pc.query(businessLeverageAmountQuery());
		moveBar(pBar);
		statusLabel.setText(PULLING + "-Items Value");
		this.itemsValueQuery = pc.query(itemsValueQuery());
		moveBar(pBar);
		statusLabel.setText(PULLING + "-Donated Money");
		this.donatedMoneyQuery = pc.query(donatedMoneyQuery());
		moveBar(pBar);
		statusLabel.setText(PULLING + "-Chapter Narrative");
		this.chapterNarrativeQuery = pc.query(chapterNarrativeQuery());
		moveBar(pBar);
		statusLabel.setText(PULLING + "-New Families Served");
		this.newFamiliesServedQuery = pc.query(newFamiliesServedQuery());
		moveBar(pBar);
		statusLabel.setText(PULLING + "-Volunteer Hours");
		this.volunteerHoursQuery = pc.query(volunteerHoursQuery());
		moveBar(pBar);
		statusLabel.setText(PULLING + "-Number of Referrals");
		this.referredQuery = pc.query(referredQuery());
		moveBar(pBar);
		statusLabel.setText(PULLING + "-Complete");
	}

	private void moveBar(JProgressBar pBar) {
		value = pBar.getValue();
		pBar.setValue(value + MOVE);

	}

	/**
	 * This method processes and sets 
	 * dates based on fiscal year and 
	 * quarter number
	 *
	 * @param endFY
	 * @param quarter
	 */
	private void processDates(int endFY, int quarter) {
		this.quarter = quarter;
		switch (quarter) {
		case 1:
			this.begDate = endFY - 1 + "-07-01";
			this.endDate = endFY - 1 + "-09-30";
			break;
		case 2:
			this.begDate = endFY - 1 + "-10-01";
			this.endDate = endFY - 1 + "-12-31";
			break;
		case 3:
			this.begDate = endFY + "-01-01";
			this.endDate = endFY + "-03-31";
			break;
		case 4:
			this.begDate = endFY + "-04-01";
			this.endDate = endFY + "-06-30";
			break;
		default:
			System.out.println("Error");
			break;
		}

		try {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			// Set begin date for all SOQL queries
			Date beginDate = format.parse(begDate);
			Date today = new Date();
			if (beginDate.compareTo(today) > 0) {
				JOptionPane.showMessageDialog(null, "Invalid Quarter/Year Combination, Program will exit");
				System.exit(1);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Query:
	 * program name
	 * fif $ amount
	 * request number
	 * status of request
	 * total leverage $
	 * total $ of support
	 * family first & last name
	 * 
	 * from all requests where
	 * met date is equal to or between
	 * begin and end dates of this quarter
	 *
	 * @return Query String
	 */
	private String spentQuery() {
		return 
				"SELECT " + 
				"Program__c, " + 
				"FIF__c, " + 
				"CaseNumber, " + 
				"Status, " + 
				"Total_Leverage__c, " + 
				"Total_Value_of_Support__c, " + 
				"Account.Family_First_Name__c, " +
				"Account.Family_Last_Name__c " + 
				"FROM Case " + "WHERE Met_Date__c >= " + 
				begDate + " " + 
				"AND Met_Date__c <= " + 
				endDate;
	}

	/**
	 * Query for the percent of new families served since the beginning of the
	 * fiscal year. Begin date is changed to beginning of the fiscal year and
	 * end date remains unchanged.
	 *
	 * @return Query String
	 */
	private String newFamiliesServedQuery() {
		String begDate = "";
		int begDateMonth = Integer.parseInt(this.begDate.substring(5, 7));
		int begDateYear = Integer.parseInt(this.begDate.substring(0, 4));

		if ((begDateMonth >= JANUARY && begDateMonth <= JUNE)) {
			begDate = (begDateYear - 1) + "-07-01";
		} else {
			begDate = (begDateYear) + "-07-01";
		}
		/**
		 * Query:
		 * program name
		 * status of request
		 * start date of families account
		 * family first and last names
		 * family residential county
		 * 
		 * from requests whose met dates
		 * fall between beginning of FY year
		 * and the end date of the quarter.
		 */
		return 
		"SELECT " + 
		"Program__c, " + 
		"Status, " +
		"Account.StartDate__c, " + 
		"Account.Family_First_Name__c, " + 
		"Account.Family_Last_Name__c, " + 
		"Account.County__c " + 
		"FROM Case " + "WHERE Met_Date__c >= " + begDate + " " + "AND Met_Date__c <= " + endDate;
	}

	/**
	 *  
	 * Query:
	 * type of leveraging
	 * value of leveraging
	 * leveraging code/name
	 * program name (from parent)
	 * status of request (grandparent of leveraging)
	 * leveraging program
	 * 
	 * from leveraging object (child of disposition)
	 * where met date of request (grandparent)
	 * falls within begin and end date of quarter
	 *
	 * @return
	 */
	private String businessLeverageAmountQuery() {
		return 
				"SELECT " + 
				"Type__c, " + 
				"Value__c, " + 
				"Name, " + 
				"Disposition__r.Program__c, " + 
				"Disposition__r.Request__r.Status, " + 
				"Program__c " + "FROM Leverage__c "
				+ "WHERE Leverage__c.Disposition__r.Request__r.Met_Date__c >= " + 
				begDate +
				" AND Leverage__c.Disposition__r.Request__r.Met_Date__c <= " + 
				endDate;
	}

	/**
	 * Query for value of items donated
	 *
	 * @return Query String
	 */
	private String itemsValueQuery() {
		return "SELECT " + 
				"Program__c, " +
				"Item_Request_Value__c," +
				"Name, " +
				"Request__r.Status, " +
				"Request__r.Program__c, " +
				"Request__r.Met_Date__c, " + 
				"Request__r.RecordType.DeveloperName, " + 
				"Item__r.Organization__r.Date_of_Last_Request__c, " + 
				"Item__r.Name, " + 
				"Item__r.Organization__r.Program__c, " + 
				"Item__r.Organization__r.Name " + 
				"FROM Item_Request__c " + 
				"WHERE Item_Request__c.Request__r.Met_Date__c >= " + begDate + " " + 
				"AND Item_Request__c.Request__r.Met_Date__c <= " + endDate;
	}

	/**
	 * Query for amount of donated money.
	 *
	 * @return Query String
	 */
	private String donatedMoneyQuery() {
		return "SELECT " + 
				"Program__c, " + 
				"Donation_Amount__c, " + 
				"Request__r.Status, " + 
				"Request__r.CaseNumber, " + 
				"Request__r.Program__c " + 
				"FROM Disposition__c "
				+ "WHERE Request__r.Met_Date__c >= " + begDate + " " + 
				"AND Request__r.Met_Date__c <= " + 	endDate;
	}

	/**
	 * Chapter narrative, stories, and additional information needed for the
	 * quarterly report.
	 *
	 * @return Query String
	 */
	private String chapterNarrativeQuery() {
		return "SELECT " + 
				"Name, " + 
				"Best_Community_Partnership__c, " +
				"Best_Giving_Back_Story__c, " +
				"Best_Story__c, " +
				"Community_Events__c, " +
				"Date_From__c, " +
				"Date_To__c, "
				+ "Date_Policy_Procedure_Review__c, " + "Previous_Year_Leveraging__c, " + "Number_of_Grants_Applied_for_this_FQ__c, " + "Number_of_Fundraisers_Held__c, "
				+ "Number_of_MGMT_Team_Meetings_this_FQ__c, " + "Fundraising_Activity__c, " + "Grant_Money__c, " + "Total_Amount_Raised__c " + "FROM Chapter_Narrative__c " + "WHERE Date_To__c = "
				+ endDate;
	}

	/**
	 * Query for number of volunteer hours for a specific chapter.
	 *
	 * @return Query String
	 */
	private String volunteerHoursQuery() {
		return "SELECT " + "Volunteer_Task__c, " + "Volunteer_Hours__c, " + "Name, " + "Individual_Name__r.Family_Member_SelfAdvocate__c, " + "Individual_Name__r.Program__c, "
				+ "Individual_Name__r.FirstName, " + "Individual_Name__r.LastName, " + "Individual_Name__r.Account.Date_of_Last_Request__c, " + "Individual_Name__r.Management_Team_Member__c "
				+ "FROM Volunteer_Activity__c " + "WHERE Volunteer_Date__c >= " + begDate + " " + "AND Volunteer_Date__c <= " + endDate;
	}
	
	/**
	
	 * Query for number of volunteer hours for a specific chapter.
	 *
	 * @return Query String
	private String mtMembersQuery() {
		return "SELECT " + "Name, " + "Family_Member_SelfAdvocate__c, " + "Program__c, "
				+ "FirstName, " + "LastName, " + "Individual_Name__r.Management_Team_Member__c "
				+ "FROM Contact";
	}
	*/

	/**
	 * Query for number of referrals.
	 *
	 * @return Query String
	 */
	private String referredQuery() {
		return "SELECT " + 
				"RecordType.DeveloperName, " + 
				"Name, " + 
				"Program__c " + 
				"FROM Account " + 
				"WHERE StartDate__c >= " + 
				begDate + " " + "AND StartDate__c <= " + endDate;
	}

	/**
	 * This method TODO
	 *
	 * @return Spent Query
	 */
	public QueryResult getSpentQuery() {
		return spentQuery;
	}

	/**
	 * This method TODO
	 *
	 * @return Leverage Query
	 */
	public QueryResult getLeverageAmountQuery() {
		return businessLeverageAmountQuery;
	}

	/**
	 * This method TODO
	 *
	 * @return Items Value Query
	 */
	public QueryResult getItemsValueQuery() {
		return itemsValueQuery;
	}

	/**
	 * This method TODO
	 *
	 * @return Donated Money Query
	 */
	public QueryResult getDonatedMoneyQuery() {
		return donatedMoneyQuery;
	}

	/**
	 * This method TODO
	 *
	 * @return Business Leverage Query
	 */
	public QueryResult getBusinessLeverageAmountQuery() {
		return businessLeverageAmountQuery;
	}

	/**
	 * This method TODO
	 *
	 * @return Chapter Narrative Query
	 */
	public QueryResult getChapterNarrativeQuery() {
		return chapterNarrativeQuery;
	}

	/**
	 * This method TODO
	 *
	 * @return New Families Served Query
	 */
	public QueryResult getNewFamServedQuery() {
		return newFamiliesServedQuery;
	}

	/**
	 * This method TODO
	 *
	 * @return Volunteer Hours Query
	 */
	public QueryResult getVolunteerHoursQuery() {
		return volunteerHoursQuery;
	}

	/**
	 * This method TODO
	 *
	 * @return Referred Query
	 */
	public QueryResult getReferredQuery() {
		return referredQuery;
	}

	/**
	 * This method TODO
	 *
	 * @return quarter number
	 */
	public int getQuarter() {
		return quarter;
	}

	/**
	 * This method TODO
	 *
	 * @return begin date
	 */
	public String getBegDate() {
		return begDate;
	}

	/**
	 * This method TODO
	 *
	 * @return end date
	 */
	public String getEndDate() {
		return endDate;
	}

}
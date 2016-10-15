package connection;

import java.awt.Color;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.*;

import util.*;
import query.*;

import com.sforce.async.AsyncApiException;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

/**
 * Creates connection with Salesforce (SF), initializes query object and the
 * report file. Populates file fields with SF query data.
 * 
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
public class Connection {
	// Funds
	/** Empty Fund */
	public static final double EMPTYFUND = 0;
	/** Central Carolina Fund */
	public static final double CCFUND = 60_000;
	/** Durham Fund */
	public static final double DURFUND = 67_000;
	/** Gaston-Cleveland-Lincoln Fund */
	public static final double FCFUND = 62_500;
	/** Gaston-Cleveland-Lincoln Fund */
	public static final double GCLFUND = 74_100;
	/** High Country Fund */
	public static final double HCFUND = 130_000;
	/** Johnston Fund */
	public static final double JHNFUND = 89_250;
	/** Lifeline Project Fund */
	public static final double LPFUND = 93_588;
	/** Mecklenburg Fund */
	public static final double MCKFUND = 143_000;
	/** Sandhills Fund */
	public static final double SHFUND = 145_000;
	/** Southeastern Fund */
	public static final double SEFUND = 108_735;
	/** Southern Piedmont Fund */
	public static final double SPFUND = 82_000;
	/** Smoky Mountain Fund */
	public static final double SMFUND = 63_326;
	/** Triad Fund */
	public static final double TFUND = 107_016;
	/** Wake Fund */
	public static final double WFUND = 124_400;
	
	// Move Progress bar ahead
	/** Integer to move progress bar */
	public static final int MOVE = 1;
	
	// Punctuation
	/** Comma */
	public static final String C = ",";
	/** Quotation Mark */
	public static final String Q = "\"";

	// Number of columns, questions, programs
	/** Number of total iterations for column headers */
	public static final int QUARTER_MAX = 4;
	/** Number of data points for sheet header */
	public static final int DATA_POINTS = 30;
	/** Number of programs  TODO Add Cumberland */
	public static final int NUM_PROGRAMS = 14;
	
	/** Name of File */
	private static final String FILENAME = "Quarterly Report Data.csv";
	private BufferedWriter out;
	
	/** System messages & errors */
	private static final String COMPLETE = "Data processing complete. File saved";
	private static final String CONNECTING = "Establishing connection with Salesforce.com";
	private static final String CREATING = "Creating File";
	private static final String PULLING = "Pulling Data";
	private static final String OPEN = "File open. Please close all related files and restart program";
	private static final String CONNECTION_ERR = "Failed to connect. Please check your internet connection";
	private static final String IO_ERR = "IO Error";
	private static final String SAVING = "Saving file and terminating connection";
	private static final String INTERRUPTED_ERR = "Interrupted Error";
	
	/** Status of program */
	private String status;
	/** Error messages */
	private String error;
	
	/** LME/MCO Name */
	// LME/MCOs
	public static final String CI = "Cardinal Innovations HealthCare Solution";
	/** LME/MCO Name */
	public static final String ABH = "Alliance Behavioral Health";
	/** LME/MCO Name */
	public static final String PBH = "Partners Behavior Health Management";
	/** LME/MCO Name */
	public static final String SMO = "Smoky Mountain";
	/** LME/MCO Name */
	public static final String SC = "Sandhills Center";
	/** LME/MCO Name */
	public static final String TR = "Trillium";
	/** LME/MCO Name */
	public static final String CP = "Center Point";
	/** No Funder */
	public static final String NONE = "None";
	
	// Progress bar
	/** Value of progress bar */
	private int value;
	/** Progress bar object */
	private JProgressBar pBar;
	/** Label of the bar */
	private JLabel barLabel;
	
	/** Family Sets */
	private ArrayList<HashSet<String>> famServed = new ArrayList<HashSet<String>>(4);
	private ArrayList<HashSet<String>> newFamServed = new ArrayList<HashSet<String>>(4);
	
	/** Data Parameters */
	private int endFy;
	private int quarterNum;
	private String program;
	private String quarter;
	private String fy;
	private String[] programs;
	private StringBuffer data = new StringBuffer();
	private double leveraging;
	private double newFamPercent;
	private double newFamTotal;
	private double famTotal;
	
	/** Query Results */
	private ArrayList<SFQuery> queryResults = new ArrayList<SFQuery>();
	
	/** Salesforce Connections */
	private SFBulkConnect sfbc;
	private PartnerConnection pc;

	/**
	 * Creates connection with Salesforce.com
	 * and sets parameters
	 * 
	 * @param program program name
	 * @param quarter quarter number
	 * @param fy fiscal year
	 * @param programs programs array
	 * @param pBar progress bar
	 * @param barLabel progress bar label
	 * @throws AsyncApiException
	 * @throws ConnectionException
	 */
	public Connection(String program, String quarter, String fy, String[] programs, JProgressBar pBar, JLabel barLabel) throws ConnectionException, AsyncApiException {
		try {
		this.program = program;
		this.quarter = quarter;
		this.fy = fy;
		this.programs = programs;
		this.pBar = pBar;
		this.barLabel = barLabel;
		this.quarterNum = Integer.parseInt(this.quarter.substring(0));
		this.endFy = Integer.parseInt(this.fy.substring(5));
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILENAME)));
		setConnection(); // Creates API connection with Salesforce
		barLabel.setText(CREATING);
		createColumns(quarterNum, out, this.fy); // Sets up excel doc
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method creates a Bulk API 
	 * Connection using a username and password.
	 *
	 * @throws ConnectionException
	 * @throws AsyncApiException
	 */
	private void setConnection() throws ConnectionException, AsyncApiException {
		barLabel.setText(CONNECTING);
		sfbc = new SFBulkConnect(Credentials.USERNAME, Credentials.PASSWORD);
		pc = sfbc.getPartner();
	}

	/**
	 * Creates column headers for CSV file
	 *
	 * @param quarterNum Quarter Number
	 * @param out PrintWriter used create CSV file
	 * @throws IOException
	 */
	private void createColumns(int quarter, BufferedWriter out, String fiscalYearSelect) {
		/*
		 * Each document will begin with program name,
		 * quarter number, and fiscal year.
		 * 
		 * j is quarter number, k is
		 * associated number on quarterly
		 * report. e.g., 3.2 is Quarter 3
		 * question 2
		 */
		error = "";
		status = "";
		
		String header = "Program, Quarter Number, Year";
		for (int j = 1; j <= quarter; j++) {
            for (int k = 1; k <= DATA_POINTS; k++) {
                header += ", Q" + j + " " + k;
            }
        }
		try {
			/*
			 * Header is added to csv file
			 * and cursor is set to next line
			 */
			out.append(header);
			out.append("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Processes data for chapter(s)
	 * @throws InterruptedException
	 */
	public void process(){
		try {
			// Sets up funds and lme/mco array for processing
			double[] funding = new double[] { EMPTYFUND, CCFUND, DURFUND, FCFUND, GCLFUND, HCFUND, JHNFUND, LPFUND, MCKFUND, SHFUND, SEFUND, SPFUND, SMFUND, TFUND, WFUND };
			String[] mco = { NONE, CI, ABH, CI, SMO, SMO, ABH, ABH, CI, SC, TR, CI, SMO, CI, ABH }; 
			barLabel.setText(PULLING);
			for (int k = 1; k <= quarterNum; k++) {
				SFQuery query = new SFQuery(pc, endFy, k, pBar, barLabel);
				queryResults.add(query);
			}
			/*
			 * Queries Salesforce using SOQL,
			 * Partner Connection, current Fiscal Year,
			 * and current quarter. pBar and barLabel
			 * is passed to show current status
			 * Each query result is stored in queryResults
			 * array for access during 'process' method.
			 */
			if (program.equals("All")) {
				// Process all chapters if user selects 'All'
				for (int k = 1; k < programs.length; k++) {
					process(queryResults, pc, out, endFy, programs[k], funding[k], mco[k]);
					moveBar(pBar);
					clear();
				}
			} else {
				// Only process data for selected program
				for (int k = 1; k < programs.length; k++) {
					if (programs[k].equals(program)) {
						process(queryResults, pc, out, endFy, programs[k], funding[k], mco[k]);
						moveBar(pBar);
						clear();
					}
				}
			}
			out.close(); // Saves file
			pc.logout(); // logs out of SF
			barLabel.setText(SAVING);
			value = pBar.getValue();
			while (value < 100) {
				pBar.setValue(value++);
				Thread.sleep(50);
			}
			pBar.setValue(value++);
			barLabel.setText(COMPLETE);
		} catch (FileNotFoundException e) {
			barLabel.setForeground(Color.RED);
			barLabel.setText(OPEN);
			pBar.setValue(0);
		} catch (IOException e) {
			barLabel.setForeground(Color.RED);
			barLabel.setText(IO_ERR);
			pBar.setValue(0);
		} catch (ConnectionException e) {
			barLabel.setForeground(Color.RED);
			barLabel.setText(CONNECTION_ERR);
			e.printStackTrace();
			pBar.setValue(0);
		} catch (InterruptedException e) {
			barLabel.setForeground(Color.RED);
			barLabel.setText(INTERRUPTED_ERR);
			e.printStackTrace();
			pBar.setValue(0);
		}
	}

	/**
	 * This method processes each programs data based on the fiscal year,
	 * quarter number, and program name. Data for each quarter is printed to the
	 * file
	 *
	 * @param queryResults Query Results
	 * @param pc Partner Connection
	 * @param out Buffered Writer
	 * @param fy Fiscal Year
	 * @param programName Program
	 * @param funding Funding amount
	 * @param mco MCO/LME
	 */
	public void process(ArrayList<SFQuery> queryResults, PartnerConnection pc, BufferedWriter out, int fy, String programName, double funding, String mco) {
		try {
			int quarter = queryResults.size();
			if (programName.equals("Lifeline Project")) {
				out.append("Lifeline Project" + C + quarter + C + (fy - 1) + "-" + fy + C);
				programName = "Lifeline Project-Gen";
			} else {
				out.append(programName + C + quarter + C + (fy - 1) + "-" + fy + C);
			}
			boolean lastLine = false;
			if (programName.equals("State-wide")) {
				lastLine = true;
			}
			for (int k = 1; k <= quarter; k++) {
				barLabel.setText("Processing data for " + programName + "\t\t-Quarter " + k);
				DecimalFormat dcf = new DecimalFormat("#.00");
				DecimalFormat dcfRound = new DecimalFormat("#");
				NumberFormat nmf = NumberFormat.getCurrencyInstance();
				SFQuery qr = queryResults.get(k - 1);
				String begDate = qr.getBegDate();
				String endDate = qr.getEndDate();
				Spent spent = new Spent(pc, qr.getSpentQuery(), programName, begDate, endDate);
				leveraging += spent.getTotalLeverageAmt();
				BusinessLeverageAmount businessLeverage = new BusinessLeverageAmount(pc, qr.getBusinessLeverageAmountQuery(), programName, begDate, endDate);
				DonatedMoneyItemsValue donatedMoneyItemsValue = new DonatedMoneyItemsValue(pc, qr.getItemsValueQuery(), qr.getDonatedMoneyQuery(), programName);
				ChapterNarrative chapterNarrative = new ChapterNarrative(pc, qr.getChapterNarrativeQuery(), programName, begDate, endDate);
				NewFamilies newFamilies = new NewFamilies(pc, qr.getNewFamServedQuery(), programName, begDate, endDate);
				cleanSet(newFamilies, famServed, newFamilies.getFamYTD(), k);
				cleanSet(newFamilies, newFamServed, newFamilies.getNewFamYTD(), k);
				newFamTotal += newFamServed.get(k - 1).size();
				famTotal += famServed.get(k - 1).size();
				newFamPercent = ((double)newFamTotal / famTotal) * 100;
				VolunteerHours volunteerHours = new VolunteerHours(pc, qr.getVolunteerHoursQuery(), programName, begDate, endDate);
				MTStats mtStats = new MTStats(pc, qr.getVolunteerHoursQuery(), programName, begDate, endDate);
				Referred referred = new Referred(pc, qr.getSpentQuery(), qr.getReferredQuery(), programName, begDate, endDate);
				int giveBack = volunteerHours.getNumGiveBack() + donatedMoneyItemsValue.getNumGiveBack();
				error += chapterNarrative.getError();
				float percentLeveraged = ((float) spent.getLeveragedCases()) / spent.getNumRequests();
				if (lastLine) {
				} else {
					data.append(spent.getNumRequests() + C); // 1 Num Requests
					data.append(spent.getNumFamily() + C); // 2 Num families served
					data.append(Q + nmf.format(spent.getTotalSpent()) + Q + C); // 3 Total spent
					data.append(Q + nmf.format(businessLeverage.getTotalBusLeverage()) + Q + C); // 4 Business Leverage
					data.append(Q + nmf.format(donatedMoneyItemsValue.getTotal()) + Q + C);  // 5 Donated Money Total
					data.append(Q + nmf.format(spent.getTotalValueOfSupport()) + Q + C); // 6 Total value of support
					data.append(Q + nmf.format(chapterNarrative.getTotalMoney()) + Q + C); // 7 Total Money
					data.append(volunteerHours.getNumVolunteerHours() + C); // 8 Volunteer Hours
					data.append(dcfRound.format(newFamPercent) + "%" + C); // 9 New Families
					data.append("Survey Results N/a" + C); // 10
					data.append(referred.getNumReferrals() + C); // 11 Num of Referrals		
					data.append(chapterNarrative.getNumMgmtMeetings() + C ); // 12 Num MT Meetings	
					data.append(dcfRound.format(100 * (mtStats.getTotalMTFamPercent())) + "%" + C); // 13 MT Family Percentage
					data.append(dcfRound.format(100 * (mtStats.getTotalMTActPercent())) + "%" + C); // 14 MT Activity Percentage
					data.append(giveBack + C); //15 Num of Families giving back
					data.append(chapterNarrative.getPolicyReview() + C); // 16 Date of Last Policy Review
					data.append("Summit Attendance" + C); //17
					data.append(chapterNarrative.getGrantsAppliedFor() + C); // 18 Num grants applied for
					data.append(chapterNarrative.getFundraisersHeld() + C); // 19
					data.append(Q + dcf.format(leveraging / ((funding / QUARTER_MAX) * (k))) + Q + C); // 20 Leveraging
					data.append(dcfRound.format(100 * percentLeveraged) + "%" + C); // 21
					data.append("Survey Result N/A" + C); // 22
					data.append("All Requests" + C); // 23
					data.append(Q + chapterNarrative.getBestCommunityPartnership() + Q + C); // Partnership (24)
					data.append(Q + chapterNarrative.getBestGivingBackStory() + Q + C); // Giving Back (25)
					data.append(Q + chapterNarrative.getBestStory() + Q + C); // Best Story (26)
					data.append(Q + chapterNarrative.getFundraisingActivity() + Q + C); // Fundraising (27)
					data.append(Q + chapterNarrative.getCommunityEvents() + Q + C); // Community Events (28)
					data.append(mco + C); // LME/MCO (29)
					data.append(Q + nmf.format(chapterNarrative.getPreviousYearLeverage()) + Q); // Leveraging (Previous FY) (30) 
				}
				printData(k, programName, nmf, dcf, dcfRound, spent, funding, businessLeverage, donatedMoneyItemsValue, volunteerHours, referred, mtStats, giveBack, funding, quarter, percentLeveraged);
				out.append(data.toString());
				data.delete(0, data.length());
				if (k != quarter) {
					out.append(C);
				}
			}
			out.append("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * For testing
	 * @param k 
	 * @param programName 
	 * @param dcf 
	 * @param nmf 
	 * @param dcfRound 
	 * @param percentLeveraged 
	 * @param quarter2 
	 * @param funding 
	 * @param giveBack 
	 * @param mtStats 
	 * @param referred 
	 * @param volunteerHours 
	 * @param donatedMoneyItemsValue 
	 * @param businessLeverage 
	 * @param spent 
	 * @param funding 
	 * @param funding2 
	 * 
	 * @param data
	 */
	private void printData(
			int k,
			String programName, 
			NumberFormat nmf, 
			DecimalFormat dcf, 
			DecimalFormat dcfRound, 
			Spent spent, 
			double funding, 
			BusinessLeverageAmount businessLeverage, 
			DonatedMoneyItemsValue donatedMoneyItemsValue, 
			VolunteerHours volunteerHours, 
			Referred referred, 
			MTStats mtStats, 
			int giveBack, 
			double funding2, 
			int quarter, 
			float percentLeveraged) {
		
		StringBuffer data = new StringBuffer();
		data.append("Program: " + programName + "\tQuarter: " + k + "\n");
		data.append("1. " + spent.getNumRequests() + "\n"); // 1 Num Requests
		data.append("2. " + spent.getNumFamily() + "\n"); // 2 Num Families Served
		data.append("3. " + nmf.format(spent.getTotalSpent())+ "\n"); // 3 Total Spent
		data.append("4. " + nmf.format(businessLeverage.getTotalBusLeverage())+ "\n");  // 4 Business Leverage
		data.append("5. " + nmf.format(donatedMoneyItemsValue.getTotal())+ "\n"); // 5 Donated Money Total
		data.append("6. " + nmf.format(spent.getTotalValueOfSupport())+ "\n"); // 6 Total Value of Support
		data.append("8. " + volunteerHours.getNumVolunteerHours()+ "\n"); // 8 Volunteer Hours
		data.append("9. " + dcfRound.format(newFamPercent) + "%"+ "\n"); // 9 New Families
		data.append("11. " + referred.getNumReferrals() + "\n"); // 11 Num of Referrals
		data.append("13. " + dcfRound.format(100 * (mtStats.getTotalMTFamPercent())) + "%" + "\n"); // 13 MT Family Percentage
		data.append("14. " + dcfRound.format(100 * (mtStats.getTotalMTActPercent())) + "%" + "\n"); // 14 MT Activity Percentage
		data.append("15. " + giveBack + "\n"); //15 Num of Families giving back
		data.append("20. " + dcf.format(leveraging / ((funding / QUARTER_MAX) * (k))) + "\n"); // 20 Leveraging
		data.append("21. " + dcfRound.format(100 * percentLeveraged) + "%" + "\n"); // 21
		System.out.println(data);
	}

	/**
	 * For testing. Returns all report data
	 * 
	 * @return the data
	 */
	public StringBuffer getData() {
		return data;
	}

	/**
	 * This method returns the status of the program and includes errors
	 * encountered.
	 *
	 * @return status and associated errors of program
	 */
	public String getStatus() {
		if (error.equals("")) {
			return "";
		}
		return "<html>" + status + "<br>Errors:<br>" + error + "</html>";
	}

	/**
	 * Return the fiscal year
	 * @return the fy
	 */
	public String getFy() {
		return fy;
	}

	/**
	 * Return array of query results for each quarter
	 * @return the queryResults
	 */
	public ArrayList<SFQuery> getQueryResults() {
		return queryResults;
	}

	/**
	 * Set array to query results for each quarter
	 * @param queryResults the queryResults to set
	 */
	public void setQueryResults(ArrayList<SFQuery> queryResults) {
		this.queryResults = queryResults;
	}

	/**
	 * Return bulk connection for Salesforce (SF)
	 * @return the sfbc
	 */
	public SFBulkConnect getSfbc() {
		return sfbc;
	}

	/**
	 * Returns the partner connection
	 * @return the pc
	 */
	public PartnerConnection getPc() {
		return pc;
	}

	/**Returns latest fiscal year as int
	 * @return the endFy
	 */
	public int getEndFy() {
		return endFy;
	}
	
	/**
	 * Returns the quarter number
	 * @return the quarterNum
	 */
	public int getQuarterNum() {
		return quarterNum;
	}

	/**
	 * Returns the array list of all new
	 * families served for quarters 1 - quarter.
	 * @return the newFamServed
	 */
	public ArrayList<HashSet<String>> getNewFamServed() {
		return newFamServed;
	}

	private void moveBar(JProgressBar pBar) {
		value = pBar.getValue();
		pBar.setValue(value + MOVE);
	}

	/**
	 * Resets numbers and sets once a given program has processed
	 */
	private void clear() {
		famServed.clear();
		newFamServed.clear();
		leveraging = 0;
	}

	/**
	 * Looks for dups and cleans sets
	 * @param newFamilies New Families
	 * @param fullSet Cumulative set containing data for each quarter
	 * @param quartSet Single set containing data for this quarter
	 * @param k Quarter
	 */
	private void cleanSet(NewFamilies newFamilies, ArrayList<HashSet<String>> fullSet, HashSet<String> quartSet, int k) {
		// Cycles through set 
		for (int m = 0; m < fullSet.size(); m++) {
			
			/**
			 * Creates a set named compare that contains 
			 * the elements for the current quarter. We
			 * want to see if any of the data in 
			 * this set is found in the set named
			 * fullSet, which includes  the elements
			 * from each quarter so far. We retain
			 * only the elements in the compare
			 * set that are found in the fullSet. In
			 * other words, we remove all the elements
			 * in the compare set that are not found in
			 * the fullSet. Compare set now contains
			 * the duplicates. We then remove the 
			 * duplicates from the quarter set that
			 * have been found and stored in the
			 * compare set.
			 */
			HashSet<String> compare = new HashSet<String>(quartSet);
			compare.retainAll(fullSet.get(m));
			quartSet.removeAll(compare);
			}
		 fullSet.add(k - 1, quartSet);
	}
}
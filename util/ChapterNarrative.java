package util;

import java.awt.event.ActionEvent;
import java.text.ParseException;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

/**
 * Pulls data fromt the chapter narrative in SF
 * 
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
public class ChapterNarrative implements ProgramData{
	/** Error message for missing data */
	public static final String NO_DATA = "No data available";
	String programName;
	String bestCommunityPartnership;
	String bestGivingBackStory;
	String bestStory;
	String fundraisingActivity;
	String communityEvents;
	String error;
	String policyReview;
	int grantsAppliedFor;
	int fundraisersHeld;
	int numMgmtMeetings;
	double grantMoneyAwarded;
	double fundraisingMoney;
	double previousYearLeveraging;

	/**
	 * Constructor, pulls data from chapter narrative
	 * 
	 * @param pc PartnerConnection
	 * @param qr QueryResult
	 * @param programName Program Name
	 * @param begDate Beginning Date
	 * @param endDate Ending Date
	 */
	public ChapterNarrative(PartnerConnection pc, QueryResult qr, String programName, String begDate, String endDate) {
		try {
			boolean done = false;
			grantsAppliedFor = 0;
			fundraisersHeld = 0;
			numMgmtMeetings = 0;
			grantMoneyAwarded = 0;
			fundraisingMoney = 0;
			previousYearLeveraging = 0;
			while (!done) {
				SObject[] records = qr.getRecords();
				for (int i = 0; i < records.length; i++) {
					if (programName.equals("Lifeline Project-Gen")) {
						programName = "Lifeline Project";
					}
					SObject info = records[i];
					Object program = info.getField("Name");
					Object bestCommunityPartnershipThisFQ = info.getField("Best_Community_Partnership__c");
					Object bestGivingBackStoryThisFQ = info.getField("Best_Giving_Back_Story__c");
					Object bestStoryThisFQ = info.getField("Best_Story__c");
					Object communityEventsThisFQ = info.getField("Community_Events__c");
					Object numberOfGrantsAppliedForThisFQ = info.getField("Number_of_Grants_Applied_for_this_FQ__c");
					Object numberOfFundraisersHeldThisFQ = info.getField("Number_of_Fundraisers_Held__c");
					Object numberOfMGMTTeamMeetingsThisFQ = info.getField("Number_of_MGMT_Team_Meetings_this_FQ__c");
					Object fundraisingActivity = info.getField("Fundraising_Activity__c");
					Object grantMoneyThisFQ = info.getField("Grant_Money__c");
					Object totalAmountRaised = info.getField("Total_Amount_Raised__c");
					Object policyReview = info.getField("Date_Policy_Procedure_Review__c");
					Object previousYearLeverage = info.getField("Previous_Year_Leveraging__c");

					program = chooseProgram(program);
					if (program.toString().equals(programName)) {

						try {
							grantsAppliedFor = new Double(numberOfGrantsAppliedForThisFQ.toString()).intValue();
							fundraisersHeld = new Double(numberOfFundraisersHeldThisFQ.toString()).intValue();
							numMgmtMeetings = new Double(numberOfMGMTTeamMeetingsThisFQ.toString()).intValue();
							grantMoneyAwarded = new Double(grantMoneyThisFQ.toString());
							fundraisingMoney = new Double(totalAmountRaised.toString());
							previousYearLeveraging = new Double(previousYearLeverage.toString());
							this.programName = (String) program;
							this.bestCommunityPartnership = bestCommunityPartnershipThisFQ.toString();
							this.bestGivingBackStory = bestGivingBackStoryThisFQ.toString();
							this.bestStory = bestStoryThisFQ.toString();
							this.fundraisingActivity = fundraisingActivity.toString();
							this.communityEvents = communityEventsThisFQ.toString();
							if (policyReview == null) {
								this.policyReview = "No Policy Review";
							} else {
								this.policyReview = policyReview.toString();
							}
							error = "";
							break;
						} catch (NullPointerException e) {
							error = programName + " - Chapter Narrative Incomplete" + "(" + begDate + ")<br>";
							break;
						}
					} else {
						error = "No Chapter Narrative for " + programName + "<br>";
					}

				}
				if (qr.isDone()) {
					done = true;
				} else {
					qr = pc.queryMore(qr.getQueryLocator());
				}
			}
		} catch (ConnectionException ce) {
			ce.printStackTrace();
		}
	}

	/**
	 * Gets program name
	 * 
	 * @return the Program name
	 */
	public String getProgramName() {
		return programName;
	}

	/**
	 * Gets best community partnership and formats it by removing all "
	 * 
	 * @return the bestCommunityPartnership
	 */
	public String getBestCommunityPartnership() {
		if (bestCommunityPartnership != null) {
			String edit = bestCommunityPartnership.replaceAll("\"", "");
			return edit;
		}
		return NO_DATA;
	}

	/**
	 * Gets the best giving back story and formats it by removing all "
	 * 
	 * @return the bestGivingBackStory
	 */
	public String getBestGivingBackStory() {
		if (bestGivingBackStory != null) {
			String edit = bestGivingBackStory.replaceAll("\"", "");
			return edit;
		}
		return NO_DATA;
	}

	/**
	 * Gets the best story and formats it by removing all "
	 * 
	 * @return the bestStory
	 */
	public String getBestStory() {
		if (bestStory != null) {
			String edit = bestStory.replaceAll("\"", "");
			return edit;
		}
		return NO_DATA;
	}

	/**
	 * Gets fundraising activity
	 * 
	 * @return the bestStory
	 */
	public String getFundraisingActivity() {
		if (fundraisingActivity != null) {
			String edit = fundraisingActivity.replaceAll("\"", "");
			return edit;
		}
		return NO_DATA;
	}

	/**
	 * Gets community events and formats it by removing all "
	 * 
	 * @return the communityEvents
	 */
	public String getCommunityEvents() {
		if (communityEvents != null) {
			String edit = communityEvents.replaceAll("\"", "");
			return edit;
		}
		return NO_DATA;
	}

	/**
	 * This method gets the error messages that are displayed on the GUI
	 *
	 * @return error messages
	 */
	public String getError() {
		if (error == null || error.contains("null")) {
			return "";
		}
		return error;
	}

	/**
	 * Gets integer value from string for number of grants applied for
	 * 
	 * @return the grantsAppliedFor
	 */
	public int getGrantsAppliedFor() {
		return grantsAppliedFor;
	}

	/**
	 * Gets integer value from string for number of fundraisers held
	 * 
	 * @return the fundraisersHeld
	 */
	public int getFundraisersHeld() {
		return fundraisersHeld;
	}

	/**
	 * Returns number of management team meetings held
	 * 
	 * @return the numMgmtMeetings
	 */
	public int getNumMgmtMeetings() {
		return numMgmtMeetings;
	}

	/**
	 * Returns grant money received
	 * 
	 * @return the grantMoney
	 */
	public double getGrantMoneyAwarded() {
		return grantMoneyAwarded;
	}

	/**
	 * Returns fundraising money received
	 * 
	 * @return the fundraisingMoney
	 */
	public double getFundraisingMoney() {
		return fundraisingMoney;
	}

	/**
	 * Returns leverage from previous year
	 * 
	 * @return the previousYearLeveraging
	 */
	public double getPreviousYearLeverage() {
		return previousYearLeveraging;
	}

	/**
	 * Returns total money raised/awarded
	 *
	 * @return total money raised/awarded
	 */
	public double getTotalMoney() {
		return fundraisingMoney + grantMoneyAwarded;
	}

	/**
	 * This method gets policy review date
	 *
	 * @return Policy Review Date
	 * @throws ParseException
	 */
	public String getPolicyReview() throws ParseException {
		if (policyReview == null || !policyReview.matches(".*\\d+.*")
				|| Integer.parseInt(policyReview.substring(0, 4)) < 2000) {
			return "N/A";
		}
		return policyReview.substring(5) + "-" + policyReview.substring(0, 4);
	}

	/**
	 * This method formats the program name
	 * 
	 * @param Name program name
	 * @return formatted program name
	 */
	private String chooseProgram(Object name) {
		
		if (name!= null) {
			for (int i = 0; i < programs.length; i++) {
				if(name.toString().contains(programs[i])){
					name = programs[i];
				}
			}
		}
		return (String) name;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}

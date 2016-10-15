package query;

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
public class SFStoriesQuery {
	private int quarter;
	private QueryResult chapterStoriesQuery;



	/**
	 * Queries data and stores in SFQuery Object
	 * 
	 * @param pc Partner Connection
	 * @throws ConnectionException
	 */
	public SFStoriesQuery(PartnerConnection pc) throws ConnectionException {
		this.chapterStoriesQuery = pc.query(chapterStoriesQuery());

	}

	/**
	 * Chapter narrative, stories, and
	 * additional information needed for
	 * the quarterly report.
	 *
	 * @return Query String
	 */
	private String chapterStoriesQuery() {
		return "SELECT " 
				+ "Name, " 
				+ "Best_Community_Partnership__c, "
				+ "Best_Giving_Back_Story__c, " 
				+ "Best_Story__c, "
				+ "Community_Events__c, " 
				+ "Date_From__c, " 
				+ "Date_To__c, "
				+ "Date_Policy_Procedure_Review__c, "
				+ "Number_of_Grants_Applied_for_this_FQ__c, "
				+ "Number_of_Fundraisers_Held__c, "
				+ "Number_of_MGMT_Team_Meetings_this_FQ__c,"
				+ "Grant_Money__c, " 
				+ "Total_Amount_Raised__c "
				+ "FROM Chapter_Narrative__c";
	}
	/**
	 * This method TODO
	 *
	 * @return Chapter Narrative Query
	 */
	public QueryResult getChapterStoriesQuery() {
		return chapterStoriesQuery;
	}

	/**
	 * This method TODO
	 *
	 * @return quarter number
	 */
	public int getQuarter() {
		return quarter;
	}

}
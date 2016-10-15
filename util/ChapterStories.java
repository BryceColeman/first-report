package util;

import java.io.IOException;
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

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

/**
 * Pulls data from the chapter narrative in SF
 * 
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
public class ChapterStories {
	String programName;
	String bestCommunityPartnership;
	String bestGivingBackStory;
	String bestStory;
	String communityEvents;
	String error;
	String policyReview;

	/**
	 * Constructor, pulls data from chapter narrativ
	 * 
	 * @param programName Program Name
	 * @throws IOException
	 */
	public ChapterStories(String programName) throws IOException {
		this.programName = programName;
	}


	/**
	 * This method gets story data for given program
	 *
	 * @param programName Program Name
	 * @param pc Partner Connection
	 * @param qr Query Results
	 * @param doc 
	 */
	public void processStories(String programName, PartnerConnection pc, QueryResult qr, XWPFDocument doc) {
		try {
			boolean done = false;
			HashSet<String> givingBackSet = new HashSet<String>();
			HashSet<String> partnershipSet = new HashSet<String>();
			HashSet<String> bestStorySet = new HashSet<String>();
			XWPFParagraph title = doc.createParagraph();
			XWPFParagraph story = doc.createParagraph();
			XWPFRun runTitle = title.createRun();
			
			runTitle.setBold(true);
			runTitle.setFontSize(14);
			runTitle.setFontFamily("Cambria");
			runTitle.setText("Chapter Narrative Stories - " + programName);
			while (!done) {
				SObject[] records = qr.getRecords();
				for (int i = 0; i < records.length; i++) {
					SObject info = records[i];
					Object program = info.getField("Name");
					Object partnershipStory = info.getField("Best_Community_Partnership__c");
					Object givingBackStory = info.getField("Best_Giving_Back_Story__c");
					Object bestOverallStory = info.getField("Best_Story__c");
					Object dateFrom = info.getField("Date_From__c");
					Object dateTo = info.getField("Date_To__c");
				
					program = formatProgramName(program);
					
					if (program.toString().equals(programName) && dateFrom != null && dateTo != null) {
						try {
							
							DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
							Date dateToAsDate = format.parse(dateTo.toString());
							Date dateFromAsDate = format.parse(dateFrom.toString());
							
							String givingBack = givingBackStory.toString();
							String partnership = partnershipStory.toString();
							String bestStory = bestOverallStory.toString();

							String fromMonth = new SimpleDateFormat("MMMM").format(dateFromAsDate);
							String toMonth = new SimpleDateFormat("MMMM").format(dateToAsDate);
							String fromYear = new SimpleDateFormat("yyyy").format(dateFromAsDate);
							
							XWPFRun runStoryBlockBold = story.createRun();
							
							runStoryBlockBold.setFontSize(12);
							runStoryBlockBold.setFontFamily("Cambria");
							runStoryBlockBold.setBold(true);
							
							runStoryBlockBold.setText(fromMonth + "-" + toMonth + " | " + fromYear + " ");
							
							XWPFRun runStoryBlock = story.createRun();
							
							runStoryBlock.setFontSize(12);
							runStoryBlock.setFontFamily("Cambria");
							runStoryBlock.setBold(false);
							runStoryBlock.addBreak();
							
							runStoryBlock.setText("Giving Back Story: " + givingBack);
							runStoryBlock.addBreak();
							runStoryBlock.addBreak();

							runStoryBlock.setText("Partnership Story: " + partnership);
							runStoryBlock.addBreak();
							runStoryBlock.addBreak();

							runStoryBlock.setText("Best Story: " + bestStory);
							runStoryBlock.addBreak();
							runStoryBlock.addBreak();
							
							
							givingBackSet.add(givingBack);
							partnershipSet.add(partnership);
							bestStorySet.add(bestStory);
							

						} catch (NullPointerException | ParseException e) {
							continue;
						}
					} // End if
				} // End for
				if (qr.isDone()) {
					done = true;
				} else {
					qr = pc.queryMore(qr.getQueryLocator());
				}
			} // End while
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
		return bestCommunityPartnership;
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
		return bestGivingBackStory;
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
		return "Missing Chapter Narrative Data";
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
		return communityEvents;
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
	 * This method formats the program name
	 * 
	 * @param Name
	 *            program name
	 * @return formatted program name
	 */
	private String formatProgramName(Object name) {
		if (name != null) {
			String nameVar = name.toString().substring(0, 3);
			switch (nameVar) {
			case "Joh":
				name = "Johnston";
				break;
			case "San":
				name = "Sandhills";
				break;
			case "Sou":
				if (name.toString().length() > 11) {
					name = "Southern Piedmont";
				} else {
					name = "Southeastern";
				}
				break;
			case "Wak":
				name = "Wake";
				break;
			case "Dur":
				name = "Durham";
				break;
			case "Smo":
				name = "Smoky Mountain";
				break;
			case "Hig":
				name = "High Country";
				break;
			case "Cen":
				name = "Central Carolina";
				break;
			case "Mec":
				name = "Mecklenburg";
				break;
			case "Gas":
				name = "Gaston-Cleveland-Lincoln";
				break;
			case "Fiv":
				name = "Five County";
				break;
			case "Tri":
				name = "Triad";
				break;
			default:
				name = "Error";
				break;
			}
		}
		return (String) name;
	}
}
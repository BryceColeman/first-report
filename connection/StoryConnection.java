package connection;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import util.*;
import query.*;

import com.sforce.soap.partner.PartnerConnection;

/**
 * Creates connection with Salesforce (SF), initializes query object and the
 * report file. Populates file fields with SF query data.
 * 
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
public class StoryConnection {
	/** Number of iterations for sheet header */
	public static final int HEADER_LENGTH = 4;
	/** Number of questions for sheet header */
	public static final int NUM_QUESTIONS = 27;
	/** Name of file */
	public static final String FILENAME = "Stories.doc";
	/** SF Username */
	@SuppressWarnings("unused")
	private static final String USERNAME = "bryce@fifnc.org";
	/** SF Password */
	@SuppressWarnings("unused")
	private static final String PASSWORD = "jemaco21dZuNOIlRNsHyZfrUGy4CgmG5";
	/** Program Name */
	private String programName;
	private String status;

	/**
	 * Creates connection with SF and initializes file. Stores query data
	 * objects in ArrayList 'queryResults' and uses queryResults to process
	 * data.
	 * 
	 * @param programSelect
	 *            Program selected by user
	 * @param programs
	 *            Array of programs
	 * @throws ClassCastException
	 * @throws ConnectionException
	 * @throws AsyncApiException
	 * @throws IOException
	 * @throws ParseException
	 */
	/** public StoryConnection(String programSelect, String[] programs) {
		try {
			SFBulkConnect sfbc = createConnection(USERNAME, PASSWORD);
			PartnerConnection pc = sfbc.getPartner();
			FileOutputStream out = new FileOutputStream("FIFNC Stories.doc");
			XWPFDocument doc = new XWPFDocument();
			// Creates ArrayList to hold query objects
			SFStoriesQuery queryResults = new SFStoriesQuery(pc);
			if (programSelect.equals("All")) {
				for (int k = 1; k < programs.length; k++) {
					process(queryResults, pc, out, doc, programs[k]);
				}
			} else {
				// Only process data for selected program
				process(queryResults, pc, out, doc, programSelect);
			}
			status = " All stories have been processed. File saved.";
			doc.write(out);
			doc.close();
			out.close(); // Saves file
			pc.logout();

		} catch (ConnectionException | AsyncApiException | IOException e) {
			JOptionPane.showMessageDialog(null, "Please close all related files");
		}

	}

	/**
	 * This method creates a Bulk Connection using a username and password.
	 *
	 * @param username
	 *            Master Username for SF
	 * @param password
	 *            Master Password for SF
	 * @return bulkConnection
	 * @throws ConnectionException
	 * @throws AsyncApiException
	 */
	/**
	 * public SFBulkConnect createConnection(String username, String password) throws ConnectionException, AsyncApiException {
		SFBulkConnect bulkConnection = new SFBulkConnect(username, password, null);
		return bulkConnection;
	}
	*/

	/**
	 * This method processes each programs data based on the fiscal year,
	 * quarter number, and program name. Data for each quarter is printed to the
	 * file
	 *
	 * @param qr
	 *            QueryResults
	 * @param pc
	 *            PartnerConnection
	 * @param out
	 *            FileOutputStream
	 * @param doc
	 *            XWPFDocument
	 * @param programName
	 *            Program Name
	 * @throws ConnectionException
	 * @throws IOException
	 * @throws ParseException
	 */
	public void process(SFStoriesQuery qr, PartnerConnection pc, FileOutputStream out, XWPFDocument doc, String programName) {
		try {
			ChapterStories cs = new ChapterStories(programName);
			cs.processStories(cs.getProgramName(), pc, qr.getChapterStoriesQuery(), doc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method @return name of program
	 */
	public String getProgramName() {
		return programName;
	}

	/**
	 * This method @return application status
	 */
	public String getStatus() {
		return status;
	}
}
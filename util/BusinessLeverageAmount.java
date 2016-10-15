package util;

import java.util.HashSet;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;
/**
 * Calculates total business leveraging
 * and total requests leveraged.
 * 
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
public class BusinessLeverageAmount {
	/* Total Business Leverage	 */
	private double totalBusLeverage;
	/* Total Requests Leveraged */
	private double totalRequestsLeveraged;

	/**
	 * Initializes object
	 * 
	 * @param pc PartnerConnection
	 * @param qr QueryResult
	 * @param programName Program name
	 * @param begDate Beginning date
	 * @param endDate End date
	 */
	public BusinessLeverageAmount(PartnerConnection pc, QueryResult qr, String programName, String begDate, String endDate) {
		this.totalBusLeverage = processData(pc, qr, 0, programName, begDate, endDate);
		
	}


	/**
	 * This method TODO
	 *
	 * @param pc PartnerConnection
	 * @param qr QueryResult
	 * @param totalBusLeverage total business leveraging
	 * @param programName program name
	 * @param begDate beginning date
	 * @param endDate ending date
	 * @return total business leveraging
	 */
	public double processData(PartnerConnection pc, QueryResult qr, double totalBusLeverage, String programName, String begDate, String endDate) {
		try {
			boolean done = false;
			HashSet<String> leveragedRequestsSet = new HashSet<String>();
			while (!done) {
				SObject[] records = qr.getRecords();
				for (int i = 0; i < records.length; i++) {
					SObject info = records[i];
					XmlObject disposition = (XmlObject) records[i].getField("Disposition__r");
					XmlObject request = (XmlObject) disposition.getField("Request__r");
					String status = (String) request.getChild("Status").getValue();
					String program = (String) disposition.getChild("Program__c").getValue();
					Object leverageProgram = info.getField("Program__c");
					Object type = info.getField("Type__c");
					Object value = info.getField("Value__c");
					Object name = info.getField("Name");
					double discountValue = new Double(value.toString());
					if (leverageProgram != null 
							&& program.toString().equals(programName)
							&& leverageProgram.toString().equals(programName)
							&& (type.toString().equals("Discounted/Donated Other Service(s)")
							|| type.toString().equals("Discounted/Donated Professional Service")
							|| type.toString().equals("Discounted/Donated Product(s)"))
							&& !status.toString().equals("Closed - Referred")
							&& !status.toString().equals("Closed - Suspended")
							&& !status.toString().equals("Resourcing")) {
						totalBusLeverage += discountValue;
						leveragedRequestsSet.add((String) name);
					}
				}
				if (qr.isDone()) {
					done = true;
				} else {
					qr = pc.queryMore(qr.getQueryLocator());
				}
				this.totalBusLeverage = totalBusLeverage;
				this.totalRequestsLeveraged = leveragedRequestsSet.size();
			}
		} catch (ConnectionException ce) {
			ce.printStackTrace();
		}
		return totalBusLeverage;
	}

	/**
	 * This method TODO
	 *
	 * @return total business leverage
	 */
	public double getTotalBusLeverage() {
		return totalBusLeverage;
	}
	
	/**
	 * This method TODO
	 *
	 * @return total requests leveraged
	 */
	public double getTotalRequestsLeveraged(){
		return totalRequestsLeveraged;
	}
}
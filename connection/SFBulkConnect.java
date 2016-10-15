package connection;

import com.sforce.async.*;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * Creates SOAP API connection with Salesforce.com
 * 
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
public class SFBulkConnect {
	/** For moving progress bar by 1 */
	public static final int MOVE = 1;
	private ConnectorConfig partnerConfig;
	private ConnectorConfig connectorConfig;
	private BulkConnection bulkConnection;
	private PartnerConnection partnerConnection;

	/**
	 * Creates connection using master
	 * username and password.
	 * 
	 * @param username Username
	 * @param password Passwords
	 * @throws ConnectionException
	 * @throws AsyncApiException
	 */
	public SFBulkConnect(String username, String password)
			throws ConnectionException, AsyncApiException{
		partnerConfig = new ConnectorConfig();
		partnerConfig.setUsername(username);
		partnerConfig.setPassword(password);
		partnerConfig.setAuthEndpoint("https://login.salesforce.com/services/Soap/u/34.0");
		partnerConnection = new PartnerConnection(partnerConfig);
		connectorConfig = new ConnectorConfig();
		connectorConfig.setSessionId(partnerConfig.getSessionId());
		String soapEndpoint = partnerConfig.getServiceEndpoint();
		String apiVersion = "34.0";
		String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + apiVersion;
		connectorConfig.setRestEndpoint(restEndpoint);
		bulkConnection = new BulkConnection(connectorConfig);
	}

	/**
	 * Gets Partner Configuration for connecting
	 * to Salesforce.com
	 *
	 * @return partnerConfig Partner Configuration
	 */
	public ConnectorConfig getPartnerConfig() {
		return partnerConfig;
	}

	/**
	 * Gets Connecter Configuration for connecting
	 * to Salesforce.com
	 *
	 * @return connectorConfig Connector Configuration
	 */
	public ConnectorConfig getConfig() {
		return connectorConfig;
	}

	/**
	 * Gets Bulk Connection for connecting
	 * to Salesforce.com
	 *
	 * @return bulkConnection Bulk Connection
	 */
	public BulkConnection getConnection() {
		return bulkConnection;
	}

	/**
	 * Gets Partner Connection for connecting
	 * to Salesforce.com
	 *
	 * @return partnerConnection Partner Connection
	 */
	public PartnerConnection getPartner() {
		return partnerConnection;
	}

}
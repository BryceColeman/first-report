package ui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.concurrent.Executors;

import javax.swing.*;

import util.ProgramData;

import com.sforce.async.AsyncApiException;
import com.sforce.ws.ConnectionException;

import connection.Connection;

/**
 * GUI to run program
 * 
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
public class FirstReportGUI implements ProgramData {
	// Font
	/** System Font */
	public static final String FONT = "Century Gothic";

	// GUI Labels
	private JLabel programLabel = new JLabel("Program");
	private JLabel quarterLabel = new JLabel("Quarter");
	private JLabel fiscalYearLabel = new JLabel("Fiscal Year");
	private JLabel errorLabel = new JLabel("");
	private JLabel firstReportLabel = new JLabel("First Report 4.1");
	private JLabel storiesLabel = new JLabel("Stories Only");
	private JLabel statusLabel = new JLabel("");

	// Combo boxes
	private JComboBox<String> programsCombo = new JComboBox<String>();
	private JComboBox<String> quartersCombo = new JComboBox<String>();
	private JComboBox<String> fiscalYearsCombo = new JComboBox<String>();
	private JComboBox<String> beginStoryComboQuarter = new JComboBox<String>();
	private JComboBox<String> beginStoryComboYear = new JComboBox<String>();
	private JComboBox<String> endStoryComboQuarter = new JComboBox<String>();
	private JComboBox<String> endStoryComboYear = new JComboBox<String>();

	// Icon, check box, progress bar, start button
	private Icon fifLogo;
	private JCheckBox storiesCheckBox = new JCheckBox();
	private JProgressBar pBar = new JProgressBar();
	private JButton startButton = new JButton("Start");

	// Panels
	private JPanel operationPanel = new JPanel();
	private JPanel titlePanel = new JPanel();
	private JPanel mainPanel = new JPanel();

	// Frame
	private JFrame frame;

	/**
	 * Initialize the contents of the frame.
	 */
	public void initialize() {
		// First In Families of NC Logo
		fifLogo = createImageIcon("/images/fifLogo.png", "fifLogo");

		// Set up frame
		frame = new JFrame();
		frame.setBounds(100, 100, 440, 350);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null); // Center on screen

		// Add title and main components
		frame.getContentPane().add(titlePanel, BorderLayout.NORTH);
		frame.getContentPane().add(mainPanel, BorderLayout.CENTER);

		// Add
		mainPanel.add(operationPanel);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		operationPanel.setLayout(null); // Center components
		pBar.setValue(0);
		pBar.setStringPainted(true);

		// Add content to Combo boxes
		for (int k = 0; k < programs.length; k++) {
			programsCombo.insertItemAt(programs[k], k);
		}
		for (int k = 0; k < fiscalYears.length; k++) {
			fiscalYearsCombo.insertItemAt(fiscalYears[k], k);
		}
		for (int k = 0; k < quarters.length; k++) {
			quartersCombo.insertItemAt(quarters[k], k);
		}
		for (int k = 0; k < quarters.length; k++) {
			beginStoryComboQuarter.insertItemAt(quarters[k], k);
		}
		for (int k = 0; k < quarters.length; k++) {
			endStoryComboQuarter.insertItemAt(quarters[k], k);
		}
		for (int k = 0; k < quarters.length; k++) {
			beginStoryComboYear.insertItemAt(fiscalYears[k], k);
		}
		for (int k = 0; k < quarters.length; k++) {
			endStoryComboYear.insertItemAt(fiscalYears[k], k);
		}

		/** For testing
		programsCombo.setSelectedIndex(0);
		quartersCombo.setSelectedIndex(1);
		fiscalYearsCombo.setSelectedIndex(1);
		*/

		// Set location, width, and height of components
		programLabel.setBounds(55, 24, 85, 20);
		quarterLabel.setBounds(55, 55, 85, 20);
		fiscalYearLabel.setBounds(55, 86, 85, 20);
		storiesLabel.setBounds(55, 117, 85, 20);
		programsCombo.setBounds(175, 24, 190, 20);
		quartersCombo.setBounds(175, 55, 190, 20);
		fiscalYearsCombo.setBounds(175, 86, 190, 20);
		beginStoryComboQuarter.setBounds(175, 55, 90, 20);
		beginStoryComboYear.setBounds(275, 55, 90, 20);
		endStoryComboQuarter.setBounds(175, 86, 90, 20);
		endStoryComboYear.setBounds(275, 86, 90, 20);
		storiesCheckBox.setBounds(172, 117, 190, 20);
		startButton.setBounds(145, 215, 135, 25);
		statusLabel.setBounds(55, 150, 350, 15);
		pBar.setBounds(55, 165, 325, 30);

		// Hide story components
		beginStoryComboQuarter.setVisible(false);
		beginStoryComboYear.setVisible(false);
		endStoryComboQuarter.setVisible(false);
		endStoryComboYear.setVisible(false);

		// Right justify elements
		programLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		quarterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		fiscalYearLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		storiesLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		// Format font of components
		programLabel.setFont(new Font(FONT, Font.PLAIN, 14));
		quarterLabel.setFont(new Font(FONT, Font.PLAIN, 14));
		fiscalYearLabel.setFont(new Font(FONT, Font.PLAIN, 14));
		storiesLabel.setFont(new Font(FONT, Font.PLAIN, 14));
		startButton.setFont(new Font(FONT, Font.PLAIN, 15));
		firstReportLabel.setFont(new Font(FONT, Font.BOLD, 30));
		statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

		firstReportLabel.setIcon(fifLogo);

		// Add components to respective panels
		titlePanel.add(firstReportLabel);
		operationPanel.add(programLabel);
		operationPanel.add(quarterLabel);
		operationPanel.add(fiscalYearLabel);
		operationPanel.add(storiesLabel);
		operationPanel.add(programsCombo);
		operationPanel.add(quartersCombo);
		operationPanel.add(fiscalYearsCombo);
		operationPanel.add(beginStoryComboQuarter);
		operationPanel.add(beginStoryComboYear);
		operationPanel.add(endStoryComboQuarter);
		operationPanel.add(endStoryComboYear);
		operationPanel.add(storiesCheckBox);
		operationPanel.add(statusLabel);
		operationPanel.add(pBar);
		operationPanel.add(startButton);

		// Add listener to start button and story check box
		startButton.addActionListener(this);
		storiesCheckBox.addActionListener(this);
		frame.setResizable(false);
		frame.setVisible(true);
	}

	/**
	 * Create FIF logo icon for GUI
	 * 
	 * @param path
	 * @param descr
	 * @return the icon
	 */
	private ImageIcon createImageIcon(String path, String descr) {
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, descr);
		} else {
			JOptionPane.showMessageDialog(frame, "Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * Executes program
	 * 
	 * @param ae Start button click
	 */
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource().equals(startButton)) {
			if (!storiesCheckBox.isSelected()
					&& !(programsCombo.getSelectedItem() == null || quartersCombo.getSelectedItem() == null || fiscalYearsCombo
							.getSelectedItem() == null)) {
				storiesCheckBox.setEnabled(false);
				errorLabel.setVisible(true);
				errorLabel.setText("");
				startButton.setText("Processing...");
				startButton.setEnabled(false);
				Executors.newSingleThreadExecutor().submit(new Runnable() {
					@Override
					public void run() {
						try {
							statusLabel.setForeground(Color.BLACK);
							pBar.setValue(0);
							String program = programsCombo.getSelectedItem().toString();
							String quarter = quartersCombo.getSelectedItem().toString();
							String fy = fiscalYearsCombo.getSelectedItem().toString();
							// Sets the connection and sets up variables;
							Connection connection = new Connection(program, quarter, fy, programs, pBar, statusLabel);
							// Processes data
							connection.process();
						} catch (ConnectionException | AsyncApiException e) {
							statusLabel.setText("Invalid Username/Password");
						}
						startButton.setText("Start");
						startButton.setEnabled(true);
						storiesCheckBox.setEnabled(true);
					}
				});
			} else if (storiesCheckBox.isSelected() && programsCombo.getSelectedItem() != null) {
				storiesCheckBox.setEnabled(false);
				errorLabel.setVisible(true);
				errorLabel.setText("");
				startButton.setText("Processing...");
				startButton.setEnabled(false);
				Executors.newSingleThreadExecutor().submit(new Runnable() {
					@Override
					public void run() {
						try {
							startButton.setText("Start");
							startButton.setEnabled(true);
							storiesCheckBox.setEnabled(true);
						} catch (ClassCastException e) {
							e.printStackTrace();
							startButton.setText("Start");
							startButton.setEnabled(true);
						}
					}
				});
			} else {
				JOptionPane.showMessageDialog(frame, "Please complete your selection");
			}
		} else if (ae.getSource().equals(storiesCheckBox)) {
			if (storiesCheckBox.isSelected()) {
				beginStoryComboQuarter.setVisible(true);
				beginStoryComboYear.setVisible(true);
				endStoryComboQuarter.setVisible(true);
				endStoryComboYear.setVisible(true);
				quarterLabel.setText("Begin");
				quartersCombo.setVisible(false);
				fiscalYearLabel.setText("End");
				fiscalYearsCombo.setVisible(false);
			} else {
				quarterLabel.setText("Quarter");
				quartersCombo.setVisible(true);
				fiscalYearLabel.setText("Fiscal Year");
				fiscalYearsCombo.setVisible(true);
			}
		} else {
			JOptionPane.showMessageDialog(frame, "System Error. Please restart program.");
		}
	}
}
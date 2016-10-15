/**
 * 
 */
/**
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
package run;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ui.FirstReportGUI;

/**
 * Starts FirstReport program
 * 
 * @author Bryce Coleman
 * @version 2.0 - Jan 2016
 */
public class Start {
	/**
	 * Starts program
	 *
	 * @param args args
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					// Set look and feel of operating system gui
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
				// Starts GUI
				FirstReportGUI firstReport = new FirstReportGUI();
				// Initializes interface components
				firstReport.initialize();
			}
		});
	}
}
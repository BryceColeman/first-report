package util;

import java.awt.event.ActionListener;

/**
 * Contains all data associated with the org
 * @author Bryce Coleman
 *
 */
public interface ProgramData extends ActionListener {
	// Number of programs
		/** Total Programs in Org */
		public static final int NUM_PROGRAMS = 14;

		// Quarters
		/** Quarter 1 */
		public static final String Q1 = "1";
		/** Quarter 2 */
		public static final String Q2 = "2";
		/** Quarter 3 */
		public static final String Q3 = "3";
		/** Quarter 4 */
		public static final String Q4 = "4";

		// Fiscal years
		/** Fiscal Year 14-15 */
		public static final String FY1415 = "2014-2015";
		/** Fiscal Year 15-16 */
		public static final String FY1516 = "2015-2016";
		/** Fiscal Year 16-17 */
		public static final String FY1617 = "2016-2017";
		/** Fiscal Year 17-18 */
		public static final String FY1718 = "2017-2018";
		/** Fiscal Year 18-19 */
		public static final String FY1819 = "2018-2019";
		/** Fiscal Year 19-20 */
		public static final String FY1920 = "2019-2020";

		// Programs
		/** All programs selection */
		public static final String ALL = "All";
		/** Central Carolina */
		public static final String CC = "Central Carolina";
		/** Durham */
		public static final String DUR = "Durham";
		/** Gaston-Cleveland-Lincoln */
		public static final String FC = "Five County";
		/** Gaston-Cleveland-Lincoln */
		public static final String GCL = "Gaston-Cleveland-Lincoln";
		/** High Country */
		public static final String HC = "High Country";
		/** Johnston */
		public static final String JHN = "Johnston";
		/** Lifeline Project */
		public static final String LP = "Lifeline Project";
		/** Mecklenburg */
		public static final String MCK = "Mecklenburg";
		/** Sandhills */
		public static final String SH = "Sandhills";
		/** Southeastern */
		public static final String SE = "Southeastern";
		/** Southern Piedmont */
		public static final String SP = "Southern Piedmont";
		/** Smoky Mountain */
		public static final String SM = "Smoky Mountain";
		/** Triad */
		public static final String T = "Triad";
		/** Wake */
		public static final String W = "Wake";
		/** State-wide */
		public static final String SW = "State-wide";
		
		// Content for combo boxes
		/**	Org program names	 */
		public String[] programs = { ALL, CC, DUR, FC, GCL, HC, JHN, LP, MCK, SH, SM, SE, SP, T, W };
		/**	Quarters	 */
		public String[] quarters = { Q1, Q2, Q3, Q4 };
		/**	Fiscal year names	 */
		public String[] fiscalYears = { FY1415, FY1516, FY1617, FY1718, FY1819, FY1920 };
}

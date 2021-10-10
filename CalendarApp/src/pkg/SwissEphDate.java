package pkg;

import java.time.LocalDate;
import swisseph.*;

/*
 * This class is intended to act as an easy way for the calendar GUI to interface with the Swiss Ephemeris library
 * The class is created with a date object which cannot be changed after creation, only creating a new one will change it
 * Link to swiss eph documentation: http://www.th-mack.de/download/swisseph-doc/swisseph/SwissEph.html
 * TODO: Look up all requirements, ask for special gui requirements, 
 */

public class SwissEphDate {
	// Two SwissEph APIs used to interact with the library, sw performs calculations
	// while sd handles dates and converting from greorian to julian
	SweDate sd = new SweDate();
	SwissEph sw = new SwissEph();

	// Setting up constants which will be used with sw when performing calculations
	// later
	final int EARTH = SweConst.SE_EARTH;
	final int MOON = SweConst.SE_MOON;
	final int SUN = SweConst.SE_SUN;
	final int SWISSEPH = SweConst.SEFLG_SWIEPH;

	// Variables set up during constructor which will be used in calculations
	LocalDate gregDate;
	double julianDate;
	double[] position; // contains longitude, latitude and elevation in that order
	double[] data, tret, attr = new double[20]; // used for holding data from calculations

	public SwissEphDate(LocalDate date, double longitude, double latitude, double elevation) {
		// Setting up sd with date provided
		sd.setDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 0);
		
		julianDate = sd.getJulDay();
		gregDate = date;

		// Creating position array with data provided
		position = new double[] { longitude, latitude, elevation };
	}

	
	// *** Get methods for variables that may be useful
	public double getJulianDate() { return julianDate; }

	// *** Main calculation methods (moon, sunrise/set, eclipses, ...)

	// Returns phase of moon in a string such as "Waning Crescent" or "Full Moon"
	public String getMoonPhase() {
		double litpercent = getMoonPhasePercent();
		String output = "";
		
		// Calculates wether Waxing or Waning based on previous day
		if(litpercent > anotherDate(-1).getMoonPhasePercent())
			output += "Waxing ";
		else
			output += "Waning ";
		
		// Calculates moon phase based on % lit
		if(litpercent > .98)
			output = "Full Moon";
		else if (litpercent > .5)
			output += "Gibbous";
		else if (litpercent > .02)
			output += "Crescent";
		else
			output += "New Moon";
		
		return output;
	}
	
	// Returns % of moon lit relative to earth, useful for moon phase calculations
	public double getMoonPhasePercent() {
		// Calculates % of moon currently lit relative to earth into data[0]
		sw.swe_pheno(julianDate, MOON, SweConst.SEFLG_SWIEPH, data, new StringBuffer("Calculation failed"));
		return data[0];
	}

	/*
	 * public String getSunriseTime() {
	 * 
	 * }
	 * 
	 * public String getSunsetTime() {
	 * 
	 * }
	 * 
	 * public boolean isSolarEclipse() {
	 * 
	 * }
	 * 
	 * public boolean isLunarEclipse() {
	 * 
	 * }
	 */
	// *** Other calculation methods that might be useful for a GUI
	
	// Returns another SwissEphDate object + or - the amount of days specified in the method
	public SwissEphDate anotherDate(int days) {
		return new SwissEphDate(gregDate.plusDays(days), position[0], position[1], position[2]);
	}

		// *** Private methods to help with calculations

	// Sets an hour output to a time zone (in our case PST is +7)
	public int setTimezone(double input, int timedifference) {
		int output = (int) input - timedifference;
		if (output < 0) output += 24;
		return output;
	}

	// Gets number of minutes from the fraction on the sd.getHour() method
	public String getMinutes(double input) {
		int minutes = (int) ((input - (int) input) * 60);
		String output = Integer.toString(minutes);
		if (minutes < 10)
			output = "0" + output;
		return output;
	}
}

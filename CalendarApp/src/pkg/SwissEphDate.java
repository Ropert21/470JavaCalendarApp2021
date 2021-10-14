package pkg;

import java.time.LocalDate;
import java.time.LocalTime;

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
	final int EARTH = SweConst.SE_EARTH;
	final int MOON = SweConst.SE_MOON;
	final int SUN = SweConst.SE_SUN;
	final int SWISSEPH = SweConst.SEFLG_SWIEPH;

	// Variables set up during constructor which will be used in calculations
	LocalDate gregDate;
	double julianDate;
	double[] position; // contains longitude, latitude and elevation in that order
	int timeZoneOffset;

	public SwissEphDate(LocalDate date, double longitude, double latitude, double elevation) {
		// Setting up sd with date provided
		sd.setDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 0);
		
		julianDate = sd.getJulDay();
		gregDate = date;

		// Creating position array with data provided
		position = new double[] { longitude, latitude, elevation };
		
		// Calculates time zone from longitude 
		timeZoneOffset = (int)(longitude / 15) * -1;
		
		// Puts sd object in gregorian mode to make converting from julian easier later
		sd.setCalendarType(true, true);
	}

	
	// *** Get methods for variables that may be useful
	public double getJulianDate() { return julianDate; }
	public int getYear() { return gregDate.getYear(); }
	public int getMonth() { return gregDate.getMonthValue(); }
	public int getDayOfMonth() { return gregDate.getDayOfMonth(); }
	public int getDayOfWeek() { return gregDate.getDayOfWeek().getValue(); }
	public int getTimeZone() { return timeZoneOffset; } // returns amount of hours ahead / behind greenwich time

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
		// Calculates % of moon currently lit relative to earth into data[1]
		double[] data = new double[20];
		sw.swe_pheno(julianDate, MOON, SWISSEPH, data, new StringBuffer("Calculation failed"));
		return data[1];
	}
	
	// Returns a string of the sunrise time in 24 hours for current date eg. 18:32 or 6:07
	public String getSunriseTime() {
		DblObj data = new DblObj(0); // Holds julian date of when sunrise occurs after calculation
		sw.swe_rise_trans(julianDate, SUN, null, SWISSEPH, SweConst.SE_CALC_RISE, position, 0, 22, data, new StringBuffer("Sunrise Calculation failed"));
		
		// Converting julian date provided to time
		sd.setJulDay(data.val); 
		int hour = setTimezone(sd.getHour());
		return (hour + ":" + getMinutes(sd.getHour()));
	}
	
	// Same as above but for sunset
	public String getSunsetTime() {
		DblObj data = new DblObj(0); // Holds julian date of when sunrise occurs after calculation
		sw.swe_rise_trans(julianDate, SUN, null, SWISSEPH, SweConst.SE_CALC_SET, position, 0, 22, data, new StringBuffer("Sunset Calculation failed"));
		
		// Converting julian date provided to time
		sd.setJulDay(data.val); 
		int hour = setTimezone(sd.getHour());
		return (hour + ":" + getMinutes(sd.getHour()));
	}
	
	// Returns a LocalDate object containing the next solar eclipse for provided location
	public LocalDate getNextSolarEclipse() {
		double[] tret = new double[20], attr = new double[20]; // used for holding data from calculations
		sw.swe_sol_eclipse_when_loc(julianDate, SWISSEPH, position, tret, attr, 0, new StringBuffer("s eclipse Calculation failed"));
		
		sd.setJulDay(tret[0]); // Calculation puts julian date of occurance in tret[0]
		return LocalDate.of(sd.getYear(), sd.getMonth(), sd.getDay());
	}
	
	public LocalDate getNextLunarEclipse() {
		double[] tret = new double[20], attr = new double[20]; // used for holding data from calculations
		sw.swe_lun_eclipse_when_loc(julianDate, SWISSEPH, position, tret, attr, 0, new StringBuffer("Machine broke"));
		
		sd.setJulDay(tret[0]); // Calculation puts julian date of occurance in tret[0]
		return LocalDate.of(sd.getYear(), sd.getMonth(), sd.getDay());
	}
	
	// *** Other calculation methods that might be useful for a GUI
	
	// Returns another SwissEphDate object + or - the amount of days specified in the method
	public SwissEphDate anotherDate(int days) {
		return new SwissEphDate(gregDate.plusDays(days), position[0], position[1], position[2]);
	}
	
	// Returns amount of days of the month the date is in
	public int daysInMonth() {
		int current = gregDate.getDayOfMonth();
		int next = gregDate.getDayOfMonth();
		int inc = 1;
		
		while (next != 1) {
			current = next;
			next = anotherDate(inc).getDayOfMonth();
			inc ++;
		}
		
		return current;
	}

	// *** Private methods to help with calculations

	// Sets an hour output to a time zone
	private int setTimezone(double input) {
		int output = (int) input - timeZoneOffset;
		if (output < 0) output += 24;
		return output;
	}

	// Gets number of minutes from the fraction on the sd.getHour() method
	private String getMinutes(double input) {
		int minutes = (int) ((input - (int) input) * 60);
		String output = Integer.toString(minutes);
		if (minutes < 10)
			output = "0" + output;
		return output;
	}
}

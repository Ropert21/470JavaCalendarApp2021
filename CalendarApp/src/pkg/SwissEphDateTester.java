package pkg;

import java.time.LocalDate;

public class SwissEphDateTester {
	public static void main(String[] args) {
		SwissEphDate s = new SwissEphDate(LocalDate.of(2021, 10, 13), -119.4960, 49.803, 334.0);
		
		// Julian to gregorian test
		if(s.getJulianDate() != 2459500.5)
			System.out.println("test 1 failed");
		
		// Get day of week test
		if(s.getDayOfWeek() != 3)
			System.out.println("test 2 failed");
		
		// Another day test
		if(s.anotherDate(1).getDayOfWeek() != 4)
			System.out.println("test 3 failed");
		
		// Timezone test
		if(s.getTimeZone() != 7)
			System.out.println("test 4 failed");
		
		// Moon % test
		if(s.getMoonPhasePercent() != 0.48514204801618566)
			System.out.println("test 5 failed");
		
		// Moon phase test
		if(!s.getMoonPhase().contentEquals("Waxing Crescent"))
			System.out.println("test 6 failed");
		
		// Sunrise time test
		if(!s.getSunriseTime().contentEquals("7:17"))
			System.out.println("test 7 failed");
		
		// Sunset time test
		if(!s.getSunsetTime().contentEquals("18:11"))
			System.out.println("test 8 failed");
		
		// Solar eclipse test
		if(!s.getNextSolarEclipse().equals(LocalDate.of(2023, 10, 14)))
			System.out.println("test 9 failed");
		
		// Lunar eclipse test
		if(!s.getNextLunarEclipse().equals(LocalDate.of(2021, 11, 19)))
			System.out.println("test 10 failed");
		
		// Days in month test
		if(s.daysInMonth() != 31)
			System.out.println("test 11 failed");
		
		System.out.println(s.getMoonriseTime() + " " + s.getMoonsetTime());
		
		// is solar eclispe test
		s = new SwissEphDate(LocalDate.of(2023, 10, 14), -119.4960, 49.803, 334.0);
		if(s.isSolarEclipse() == false)
			System.out.println("test 12 failed");
		
		System.out.println("tests completed.");
	}
}

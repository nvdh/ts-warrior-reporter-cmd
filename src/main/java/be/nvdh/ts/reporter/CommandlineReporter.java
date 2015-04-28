package be.nvdh.ts.reporter;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import be.nvdh.ts.domain.FetchResult;
import be.nvdh.ts.domain.Prestation;
import be.nvdh.ts.exception.ReportException;
import be.nvdh.ts.report.Reporter;

public class CommandlineReporter implements Reporter{

	private Locale localeBE = new Locale("nl", "BE");
	
	private DateTimeFormatter reportDateTimeFormat  = DateTimeFormat.forPattern("E dd MMMM").withLocale(localeBE);
	private PeriodFormatter periodFormat = new PeriodFormatterBuilder().appendHours().appendSuffix("u").appendMinutes().toFormatter();
	private String standardDayAsString = "8u00";
	private String emptyDurationAsString = "0u00";
	
	public void publish(FetchResult fetchResult, Map<String, String> context) throws ReportException {
		printHeader(fetchResult);
		printPrestations(fetchResult);
		printFooter(fetchResult);
	}
	
	public void init(Map<String, String> config) {
	}
	
	public String getName() {
		return "commandLine";
	}

	private void printPrestations(FetchResult fetchResult) {
		Duration totalDuration = new Duration(0);
		Duration totalDeltaDuration = new Duration(0);
		List<Prestation> prestations = fetchResult.getPrestations();
		for (Prestation prestation : prestations) {
			Duration deltaDuration = calculateDeltaDuration(prestation);
			totalDuration = totalDuration.plus(prestation.getDuration().getMillis());
			totalDeltaDuration = totalDeltaDuration.plus(deltaDuration);
			System.out.println(reportDateTimeFormat.print(prestation.getDay()) + ": " + print(prestation.getDuration()) + "\t\t" + print(deltaDuration));
			if (isSunday(prestation.getDay())){
				System.out.println("");
			};
		}
		printSeparator();
		System.out.println("Total time : " + print(totalDuration));
		System.out.println("Total delta: " + print(totalDeltaDuration));
	}

	private Duration calculateDeltaDuration(Prestation prestation) {
		Duration deltaDuration = prestation.getDuration().minus(standardDayDuration());
		boolean hasPrestation = prestation.getDuration().isLongerThan(Duration.ZERO);
		return hasPrestation ? deltaDuration : emptyDuration();
	}

	private String print(Duration duration) {
		return periodFormat.print(duration.toPeriod());
	}

	private Duration standardDayDuration() {
		return periodFormat.parsePeriod(standardDayAsString).toStandardDuration();
	}

	private Duration emptyDuration() {
		return periodFormat.parsePeriod(emptyDurationAsString).toStandardDuration();
	}
	
	private boolean isSunday(LocalDate localDate) {
		return localDate.getDayOfWeek() == DateTimeConstants.SUNDAY;
	}

	private void printHeader(FetchResult fetchResult) {
		printSeparator();
		System.out.println("Report for " + fetchResult.getFetchedMonth());
		printSeparator();
	}

	private void printFooter(FetchResult fetchResult) {
		printSeparator();
		System.out.println("Fetched at " + fetchResult.getFetchDate());
		printSeparator();
	}

	private void printSeparator() {
		System.out.println("------------------------------------------------------");
	}

	
	public String toString(){
		return getName();
	}
	
}

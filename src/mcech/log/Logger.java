package mcech.log;

import static java.time.ZoneOffset.UTC;
import static mcech.log.LogLevel.DEBUG;
import static mcech.log.LogLevel.ERROR;
import static mcech.log.LogLevel.INFO;
import static mcech.log.LogLevel.WARNING;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class Logger {
	public static synchronized LogLevel getLevel() {
		return level_;
	}
	
	public static synchronized void setLevel(LogLevel level) {
		level_ = level;
	}
	
	public static synchronized void logDebug(String message) {
		if (level_.ordinal() >= DEBUG.ordinal()) {
			String timestamp = now();
			System.out.println(timestamp + " DEBUG:   " + message);
		}
	}
	
	public static synchronized void logInfo(String message) {
		if (level_.ordinal() >= INFO.ordinal()) {
			String timestamp = now();
			System.out.println(timestamp + " INFO:    " + message);
		}
	}
	
	public static synchronized void logWarning(String message) {
		if (level_.ordinal() >= WARNING.ordinal()) {
			String timestamp = now();
			System.out.println(timestamp + " WARNING: " + message);
		}
	}
	
	public static synchronized void logWarning(Exception exception) {
		if (level_.ordinal() >= WARNING.ordinal()) {
			String timestamp = now();
			StringWriter sw = new StringWriter();
			PrintWriter  pw = new PrintWriter(sw);
			exception.printStackTrace(pw);
			System.out.println(timestamp + " WARNING: " + exception.getMessage());
			System.out.println(sw.toString());
		}
	}
	
	public static synchronized void logError(Exception exception) {
		String timestamp = now();
		StringWriter sw = new StringWriter();
		PrintWriter  pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		System.out.println(timestamp + " ERROR:   " + exception.getMessage());
		System.out.println(sw.toString());
	}
	
	private static String now() {
		return Instant.now().atOffset(UTC).format(formatter_);
	}
	
	private static final DateTimeFormatter formatter_ = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
	private static LogLevel level_ = ERROR;
}

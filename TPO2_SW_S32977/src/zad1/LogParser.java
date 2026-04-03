/**
 * @author Sajnóg Wiktor s32977
 */

package zad1;


import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class LogParser {

	public Optional<LogEntry> parseLine(String line) {

		if (line == null || line.isBlank()) return Optional.empty();

		String[] parts = line.split("\\|", -1);

		if (parts.length != 8) {
			return Optional.empty();
		}

		for (String part : parts) {
			if (part.isEmpty()) return Optional.empty();
		}

		String ip = parts[2];

		String[] octets = ip.split("\\.");
		if (octets.length != 4) {
			return Optional.empty();
		}
		for (String octet : octets) {
			if (octet.isBlank() || !octet.matches("\\d+")) return Optional.empty();
			int octetInt = Integer.parseInt(octet);
			if (octetInt > 255 || octetInt < 0) {
				return Optional.empty();
			}
		}

		LocalDateTime localDateTime;
		int status;
		int latencyMs;

		int bytes;
		try {
			localDateTime = LocalDateTime.parse(parts[1]);
			status = Integer.parseInt(parts[5]);
			latencyMs = Integer.parseInt(parts[6]);
			bytes = Integer.parseInt(parts[7]);
		} catch (DateTimeParseException | NumberFormatException e) {
			return Optional.empty();
		}
		if (latencyMs < 0 || bytes < 0) {
			return Optional.empty();
		}

		return Optional.of(new LogEntry(
				parts[0],
				localDateTime,
				ip,
				parts[3],
				parts[4],
				status,
				latencyMs,
				bytes
		));

	}
}

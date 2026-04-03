/**
 * @author Sajnóg Wiktor s32977
 */

package zad1;


import java.time.ZoneId;
import java.util.*;

public record AnalyticsService(
		LogParser logParser,
		TimestampRepairService timestampRepairService
) {

	public AnalysisReport analyze(GeoTimeOptions options, GeoLookup lookup) throws Exception {
		int invalidLines = 0;
		int repairedGapTimes = 0;
		int resolvedOverlapEntries = 0;
		int droppedAmbiguousEntries = 0;
		int geoLookupFailures = 0;
		long[] hours = new long[24];
		Map<String, long[]> histogram = new LinkedHashMap<>();
		List<String> ambiguousEntriesList = new ArrayList<>();
		Map<String, Long> countries = new LinkedHashMap<>();
		Map<String, Long> timeZones = new LinkedHashMap<>();

		List<LogEntry> logEntries = new ArrayList<>();
		for (String line : options.logLines()) {
			Optional<LogEntry> parsedLog = logParser.parseLine(line);
			if (parsedLog.isPresent()) logEntries.add(parsedLog.get());
			else invalidLines++;
		}
		List<ResolvedLogEntry> resolvedLogEntries = timestampRepairService.repair(logEntries, ZoneId.of(options.serverZoneId()));
		for (ResolvedLogEntry resolvedLogEntry : resolvedLogEntries) {
			if (resolvedLogEntry.resolutionKind() == ResolutionKind.OVERLAP_RESOLVED) resolvedOverlapEntries++;
			else if (resolvedLogEntry.resolutionKind() == ResolutionKind.GAP_REPAIRED) repairedGapTimes++;
			else if (resolvedLogEntry.resolutionKind() == ResolutionKind.AMBIGUOUS_DROPPED) {
				droppedAmbiguousEntries++;
				ambiguousEntriesList.add(resolvedLogEntry.source().requestId());
				continue;
			}

			GeoInfo geoInfo;
			try {
				geoInfo = lookup.lookup(resolvedLogEntry.source().clientIp());

			} catch (GeoLookupException e) {
				geoLookupFailures++;
				continue;
			}

			if (countries.containsKey(geoInfo.countryCode())) {
				countries.put(geoInfo.countryCode(), countries.get(geoInfo.countryCode()) + 1);
			} else {
				countries.put(geoInfo.countryCode(), 1L);
			}

			String zoneId = geoInfo.zoneId().getId();

			if (timeZones.containsKey(zoneId)) {
				timeZones.put(zoneId, timeZones.get(zoneId) + 1);
			} else {
				timeZones.put(zoneId, 1L);
			}
			int hour = resolvedLogEntry.serverTime().withZoneSameInstant(geoInfo.zoneId()).getHour();
			hours[hour]++;

			if (!histogram.containsKey(zoneId)) {
				histogram.put(zoneId, new long[24]);
			}
			histogram.get(zoneId)[hour]++;


		}


		return new AnalysisReport(
				invalidLines,
				repairedGapTimes,
				resolvedOverlapEntries,
				droppedAmbiguousEntries,
				geoLookupFailures,
				ambiguousEntriesList,
				countries,
				timeZones,
				hours,
				histogram

		);
	}
}

/**
 * @author Sajnóg Wiktor s32977
 */

package zad1;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.ArrayList;
import java.util.List;

public class TimestampRepairService {

	public List<ResolvedLogEntry> repair(List<LogEntry> entries, ZoneId serverZone) {

		List<ResolvedLogEntry> repairedEntries = new ArrayList<>();

		ZoneRules rules = serverZone.getRules();

		for (int i = 0; i < entries.size(); i++) {
			LocalDateTime localDateTime = entries.get(i).serverLocalTime();
			List<ZoneOffset> offsets = rules.getValidOffsets(localDateTime);

			if (offsets.size() == 1) {

				repairedEntries.add(new ResolvedLogEntry(
						entries.get(i),
						ZonedDateTime.of(localDateTime, serverZone),
						ResolutionKind.OK
				));
			} else if (offsets.isEmpty()) {
				ZoneOffsetTransition transition = rules.getTransition(localDateTime);
				LocalDateTime newLocalDateTime = localDateTime.plus(transition.getDuration());
				repairedEntries.add(new ResolvedLogEntry(
						entries.get(i),
						ZonedDateTime.of(newLocalDateTime, serverZone),
						ResolutionKind.GAP_REPAIRED
				));
			} else {
				List<LogEntry> block = new ArrayList<>();

				while (i < entries.size()) {
					LogEntry overlapEntry = entries.get(i);
					List<ZoneOffset> overlapOffsets = rules.getValidOffsets(overlapEntry.serverLocalTime());
					if (overlapOffsets.size() != 2) {
						break;
					}
					block.add(overlapEntry);
					i++;

				}
				i--;
				int wentBack = 0;
				int changeIndex = 0;
				for (int j = 0; j < block.size(); j++) {
					if (j == 0) continue;

					if (block.get(j).serverLocalTime().isBefore(block.get(j - 1).serverLocalTime())) {
						changeIndex = j;
						wentBack++;

					}
				}


				if (wentBack == 1) {
					for (int j = 0; j < block.size(); j++) {
						if (j< changeIndex) {
							repairedEntries.add(new ResolvedLogEntry(
									block.get(j),
									ZonedDateTime.ofLocal(block.get(j).serverLocalTime(), serverZone, offsets.getFirst()),
									ResolutionKind.OVERLAP_RESOLVED
							));
						}else {
							repairedEntries.add(new ResolvedLogEntry(
									block.get(j),
									ZonedDateTime.ofLocal(block.get(j).serverLocalTime(), serverZone, offsets.getLast()),
									ResolutionKind.OVERLAP_RESOLVED

							));
						}
					}
				}else{
					for (LogEntry logEntry : block) {
						repairedEntries.add(new ResolvedLogEntry(
								logEntry,
								null,
								ResolutionKind.AMBIGUOUS_DROPPED
						));
					}

				}



			}


		}


		return repairedEntries;
	}
}

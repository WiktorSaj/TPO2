/**
 *
 *  @author Sajnóg Wiktor s32977
 *
 */

package zad1;


import java.time.ZonedDateTime;

public record ResolvedLogEntry(
    LogEntry source,
    ZonedDateTime serverTime,
    ResolutionKind resolutionKind
) {}

# Development notes

## Scope
This prototype reports JVM heap metrics and garbage-collection activity using the standard Java Management APIs.

## Validation checklist
- Compile the Java source with a JDK.
- Run with the default five snapshots.
- Run with an explicit snapshot count.
- Confirm that invalid snapshot counts are rejected.
- Treat heap utilization as a warning signal, not proof of a memory leak.

## Next development tasks
- Export snapshots to CSV.
- Add trend analysis across multiple runs.
- Add tests for snapshot argument validation.
- Connect measurements to a visualization layer.

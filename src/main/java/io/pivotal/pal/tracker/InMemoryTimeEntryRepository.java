package io.pivotal.pal.tracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTimeEntryRepository implements TimeEntryRepository {
    private final Map<Long, TimeEntry> entryMap = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public TimeEntry create(TimeEntry timeEntry) {
        timeEntry.setId(nextId);
        entryMap.put(nextId, timeEntry);
        nextId++;
        return timeEntry;
    }

    @Override
    public TimeEntry find(long id) {
        return entryMap.get(id);
    }

    @Override
    public List<TimeEntry> list() {
        return List.copyOf(entryMap.values());
    }

    @Override
    public TimeEntry update(long id, TimeEntry timeEntry) {
        if (entryMap.get(id) == null) {
            return null;
        }
        timeEntry.setId(id);
        entryMap.put(id, timeEntry);
        return timeEntry;
    }

    @Override
    public void delete(long id) {
        entryMap.remove(id);
    }
}

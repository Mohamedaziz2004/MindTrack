package org.mindtrack.mindtrackfxx.service;

import org.mindtrack.mindtrackfxx.model.MindTrackEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MindTrackService {

    private final List<MindTrackEntry> entries = new ArrayList<>();

    public MindTrackEntry addEntry(String title, String note, MindTrackEntry.Mood mood) {
        MindTrackEntry entry = new MindTrackEntry(title, note, mood);
        entries.add(entry);
        return entry;
    }

    public List<MindTrackEntry> listEntries() {
        return Collections.unmodifiableList(entries);
    }

    public boolean deleteEntry(long id) {
        return entries.removeIf(e -> e.getId() == id);
    }
}


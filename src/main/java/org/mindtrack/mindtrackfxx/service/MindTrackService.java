package org.mindtrack.mindtrackfxx.service;

import org.mindtrack.mindtrackfxx.model.MindTrackEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MindTrackService {
    private final List<MindTrackEntry> entries = new ArrayList<>();

    public List<MindTrackEntry> listEntries() {
        return Collections.unmodifiableList(entries);
    }

    public MindTrackEntry addEntry(String title, String note, MindTrackEntry.Mood mood) {
        MindTrackEntry e = new MindTrackEntry(title, note, mood);
        entries.add(e);
        return e;
    }

    public boolean deleteEntry(String id) {
        return entries.removeIf(e -> e.getId().equals(id));
    }

    public Optional<MindTrackEntry> findById(String id) {
        return entries.stream().filter(e -> e.getId().equals(id)).findFirst();
    }
}

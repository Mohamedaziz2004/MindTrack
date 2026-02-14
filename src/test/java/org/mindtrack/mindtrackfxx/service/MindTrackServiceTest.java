package org.mindtrack.mindtrackfxx.service;

import org.junit.jupiter.api.Test;
import org.mindtrack.mindtrackfxx.model.MindTrackEntry;

import static org.junit.jupiter.api.Assertions.*;

public class MindTrackServiceTest {
    @Test
    void addAndList() {
        MindTrackService svc = new MindTrackService();
        assertEquals(0, svc.listEntries().size());
        svc.addEntry("Title", "Note", MindTrackEntry.Mood.GOOD);
        assertEquals(1, svc.listEntries().size());
    }

    @Test
    void deleteById() {
        MindTrackService svc = new MindTrackService();
        MindTrackEntry e = svc.addEntry("T", "N", MindTrackEntry.Mood.BAD);
        assertTrue(svc.deleteEntry(e.getId()));
        assertEquals(0, svc.listEntries().size());
    }
}

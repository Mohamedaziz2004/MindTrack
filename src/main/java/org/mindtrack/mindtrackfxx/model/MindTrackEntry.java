package org.mindtrack.mindtrackfxx.model;

import java.util.concurrent.atomic.AtomicLong;

public class MindTrackEntry {

    public enum Mood {
        GOOD,
        BAD,
        NEUTRAL
    }

    private static final AtomicLong ID_GEN = new AtomicLong(1);

    private final long id;
    private final String title;
    private final String note;
    private final Mood mood;

    public MindTrackEntry(String title, String note, Mood mood) {
        this.id = ID_GEN.getAndIncrement();
        this.title = title;
        this.note = note;
        this.mood = mood;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public Mood getMood() {
        return mood;
    }
}


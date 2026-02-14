package org.mindtrack.mindtrackfxx.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class MindTrackEntry {
    private final String id;
    private LocalDateTime timestamp;
    private String title;
    private String note;
    private Mood mood;

    public enum Mood { VERY_BAD, BAD, NEUTRAL, GOOD, VERY_GOOD }

    public MindTrackEntry(String title, String note, Mood mood) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.title = title;
        this.note = note;
        this.mood = mood;
    }

    public String getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Mood getMood() { return mood; }
    public void setMood(Mood mood) { this.mood = mood; }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + title + " (" + mood + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MindTrackEntry that = (MindTrackEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}

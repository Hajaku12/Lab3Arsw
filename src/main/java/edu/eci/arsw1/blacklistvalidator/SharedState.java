package edu.eci.arsw1.blacklistvalidator;

import java.util.LinkedList;
import java.util.List;

public class SharedState {
    private int totalOccurrences;
    private List<Integer> blacklists;
    private volatile boolean stop;

    public SharedState() {
        this.totalOccurrences = 0;
        this.blacklists = new LinkedList<>();
        this.stop = false;
    }

    public synchronized void addOccurrence(int index) {
        if (!stop) {
            blacklists.add(index);
            totalOccurrences++;
        }
    }

    public synchronized int getTotalOccurrences() {
        return totalOccurrences;
    }

    public synchronized List<Integer> getBlacklists() {
        return new LinkedList<>(blacklists);
    }

    public void requestStop() {
        stop = true;
    }

    public boolean shouldStop() {
        return stop;
    }
}

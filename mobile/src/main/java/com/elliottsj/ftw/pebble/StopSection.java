package com.elliottsj.ftw.pebble;

import java.io.Serializable;
import java.util.List;

public class StopSection implements Serializable, Comparable<StopSection> {

    private String tag;
    private String title;
    private List<Stop> stops;

    public StopSection(String tag, String title) {
        this.tag = tag;
        this.title = formatStopTitle(title);
    }

    public String getTag() {
        return tag;
    }

    public String getTitle() {
        return title;
    }

    public List<Stop> getStops() {
        return stops;
    }

    public StopSection setStops(List<Stop> stops) {
        this.stops = stops;
        return this;
    }

    public static String formatStopTitle(String stopTitle) {
        return stopTitle.replaceAll(" At ", " @ ");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || ((Object) this).getClass() != o.getClass()) return false;

        StopSection section = (StopSection) o;

        return tag.equals(section.tag) && title.equals(section.title);
    }

    @Override
    public int hashCode() {
        int result = tag.hashCode();
        result = 31 * result + title.hashCode();
        return result;
    }

    @Override
    public int compareTo(@SuppressWarnings("NullableProblems") StopSection another) {
        return this.title.compareTo(another.getTitle());
    }

}

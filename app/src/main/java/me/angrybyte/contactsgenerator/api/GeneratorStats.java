
package me.angrybyte.contactsgenerator.api;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Statistics wrapper. Holding some interesting statistic values described using class field names, so check those out.
 */
public class GeneratorStats implements Serializable {

    // number
    public int males;
    public int females;
    public int requested;
    public int generated;

    // milliseconds
    public float totalTime;
    public float longestContactTime;
    public float shortestContactTime;
    public float averageTimePerContact;

    // names
    @Nullable
    public String longestContact;
    @Nullable
    public String shortestContact;

    public GeneratorStats() {
        this(0, 0, 0, 0, 0f, 0f, 0f, 0f, null, null);
    }

    public GeneratorStats(int males, int females, int requested, int generated, float totalTime, float longestContactTime,
            float shortestContactTime, float averageTimePerContact, @Nullable String longestContact, @Nullable String shortestContact) {
        super();
        this.males = males;
        this.females = females;
        this.requested = requested;
        this.generated = generated;
        this.totalTime = totalTime;
        this.longestContactTime = longestContactTime;
        this.shortestContactTime = shortestContactTime;
        this.averageTimePerContact = averageTimePerContact;
        this.longestContact = longestContact;
        this.shortestContact = shortestContact;
    }

}

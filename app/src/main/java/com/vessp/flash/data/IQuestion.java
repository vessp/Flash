package com.vessp.flash.data;

import org.joda.time.Duration;

/*
 *  Contains all the marking information to be able to calculate how importantly and when retesting is required.
 */
public interface IQuestion extends Comparable<IQuestion>
{
    int hitCount();
    int maruCount();
    int maruStreak();
    long lastHitInstant();
    long lastMaruInstant();
    void markHit(boolean maru);

    //bigger value means need testing sooner
    int value();

    Duration timeBetweenTakes();
    Duration timeTillTest();
    long nextTestDate();
    boolean needsTesting();

    @Override
    int compareTo(IQuestion another);

    String getScoreSnippet();
}

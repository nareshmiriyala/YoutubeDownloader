package com.youtube.downloader.schedule;

import org.quartz.DateBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;

import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by nareshm on 11/07/2015.
 */
public class TriggerFactory {

    /**
     * Build a trigger that will fire every other minute, between 8am and 5pm, every day:
     *
     * @return
     */
    public static Trigger createTriggerSampleOne() {

        return newTrigger()
                .withIdentity("trigger3", "group1")
                .withSchedule(cronSchedule("0 0/2 8-17 * * ?"))
                .forJob("myJob", "group1")
                .build();
    }

    /**
     * Build a trigger that will fire daily at 10:42 am:
     *
     * @return
     */
    public static Trigger createTriggerSampleTwo(JobDetail jobDetail) {

        return newTrigger()
                .withIdentity("trigger3", "group1")
                .withSchedule(dailyAtHourAndMinute(10, 42))
                .forJob(jobDetail)
                .build();
    }

    /**
     * Build a trigger that will fire on Wednesdays at 10:42 am
     *
     * @return
     */
    public static Trigger createTriggerSampleThree(JobDetail jobDetail) {

        return newTrigger()
                .withIdentity("trigger3", "group1")
                .withSchedule(weeklyOnDayAndHourAndMinute(DateBuilder.WEDNESDAY, 10, 42))
                .forJob(jobDetail)
                .build();
    }

    /**
     * Build a with interval in Hours
     *
     * @return
     */
    public static Trigger createTriggerIntervalHours(JobDetail jobDetail,int hours) {

        return newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInHours(hours)
                .repeatForever())
                .build();

    }
    /**
     * Build a with interval in Minutes
     *
     * @return
     */
    public static Trigger createTriggerIntervalMinutes(JobDetail jobDetail,int minutes) {

        return newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMinutes(minutes)
                .repeatForever())
                .build();

    }
    /**
     * Build a with interval in Minutes
     *
     * @return
     */
    public static Trigger createTriggerIntervalSeconds(JobDetail jobDetail,int seconds) {

        return newTrigger().withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(seconds)
                .repeatForever())
                .build();

    }
}

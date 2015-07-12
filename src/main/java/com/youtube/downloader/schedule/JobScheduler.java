package com.youtube.downloader.schedule;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by nareshm on 11/07/2015.
 */
public class JobScheduler {
    private static final Logger logger= LoggerFactory.getLogger(JobScheduler.class.getName());
    public static void main(String[] args) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(ScheduleJob.class).withIdentity("DownloadJob").build();
            //specify the running period of job
//            Trigger trigger = TriggerFactory.createTriggerIntervalSeconds(jobDetail, 40);
            Trigger trigger = TriggerFactory.createTriggerIntervalHours(jobDetail, 1);
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler sch = schedulerFactory.getScheduler();
            logger.info("Starting the Job Scheduler");
            sch.start();
            sch.scheduleJob(jobDetail, trigger);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}

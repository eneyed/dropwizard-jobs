package de.spinscale.dropwizard.jobs;

import com.google.common.collect.Sets;
import de.spinscale.dropwizard.jobs.annotations.Every;
import de.spinscale.dropwizard.jobs.annotations.On;
import de.spinscale.dropwizard.jobs.annotations.OnApplicationStart;
import de.spinscale.dropwizard.jobs.annotations.OnApplicationStop;
import de.spinscale.dropwizard.jobs.parser.TimeParserUtil;
import io.dropwizard.lifecycle.Managed;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.*;

public class JobManager implements Managed {

    private static final Logger log = LoggerFactory.getLogger(JobManager.class);
    protected Reflections reflections = null;
    protected Scheduler scheduler;
    private Map<String, Object> scanIntervals = new HashMap<>();

    public JobManager() {
        this("");
    }

    public JobManager(String scanUrl) {
        reflections = new Reflections(scanUrl);
    }

    public JobManager(String scanUrl, Map<String, Object> scanIntervals) {
        this.scanIntervals = scanIntervals;
        reflections = new Reflections(scanUrl);
    }

    @Override
    public void start() throws Exception {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        scheduleAllJobs();
    }

    @Override
    public void stop() throws Exception {
        scheduleAllJobsOnApplicationStop();

        // this is enough to put the job into the queue, otherwise the jobs wont
        // be executed
        // anyone got a better solution?
        Thread.sleep(100);

        scheduler.shutdown(true);
    }

    protected void scheduleAllJobs() throws SchedulerException {
        scheduleAllJobsOnApplicationStart();
        scheduleAllJobsWithEveryAnnotation();
        scheduleAllJobsWithOnAnnotation();
    }

    protected void scheduleAllJobsOnApplicationStop() throws SchedulerException {
        List<Class<? extends Job>> stopJobClasses = getJobClasses(OnApplicationStop.class);
        for (Class<? extends Job> clazz : stopJobClasses) {
            JobBuilder jobDetail = JobBuilder.newJob(clazz);
            scheduler.scheduleJob(jobDetail.build(), executeNowTrigger());
        }
    }

    protected List<Class<? extends Job>> getJobClasses(Class<? extends Annotation> annotation) {
        Set<Class<? extends Job>> jobs = reflections.getSubTypesOf(Job.class);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(annotation);

        return Sets.intersection(new HashSet<Class<? extends Job>>(jobs), annotatedClasses).immutableCopy().asList();
    }

    protected void scheduleAllJobsWithOnAnnotation() throws SchedulerException {
        List<Class<? extends Job>> onJobClasses = getJobClasses(On.class);
        log.info("Jobs with @On annotation: " + onJobClasses);

        for (Class<? extends org.quartz.Job> clazz : onJobClasses) {
            On annotation = clazz.getAnnotation(On.class);

            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(getDuration(clazz, annotation.value()));
            Trigger trigger = TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).build();
            JobBuilder jobBuilder = JobBuilder.newJob(clazz);
            scheduler.scheduleJob(jobBuilder.build(), trigger);
        }
    }

    protected void scheduleAllJobsWithEveryAnnotation() throws SchedulerException {
        List<Class<? extends Job>> everyJobClasses = getJobClasses(Every.class);
        log.info("Jobs with @Every annotation: " + everyJobClasses);

        for (Class<? extends org.quartz.Job> clazz : everyJobClasses) {
            Every annotation = clazz.getAnnotation(Every.class);

            int secondDelay = TimeParserUtil.parseDuration(getDuration(clazz, annotation.value()));
            SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                    .withIntervalInSeconds(secondDelay).repeatForever();
            Trigger trigger = TriggerBuilder.newTrigger().withSchedule(scheduleBuilder).build();

            JobBuilder jobBuilder = JobBuilder.newJob(clazz);
            scheduler.scheduleJob(jobBuilder.build(), trigger);
        }
    }

    protected void scheduleAllJobsOnApplicationStart() throws SchedulerException {
        List<Class<? extends Job>> startJobClasses = getJobClasses(OnApplicationStart.class);
        log.info("Jobs to run on application start: " + startJobClasses);
        for (Class<? extends org.quartz.Job> clazz : startJobClasses) {
            JobBuilder jobBuilder = JobBuilder.newJob(clazz);
            scheduler.scheduleJob(jobBuilder.build(), executeNowTrigger());
        }
    }

    protected Trigger executeNowTrigger() {
        return TriggerBuilder.newTrigger().startNow().build();
    }

    private String getDuration(Class<? extends org.quartz.Job> clazz, String duration) {
        String classSimpleName = clazz.getSimpleName();
        return scanIntervals.containsKey(classSimpleName) ? scanIntervals.get(classSimpleName).toString() : duration;
    }
}

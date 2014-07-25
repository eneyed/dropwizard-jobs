package de.spinscale.dropwizard.jobs;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class JobManagerTest {

    JobManager jobManager;

    @Before
    public void setUp() throws Exception {
        jobManager = new JobManager("");
    }

    @Test
    public void jobsOnStartupShouldBeExecuted() throws Exception {
        ApplicationStartTestJob.results.clear();
        jobManager.start();
        Thread.sleep(1000);
        jobManager.stop();
        assertThat(ApplicationStartTestJob.results, hasSize(1));
    }

    @Test
    public void jobsOnStoppingShouldBeExecuted() throws Exception {
        ApplicationStopTestJob.results.clear();
        jobManager.start();
        jobManager.stop();
        assertThat(ApplicationStopTestJob.results, hasSize(1));
    }

    @Test
    public void jobsWithOnAnnotationShouldBeExecuted() throws Exception {
        OnTestJob.results.clear();
        jobManager.start();
        Thread.sleep(5000);
        jobManager.stop();
        assertThat(OnTestJob.results, hasSize(isOneOf(5, 6)));
    }

    @Test
    public void jobsWithOnAnnotationShouldBeExecutedWithOverriddenValuesWhenPresent() throws Exception {
        Map<String, Object> intervals = new HashMap<String, Object>() {{
            put("OnTestJob", "0/2 * * * * ?");
        }};

        OnTestJob.results.clear();
        jobManager = new JobManager("", intervals);
        jobManager.start();
        Thread.sleep(5000);
        jobManager.stop();
        assertThat(OnTestJob.results, hasSize(isOneOf(2, 3)));
    }

    @Test
    public void jobsWithEveryAnnotationShouldBeExecuted() throws Exception {
        EveryTestJob.results.clear();
        jobManager.start();
        Thread.sleep(5000);
        jobManager.stop();
        assertThat(EveryTestJob.results, hasSize(isOneOf(5, 6)));
    }

    @Test
    public void jobsWithEveryAnnotationShouldBeExecutedWithOverriddenValuesWhenPresent() throws Exception {
        Map<String, Object> intervals = new HashMap<String, Object>() {{
            put("EveryTestJob", "2s");
        }};

        jobManager = new JobManager("", intervals);
        EveryTestJob.results.clear();

        jobManager.start();
        Thread.sleep(5000);
        jobManager.stop();
        assertThat(EveryTestJob.results, hasSize(isOneOf(2, 3)));
    }
}

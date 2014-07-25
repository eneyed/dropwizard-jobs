package de.spinscale.dropwizard.jobs;

import de.spinscale.dropwizard.jobs.config.JobConfiguration;
import io.dropwizard.Configuration;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;

public class ConfiguredJobsBundleTest {

    private final Environment environment = mock(Environment.class);
    private final LifecycleEnvironment applicationContext = mock(LifecycleEnvironment.class);
    private JobConfiguration jobConfiguration = mock(JobConfiguration.class);

    @Test
    public void assertJobsBundleIsWorking() {
        when(environment.lifecycle()).thenReturn(applicationContext);
        getJobsBundle().run(new Configuration(), environment);

        final ArgumentCaptor<JobManager> captor = ArgumentCaptor.forClass(JobManager.class);
        verify(applicationContext).manage(captor.capture());

        JobManager jobManager = captor.getValue();
        assertThat(jobManager, is(notNullValue()));
    }

    private ConfiguredJobsBundle<Configuration> getJobsBundle() {
        return new ConfiguredJobsBundle<Configuration>() {
            @Override
            public JobConfiguration getJobConfiguration(Configuration configuration) {
                return jobConfiguration;
            }
        };
    }
}

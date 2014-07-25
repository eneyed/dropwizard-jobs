package de.spinscale.dropwizard.jobs;

import com.codahale.metrics.SharedMetricRegistries;
import de.spinscale.dropwizard.jobs.config.ConfigurationStrategy;
import de.spinscale.dropwizard.jobs.config.JobConfiguration;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public abstract class ConfiguredJobsBundle<T extends Configuration> implements ConfiguredBundle<T>, ConfigurationStrategy<T> {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // add shared metrics registry to be used by Jobs, since defaultRegistry
        // has been removed
        SharedMetricRegistries.add(Job.DROPWIZARD_JOBS_KEY,
                bootstrap.getMetricRegistry());
    }

    public ConfiguredJobsBundle() {
    }

    @Override
    public void run(T configuration, Environment environment) {
        JobConfiguration jobsConfiguration = getJobConfiguration(configuration);

        JobManager jobManager = new JobManager(jobsConfiguration.getScanUrl(), jobsConfiguration.getScanIntervals());
        environment.lifecycle().manage(jobManager);
    }

}

package de.spinscale.dropwizard.jobs.config;


import io.dropwizard.Configuration;

public interface ConfigurationStrategy<T extends Configuration> {
    JobConfiguration getJobConfiguration(T configuration);
}

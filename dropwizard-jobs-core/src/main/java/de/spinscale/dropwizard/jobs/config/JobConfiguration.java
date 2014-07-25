package de.spinscale.dropwizard.jobs.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class JobConfiguration {
    @JsonProperty
    private String scanUrl = null;

    @NotNull
    @JsonProperty
    private Map<String, Object> scanIntervals = null;

    public String getScanUrl() {
        return scanUrl;
    }

    public void setScanUrl(String scanUrl) {
        this.scanUrl = scanUrl;
    }

    public Map<String, Object> getScanIntervals() {
        return scanIntervals;
    }

    public void setScanIntervals(Map<String, Object> scanIntervals) {
        this.scanIntervals = scanIntervals;
    }
}

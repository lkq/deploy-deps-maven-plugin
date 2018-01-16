package com.github.lkq.maven.plugin.deploydeps.report;

import org.apache.maven.plugin.logging.Log;

import java.util.*;

public class Reporter {

    private Map<String, Integer> successCounts = new HashMap<>();
    private Map<String, Integer> failCounts = new HashMap<>();
    private Map<String, Integer> skipCounts = new HashMap<>();

    public synchronized void reportSuccess(String repoArtifactPath) {
        successCounts.merge(repoArtifactPath, 1, (a, b) -> a + b);
    }

    public synchronized void reportFail(String repoArtifactPath) {
        failCounts.merge(repoArtifactPath, 1, (a, b) -> a + b);
    }

    public synchronized void reportSkipped(String repoArtifactPath) {
        skipCounts.merge(repoArtifactPath, 1, (a, b) -> a + b);
    }

    public int totalFails() {
        return sumCount(failCounts);
    }

    public void print(Log logger) {
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(successCounts.keySet());
        allKeys.addAll(failCounts.keySet());
        allKeys.addAll(skipCounts.keySet());

        ArrayList<String> sortedKeys = new ArrayList<>();
        sortedKeys.addAll(allKeys);
        sortedKeys.sort(Comparator.naturalOrder());

        for (String key : sortedKeys) {
            Integer successCnt = getCount(successCounts.get(key));
            Integer failCnt = getCount(failCounts.get(key));
            Integer skipCnt = getCount(skipCounts.get(key));
            logger.info("success: " + successCnt + " fail: " + failCnt + " skipped: " + skipCnt + " " + key);
        }
        logger.info("total success: " + sumCount(successCounts) + " total fail: " + sumCount(failCounts) + " total skipped: " + sumCount(skipCounts));

    }

    private Integer getCount(Integer value) {
        if (value != null) {
            return value;
        } else {
            return 0;
        }
    }

    private Integer sumCount(Map<String, Integer> values) {
        int sum = 0;
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            sum += entry.getValue();
        }
        return sum;
    }
}

package com.mlops.model;

public class EvaluationMetric {
    private String id;
    private long timestamp;
    private double accuracyScore;
    private String modelId;

    public EvaluationMetric() {}

    public EvaluationMetric(String id, long timestamp, double accuracyScore, String modelId) {
        this.id = id;
        this.timestamp = timestamp;
        this.accuracyScore = accuracyScore;
        this.modelId = modelId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getAccuracyScore() { return accuracyScore; }
    public void setAccuracyScore(double accuracyScore) { this.accuracyScore = accuracyScore; }

    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
}

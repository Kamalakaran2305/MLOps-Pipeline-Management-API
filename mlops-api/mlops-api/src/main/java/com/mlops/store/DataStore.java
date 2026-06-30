package com.mlops.store;

import com.mlops.model.EvaluationMetric;
import com.mlops.model.MLWorkspace;
import com.mlops.model.MachineLearningModel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, MLWorkspace> workspaces = new ConcurrentHashMap<>();
    private final Map<String, MachineLearningModel> models = new ConcurrentHashMap<>();
    private final Map<String, List<EvaluationMetric>> metrics = new ConcurrentHashMap<>();

    private DataStore() {}

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ── Workspace helpers ────────────────────────────────────────────────────
    public Map<String, MLWorkspace> getWorkspaces() { return workspaces; }
    public MLWorkspace getWorkspace(String id) { return workspaces.get(id); }
    public void addWorkspace(MLWorkspace ws) { workspaces.put(ws.getId(), ws); }
    public boolean deleteWorkspace(String id) { return workspaces.remove(id) != null; }

    // ── Model helpers ────────────────────────────────────────────────────────
    public Map<String, MachineLearningModel> getModels() { return models; }
    public MachineLearningModel getModel(String id) { return models.get(id); }
    public void addModel(MachineLearningModel model) {
        models.put(model.getId(), model);
        metrics.putIfAbsent(model.getId(), new ArrayList<>());
    }
    public boolean deleteModel(String id) { return models.remove(id) != null; }

    // ── Metric helpers ───────────────────────────────────────────────────────
    public List<EvaluationMetric> getMetrics(String modelId) {
        return metrics.getOrDefault(modelId, new ArrayList<>());
    }
    public void addMetric(String modelId, EvaluationMetric metric) {
        metrics.computeIfAbsent(modelId, k -> new ArrayList<>()).add(metric);
    }
}

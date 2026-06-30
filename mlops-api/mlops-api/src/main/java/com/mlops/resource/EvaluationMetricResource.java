package com.mlops.resource;

import com.mlops.exception.ModelDeprecatedException;
import com.mlops.model.EvaluationMetric;
import com.mlops.model.MachineLearningModel;
import com.mlops.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EvaluationMetricResource {

    private final String modelId;
    private final DataStore store = DataStore.getInstance();

    public EvaluationMetricResource(String modelId) {
        this.modelId = modelId;
    }

    @GET
    public Response getMetrics() {
        MachineLearningModel model = store.getModel(modelId);
        if (model == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Model not found with ID: " + modelId + "\"}")
                    .build();
        }
        List<EvaluationMetric> metricList = store.getMetrics(modelId);
        return Response.ok(metricList).build();
    }

    @POST
    public Response addMetric(EvaluationMetric metric) {
        MachineLearningModel model = store.getModel(modelId);
        if (model == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Model not found with ID: " + modelId + "\"}")
                    .build();
        }

        // Business rule: DEPRECATED models cannot accept new metrics
        if ("DEPRECATED".equalsIgnoreCase(model.getStatus())) {
            throw new ModelDeprecatedException(
                    "Model " + modelId + " is DEPRECATED and cannot accept new evaluation metrics.");
        }

        // Server generates ID and timestamp
        metric.setId(UUID.randomUUID().toString());
        metric.setTimestamp(System.currentTimeMillis());
        metric.setModelId(modelId);

        store.addMetric(modelId, metric);

        // Side-effect: update the parent model's latestAccuracy
        model.setLatestAccuracy(metric.getAccuracyScore());

        return Response.status(Response.Status.CREATED)
                .entity(metric)
                .build();
    }
}

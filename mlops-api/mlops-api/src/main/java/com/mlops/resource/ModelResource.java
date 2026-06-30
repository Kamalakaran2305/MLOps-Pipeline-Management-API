package com.mlops.resource;

import com.mlops.exception.LinkedWorkspaceNotFoundException;
import com.mlops.model.MachineLearningModel;
import com.mlops.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/v1/models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModelResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllModels(@QueryParam("status") String status) {
        List<MachineLearningModel> modelList = new ArrayList<>(store.getModels().values());

        // Filter by status query param if provided
        if (status != null && !status.isEmpty()) {
            modelList = modelList.stream()
                    .filter(m -> m.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        return Response.ok(modelList).build();
    }

    @POST
    public Response createModel(MachineLearningModel model) {
        // Server generates the ID — client must NOT supply one
        model.setId("MOD-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());

        // Validate that the referenced workspace actually exists
        if (model.getWorkspaceId() == null || store.getWorkspace(model.getWorkspaceId()) == null) {
            throw new LinkedWorkspaceNotFoundException(
                    "Workspace with ID '" + model.getWorkspaceId() + "' does not exist.");
        }

        store.addModel(model);

        // Link model ID into the workspace's modelIds list
        store.getWorkspace(model.getWorkspaceId()).getModelIds().add(model.getId());

        return Response.status(Response.Status.CREATED)
                .entity(model)
                .build();
    }

    @GET
    @Path("/{modelId}")
    public Response getModel(@PathParam("modelId") String modelId) {
        MachineLearningModel model = store.getModel(modelId);
        if (model == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Model not found with ID: " + modelId + "\"}")
                    .build();
        }
        return Response.ok(model).build();
    }

    /**
     * Sub-resource locator for /api/v1/models/{modelId}/metrics
     * JAX-RS delegates all requests under this path to EvaluationMetricResource.
     */
    @Path("/{modelId}/metrics")
    public EvaluationMetricResource getMetricResource(@PathParam("modelId") String modelId) {
        return new EvaluationMetricResource(modelId);
    }
}

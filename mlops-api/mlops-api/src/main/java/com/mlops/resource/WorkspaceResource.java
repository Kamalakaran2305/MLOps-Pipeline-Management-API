package com.mlops.resource;

import com.mlops.exception.WorkspaceNotEmptyException;
import com.mlops.model.MLWorkspace;
import com.mlops.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/api/v1/workspaces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkspaceResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllWorkspaces() {
        List<MLWorkspace> workspaceList = new ArrayList<>(store.getWorkspaces().values());
        return Response.ok(workspaceList).build();
    }

    @POST
    public Response createWorkspace(MLWorkspace workspace) {
        if (workspace.getId() == null || workspace.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Workspace ID is required\"}")
                    .build();
        }
        if (store.getWorkspace(workspace.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Workspace with this ID already exists\"}")
                    .build();
        }
        store.addWorkspace(workspace);
        return Response.status(Response.Status.CREATED)
                .entity(workspace)
                .build();
    }

    @GET
    @Path("/{workspaceId}")
    public Response getWorkspace(@PathParam("workspaceId") String workspaceId) {
        MLWorkspace workspace = store.getWorkspace(workspaceId);
        if (workspace == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Workspace not found with ID: " + workspaceId + "\"}")
                    .build();
        }
        return Response.ok(workspace).build();
    }

    @DELETE
    @Path("/{workspaceId}")
    public Response deleteWorkspace(@PathParam("workspaceId") String workspaceId) {
        MLWorkspace workspace = store.getWorkspace(workspaceId);
        if (workspace == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Workspace not found with ID: " + workspaceId + "\"}")
                    .build();
        }
        if (!workspace.getModelIds().isEmpty()) {
            throw new WorkspaceNotEmptyException("Workspace " + workspaceId +
                    " cannot be deleted as it still has models assigned to it.");
        }
        store.deleteWorkspace(workspaceId);
        return Response.noContent().build();
    }

    /**
     * HEAD /{workspaceId} - Check existence without downloading the body.
     * Part 2 question answer in practice: use HEAD instead of GET to save bandwidth.
     */
    @HEAD
    @Path("/{workspaceId}")
    public Response checkWorkspaceExists(@PathParam("workspaceId") String workspaceId) {
        MLWorkspace workspace = store.getWorkspace(workspaceId);
        if (workspace == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();
    }
}

package com.mlops;

import com.mlops.model.EvaluationMetric;
import com.mlops.model.MLWorkspace;
import com.mlops.model.MachineLearningModel;
import com.mlops.store.DataStore;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

public class Main {

    public static void main(String[] args) throws IOException {
        ResourceConfig config = new ResourceConfig()
                .packages(
                        "com.mlops.resource",
                        "com.mlops.config",
                        "com.mlops.exception",
                        "com.mlops.filter"
                )
                .register(JacksonFeature.class);

        HttpServer server = GrizzlyHttpServerFactory
                .createHttpServer(URI.create("http://0.0.0.0:8080/"), config);

        seedData();

        System.out.println("Server started at http://localhost:8080/");
        System.out.println("API base path: http://localhost:8080/api/v1");
        System.in.read();
        server.shutdownNow();
    }

    private static void seedData() {
        DataStore store = DataStore.getInstance();
        if (!store.getWorkspaces().isEmpty()) return;

        // Seed workspaces
        MLWorkspace ws1 = new MLWorkspace("WS-VISION-01", "Computer Vision Lab", 500);
        MLWorkspace ws2 = new MLWorkspace("WS-NLP-02", "NLP Research Team", 300);
        MLWorkspace ws3 = new MLWorkspace("WS-ROBOTICS-03", "Robotics Engineering", 200);
        store.addWorkspace(ws1);
        store.addWorkspace(ws2);
        store.addWorkspace(ws3);

        // Seed models
        MachineLearningModel m1 = new MachineLearningModel("MOD-8832", "TensorFlow", "DEPLOYED", 0.94, "WS-VISION-01");
        MachineLearningModel m2 = new MachineLearningModel("MOD-1011", "PyTorch", "TRAINING", 0.78, "WS-NLP-02");
        MachineLearningModel m3 = new MachineLearningModel("MOD-5500", "Scikit-Learn", "DEPRECATED", 0.65, "WS-VISION-01");
        store.addModel(m1);
        store.addModel(m2);
        store.addModel(m3);

        // Link models to workspaces
        ws1.getModelIds().add(m1.getId());
        ws1.getModelIds().add(m3.getId());
        ws2.getModelIds().add(m2.getId());

        // Seed a sample metric for MOD-8832
        EvaluationMetric metric = new EvaluationMetric(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                0.94,
                "MOD-8832"
        );
        store.addMetric("MOD-8832", metric);

        System.out.println("Seed data loaded successfully!");
    }
}

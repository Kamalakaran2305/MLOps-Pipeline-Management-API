# MLOps Pipeline Management API

A RESTful API built with **JAX-RS (Jersey 2.35)** and **Grizzly** for managing Machine Learning Workspaces, Models, and Evaluation Metrics.

---

## Student Information

| Field | Details |
| :--- | :--- |
| **Name** | Kamalakaran Raveendrakumar |
| **Module** | 5COSC022C.2 Client-Server Architectures |
| **University** | University of Westminster |
| **GitHub Repo** | [https://github.com/Kamalakaran2305/MLOps-Pipeline-Management-API](https://github.com/Kamalakaran2305/MLOps-Pipeline-Management-API) |
| **Submission Date** | June 2026 |

---

## How to Build and Run

### Prerequisites

* Java 11 or higher
* Maven 3.6 or higher
* NetBeans IDE (recommended) or any Java IDE

### Step 1 — Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/MLOpsAPI.git
cd MLOpsAPI
```

### Step 2 — Build the project

```bash
mvn clean package
```

### Step 3 — Run the server

```bash
java -jar target/MLOpsAPI-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Or run directly from NetBeans:

* Open `Main.java`
* Press **F6** to run

### Step 4 — Verify server is running

```bash
curl http://localhost:8080/api/v1
```

---


## Seed Data

The server automatically loads sample data on startup:

| ID | Name | Type |
|---|---|---|
| WS-VISION-01 | Computer Vision Lab | Workspace |
| WS-NLP-02 | NLP Research Team | Workspace |
| WS-ROBOTICS-03 | Robotics Engineering | Workspace |
| MOD-8832 | TensorFlow Model | Model (DEPLOYED) |
| MOD-1011 | PyTorch Model | Model (TRAINING) |
| MOD-5500 | Scikit-Learn Model | Model (DEPRECATED) |

---

## Complete API Endpoints Table

| Method | Endpoint | Description | Success Code | Error Codes |
|---|---|---|---|---|
| GET | /api/v1 | API discovery and resource links | 200 | - |
| GET | /api/v1/workspaces | Get all workspaces | 200 | - |
| POST | /api/v1/workspaces | Create a new workspace | 201 | 400, 409 |
| GET | /api/v1/workspaces/{workspaceId} | Get workspace by ID | 200 | 404 |
| HEAD | /api/v1/workspaces/{workspaceId} | Check workspace exists | 200 | 404 |
| DELETE | /api/v1/workspaces/{workspaceId} | Delete a workspace | 204 | 404, 409 |
| GET | /api/v1/models | Get all models | 200 | - |
| GET | /api/v1/models?status={status} | Filter models by status | 200 | - |
| POST | /api/v1/models | Create a new model | 201 | 422 |
| POST | /api/v1/models | Create model with invalid workspaceId | - | 422 |
| GET | /api/v1/models/{modelId} | Get model by ID | 200 | 404 |
| GET | /api/v1/models/{modelId}/metrics | Get all evaluation metrics | 200 | 404 |
| POST | /api/v1/models/{modelId}/metrics | Add a new evaluation metric | 201 | 403, 404 |
| POST | /api/v1/models/{modelId}/metrics | Reading on DEPRECATED model | - | 403 |

---

## API Endpoints

### Discovery

#### GET /api/v1

Returns API metadata and resource links.

```bash
curl http://localhost:8080/api/v1
```

Response 200:
```json
{
    "name": "MLOps Pipeline Management API",
    "version": "1.0",
    "description": "RESTful API for managing ML Workspaces, Models, and Evaluation Metrics",
    "contact": "admin@mlops-lab.com",
    "resources": {
        "workspaces": "/api/v1/workspaces",
        "models": "/api/v1/models"
    }
}
```

---

### Workspaces

#### GET /api/v1/workspaces — Get all workspaces

```bash
curl http://localhost:8080/api/v1/workspaces
```

#### POST /api/v1/workspaces — Create a workspace

```bash
curl -X POST http://localhost:8080/api/v1/workspaces \
  -H "Content-Type: application/json" \
  -d '{"id":"WS-TEST-99","teamName":"Test Team","storageQuotaGb":100}'
```

#### GET /api/v1/workspaces/{workspaceId} — Get workspace by ID

```bash
curl http://localhost:8080/api/v1/workspaces/WS-VISION-01
```

#### DELETE /api/v1/workspaces/{workspaceId} — Delete a workspace (no models)

```bash
curl -X DELETE http://localhost:8080/api/v1/workspaces/WS-ROBOTICS-03
```

---

### Models

#### GET /api/v1/models — Get all models

```bash
curl http://localhost:8080/api/v1/models
```

#### GET /api/v1/models?status=DEPLOYED — Filter by status

```bash
curl "http://localhost:8080/api/v1/models?status=DEPLOYED"
```

#### POST /api/v1/models — Create a valid model

```bash
curl -X POST http://localhost:8080/api/v1/models \
  -H "Content-Type: application/json" \
  -d '{"framework":"PyTorch","status":"TRAINING","latestAccuracy":0.81,"workspaceId":"WS-VISION-01"}'
```

#### GET /api/v1/models/{modelId} — Get model by ID

```bash
curl http://localhost:8080/api/v1/models/MOD-8832
```

---

### Evaluation Metrics

#### GET /api/v1/models/{modelId}/metrics — Get metric history

```bash
curl http://localhost:8080/api/v1/models/MOD-8832/metrics
```

#### POST /api/v1/models/{modelId}/metrics — Add a metric

```bash
curl -X POST http://localhost:8080/api/v1/models/MOD-8832/metrics \
  -H "Content-Type: application/json" \
  -d '{"accuracyScore":0.97}'
```

---

### Error Responses

#### 409 Conflict — Delete workspace that still has models

```bash
curl -X DELETE http://localhost:8080/api/v1/workspaces/WS-VISION-01
```

Response:
```json
{
    "status": 409,
    "error": "Conflict",
    "message": "Workspace WS-VISION-01 cannot be deleted as it still has models assigned to it."
}
```

#### 422 Unprocessable Entity — Model with invalid workspaceId

```bash
curl -X POST http://localhost:8080/api/v1/models \
  -H "Content-Type: application/json" \
  -d '{"framework":"TensorFlow","status":"TRAINING","latestAccuracy":0.5,"workspaceId":"WS-FAKE-00"}'
```

Response:
```json
{
    "status": 422,
    "error": "Unprocessable Entity",
    "message": "Workspace with ID 'WS-FAKE-00' does not exist."
}
```

#### 403 Forbidden — Add metric to DEPRECATED model

```bash
curl -X POST http://localhost:8080/api/v1/models/MOD-5500/metrics \
  -H "Content-Type: application/json" \
  -d '{"accuracyScore":0.70}'
```

Response:
```json
{
    "status": 403,
    "error": "Forbidden",
    "message": "Model MOD-5500 is DEPRECATED and cannot accept new evaluation metrics."
}
```

---

## Answers to Report Questions

### Part 1.1 — Role of MessageBodyWriter / Jackson in JSON Serialisation

When a JAX-RS resource method returns a Java object (e.g., an `MLWorkspace`), the framework cannot convert it to bytes on its own. It delegates this to a `MessageBodyWriter`. Jackson registers itself as a `MessageBodyWriter` for the `application/json` media type via `JacksonFeature`. When a response is built, JAX-RS inspects the `@Produces` annotation, finds the matching writer, and calls Jackson's `ObjectMapper.writeValue()` to serialise the Java object into a JSON byte stream sent back to the client. Without a registered JSON provider, JAX-RS would have no way to automatically convert POJOs to JSON and would return a 500 error or a blank response.

### Part 1.2 — Statelessness in REST and Horizontal Scaling

Statelessness means each HTTP request from a client must contain all the information the server needs to process it — no client session data is stored on the server between requests. Every request is treated independently. This makes horizontal scaling straightforward: since no session state lives on a specific server, any request can be routed to any instance in a load-balanced cluster without requiring sticky sessions or shared session stores. New server instances can be added or removed freely in response to traffic spikes, which is essential for cloud elasticity and high availability.

### Part 2.1 — HTTP Cache-Control Headers on GET /workspaces

By adding a `Cache-Control: max-age=60` header to the workspace list response, the client or an intermediary proxy can cache the response for 60 seconds. Subsequent identical requests within that window are served from the cache without hitting the server at all. This reduces unnecessary processing load on the server and lowers latency for the client. For data that changes infrequently, such as workspace metadata, this is a significant performance optimisation that also reduces bandwidth consumption.

### Part 2.2 — HEAD Method for Checking Workspace Existence

The client should use the **HEAD** method. HEAD is identical to GET but the server returns only the response headers and status code — no body is transmitted. This lets the client verify existence (200 OK vs 404 Not Found) while saving bandwidth, since the full JSON body is never transferred over the network. This is particularly valuable when the client only needs to confirm a resource exists before performing a follow-up operation, without the overhead of downloading its full representation.

### Part 3.1 — Server-Generated Model IDs (Security and Data Integrity)

Allowing clients to supply their own IDs creates two problems. First, a **security risk**: a malicious client could supply an ID that collides with an existing model, effectively overwriting it — a form of unauthorised data manipulation. Second, a **data integrity risk**: two clients could independently submit the same ID simultaneously, causing a race condition and corrupted data state. Server-side generation using `UUID.randomUUID()` guarantees global uniqueness without any client coordination, and keeps control of the identifier namespace firmly on the server, preventing both accidental and deliberate conflicts.

### Part 3.2 — URL Encoding for Special Characters in Query Parameters

The client must apply **percent-encoding** (URL encoding) to the query string value. Spaces become `%20` (or `+`) and ampersands become `%26`. The encoded URL would be: `?framework=Scikit%20Learn%20%26%20Tools`. This is necessary because spaces and `&` are reserved characters in a URI — a bare `&` would be interpreted as a query parameter separator, breaking the request entirely. All modern HTTP clients and libraries perform this encoding automatically when using their request-builder APIs.

### Part 4.1 — The Sub-Resource Locator Pattern

In `ModelResource`, the method `getMetricResource(@PathParam("modelId") String modelId)` is annotated with `@Path("/{modelId}/metrics")` but has no HTTP method annotation (no `@GET`/`@POST`). This makes it a **sub-resource locator** rather than a sub-resource method. JAX-RS calls this method first, passing in the extracted `modelId`, and the method returns a *new instance* of `EvaluationMetricResource`. JAX-RS then continues matching the remaining path (`/`, for example) against the methods of that returned instance. This is what allows `GET /api/v1/models/MOD-8832/metrics` to ultimately be handled by `EvaluationMetricResource.getMetrics()`, with the `modelId` already bound through the locator. The locator pattern effectively delegates a whole sub-tree of the URI space to a dedicated class.

### Part 4.2 — Benefit of Class-Level @Produces and Method-Level Overriding

Placing `@Produces(MediaType.APPLICATION_JSON)` at the class level (as done on `WorkspaceResource`, `ModelResource`, and `EvaluationMetricResource`) applies it as the default content type for every method in that class, avoiding repetitive annotation and following the DRY principle. If a specific method needs to return a different format — for example, a `text/plain` health-check response, or a CSV export of metrics — it can declare its own `@Produces` annotation directly on that method, and JAX-RS gives precedence to the more specific, method-level annotation for that single endpoint while every other method in the class continues using the class-level default.

### Part 4.3 — Side Effect: Updating latestAccuracy on POST Metric

When `EvaluationMetricResource.addMetric()` successfully stores a new `EvaluationMetric`, it also calls `model.setLatestAccuracy(metric.getAccuracyScore())` on the parent `MachineLearningModel` object retrieved from the shared `DataStore`. Because the `DataStore` holds a single shared instance of each model in its `ConcurrentHashMap` (rather than a copy), mutating that object directly updates the data seen by every other endpoint, such as `GET /api/v1/models/{modelId}`. This keeps the model's `latestAccuracy` field consistent with its most recent evaluation run without requiring a separate update call, which mirrors how real MLOps systems track concept drift over time.

### Part 5.1 — Resource Conflict (409 Conflict)

`WorkspaceResource.deleteWorkspace()` checks `workspace.getModelIds().isEmpty()` before allowing deletion. If the list is not empty, it throws a custom `WorkspaceNotEmptyException` instead of deleting anything. This exception is intercepted by `WorkspaceNotEmptyExceptionMapper`, registered via `@Provider`, which converts it into an HTTP `409 Conflict` response with a structured JSON body explaining exactly why the deletion was blocked. This prevents orphaned `MachineLearningModel` records that would otherwise reference a workspace that no longer exists, preserving referential integrity within the in-memory data store.

### Part 5.2 — Dependency Validation (422 Unprocessable Entity)

`ModelResource.createModel()` checks whether the incoming `workspaceId` corresponds to a real entry in the `DataStore` before the model is saved. If the workspace does not exist, a `LinkedWorkspaceNotFoundException` is thrown and mapped by `LinkedWorkspaceNotFoundExceptionMapper` to an HTTP `422 Unprocessable Entity` response. **422 is chosen over 404 or 500** because the request URL (`/api/v1/models`) and the JSON body are both syntactically valid — the failure is purely semantic: the referenced workspace simply does not exist. This falls firmly into the 4xx class because the client is responsible for the bad reference; the server processed everything correctly and detected an invalid foreign-key relationship, which is fundamentally different from a malformed URL (404) or an unexpected server fault (500).

### Part 5.3 — State Constraint (403 Forbidden)

`EvaluationMetricResource.addMetric()` checks `model.getStatus().equalsIgnoreCase("DEPRECATED")` before accepting a new metric. If the model is deprecated, a `ModelDeprecatedException` is thrown, which `ModelDeprecatedExceptionMapper` converts into an HTTP `403 Forbidden` response. **403 is the correct code here, not 409 or 400**, because the request is well-formed and the resource (`model`) and target (`metrics` sub-resource) both genuinely exist — the server is deliberately refusing the action based on the model's current business state. This communicates to the client that the operation is not allowed under current conditions, distinct from a missing resource (404) or a conflicting concurrent state (409).

### Part 5.4 — The Global Safety Net (500 Internal Server Error) and Exception Mapper Resolution

`GlobalExceptionMapper implements ExceptionMapper<Throwable>` is registered as a catch-all. JAX-RS resolves which `ExceptionMapper` to invoke by walking up the exception's class hierarchy and selecting the **most specific match** registered. For example, if a `NullPointerException` is thrown anywhere in a resource method, JAX-RS checks for a mapper registered for `NullPointerException`, finds none, walks up to `RuntimeException`, finds none, and eventually reaches `Throwable`, where `GlobalExceptionMapper` is registered — so that mapper handles it and returns a generic `500 Internal Server Error` with no stack trace exposed. Conversely, if a `LinkedWorkspaceNotFoundException` is thrown, JAX-RS finds the exact match `LinkedWorkspaceNotFoundExceptionMapper` immediately and never falls through to the global mapper. This hierarchy-based resolution is what allows specific business exceptions (409, 422, 403) to be handled precisely while still guaranteeing that *any* unexpected error is caught and never leaks a raw Java stack trace to the client — satisfying the "leak-proof" requirement of the coursework.

### Part 5.5 — API Request & Response Logging Filters

`LoggingFilter` implements both `ContainerRequestFilter` and `ContainerResponseFilter`, registered once via `@Provider` and applied automatically to every incoming request and outgoing response across the entire API — without needing to add logging code inside any individual resource method. This follows the separation-of-concerns principle: resource classes focus purely on business logic, while cross-cutting concerns like logging live in one dedicated class.

From `ContainerRequestContext` (incoming request), two pieces of crucial metadata extracted are:
1. **Request URI** (`requestContext.getUriInfo().getRequestUri()`) — shows exactly which endpoint was hit, essential for tracing which call caused a downstream issue.
2. **HTTP Method** (`requestContext.getMethod()`) — combined with the URI, this pinpoints the precise operation (GET, POST, DELETE) being performed.

From `ContainerResponseContext` (outgoing response), two more pieces of metadata extracted are:
1. **HTTP Status Code** (`responseContext.getStatus()`) — immediately shows whether the request succeeded (2xx) or failed (4xx/5xx), and which error class applies.
2. **Response Headers** (`responseContext.getHeaders()`) — reveals `Content-Type` and any custom headers, useful for diagnosing content-negotiation mismatches or missing metadata.

Together, the request and response logs form a matched pair (`--> METHOD URI` followed by `<-- METHOD URI | Status: code`), giving full observability over every transaction the API processes, which satisfies the coursework's requirement for request/response logging via `java.util.logging.Logger`.

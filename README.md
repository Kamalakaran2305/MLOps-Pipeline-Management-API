# MLOps Pipeline Management API

A RESTful API built with **JAX-RS (Jersey 2.35)** and **Grizzly** for managing Machine Learning Workspaces, Models, and Evaluation Metrics.

---

## API Overview

| Resource | Base Path |
|---|---|
| Discovery | `GET /api/v1` |
| Workspaces | `/api/v1/workspaces` |
| Models | `/api/v1/models` |
| Metrics (sub-resource) | `/api/v1/models/{modelId}/metrics` |

---

## How to Build and Run

### Prerequisites
- Java 11+
- Maven 3.6+

### Build
```bash
mvn clean package
```
This produces `target/MLOpsAPI-1.0-SNAPSHOT-jar-with-dependencies.jar`.

### Run
```bash
java -jar target/MLOpsAPI-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Server starts at: `http://localhost:8080/`  
API base path: `http://localhost:8080/api/v1`

Press **Enter** to stop the server.

---

## Sample curl Commands

### 1. Discovery endpoint
```bash
curl -X GET http://localhost:8080/api/v1
```

### 2. Get all workspaces
```bash
curl -X GET http://localhost:8080/api/v1/workspaces
```

### 3. Create a new workspace
```bash
curl -X POST http://localhost:8080/api/v1/workspaces \
  -H "Content-Type: application/json" \
  -d '{"id":"WS-TEST-99","teamName":"Test Team","storageQuotaGb":100}'
```

### 4. Get all DEPLOYED models (filtered)
```bash
curl -X GET "http://localhost:8080/api/v1/models?status=DEPLOYED"
```

### 5. Create a new model (server generates the ID)
```bash
curl -X POST http://localhost:8080/api/v1/models \
  -H "Content-Type: application/json" \
  -d '{"framework":"PyTorch","status":"TRAINING","latestAccuracy":0.81,"workspaceId":"WS-VISION-01"}'
```

### 6. Post an evaluation metric for a model
```bash
curl -X POST http://localhost:8080/api/v1/models/MOD-8832/metrics \
  -H "Content-Type: application/json" \
  -d '{"accuracyScore":0.97}'
```

### 7. Get all metrics for a model
```bash
curl -X GET http://localhost:8080/api/v1/models/MOD-8832/metrics
```

### 8. Delete a workspace (fails if models exist — 409)
```bash
curl -X DELETE http://localhost:8080/api/v1/workspaces/WS-ROBOTICS-03
```

### 9. Post metric to DEPRECATED model (fails — 403)
```bash
curl -X POST http://localhost:8080/api/v1/models/MOD-5500/metrics \
  -H "Content-Type: application/json" \
  -d '{"accuracyScore":0.70}'
```

### 10. Create model with non-existent workspaceId (fails — 422)
```bash
curl -X POST http://localhost:8080/api/v1/models \
  -H "Content-Type: application/json" \
  -d '{"framework":"TensorFlow","status":"TRAINING","latestAccuracy":0.5,"workspaceId":"WS-FAKE-00"}'
```

---

## Answers to Report Questions

### Part 1 — Setup & Discovery

**Q: Explain the role of a `MessageBodyWriter` or a JSON provider (like Jackson) in the JSON serialisation process.**

When a JAX-RS resource method returns a Java object (e.g., an `MLWorkspace`), the framework does not know how to convert it to bytes on its own. It delegates this to a `MessageBodyWriter`. Jackson registers itself as a `MessageBodyWriter` for the `application/json` media type via the `JacksonFeature`. When a response is built, JAX-RS inspects the `@Produces` annotation, finds a matching writer, and calls `Jackson's ObjectMapper.writeValue()` to serialise the Java object into a JSON byte stream that is sent back to the client.

**Q: Define statelessness in REST and explain why it makes cloud APIs easier to scale horizontally.**

Statelessness means each HTTP request from a client must contain all the information the server needs to process it — no client session data is stored on the server between requests. The server treats every request independently. This makes horizontal scaling straightforward: since no session state lives on a specific server, any request can be routed to any instance in a load-balanced cluster without requiring sticky sessions or shared session stores. New servers can be added or removed freely, which is essential for cloud elasticity.

---

### Part 2 — Workspace Management

**Q: How could HTTP `Cache-Control` headers improve performance on `GET /workspaces`?**

By adding a `Cache-Control: max-age=60` header to the workspace list response, the client (or an intermediary proxy) can cache the response for 60 seconds. Subsequent identical requests within that window are served from the cache without hitting the server at all. This reduces unnecessary processing load on the server and lowers latency for the client. For data that changes infrequently, such as workspace metadata, this is a significant optimisation.

**Q: Which HTTP method should a client use to check if a workspace exists without downloading the full JSON body?**

The client should use the **HEAD** method. HEAD is identical to GET but the server returns only the response headers and status code — no body is sent. This lets the client verify existence (200 OK vs 404 Not Found) while saving bandwidth, since the full JSON body is never transferred. This is implemented in the `WorkspaceResource` as `HEAD /api/v1/workspaces/{workspaceId}`.

---

### Part 3 — Model Operations & Filtering

**Q: Why should the server generate the model `id` rather than accepting it from the client?**

Allowing clients to supply their own IDs creates two problems. First, a **security risk**: a malicious client could supply an ID that collides with an existing model, effectively overwriting it — a form of unauthorised data manipulation. Second, a **data integrity risk**: two clients could independently submit the same ID simultaneously, causing a race condition and a corrupted data state. Server-side generation using `UUID.randomUUID()` guarantees global uniqueness without any client coordination, and keeps control of the identifier namespace firmly on the server.

**Q: How must a client encode a URL with spaces or special characters (e.g., `?framework=Scikit Learn & Tools`)?**

The client must apply **percent-encoding** (URL encoding) to the query string value. Spaces become `%20` (or `+`), ampersands become `%26`, etc. The encoded URL would be: `?framework=Scikit%20Learn%20%26%20Tools`. This is necessary because spaces and `&` are reserved characters in a URI — a bare `&` would be interpreted as a query parameter separator, breaking the request. All HTTP clients and libraries perform this encoding automatically when using their request-builder APIs.

---

### Part 4 — Deep Nesting with Sub-Resources

**Q: What is the benefit of class-level `@Produces` placement, and how does method-level overriding work?**

Placing `@Produces(MediaType.APPLICATION_JSON)` at the class level applies it as a default to every method in the resource, avoiding repetition. If a specific method needs a different content type — for example, returning plain text or a CSV — it can declare its own `@Produces` annotation at the method level, which overrides the class-level default for that method only. This promotes the DRY principle while still allowing per-method flexibility.

---

### Part 5 — Error Handling & Logging

**Q: Why must a validation failure (non-existent `workspaceId`) return a 4xx code rather than a 5xx code?**

HTTP status code classes express *who is responsible* for the error. **4xx codes** indicate a **client error** — the request itself was malformed or referenced something that does not exist. Providing a non-existent `workspaceId` is the client's fault; the server received and processed the request correctly. **5xx codes** indicate a **server error** — something went wrong on the server that the client could not have predicted or prevented. Returning a 5xx for a bad client input would be misleading, as it implies a server-side problem and could cause the client to retry a fundamentally invalid request.

**Q: If both a specific `ExceptionMapper` and the global `ExceptionMapper<Throwable>` exist, how does JAX-RS choose?**

JAX-RS always selects the **most specific** mapper available. It inspects the inheritance hierarchy of the thrown exception and picks the mapper whose generic type most closely matches it. So if a `LinkedWorkspaceNotFoundException` is thrown, JAX-RS will use `LinkedWorkspaceNotFoundExceptionMapper` rather than the global `ExceptionMapper<Throwable>`. The global mapper only executes when no more-specific mapper matches — it is a true catch-all safety net.

**Q: List two pieces of HTTP metadata extractable from `ContainerRequestContext` and `ContainerResponseContext` useful for debugging.**

From `ContainerRequestContext`:
1. **Request URI** (`getUriInfo().getRequestUri()`) — identifies exactly which endpoint was called, essential for reproducing issues.
2. **HTTP Method** (`getMethod()`) — combined with the URI, pinpoints the operation (GET, POST, DELETE) that triggered the problem.

From `ContainerResponseContext`:
1. **HTTP Status Code** (`getStatus()`) — immediately shows whether the request succeeded or failed and which error class applies.
2. **Response Headers** (`getHeaders()`) — reveals `Content-Type`, `Cache-Control`, and any custom headers, which can expose misconfiguration or unexpected content negotiation results.

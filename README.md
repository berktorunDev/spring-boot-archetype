This project includes the following services to ensure observability of the application:

*   Prometheus
*   Grafana
*   Elasticsearch
*   Kibana
*   Filebeat
*   Global Exception Handler

Service URLs
------------
*   Prometheus: http://localhost:9090
*   Grafana: http://localhost:3000
*   Elasticsearch: http://localhost:9200
*   Kibana: http://localhost:5601

Grafana default login credentials:
*   Username: admin
*   Password: admin

While adding Prometheus as a data source in Grafana, use http://prometheus:9090 instead of localhost:9090, since Prometheus is defined as a service named prometheus in the docker-compose file.

Exception Handling
------------------
A global exception handler is implemented to catch all exceptions across the application and return a standard JSON error response.

Example error response:

```JSON
{    
    "timestamp": "2025-03-30T23:50:00Z",    
    "status": 404,    
    "error": "Not Found",    
    "message": "Entity not found",    
    "path": "/api/example"  
}
```

Docker Compose
--------------
All services can be started using docker-compose.yml:

`docker-compose up -d   `

Filebeat reads logs from ./logs/app-log.json and forwards them to Elasticsearch.

Summary
-------

With this setup, the application:

*   Collects metrics (Prometheus)
*   Visualizes metrics (Grafana)
*   Writes logs in JSON format (Logback)
*   Sends logs to Elasticsearch (Filebeat)
*   Displays logs (Kibana)
*   Manages errors consistently (Global Exception Handler)
*   All HTTP requests and responses are logged in JSON format (HttpAuditFilter)
*   Cors settings are configured (SecurityConfig)
*   Graceful shutdown configured (application.yml)

Design Overview
---------------

### 1\. **Annotation-Based Configuration**

* Class-Level Annotation: When the @RateLimit annotation is applied at the class level, it affects all endpoints within that controller. This is ideal when you want a consistent rate limit across all routes in a given controller.

* Method-Level Annotation: Applying @RateLimit directly on a method allows you to override the class-level settings for that specific endpoint. This provides fine-grained control if a particular endpoint requires a different request rate compared to the rest of the controller.

* Global Configuration: If no @RateLimit annotation is present on either the controller or method, the system checks the global configuration (set via application.yml). Global settings are only applied if enabled, ensuring that only endpoints without specific annotations are rate limited based on the default values.


### 2\. **IP-Based Rate Limiting**

* Per-Client Tracking: Instead of using a single counter per endpoint, the rate limiter combines the endpoint signature with the client’s IP address. This means each client (identified by IP) has its own counter, preventing one client's heavy usage from affecting others.

* Unique Key Generation: The key used for tracking is a composite of the method’s short signature and the IP address (e.g., /orders:192.168.1.10). This ensures accurate, per-client rate limiting.


### 3\. **Time Window and Request Count**

* Fixed-Window Algorithm: The rate limiter operates on a fixed window basis. For example, if configured for 5 requests per 30 seconds, each IP can only make 5 requests within any 30-second period. Once the time window expires, the count resets automatically.

* Atomic Operations for Thread Safety: To ensure the rate limiter works correctly under concurrent conditions, atomic variables (such as AtomicInteger and AtomicLong) and CAS (Compare-And-Set) loops are used. This guarantees that request counting and window resetting are thread-safe.


### 4\. **Time Unit Validation**

* String-Based Time Unit: The annotation accepts the time unit as a string (e.g., "SECOND", "MINUTE", or "HOUR"). This design provides flexibility and allows developers to supply the unit in any case (upper, lower, or mixed).

* Error Handling: If an invalid time unit is provided, the system will throw an IllegalArgumentException with a descriptive error message. This ensures that configuration errors are caught early in the deployment or development process.


Usage Examples
--------------

### Example 1: Class-Level Rate Limiting

Apply the @RateLimit annotation to the entire controller. All endpoints in the controller will inherit this limit.

```java
@RestController
@RequestMapping("/api")
@RateLimit(limit = 5, duration = 30, unit = "SECOND")
public class DemoController {
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }

    @GetMapping("/greet")
    public String greet() {
        return "Greetings from the DemoController!";
    }
}
```

**Behavior:**

*   Every client IP can make up to 5 requests per 30 seconds for any endpoint in this controller.


### Example 2: Method-Level Override

You can override the class-level configuration for a specific endpoint by adding the @RateLimit annotation to that method.

```java
@RestController  
@RequestMapping("/api")  
@RateLimit(limit = 5, duration = 30, unit = "SECOND") 
public class DemoController {

    @GetMapping("/special")
    @RateLimit(limit = 10, duration = 60, unit = "SECOND")
    public String special() {          
        return "Special endpoint with custom rate limit.";      
    }      
    
    @GetMapping("/hello")     
    public String hello() {          
        return "Hello, World!";      
    }  
}
```

**Behavior:**

*   The /special endpoint allows up to 10 requests per 60 seconds per client IP, overriding the class-level limit.

*   Other endpoints (like /hello) continue to use the class-level limit.


### Example 3: Global Rate Limiting

If no endpoint or controller is annotated with @RateLimit, but global rate limiting is enabled in the configuration, the global settings will be applied.

**Global Configuration (application.yml):**

```yaml
rate-limit:
  enabled: true
  capacity: 100
  time: 60
  unit: SECONDS
```

**Behavior:**

*   Endpoints without specific annotations will allow up to 100 requests per 60 seconds per client IP.


Key Considerations When Using This Rate Limiter
-----------------------------------------------

* Configuration Hierarchy: Understand that the effective rate limit is resolved in the following order:

    1. Method-Level Annotation: Highest priority.

    2. Class-Level Annotation: Used when method-level annotation is absent.

    3. Global Settings: Fallback if no annotations are present.

* IP Dependency: Since the limiter is IP-based, all requests from the same IP share the same counter for an endpoint. Be aware that if multiple users are behind the same NAT or proxy, they might collectively hit the rate limit.

*   **Testing and Debugging:**

    *   Use low values (e.g., 2 requests per 10 seconds) during development to easily trigger and debug rate limiting behavior.

    *   Set breakpoints in critical methods (such as allowRequest() in the RateLimiter class) or add logging statements to observe how the request counters and time windows update.

* Error Handling: The design validates the provided time unit. If an invalid unit is supplied, an exception will be thrown immediately, preventing misconfiguration.

* Thread Safety: The use of atomic variables and a ConcurrentHashMap ensures that the rate limiter works correctly even under high concurrency. This is critical in production environments where multiple threads handle incoming requests simultaneously.

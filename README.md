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

`{    
    "timestamp": "2025-03-30T23:50:00Z",    
    "status": 404,    
    "error": "Not Found",    
    "message": "Entity not found",    
    "path": "/api/example"  
}`

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
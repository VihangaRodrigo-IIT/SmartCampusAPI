# Smart Campus API
### 5COSC022W — Client-Server Architectures Coursework 2025/26

---

## Overview

This is a RESTful API built for managing rooms and sensors across a university 
campus. The API is built using JAX-RS with Jersey 2.32 running on Apache Tomcat 9.
It provides a clean interface for campus facilities managers to track rooms, 
sensors deployed in those rooms, and the historical readings those sensors record.

The system is structured around three main resources — Rooms, Sensors, and Sensor 
Readings. Rooms contain sensors, and sensors contain a history of readings. There 
is also a discovery endpoint at the root that describes the API and links to all 
available resources.

### Technology Stack
- Java 8
- JAX-RS (Jakarta RESTful Web Services)
- Jersey 2.32 (JAX-RS Implementation)
- Jackson (JSON serialization via jersey-media-json-jackson)
- Apache Tomcat 9
- Maven

### Data Storage
No database is used in this project. All data is stored in memory using 
ConcurrentHashMap collections inside a shared DataStore class. Data persists 
for the lifetime of the server session.

---

## API Structure

```
Base URL: http://localhost:8080/SmartCampusAPI/api/v1

GET    /api/v1                                  → Discovery endpoint
GET    /api/v1/rooms                            → Get all rooms
POST   /api/v1/rooms                            → Create a new room
GET    /api/v1/rooms/{roomId}                   → Get a specific room
DELETE /api/v1/rooms/{roomId}                   → Delete a room (fails if sensors exist)
GET    /api/v1/sensors                          → Get all sensors
GET    /api/v1/sensors?type={type}              → Filter sensors by type
POST   /api/v1/sensors                          → Register a new sensor
GET    /api/v1/sensors/{sensorId}               → Get a specific sensor
GET    /api/v1/sensors/{sensorId}/readings      → Get all readings for a sensor
POST   /api/v1/sensors/{sensorId}/readings      → Post a new reading for a sensor
```

### Business Rules
- A room cannot be deleted if it still has sensors assigned to it (returns 409)
- A sensor cannot be registered with a roomId that does not exist (returns 422)
- A sensor with MAINTENANCE status cannot accept new readings (returns 403)
- Posting a new reading automatically updates the parent sensor's currentValue

### HTTP Status Codes Used
- 200 OK — successful GET
- 201 Created — successful POST
- 204 No Content — successful DELETE
- 400 Bad Request — missing required fields
- 403 Forbidden — sensor is under maintenance
- 404 Not Found — resource does not exist
- 409 Conflict — room still has sensors assigned
- 422 Unprocessable Entity — roomId reference does not exist
- 500 Internal Server Error — unexpected server error

---

## Project Structure

```
src/main/java/
├── com.smartcampus.app
│   └── SmartCampusApplication.java       ← JAX-RS application entry point
├── com.smartcampus.dao
│   └── DataStore.java                    ← In-memory data storage
├── com.smartcampus.model
│   ├── Room.java
│   ├── Sensor.java
│   ├── SensorReading.java
│   └── ErrorMessage.java
├── com.smartcampus.resource
│   ├── DiscoveryResource.java            ← GET /api/v1
│   ├── RoomResource.java                 ← /api/v1/rooms
│   ├── SensorResource.java               ← /api/v1/sensors
│   └── SensorReadingResource.java        ← /api/v1/sensors/{id}/readings
├── com.smartcampus.exception
│   ├── DataNotFoundException.java
│   ├── DataNotFoundExceptionMapper.java
│   ├── RoomNotEmptyException.java
│   ├── RoomNotEmptyExceptionMapper.java
│   ├── LinkedResourceNotFoundException.java
│   ├── LinkedResourceNotFoundExceptionMapper.java
│   ├── SensorUnavailableException.java
│   ├── SensorUnavailableExceptionMapper.java
│   └── GlobalExceptionMapper.java
└── com.smartcampus.filter
    └── LoggingFilter.java
```

---

## How to Build and Run

### Prerequisites
- Java JDK 8 or higher
- Apache Maven 3.x
- Apache Tomcat 9.x
- Apache NetBeans IDE (recommended) or any Maven-compatible IDE

### Step 1 — Clone the Repository
```bash
git clone https://github.com/YOUR_USERNAME/SmartCampusAPI.git
```

### Step 2 — Open in NetBeans
- Open Apache NetBeans
- Go to File → Open Project
- Navigate to the cloned folder and open it
- Maven will automatically download all required dependencies

### Step 3 — Build the Project
- Right-click the project → Clean and Build
- Wait until you see BUILD SUCCESS in the output window

### Step 4 — Configure Tomcat (First Time Only)
- Go to Services → Servers → Add Server
- Select Apache Tomcat 9
- Point it to your Tomcat installation folder
- Set a username and password (e.g. admin/admin)

### Step 5 — Run the Project
- Right-click the project → Run
- Wait for the deployment to complete in the output window
- The API will be available at:

```
http://localhost:8080/SmartCampusAPI/api/v1
```

---

## Sample curl Commands

### 1. Get API Discovery Info
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1
```
Expected: 200 OK with API metadata and resource links

---

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"LIB-301\", \"name\": \"Library Quiet Study\", \"capacity\": 50}"
```
Expected: 201 Created with the room object

---

### 3. Register a Sensor
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"TEMP-001\", \"type\": \"Temperature\", \"status\": \"ACTIVE\", \"currentValue\": 0.0, \"roomId\": \"LIB-301\"}"
```
Expected: 201 Created with the sensor object

---

### 4. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=Temperature"
```
Expected: 200 OK with only Temperature type sensors

---

### 5. Post a Sensor Reading
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"value\": 22.5}"
```
Expected: 201 Created with auto-generated id and timestamp

---

### 6. Get All Readings for a Sensor
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings
```
Expected: 200 OK with array of readings

---

### 7. Try to Delete a Room That Has Sensors
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```
Expected: 409 Conflict with JSON error body

---

### 8. Try to Register a Sensor With a Non-Existent Room
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"TEMP-999\", \"type\": \"Temperature\", \"status\": \"ACTIVE\", \"currentValue\": 0.0, \"roomId\": \"FAKE-999\"}"
```
Expected: 422 Unprocessable Entity with JSON error body

---

## Report: Answers to Questions

---

### Part 1.1 — JAX-RS Resource Lifecycle

By default, JAX-RS creates a brand new instance of a resource class for 
every incoming HTTP request. This is called the request-scoped lifecycle. 
What this means in practice is that any regular instance variables declared 
inside a resource class are created fresh for each request and discarded the 
moment the request finishes.

This creates a problem for in-memory data storage. If data were stored as 
instance variables inside a resource class, it would be wiped out after every 
single request. To solve this, all shared data in this project is stored in a 
separate DataStore class using static fields. Static fields belong to the class 
itself rather than any particular instance, so they persist for the entire 
lifetime of the server regardless of how many resource instances are created 
and destroyed.

The other important consideration is thread safety. Since multiple HTTP requests 
can arrive simultaneously and all of them share the same static maps, using a 
regular HashMap is not safe. Two threads writing to a HashMap at the same time 
can corrupt the data structure entirely. To handle this, ConcurrentHashMap is 
used throughout the DataStore class. ConcurrentHashMap is specifically designed 
for concurrent access and handles simultaneous reads and writes safely without 
requiring manual synchronization, which prevents race conditions and data loss.

---

### Part 1.2 — HATEOAS

HATEOAS (Hypermedia As The Engine Of Application State) is considered a hallmark 
of advanced RESTful design because it makes an API self-describing and 
self-navigable. Instead of requiring clients to rely on separate static 
documentation to discover available URLs, the API embeds navigation links 
directly within its responses.

In this project, the discovery endpoint at GET /api/v1 returns links to 
/api/v1/rooms and /api/v1/sensors in the response body itself. A client 
developer can start at the root URL and find everything they need without 
consulting any external document.

The key benefit over static documentation is that links in responses are always 
accurate because they are generated by the live server. Documentation can easily 
go out of date when URLs change, but a HATEOAS response always reflects the 
current state of the API. It also reduces tight coupling between the client and 
server. If a URL changes, only the server-side link needs updating rather than 
every client that hardcoded the old URL. This makes the API much more resilient 
to change and much easier to explore for new developers.

---

### Part 2.1 — Returning IDs vs Full Room Objects in a List

Returning only IDs in a list response produces a small and lightweight payload. 
However, it forces the client to send a separate HTTP request for each ID to 
retrieve the full details. If there are 100 rooms, that is 101 network requests 
in total. This is known as the N+1 problem and it adds significant latency and 
unnecessary load on the server.

Returning full room objects in the list means a larger payload per request, 
but the client gets everything it needs in a single round trip. For most 
real-world use cases this is the better trade-off because it dramatically 
reduces the number of network calls needed. In this project full objects are 
returned in list responses because the room objects are small and it makes the 
API far simpler to work with from the client side.

---

### Part 2.2 — Is DELETE Idempotent?

Yes, DELETE is idempotent in this implementation. Idempotency means that 
sending the same request multiple times leaves the server in the same final 
state as sending it once.

The first time a DELETE request is sent for a room that exists and has no 
sensors, the room is removed from the DataStore and the server returns 204 
No Content. If the exact same DELETE request is sent again, the room no 
longer exists, so a DataNotFoundException is thrown and the server returns 
404 Not Found. The response code is different between the two calls, but the 
server state is identical in both cases — the room is absent from the system 
either way.

Idempotency is about the server state, not the response code. The resource 
ends up in the same state regardless of how many times the operation is 
repeated, which satisfies the definition of idempotency in REST.

---

### Part 3.1 — What Happens With a Content-Type Mismatch

The @Consumes(MediaType.APPLICATION_JSON) annotation tells JAX-RS that the 
POST method only accepts requests with a Content-Type: application/json header. 
If a client sends data in a different format such as text/plain or 
application/xml, Jersey rejects the request before the method body even runs 
and automatically returns HTTP 415 Unsupported Media Type.

This happens because Jersey inspects the Content-Type header of every incoming 
request and compares it against the media types declared in @Consumes. If no 
match is found, Jersey determines that no resource method can handle the request 
and returns 415 immediately. This is actually very useful behaviour because it 
protects the method from receiving data it cannot deserialise. Jackson, the JSON 
library used in this project, cannot parse plain text or XML into a Java object, 
so catching the mismatch at the annotation level prevents unnecessary runtime 
errors and gives the client a clear and informative response.

---

### Part 3.2 — @QueryParam vs Path Parameter for Filtering

Using a query parameter like GET /api/v1/sensors?type=CO2 is a much better 
design than embedding the filter in the URL path like /api/v1/sensors/type/CO2, 
for several reasons.

Query parameters are optional by nature. When no type parameter is provided the 
endpoint simply returns all sensors, which is exactly what you want. With a 
path-based approach you would need separate endpoint definitions for the filtered 
and unfiltered cases, which increases code duplication.

A path like /sensors/type/CO2 also implies that type and CO2 are actual 
resources in the hierarchy, which they are not. They are just filter criteria 
being applied to a collection. Using them in the path violates RESTful resource 
naming conventions where path segments should represent resources, not 
operations or modifiers.

Query parameters also scale well when multiple filters are needed at once. 
Adding a second filter is as simple as ?type=CO2&status=ACTIVE. Achieving the 
same thing with path parameters would create a combinatorial explosion of URL 
patterns. For all these reasons, query parameters are the universally recognised 
standard for filtering and searching collections in REST APIs.

---

### Part 4.1 — Sub-Resource Locator Pattern

The Sub-Resource Locator pattern is a way of splitting a large API across 
multiple focused classes rather than piling all the logic into a single massive 
controller. In this project, SensorResource manages sensor-level operations and 
contains a locator method that hands off to a dedicated SensorReadingResource 
class whenever the URL path continues to /readings. All reading-related logic 
lives entirely within SensorReadingResource.

The main architectural benefit is separation of concerns. Each class has one 
clear and well-defined responsibility. SensorResource deals with sensors, and 
SensorReadingResource deals with readings. If the way readings work needs to 
change in the future, only SensorReadingResource needs to be modified without 
touching SensorResource at all.

In contrast, defining every nested path in one class would create a large, 
tightly coupled controller that becomes increasingly difficult to read, 
maintain, and extend as the API grows. The sub-resource locator pattern keeps 
each class small, focused, and independently manageable, which is an important 
quality in any production-grade API.

---

### Part 5.2 — Why 422 Is More Accurate Than 404

HTTP 404 Not Found is specifically meant to indicate that the URL being 
requested does not exist on the server. When a client posts a new sensor to 
/api/v1/sensors, that endpoint clearly exists and was found successfully. 
Returning 404 in this situation would be misleading and confusing.

HTTP 422 Unprocessable Entity is more semantically accurate because the problem 
is not with the URL — it is with the content of the request body. The JSON 
payload is syntactically valid and can be parsed without errors, but it contains 
a roomId that does not exist in the system. The server understood the request 
completely but cannot process it because the data inside it is logically invalid 
due to a broken reference. HTTP 422 was designed precisely for this situation 
where a request is well-formed but semantically unprocessable. Using 422 gives 
the client a clear signal that they need to fix the data in their request body 
rather than the URL they are calling, which leads to faster debugging and a 
better developer experience.

---

### Part 5.4 — Security Risks of Exposing Stack Traces

Exposing raw Java stack traces to external API consumers is a serious security 
risk because they reveal detailed internal information about the server that 
attackers can use to plan targeted attacks.

A stack trace can expose internal file paths and package names, which reveals 
the directory structure of the application on the server and helps an attacker 
understand how the codebase is organised. It also exposes the names and version 
numbers of third-party libraries and frameworks being used. An attacker can take 
those version numbers and search known CVE databases for vulnerabilities specific 
to those versions and then craft exploits accordingly.

Stack traces also reveal class names, method names, and line numbers which give 
away the internal logic flow of the application. If a database query fails, the 
stack trace might include the SQL driver being used, table names, or column 
names, which could directly assist a SQL injection attack.

By implementing a GlobalExceptionMapper that catches all Throwable types and 
returns only a generic 500 message, this API ensures that none of this sensitive 
internal information ever reaches the client. The client is told that something 
went wrong but learns nothing about the internals of the system.

---

### Part 5.5 — Why Filters Are Better Than Manual Logging

If logging were added manually inside every resource method, the same 
boilerplate code would be repeated across dozens of methods throughout the 
project. If the log format ever needed to change, every single occurrence would 
need to be updated and it would be very easy to miss some, leading to 
inconsistent logs.

With a JAX-RS filter, the logging code is written once in a single class and 
automatically applied to every request and response across the entire API 
without any extra effort in the resource classes. This follows the DRY principle 
(Don't Repeat Yourself) and keeps the codebase clean and maintainable.

It also achieves proper separation of concerns. Resource methods should focus 
purely on their business logic such as creating rooms or registering sensors. 
They should not be concerned with infrastructure-level concerns like logging. 
Mixing these two responsibilities in the same method makes the code harder to 
read and harder to test.

A filter also guarantees consistency. Because there is only one place where 
logging happens, every request is logged in exactly the same format. With manual 
logging, different developers writing different methods might log different 
information in different formats, making the logs harder to analyse and monitor.

---

*Submitted by: [Your Name] | Student ID: [Your ID] | Module: 5COSC022W*

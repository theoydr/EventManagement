# **Event and Booking Management System**

A robust REST API for managing events, user registrations, and bookings, built with Java 21 and Spring Boot 3\.

## **Features**

* **User Management:** User registration and retrieval.
* **Event Management:** Create, update, cancel, and view events.
* **Booking System:** Allow users to book tickets for events with capacity validation.
* **REST API:** A clean, well-documented RESTful API.
* **Validation:** Robust validation for all incoming data.
* **Error Handling:** Centralized, consistent error responses for a clean API contract.
* **API Documentation:** Interactive API documentation powered by OpenAPI 3 and Swagger UI.

## **Project Structure**

* **controller/api**: API interfaces with OpenAPI documentation.
* **controller**: Controller implementations.
* **service**: Service layer interfaces and implementations (business logic).
* **repository**: Spring Data JPA repositories.
* **model**: JPA entity classes.
* **dto**: Data Transfer Objects for API requests and responses.
* **exception**: Custom exception classes and the global exception handler.
* **constants**: Application-wide constants, like message keys.
* **resources/messages**: Centralized place for messages and labels.
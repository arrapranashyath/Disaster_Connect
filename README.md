# DisasterConnect

### A Unified Platform for Disaster Response and Relief Coordination

## Overview

DisasterConnect is a disaster response and relief coordination platform developed to make it easier for disaster-affected communities to connect with essential services and relief providers. During emergencies, information about shelters, available resources, volunteers, and support organizations can be fragmented across multiple channels, making it difficult for people to find the assistance they need.

The project aims to address this challenge through a centralized platform that brings citizens, non-governmental organizations (NGOs), volunteers, and relief services together. By organizing disaster-related information and providing a common point of access, DisasterConnect seeks to support better coordination between affected communities and the people and organizations responding to their needs.

The application is being developed as a software engineering project, with an emphasis on backend architecture, secure access, data management, and maintainable application design.

## Objectives

The primary objectives of DisasterConnect are to:

- Provide a centralized platform for accessing disaster-related assistance and relief information.
- Connect disaster-affected citizens with shelters, NGOs, volunteers, and other relevant service providers.
- Support the organization and management of information related to disaster response.
- Provide a structured backend for handling application requests, business logic, and persistent data.
- Incorporate authentication and access control to support secure interactions with the platform.
- Establish a foundation that can be extended as disaster response requirements evolve.

## Key Capabilities

The platform is designed around the following areas of functionality:

### Disaster Relief Coordination

Bring information about disaster assistance and relief services into a common system, reducing the need to search across disconnected sources.

### Shelter and Essential Service Discovery

Help affected individuals identify relevant shelters and support services, subject to the availability and accuracy of information recorded in the platform.

### Citizen and Relief Provider Connectivity

Provide a common system through which affected citizens and relief providers can interact with disaster-related information and assistance workflows.

### Secure Application Access

Use a structured backend security layer to support authenticated access and protect application resources according to the configured authorization rules.

### Structured Data Management

Organize application data through dedicated entities, repositories, services, and data transfer objects, separating persistence concerns from business logic and API handling.

*Note: These capabilities describe the project's intended functionality. The exact functionality available in a running deployment depends on the current implementation.*

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Primary programming language |
| Spring Boot | Backend application framework |
| Maven | Dependency management and build automation |
| MySQL | Relational database |
| Spring Data JPA / Hibernate | Persistence and object-relational mapping, where configured |
| Spring Security | Authentication and authorization, where configured |
| JSON Web Tokens (JWT) | Token-based authentication, where configured |
| HTML, CSS and JavaScript | Frontend technologies, where used by the application |

## Application Architecture

DisasterConnect follows a layered backend structure to keep request handling, business logic, data access, and security concerns organized.

The main application code is located under `src/main/java/com/disasterconnect/`.

### Package Structure

| Package | Responsibility |
|---|---|
| `config` | Application configuration and framework setup |
| `controller` | HTTP request handling and API endpoints |
| `dto` | Data transfer objects used to structure data exchanged between application layers |
| `entity` | Domain models mapped to persistent data |
| `exception` | Application-specific exception handling |
| `repository` | Database access and persistence operations |
| `security` | Security-related components and authentication support |
| `service` | Business logic and coordination between controllers and repositories |

`DisasterConnectApplication.java` serves as the Spring Boot application entry point.

The `src/main/resources/` directory contains application configuration, database schema resources, and static frontend assets. The current project includes `schema.sql` and a static `index.html` file.

This organization separates the responsibilities of individual components and makes the application easier to test, maintain, and extend.

## Project Structure

```text
DisasterConnect/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/disasterconnect/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       ├── security/
│   │   │       ├── service/
│   │   │       └── DisasterConnectApplication.java
│   │   └── resources/
│   │       ├── schema.sql
│   │       ├── static/
│   │       │   └── index.html
│   │       └── application.properties
│   └── test/
├── .env.example
├── .gitignore
├── pom.xml
├── 112415030_SRS.pdf
├── DisasterConnect_Design_Document.docx
└── README.md
```

The structure above represents the main application layout. Individual files and configuration details may vary as development progresses.

## Prerequisites

Before running the application, ensure that the following are installed and available:

- Java Development Kit (JDK) compatible with the version configured in `pom.xml`.
- Apache Maven, or the project's Maven Wrapper if available.
- MySQL Server.
- Git, if cloning the repository from GitHub.

A Java IDE such as IntelliJ IDEA or Visual Studio Code can be used for development, but it is not required to run the application.

## Configuration

DisasterConnect uses environment variables to separate environment-specific configuration from application source code.

The repository includes an `.env.example` file that documents the expected configuration values. This file is a reference template; it does not automatically load environment variables into a running Java application.

The following variables are used by the project's configuration template:

| Variable | Purpose |
|---|---|
| `DB_URL` | JDBC connection URL for the MySQL database |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `SERVER_PORT` | Port on which the application listens |
| `JWT_SECRET` | Secret used for JWT signing, if enabled |
| `JWT_EXPIRATION_MS` | Token expiration duration in milliseconds, if enabled |

Set these variables in your local shell or IDE run configuration, and ensure that `application.properties` references them using the appropriate Spring configuration syntax.

For local development, create a database matching the name in your configured JDBC URL. Confirm that the database exists and that the configured user has the required permissions.

Never commit actual database passwords, production credentials, or JWT signing secrets to the repository.

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/DisasterConnect.git
cd DisasterConnect
```

Replace `YOUR_USERNAME` with the GitHub account that hosts the repository.

### 2. Configure the Database

Start your MySQL server and create the database specified in `DB_URL`.

Configure the database connection details in your environment or IDE run configuration. Check `application.properties` and `schema.sql` to understand how the application expects its database to be configured and initialized.

### 3. Configure Environment Variables

Provide the required database and application settings before starting the application.

The `.env.example` file documents the expected variable names. If you use a `.env` file locally, ensure your application or development tooling explicitly loads it; Spring Boot does not automatically load arbitrary `.env` files in every setup.

Use a secure, randomly generated JWT secret if token-based authentication is enabled.

### 4. Build the Application

If Maven is installed, run:

```bash
mvn clean package
```

If the project includes the Maven Wrapper, use the appropriate command for your operating system:

macOS or Linux:

```bash
./mvnw clean package
```

Windows:

```bat
mvnw.cmd clean package
```

### 5. Run the Application

With Maven installed:

```bash
mvn spring-boot:run
```

Or, using the Maven Wrapper on macOS or Linux:

```bash
./mvnw spring-boot:run
```

The application will start on the configured server port, provided that the required configuration is valid and the database is accessible.

If no alternative port is configured, Spring Boot commonly uses port `8080`.

### 6. Access the Application

Once the application has started successfully, open the following address in a browser:

```text
http://localhost:8080
```

The exact page and available API endpoints depend on the application's current controller mappings and static resources.

## Testing

The project contains a `src/test/` directory for automated tests.

Run the test suite with Maven:

```bash
mvn test
```

Or, if using the Maven Wrapper:

```bash
./mvnw test
```

Review the test results to verify application behavior and identify configuration or implementation issues before deployment.

## Security Considerations

DisasterConnect includes a dedicated security package and configuration layer. Security behavior depends on the components and authorization rules currently implemented.

The following practices should be followed during development and deployment:

- Keep credentials and signing secrets outside version control.
- Use environment-specific configuration rather than hardcoded production values.
- Generate strong, unpredictable JWT signing secrets when JWT authentication is enabled.
- Validate incoming request data and apply appropriate authorization checks.
- Use HTTPS for deployments that handle user accounts or sensitive information.
- Avoid exposing sensitive personal information through API responses or application logs.
- Verify authentication, authorization, and error-handling behavior before production use.

## Current Scope and Future Development

DisasterConnect is intended to provide a foundation for coordinated disaster response and access to relief services.

Potential areas for further development include:

- More comprehensive shelter and relief resource management.
- Improved coordination workflows for NGOs and volunteers.
- Real-time updates about resource availability.
- Location-based discovery of nearby assistance.
- Notifications for relevant disaster-related updates.
- Additional automated tests, monitoring, and deployment infrastructure.

These are possible extensions rather than claims about features already implemented.

## Project Documentation

The repository includes supporting documentation for the software engineering lifecycle:

- **Software Requirements Specification (SRS):** Defines the project's requirements and expected behavior.
- **Design Document:** Describes the proposed system design and architectural decisions.

Refer to these documents alongside the source code for further information about the project's requirements and design.

## Contributing

Contributions and suggestions that improve the application's reliability, usability, security, or maintainability are welcome.

For proposed changes, keep modifications focused, follow the existing package organization, and add or update tests where appropriate.

## Disclaimer

DisasterConnect is an academic software engineering project intended to explore disaster response coordination and relief service accessibility. It should not be treated as an official emergency response service unless it has been independently validated, deployed with appropriate operational support, and integrated with reliable information sources.

In an actual emergency, contact the appropriate local emergency services and follow official guidance.

## License

No license has been specified yet. Unless a license is added to the repository, the project's source code should not be assumed to be available for unrestricted reuse or redistribution.

---

**Developed as an academic software engineering project at IIIT Pune.**
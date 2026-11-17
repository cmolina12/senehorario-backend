# Senéhorario Backend

> [!IMPORTANT]
> **This backend is retired.** Senéhorario is now a single Next.js app that includes the course API and schedule generation: [cmolina12/senehorario](https://github.com/cmolina12/senehorario), live at [senehorario.cmolina.xyz](https://senehorario.cmolina.xyz). This repository is archived for reference.

This is the backend service for **Senéhorario**, a web application designed to help students at Uniandes build optimal class schedules.

The backend is built with Spring Boot and is responsible for retrieving course data from the Uniandes public catalog, processing it, and exposing a REST API that the frontend consumes.

## Current Features

- Fetch raw course data from the Uniandes API.
- Convert the raw data into domain objects (`Course`, `Section`, `Meeting`).
- Generate all possible schedules given candidate course sections.
- Simple REST endpoints implemented with Spring Web.

## Requirements

- Java 17+
- Maven 3 (a wrapper `mvnw` is provided)

## Building and Running

To build the project and run the application locally:

```bash
# build the project
./mvnw package

# run the Spring Boot application
./mvnw spring-boot:run
```

The application starts `SenehorarioBackendApplication` which excludes automatic database configuration and simply launches the REST endpoints.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

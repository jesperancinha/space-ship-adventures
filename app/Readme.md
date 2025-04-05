# app

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

-   [Ktor Documentation](https://ktor.io/docs/home.html)
-   [Ktor GitHub page](https://github.com/ktorio/ktor)
-   The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need to [request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up) to join.

## Features

Here's a list of features included in this project:

| Name                                                                   | Description                                                                        |
| ------------------------------------------------------------------------|------------------------------------------------------------------------------------ |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [Routing](https://start.ktor.io/p/routing)                             | Provides a structured routing DSL                                                  |
| [Sessions](https://start.ktor.io/p/ktor-sessions)                      | Adds support for persistent sessions through cookies or headers                    |
| [Shutdown URL](https://start.ktor.io/p/shutdown-url)                   | Enables a URL that shuts down the server when accessed                             |
| [Koin](https://start.ktor.io/p/koin)                                   | Provides dependency injection                                                      |
| [Micrometer Metrics](https://start.ktor.io/p/metrics-micrometer)       | Enables Micrometer metrics in your Ktor server application.                        |
| [Metrics](https://start.ktor.io/p/metrics)                             | Adds supports for monitoring several metrics                                       |
| [Swagger](https://start.ktor.io/p/swagger)                             | Serves Swagger UI for your project                                                 |
| [OpenAPI](https://start.ktor.io/p/openapi)                             | Serves OpenAPI documentation                                                       |
| [Server-Sent Events (SSE)](https://start.ktor.io/p/sse)                | Support for server push events                                                     |
| [Status Pages](https://start.ktor.io/p/status-pages)                   | Provides exception handling for routes                                             |
| [Postgres](https://start.ktor.io/p/postgres)                           | Adds Postgres database to your application                                         |
| [Exposed](https://start.ktor.io/p/exposed)                             | Adds Exposed database to your application                                          |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                          | Description                                                          |
| -------------------------------|---------------------------------------------------------------------- |
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build everything                                                     |
| `buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `buildImage`                  | Build the docker image to use with the fat JAR                       |
| `publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `run`                         | Run the server                                                       |
| `runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## About me

[![GitHub followers](https://img.shields.io/github/followers/jesperancinha.svg?label=Jesperancinha&style=for-the-badge&logo=github&color=grey "GitHub")](https://github.com/jesperancinha)

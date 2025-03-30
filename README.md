# space-ship-adventures
Using project arrow in similar to real-life scenarios

This project uses [Gradle](https://gradle.org/).
To build and run the application, use the *Gradle* tool window by clicking the Gradle icon in the right-hand toolbar,
or run it directly from the terminal:

* Run `./gradlew run` to build and run the application.
* Run `./gradlew build` to only build the application.
* Run `./gradlew check` to run all checks, including tests.
* Run `./gradlew clean` to clean all build outputs.

Note the usage of the Gradle Wrapper (`./gradlew`).
This is the suggested way to use Gradle in production projects.

[Learn more about the Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).

[Learn more about Gradle tasks](https://docs.gradle.org/current/userguide/command_line_interface.html#common_tasks).

This project follows the suggested multi-module setup and consists of the `app` and `utils` subprojects.
The shared build logic was extracted to a convention plugin located in `buildSrc`.

This project uses a version catalog (see `gradle/libs.versions.toml`) to declare and version dependencies
and both a build cache and a configuration cache (see `gradle.properties`).

## References

- https://www.kodeco.com/11593767-functional-programming-with-kotlin-and-arrow-algebraic-data-types
- https://old.arrow-kt.io/docs/0.12/patterns/error_handling/
- https://rockthejvm.com/articles/functional-error-handling-in-kotlin-part-2-result-and-either?utm_source=chatgpt.com
- https://www.codemotion.com/magazine/frontend/mobile-dev/discover-arrow-functional-programming-in-kotlin-and-more/

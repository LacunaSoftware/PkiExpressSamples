# PKI Express - Java sample project

This project shows how to use [PKI Express](https://docs.lacunasoftware.com/articles/pki-express/) on Java using the [Spring MVC](http://spring.io/) framework.

> To use PKI Express, you'll need a license file. Please [contact us](https://www.lacunasoftware.com/en/home/purchase)
> to get a free trial license.

### Running with Gradle

1. [Install PKI Express](https://docs.lacunasoftware.com/articles/pki-express/setup/)

1. [Download the project](https://github.com/LacunaSoftware/PkiExpressSamples/archive/master.zip) or clone the repository

1. In a command prompt, navigate to the folder `Java` and run the command `gradlew bootRun` (on Linux `./gradlew bootRun`).
   If you are using Windows, you can alternatively double-click the file `Run-Sample.bat`.

1. Once you see the message "Started Application in x.xxx seconds" (the on-screen percentage
   will *not* reach 100%), open a web browser and go the URL [http://localhost:60833/](http://localhost:60833/)

> If you are using a Spring Boot version lower than 1.3.x, you need to run the task `run` instead of `bootRun`.

### Running with Maven
1. [Install PKI Express](../setup/index.md)

1. [Download the project](https://github.com/LacunaSoftware/PkiExpressSamples/archive/master.zip) or clone the [repository](https://github.com/LacunaSoftware/PkiExpressSamples.git)

1. In a command prompt, navigate to the folder `Java` and run the command `mvn spring-boot:run`.

1. Once you see the message "Started Application in x.xxx seconds" (the on-screen percentage
   will *not* reach 100%), open a web browser and go the URL [http://localhost:60833/](http://localhost:60833/)

## Documentation

https://docs.lacunasoftware.com/articles/pki-express/java/

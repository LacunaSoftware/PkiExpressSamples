# PKI Express - Java sample project

This project shows how to use PKI Express on Java using the [Spring MVC](http://spring.io/) framework.

## Running the project

1. [Install PKI Express](https://docs.lacunasoftware.com/articles/pki-express/setup/)

1. [Download the project](https://github.com/LacunaSoftware/PkiExpressSamples/archive/master.zip) or clone the repository

1. Copy your license file **LacunaPkiLicense.config** to a directory readable by the application

1. Fill the path to the license file on the class [sample/util/Util.java](src/main/java/sample/util/Util.java)

1. In a command prompt, navigate to the folder `Java` and run the command `gradlew run` (on Linux `./gradlew run`).
   If you are using Windows, you can alternatively double-click the file `Run-Sample.bat`.

1. Once you see the message "Started Application in x.xxx seconds" (the on-screen percentage
   will *not* reach 100%), open a web browser and go the URL [http://localhost:8080/](http://localhost:8080/)

## Documentation

https://docs.lacunasoftware.com/pt-br/articles/pki-express/java/

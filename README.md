# Spring URL Validator Application

This repo contains the project **URL Validator App**. 

The project URL Validator is a Spring Boot which checks whether a URL exists or not.

-   If the URL exists then it shows this URL has been searched n number of times along with a hyperlink
    
-   If a URL doesn't exist then it checks for its parent URL. For example, if  `github.com/shubhamshukla7794/pull-requests` doesn't exist or is down then it checks for `github.com/shubhamshukla7794/`. If none exists then it shows the error message with number of searches or if this is the first search for this URL then it shows the message "You may have mistyped the URL"

## Running My Recipe App locally
URL Validator is a [Spring Boot](https://spring.io/guides/gs/spring-boot) application built using [Maven](https://spring.io/guides/gs/maven/). You can build a jar file and run it from the command line:


```
git clone https://github.com/shubhamshukla7794/URLValidator.git
cd URLValidator
./mvnw package
java -jar target/*.jar
```

You can then access the application here: http://localhost:8080/

## Working with Recipe App in your IDE

### Prerequisites
The following items should be installed in your system:
* Java 11 or newer.
* Your preferred IDE.

## Database configuration

In its default configuration, URL Validator App uses an in-memory database (H2) which
gets populated at startup with data. The h2 console is automatically exposed at `http://localhost:8080/h2-console`
and it is possible to inspect the content of the database using the `jdbc:h2:mem:testdb` url.


## Application

**URL Validator Application**

https://user-images.githubusercontent.com/37581959/168570054-a5e9da1e-9a94-428c-b661-33d71f4ddc72.mp4



**URL Validator - Various Modes of URL**

https://user-images.githubusercontent.com/37581959/168570091-cbf37b9b-041b-4730-80cd-498c06c63d36.mp4



**Error pages**

https://user-images.githubusercontent.com/37581959/168570141-a12eb331-0b99-47fc-bc88-9e42d0affaec.mp4



##  Author
Project created by :
**SHUBHAM KUMAR SHUKLA**

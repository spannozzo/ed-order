# ED Order App

Creation of a Rest order CRUD application with Docker, Quarkus, Swagger, open API 3, Keycloak, Kafka, Circuit Breaker and Postgres db through Maven, Quarkus and Eclipse Microprofile specification

## Description

The Rest application is managing Orders (with id, status, total amount) with Oauth2 token that is handling different type of users (admin,user). When a status of an order is changing, the main application is sending an event through Kafka broker to a separated logging app, that it will log the change of status, eg, from SHIPPED to DELIVERED

## Getting Started
After installation you need to import the Postman json file into Postman client, and use the pre-configured Rest operations. For authentication Postman will show you the keycloak authentication form. Use for admin: admin,admin, use for user: user,1234.
For testing the logger-app you need to run docker-compose logs -f logger-app in order to see incoming messages after a order status is changed.
For testing the fault tollerance you can run docker-compose stop db (after all the containers are already running properly), doing some  insertions and updatess, and finally restart the db with docker-compose start db. You should see the application displaying the same data, that will be saved into the database as soon it comes available.
### Dependencies

Docker
Kafka
Keycloak
Postgres DB
Maven
Quarkus
java 11

### Installing
from main folder you need to move to ed-order-app and run:

```
mvn clean package -Pnative -Dquarkus.native.container-build=true -DskipTests

```
while in the folder order-change-listener:
```
mvn clean package -DskipTests
```
After both jars are ready you have to run :
```
docker-compose up -d --build
```
and the application will start. 
If you want to run the tests you have to use a second docker db container, specific for tests:

```
docker run --rm -d -p 5432:5432 -e POSTGRES_USER=root -e POSTGRES_PASSWORD="1234" -e POSTGRES_DB=order_db_test --name order_db_test postgres:alpine

```
You need to stop the docker main application with docker-compose stop app and run in local the tests with 

```
mvn test -Dacme.messaging.enabled=false 
```
note that the keycloak container must be running correctly for running tests too

### Executing program
If the docker main app is stopped, you can run the app in local with
```
mvn quarkus:dev -Ddebug
```
Once the application is running properly on your local, you can access to swagger ui from

```
localhost:8080/swagger-ui-custom.html
```
In any case you need a token to authenticate the rest calls, so you can use Postman configuration and pass the token manually from the swagger-ui page. 
You can run also the application with remote debug from your IDE usin port 5005 .
The Opean API YAML file can be imported for generating Postman documentation, on Postman use import button and select open api file, then next. Postman will generate a collection of documented API call. 
A final copy of this file is already in the main folder of the application, however is better to use the additional postman files, which will provide also the token generation settings and all urls and ports already configured.

## Version History

* 0.0.1-SNAPSHOT
    * Initial Release



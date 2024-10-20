# Product-Management-Domagoj-Borovcak

Goal of this REST service is to have small CRUD 'Product' service
where price in USD has to be computed by HNB buying currency exchange rate.

## Internal storage
 * Postgres

## Information exposure

* REST API - http://localhost:8080/swagger-ui/index.html,
* Kafka
  * products 
    * CreatedProductEvent 
    * UpdatedProductEvent
    * DeletedProductEvent

## Run locally
* docker-compose up
* mvn spring-boot:run

## Run tests
 * mvn test

## Build image 
 * mvn spring-boot:build-image
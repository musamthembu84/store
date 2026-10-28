# Store Application
The Store application keeps track of customers and orders in a database.

# Assumptions
This README assumes you're using a posix environment. It's possible to run this on Windows as well:
* Instead of `./gradlew` use `gradlew.bat`
* The syntax for creating the Docker container is different. You could also install PostgreSQL on bare metal if you prefer


# Prerequisites
This service assumes the presence of a postgresql 16.2 database server running on localhost:5433 (note the non-standard port)
It assumes a username and password `admin:admin` can be used.
It assumes there's already a database called `store`

The simplest way to start it is via the `docker-compose.yml` in this repo:
```shell
docker compose up -d postgres
```

Alternatively, you can start a standalone container yourself:
```shell
docker run -d \
  --name postgres \
  --restart always \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  -e POSTGRES_DB=store \
  -v postgres:/var/lib/postgresql/data \
  -p 5433:5432 \
  postgres:16.2 \
  postgres -c wal_level=logical
```

# Running the application
There are two ways to run the application, depending on what you're doing.

**Local development** (fastest inner loop - recompiles and restarts quickly): start only Postgres via Docker, then run the app directly on your machine.
```shell
docker compose up -d postgres
./gradlew bootRun
```
The app needs Postgres already running and reachable at `localhost:5433` before `bootRun` is started - it does not start Postgres for you. If you see a `Connection to localhost:5433 refused` error, Postgres isn't up yet.

**Fully containerized** (matches what CI builds and publishes - use this to sanity-check the Docker image itself): build and run the app and Postgres together.
```shell
docker compose up --build -d
```

In both cases, the application uses Liquibase to migrate the schema. Some sample data is provided. You can create more data by reading the documentation in utils/README.md

# Data model
An order has an ID, a description, is associated with the customer which made the order, and contains 1 or more products.
A customer has an ID, a name, and 0 or more orders.
A product has an ID, a description, and can appear in 0 or more orders.

# API
Three resources are provided:
   * `/order` - GET (list, or a single order via `/order/{id}`), POST
   * `/customer` - GET (list, or filtered via `/customer?query=...` to match a substring of one word in the customer's name), POST
   * `/products` - GET (list, or a single product via `/products/{id}`), POST

The data model is circular - a customer owns a number of orders, an order refers back to the customer which owns it, and an order contains 1 or more products (a product can appear in many orders).
To avoid loops in the serializer, entities are mapped to DTOs containing truncated versions of their dependent objects (e.g. `CustomerOrderDTO`, `OrderCustomerDTO`, `OrderProductDTO`) rather than the full related entity.

The API is fully documented in `OpenAPI.yaml`, including request/response shapes, validation rules, and the error response shape used consistently across all endpoints.

# Tasks

1. Extend the order endpoint to find a specific order, by ID
2. Extend the customer endpoint to find customers based on a query string to match a substring of one of the words in their name
3. Users have complained that in production the GET endpoints can get very slow. The database is unfortunately not co-located with the application server, and there's high latency between the two. Identify if there are any optimisations that can improve performance
4. Add a new endpoint /products to model products which appear in an order:
      * A single order contains 1 or more products. 
      * A product has an ID and a description. 
      * Add a POST endpoint to create a product
      * Add a GET endpoint to return all products, and a specific product by ID
        * In both cases, also return a list of the order IDs which contain those products
      * Change the orders endpoint to return a list of products contained in the order

# Bonus points
1. Implement a CI pipeline on the platform of your choice to build the project and deliver it as a Dockerized image

Implemented via GitHub Actions (`.github/workflows/ci.yml`): every push and pull request runs `spotlessCheck` and the full test suite; on a push to `master`, the Docker image is additionally built, pushed to GitHub Container Registry, and scanned for vulnerabilities with Trivy. See `Dockerfile` and `docker-compose.yml` for the image build and local orchestration.

# Notes on the tasks
Assume that the project represents a production application.
Think carefully about the impact on performance when implementing your changes
The specifications of the tasks have been left deliberately vague. You will be required to exercise judgement about what to deliver - in a real world environment, you would clarify these points in refinement, but since this is a project to be completed without interaction, feel free to make assumptions - but be prepared to defend them when asked.
There's no CI pipeline associated with this project, but in reality there would be. Consider the things that you would expect that pipeline to verify before allowing your code to be promoted
Feel free to refactor the codebase if necessary. Bad choices were deliberately made when creating this project.

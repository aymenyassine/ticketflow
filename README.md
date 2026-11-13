# TicketFlow

**A hands-on reference project for learning Spring Cloud and microservices, built around a real-world ticket booking platform.**

TicketFlow is an open-source educational project that takes you from
"what is a microservice?" to building resilient, event-driven, observable
distributed systems with the Spring ecosystem.

Instead of yet another e-commerce demo, it tackles problems that naturally
require distributed-systems patterns: limited seat inventory, concurrent
bookings, unreliable payment providers, and asynchronous notifications.

## What you will learn

- **Service discovery & routing**: Eureka and Spring Cloud Gateway
- **Centralized configuration**: Spring Cloud Config with dynamic refresh
- **Inter-service communication**: declarative REST clients with OpenFeign
- **Resilience**: circuit breaker, retry, timeout, bulkhead and fallbacks (Resilience4J)
- **Event-driven architecture**: asynchronous messaging with Apache Kafka
- **Distributed transactions**: Saga pattern, Outbox pattern, idempotency
- **Containerization**: Docker and Docker Compose
- **AI integration** *(later)*: natural-language search and RAG assistant with Spring AI

## Architecture

| Module | Role | Key technology | Status |
|---|---|---|---|
| `customer-service` | Customer profiles (CRUD) | Spring Web, JPA | Done |
| `gateway-server` (`api-gateway` in Eureka) | Single entry point and routing | Spring Cloud Gateway | Done |
| `discovery-server` | Service registry | Eureka | Done |
| `event-service` | Events and seats catalog | Spring Web, JPA | Done |
| `conf-server` (`config-server` application) | Centralized configuration | Spring Cloud Config | Done |
| `booking-service` | Booking and seat locking | OpenFeign, Resilience4J | Planned |
| `payment-service` | Simulated payment provider (random failures) | Resilience4J | Planned |
| `notification-service` | Confirmation emails/SMS | Kafka consumer | Planned |
| `ai-assistant-service` | Natural-language search, recommendations | Spring AI | Later |

## Roadmap

Each step has a Git tag and a lesson in `docs/lessons/`.

| Tag | Content |
|---|---|
| `step-00-setup` | Parent POM, repo structure, CI |
| `step-01-customer-service` | First business service |
| `step-02-gateway-static` | Gateway with static routes |
| `step-03-discovery` | Eureka + load-balanced routing |
| `step-04-event-service` | Second business service |
| `step-05-config` | Config Server |
| `step-06-booking-feign` | booking-service + OpenFeign |
| `step-07-resilience` | payment-service + Resilience4J |
| `step-08-kafka` | Events + notification-service |
| `step-09-saga` | Saga, Outbox, idempotency |
| `step-10-security` | Keycloak (customer-service integration) |
| `step-11-observability` | Tracing and metrics |
| `step-12-ai` | Spring AI |

To follow a lesson, check out its tag: `git checkout step-01-customer-service`.

## Tech stack

Java 21 · Spring Boot 4 · Spring Cloud 2025.1 · Resilience4J · Apache Kafka ·
PostgreSQL · Docker / Docker Compose · Spring AI (later)

## Repository structure

    ticketflow/
    ├── docs/            # lessons, ADRs, templates
    ├── infra/           # docker-compose and infrastructure config
    ├── config-repo/     # configuration served by the config server
    └── services/        # one folder per microservice, each with its own README

Every module is independently runnable and testable, and documents itself in
its own `README.md`.

## Getting started

Prerequisites: JDK 21, Maven (or the included wrapper), Docker.

    git clone https://github.com/<your-user>/ticketflow.git
    cd ticketflow
    mvn clean verify

## Run MySQL with Docker

Only MySQL runs in Docker. The Spring services run locally and connect to it on
`localhost:3307` (Docker maps host port 3307 to MySQL port 3306):

    docker compose -f infra/docker-compose.yml up -d mysql

The `customer_service`, `booking_service`, and `event_service` schemas are
initialized automatically. MySQL data persists in the `mysql_data` volume.

## Centralized configuration

Config Server reads the separate `ticketflow-config` Git repository on its
`main` branch. It expects `application.yml`, `customer-service.yml`, and
`api-gateway.yml` at the repository root. Publish configuration changes to
GitHub before using the remote URI; unpushed local commits are not available
to a remote Config Server.

From the TicketFlow root, start the services in separate terminals, in this order:

    export CONFIG_GIT_URI=https://github.com/aymenyassine/ticketflow-config.git
    ./mvnw -pl services/conf-server spring-boot:run
    ./mvnw -pl services/Discovery-server spring-boot:run
    ./mvnw -pl services/customer-service spring-boot:run
    ./mvnw -pl services/gateway-server spring-boot:run

Config Server runs on 8888; the clients require it at startup. Check the
configurations and the refreshable message with:

    curl http://localhost:8888/customer-service/default/main
    curl http://localhost:8888/api-gateway/default/main
    curl http://localhost:8081/api/config/message

After a change to `ticketflow.message` is committed and pushed, refresh the
running customer instance with `curl -X POST http://localhost:8081/actuator/refresh`.
Keep secrets out of the configuration repository.

## Who is this for?

Java developers who know Spring Boot basics and want to move into
microservices, and anyone preparing for backend or architecture interviews.

<!--
## Contributing

Contributions, issues and exercises are welcome. A `CONTRIBUTING.md` will be
added soon.
-->

## License

To be defined (MIT recommended for an educational project).

## Author

**AHAIDOUS Khadija**   | [GitHub](https://github.com/kahaidous>) · [LinkedIn](https://www.linkedin.com/in/khadijaahaidous)


## Réalisation

**Ticket TF-002 — Environnement Docker local**  
**Responsable — Adam Zaghi**

### Éléments ajoutés

- Apache Kafka `4.1.2` en mode KRaft avec un broker local.
- Deux adresses de connexion Kafka, `localhost:9092` depuis les applications Java locales et `kafka:29092` depuis le réseau Compose.
- Keycloak `26.8.2` en mode développement, accessible sur `http://localhost:8180`.
- Volumes persistants `kafka_data` et `keycloak_data`.
- Healthchecks pour vérifier la disponibilité de Kafka et Keycloak.
- Service `kafka-init` qui attribue les permissions du volume à l’utilisateur Kafka avant son démarrage.

Les microservices Spring Boot sont lancés localement depuis IntelliJ.

### Configuration locale

Créer `infra/.env` à partir du fichier `infra/.env.example`, puis renseigner les paramètres suivants.

```dotenv
KC_ADMIN_USERNAME=admin
KC_ADMIN_PASSWORD=choisir-un-mot-de-passe-local
```

Le fichier `.env` doit être exclu de Git. Le fichier `.env.example` fournit le modèle partagé avec l’équipe.

### Démarrage et vérification

Exécuter les commandes depuis la racine du projet.

Vérifier la configuration Compose.

```bash
docker compose --env-file infra/.env -f infra/docker-compose.yml config --quiet
```

Démarrer Kafka et Keycloak.

```bash
docker compose --env-file infra/.env -f infra/docker-compose.yml up -d kafka keycloak
```

Afficher l’état des services, y compris le service d’initialisation.

```bash
docker compose --env-file infra/.env -f infra/docker-compose.yml ps -a
```

Les états attendus sont `Exited (0)` pour `kafka-init` et `healthy` pour Kafka et Keycloak.

Consulter les logs.

```bash
docker compose --env-file infra/.env -f infra/docker-compose.yml logs --tail=100 kafka keycloak
```

Arrêter les deux composants.

```bash
docker compose --env-file infra/.env -f infra/docker-compose.yml stop kafka keycloak
```

Les volumes conservent les données après l’arrêt. La commande `down -v` supprime les volumes du projet et leurs données.

### Incident rencontré et correction

Kafka rencontrait une erreur `AccessDeniedException` lors de l’écriture dans `/tmp/kraft-combined-logs`. L’utilisateur du conteneur, identifié par UID/GID `1000:1000`, ne disposait pas des permissions nécessaires sur le volume.

Le service `kafka-init` applique automatiquement la commande suivante avec les droits nécessaires.

```bash
chown -R 1000:1000 /tmp/kraft-combined-logs
```

Kafka attend la réussite de cette initialisation grâce à la condition `service_completed_successfully`.

### Validation avant revue

- [ ] Configuration Compose validée sans erreur.
- [ ] Kafka démarre et devient `healthy`.
- [ ] Un message de test est publié puis consommé.
- [ ] Keycloak démarre et devient `healthy`.
- [ ] Connexion à la console Keycloak réussie.
- [ ] Données Kafka et Keycloak conservées après redémarrage.
- [ ] `.env` ignoré par Git et `.env.example` disponible.
- [ ] Configuration MySQL existante conservée dans le Compose partagé.
- [ ] Procédure de démarrage reproduite par un autre membre.
- [ ] Pull Request soumise à Ayman pour validation.

**Lien de la Pull Request —** à compléter  
**Résultats des tests et remarques —** à compléter

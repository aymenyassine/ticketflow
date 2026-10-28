# Booking Service

Version simple et locale pour preparer le prochain TP.
Structure : entities, repos, dto, service, controllers et exceptions.

Le service enregistre les reservations et leurs lignes avec JPA/H2.
Il permet la creation, la consultation, la confirmation manuelle, l'annulation et l'expiration apres 15 minutes.
Il n'appelle ni Customer Service ni Event Service. Les identifiants fournis sont enregistres sans verification distante.
Aucune place n'est retiree ou restituee dans Event Service.
Le prix unitaire est fourni dans la requete pour cette etape pedagogique ; il faudra recuperer le vrai prix du catalogue pendant le TP.

## API

| Methode | Chemin | Action |
| --- | --- | --- |
| POST | /api/v1/bookings | Creer |
| GET | /api/v1/bookings/{id} | Consulter |
| GET | /api/v1/bookings/customer/{customerId} | Reservations d'un client |
| POST | /api/v1/bookings/{id}/confirm | Confirmer |
| POST | /api/v1/bookings/{id}/cancel | Annuler |

Exemple de creation :

```json
{
  "customerId": 1,
  "eventId": "11111111-1111-1111-1111-111111111111",
  "ticketTierId": "22222222-2222-2222-2222-222222222222",
  "quantity": 2,
  "unitPrice": 100
}
```

Le total calcule est 200. La quantite doit etre comprise entre 1 et 10 et le prix doit etre positif ou nul.
La base locale H2 est en memoire. Les donnees disparaissent au redemarrage.
Config Server et Eureka restent configures comme dans les autres services.
Le port local par defaut est 8084 ; la configuration centralisee peut le remplacer.

Depuis la racine du projet :

```powershell
mvn -f services/booking-service/pom.xml spring-boot:run
```

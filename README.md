# Client Service

This microservice handles client-related operations, including registration, authentication, and balance management.

## Environment Variables

The application can be configured using the following environment variables (with default values provided):

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `SERVER_PORT` | The port on which the service runs | `7015` |
| `DB_DRIVER` | JDBC driver for the database | `org.postgresql.Driver` |
| `DB_URL` | JDBC URL for the PostgreSQL database | `jdbc:postgresql://localhost:5432/micr-synch` |
| `DB_USERNAME` | Database username | `qwe` |
| `DB_PASSWORD` | Database password | `qwe` |
| `JWT_SECRET_KEY` | Secret key for JWT signing (Base64 encoded) | `AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA` |
| `ALLOWED_ORIGIN` | Allowed CORS origin | `http://localhost:3000` |

## API Documentation

The API base path is `/api/v1`.

### Authentication & Registration

- **Register Client**
  - `POST /clients/register`
  - Body: `RegRequest { mail: "user@example.com", password: "password" }`
  
- **Login**
  - `POST /auth/login`
  - Body: `AuthRequest { mail: "user@example.com", password: "password" }`
  - Returns: JWT token (String)

- **Validate Token**
  - `POST /auth/validate`
  - Header: `Authorization: Bearer <token>`

### Client Operations

- **Get My Info**
  - `GET /clients/me`
  - Header: `Authorization: Bearer <token>`
  - Returns: `ClientInfoDto`

- **Update My Address**
  - `PATCH /clients/me/address`
  - Header: `Authorization: Bearer <token>`
  - Query Param: `address` (String)

- **Add Balance**
  - `PATCH /clients/{clientId}/balance`
  - Query Param: `amount` (BigDecimal)

### Internal Transactions

- **Deduct Balance**
  - `POST /clients/transactions/deduct`
  - Body: `OrderDTO { clientID: Long, totalSum: BigDecimal }`

- **Restore Balance**
  - `POST /clients/transactions/restore`
  - Body: `OrderDTO { clientID: Long, totalSum: BigDecimal }`

## Interactions with "Worlds" (Services)

This service interacts with:
1.  **Database (PostgreSQL)**: Stores client information, credentials, and balance.
2.  **Order/Transaction Service (Inferred)**: Through the `/clients/transactions/deduct` and `/restore` endpoints, this service acts as a participant in distributed transactions (likely compensating transactions/Saga pattern).
3.  **Frontend**: Provides endpoints for client management and authentication, supporting CORS for the configured origin.

## Tech Stack
- Java 21+
- Spring Boot 3
- Spring Security (JWT)
- Flyway (Database Migrations)
- PostgreSQL

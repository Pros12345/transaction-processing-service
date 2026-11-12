# Idempotent Background Transaction Processing Service

## 1. Project Overview

This project implements an idempotent background transaction processing service using Java 17 and Spring Boot.

The application accepts financial transaction requests through REST APIs and processes them asynchronously using a scheduled background processor.

The system handles:
- Transaction ingestion and validation
- Duplicate transaction detection
- Out-of-order transaction processing
- Background transaction execution
- Transient failure simulation
- Retry handling and maximum retry limits
- Account balance management
- Transaction processing status reporting

## 2. Technology Stack

- Java 17
- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate
- Maven
- H2 In-Memory Database
- Lombok
- JUnit 5
- Mockito
- Postman

## 3. Application Architecture

The application follows a layered architecture.

### Main Components

| Component | Responsibility |
|---|---|
| Controller | Accepts REST API requests |
| Transaction Service | Handles transaction ingestion and coordination |
| Transaction Worker Service | Executes transaction business logic |
| Repository | Performs database operations |
| Entity | Represents transaction and account records |
| Scheduler | Triggers background processing periodically |
| DTO | Handles API request and response data |
| Enum | Defines transaction types and statuses |

### Transaction Processing Flow

1. A client submits a transaction using the REST API.
2. The application validates the incoming request.
3. The service checks whether the transaction ID already exists.
4. A new transaction is stored with RECEIVED status.
5. The background scheduler identifies transactions ready for processing.
6. The worker processes transactions according to account sequence numbers.
7. The account balance is updated after successful processing.
8. The transaction status is updated to PROCESSED or an appropriate failure status.
9. Processing results are available through the summary API.

## 4. Transaction Status Lifecycle

The application supports the following transaction statuses:

| Status | Description |
|---|---|
| RECEIVED | Transaction has been successfully received |
| PROCESSING | Transaction is currently being processed |
| PROCESSED | Transaction has been processed successfully |
| DUPLICATE | Transaction ID already exists |
| FAILED | Transaction processing has permanently failed |
| RETRY_PENDING | Transaction is waiting for another processing attempt |
| PENDING | Transaction is waiting for an earlier sequence |

## 5. Idempotency and Duplicate Detection

The transactionId is used as the unique business identifier for each transaction.

Before saving a transaction, the application checks whether the transaction ID already exists.

If the transaction already exists:
- A duplicate transaction is detected.
- The API returns DUPLICATE status.
- A second transaction record is not created.
- The account balance is not modified.

The database also enforces uniqueness on the transaction ID.

## 6. Out-of-Order Transaction Processing

The application uses sequence-based processing for each account.

Each account maintains a `lastProcessedSequence` value.

The next expected sequence is calculated as:

`lastProcessedSequence + 1`

Processing rules:

- If the incoming sequence matches the expected sequence, the transaction can be processed.
- If the incoming sequence is greater than expected, the transaction is marked PENDING.
- If an earlier sequence arrives, the missing transaction can be processed first.
- Subsequent transactions can then be processed in sequence order.

This approach maintains transaction ordering at the account level.

## 7. Transaction Processing and Business Logic

The application supports the following transaction types:

- CREDIT: Adds the transaction amount to the account balance.
- DEBIT: Subtracts the transaction amount after checking sufficient balance.
- REVERSAL: Requires an original transaction reference; reversal processing is not fully implemented in the current version.

Account balance updates are performed as part of transaction processing.

## 8. Retry and Failure Handling

The application supports simulated transient processing failures.

A retry counter tracks the number of processing attempts.

The maximum retry count is three attempts.

Processing behavior:

1. A transient failure is simulated.
2. The retry counter is incremented.
3. The transaction is marked RETRY_PENDING if another attempt is permitted.
4. The background processor attempts the transaction again.
5. After the maximum retry count is exhausted, the transaction is marked FAILED.

The account balance is updated only after successful transaction processing.

## 9. REST API Endpoints

### Submit a Transaction

**HTTP Method:** POST

**Endpoint:**

`http://localhost:8080/api/transactions`

**Sample Request:**

```json
{
  "transactionId": "TXN-1001",
  "requestId": "REQ-1001",
  "accountId": "ACC-100",
  "transactionType": "CREDIT",
  "amount": 1000.00,
  "currency": "INR",
  "sequenceNumber": 1
}
```

**Sample Response:**

```json
{
  "transactionId": "TXN-1001",
  "message": "Transaction received successfully",
  "status": "RECEIVED"
}
```

### Get Processing Summary

**HTTP Method:** GET

**Endpoint:**

`http://localhost:8080/api/transactions/summary`

Returns the number of transactions grouped by their processing status.

## 10. Database Configuration

The application uses an H2 in-memory database.

### H2 Console

`http://localhost:8080/h2-console`

### Database Connection Details

| Property | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:transactiondb` |
| Username | `sa` |
| Password | Blank |
| Database | H2 In-Memory |

**Note:** The in-memory database is reset when the application stops.

## 11. Running the Application

### Prerequisites

- Java 17
- Maven
- Eclipse IDE or another Java IDE

### Clone the Repository

```bash
git clone https://github.com/Pros12345/transaction-processing-service.git
```

### Navigate to the Project

```bash
cd transaction-processing-service
```

### Build the Application

```bash
mvn clean install
```

### Run the Application

```bash
mvn spring-boot:run
```

The application starts on:

`http://localhost:8080`

## 12. Automated Testing

The application includes automated tests using JUnit 5 and Mockito.

### Test Classes

| Test Class | Test Scenarios | Test Count |
|---|---|---:|
| TransactionServiceTest | Successful ingestion and duplicate detection | 2 |
| TransactionValidationTest | Negative amount, missing transaction ID and valid request | 3 |
| TrpZEAWYtiB6bJ16NuLbGCc6CZ6jJdKfb63 | Out-of-order handling, retry success and retry exhaustion | 3 |
| **Total** | | **8** |

### Run Automated Tests

```bash
mvn clean test
```

### Latest Test Execution Results

- Tests run: 8
- Failures: 0
- Errors: 0
- Skipped: 0
- Build status: SUCCESS

## 13. Sample Processing Results

The application was manually tested using Postman and verified through the H2 database.

### Transaction Processing Results

| Scenario | Result |
|---|---|
| Valid transaction | RECEIVED |
| Duplicate transaction | DUPLICATE |
| Negative transaction amount | HTTP 400 Bad Request |
| Missing mandatory fields | HTTP 400 Bad Request |
| Out-of-order transaction | PENDING until earlier sequence is processed |
| Successful transaction | PROCESSED |
| Insufficient balance | FAILED |
| Transient processing failure | RETRY_PENDING |
| Maximum retry attempts exhausted | FAILED |

### Verified Account Balance Example

For account `ACC-100`:

| Transaction | Type | Amount |
|---|---|---:|
| TXN-1001 | CREDIT | 1000.00 |
| TXN-1002 | DEBIT | 400.00 |
| TXN-1003 | CREDIT | 100.00 |
| TXN-1004 | CREDIT | 200.00 |

Final account balance: **900.00 INR**

Last processed sequence: **4**

## 14. Logging and Monitoring

The application uses logging to report:

- Background processing execution
- Transaction processing success
- Transient processing failures
- Retry attempts
- Maximum retry exhaustion
- Transaction processing errors

The processing summary API provides transaction counts grouped by status.

## 15. Design Trade-offs and Production Improvements

### Current Implementation

- H2 in-memory database for lightweight local execution.
- Scheduled background processing instead of a distributed message queue.
- Sequence-based ordering for account transactions.
- Simulated transient failures for retry testing.
- Spring Data JPA for database persistence.

### Recommended Production Improvements

- Replace H2 with PostgreSQL or another persistent relational database.
- Introduce Kafka or RabbitMQ for reliable asynchronous message processing.
- Implement account-level concurrency control and distributed locking where required.
- Strengthen concurrent duplicate detection using database constraints and exception handling.
- Introduce structured logging, metrics and distributed tracing.
- Add dead-letter handling and operational recovery mechanisms.
- Implement reversal processing using an original transaction reference.
- Add authentication and authorization.
- Add integration and concurrency tests.

## 16. Conclusion

The project demonstrates an idempotent background transaction processing service with transaction validation, duplicate detection, sequence-based ordering, retry handling, account balance management and status reporting.

The implementation was verified through REST API testing, database queries and automated JUnit tests.

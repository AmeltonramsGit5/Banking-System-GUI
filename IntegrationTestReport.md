# Part B Integration Test Report

## Scope

The automated integration suite exercises the controller, domain objects, H2 JDBC DAOs, and transaction records together. It uses a separate in-memory H2 database for each logical test and includes one file-backed H2 test to verify that the file database can be used across DAO connections in an application session.

## Test cases and results

| Case | Expected result | Result |
|---|---|---|
| Unauthenticated balance request | Rejected before account data is returned | Passed |
| Customer deposit | Balance changes and a deposit history row is stored | Passed |
| Customer access to another customer's account | Balance and transaction requests are rejected | Passed |
| Savings withdrawal | Rejected by the account model | Passed |
| Investment opening deposit | BWP499.99 rejected; BWP500.00 accepted | Passed |
| Monthly interest | Applied once per calendar month and recorded in history | Passed |
| Teller-only registration | Customer role rejected; teller role accepted | Passed |
| Sample data | Ten customers and eleven sample accounts are inserted into an empty database | Passed |
| File-backed H2 | Schema, seed data, interest processing, and subsequent DAO reads work in the same app session | Passed |

## Execution record

Executed on 19 November 2026 with JDK 25.0.2 and Maven 3.10.1. `mvn clean test` completed with five tests, zero failures, zero errors, and zero skipped tests.

The JavaFX application also launched to its Bank Login screen using a fresh file-backed database. The UI typing automation was unavailable in this environment, so the login-to-deposit path was not exercised through clicks. The automated tests cover the controller, domain, persistence, authorization, and transaction history layers; perform one manual GUI flow before submission.
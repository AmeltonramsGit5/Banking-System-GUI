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
| Account and transaction table queries | Customer sees only their own accounts and history; teller sees all eleven accounts | Passed |

## Execution record

The original five test methods were executed on 19 November 2026 with JDK 25.0.2 and Maven 3.10.1. `mvn clean test` completed with five tests, zero failures, zero errors, and zero skipped tests.

On 20 November 2026, the new account and transaction table query test was added. All six JUnit test methods were then compiled and invoked with JDK 25.0.2, H2 2.5.252, and the JUnit 5.13.4 assertions; all six passed.

The JavaFX application launched successfully, but the GUI login and transaction controls were not clicked during this verification. The automated tests cover the controller, domain, persistence, authorization, account summary data, and transaction history data. Complete the read-only GUI smoke check listed in `ImplementationChecklist.md` before presentation.

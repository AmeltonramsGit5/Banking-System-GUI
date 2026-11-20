# Part B Implementation Checklist

This checklist covers the implementation requirements in Sections 4-8 of the assignment. It distinguishes completed code work from the remaining manual presentation check. Part A documentation and the final Turnitin/presentation submission are separate deliverables.

## 4. Core model

- [x] The project is connected to the GitHub repository at `AmeltonramsGit5/Banking-System-GUI`; the core application and database work are present in the repository history.
- [x] The core model has customers, accounts, and transaction records with encapsulated attributes.
- [x] `Account` is abstract; Savings, Investment, and Cheque accounts implement their distinct rules through inheritance and overriding.
- [x] `InterestBearing`, `AccountFactory`, and overloaded registration methods demonstrate interfaces, polymorphism, and overloading.
- [x] An account must belong to a customer, and customers can own multiple accounts.
- [x] The model enforces the assignment's account rules: savings accounts cannot be withdrawn from, investment accounts require BWP500 to open, and cheque accounts require an employed individual.
- [x] The seed data creates ten sample customers and eleven accounts.

## 5. GUI design and implementation

- [x] JavaFX boundary classes provide login, menu, customer registration, account, and deposit/withdraw screens.
- [x] Views send requests through `BankController`; JDBC calls remain in DAO classes and domain rules remain outside the views.
- [x] Account summary and transaction history results are displayed in JavaFX `TableView` controls.
- [x] The deposit/withdraw screen refreshes its transaction table after a successful transaction.
- [x] VS Code tasks now have Windows and Linux/macOS Maven Wrapper commands for running, building, and testing.

## 6. Controller implementation

- [x] `BankController` mediates between the boundary views, domain objects, and data access classes.
- [x] It validates requests and enforces login, teller permissions, and customer account ownership.
- [x] Account and transaction table queries use the same ownership checks as the banking actions.

## 7. Database connectivity

- [x] H2 is configured as a persistent local database, with schema for customers, users, accounts, transactions, and interest runs.
- [x] JDBC DAOs provide create, read, update, and delete operations for the stored records.
- [x] Account opening, deposits, withdrawals, and interest posting run in database transactions.
- [x] The schema and JDBC implementation are included in the existing GitHub commit.

## 8. Integration and testing

- [x] JavaFX views, controller, core model, and JDBC layer are wired into one application.
- [x] Integration coverage checks login, deposits, transaction persistence, ownership, account rules, registration, sample data, file-backed H2, and table query access.
- [x] `IntegrationTestReport.md` records the test cases and outcomes; all six JUnit test methods passed in the latest check.
- [ ] Complete a read-only GUI smoke check before presentation. The app is currently open on the customer registration form, which I left untouched. After deciding whether to keep or discard any draft in that form:
  1. Return to the menu and sign in as `john` if prompted.
  2. Open **Accounts**, select **Show Summary**, and confirm the two account rows appear.
  3. Enter `AC1001`, select **Transaction History**, and confirm its saved transactions appear in the table.
  4. Open **Deposit / Withdraw**, enter `AC1001`, and select **Show History** to confirm the saved rows are displayed there too.

## Separate assignment deliverables

- [ ] Part A requirements/interview documentation and UML diagrams are not in this implementation repository; prepare and submit them as the assignment specifies.
- [ ] Turnitin upload and the final presentation are student submission steps, not application code changes.

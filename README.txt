BANKING SYSTEM - JavaFX, MVC, JDBC, and H2

Project contents
  BankApp.java                 JavaFX application entry point
  *View.java                   Boundary classes for login, menu, customers, accounts, and transactions
  BankController.java          Controller and application workflow orchestration
  Customer.java                Customer domain model
  Account.java                 Abstract account model
  SavingsAccount.java          Savings account with 0.05% monthly interest and no withdrawals
  InvestmentAccount.java       Investment account with BWP500 minimum and 5% monthly interest
  ChequeAccount.java           Cheque account for employed individuals
  InterestBearing.java         Interface used by interest-bearing account types
  BankingTransaction.java      Immutable transaction history entry
  *DAO.java                    JDBC data access classes with CRUD operations
  Database.java                H2 connection and schema initialization
  database/schema.sql          Database schema
  src/test/java/               Controller-to-database integration tests
  IntegrationTestReport.md     Test cases and execution record

Build and run
  Requirement: JDK 25 or later. Internet is needed on first run to download Maven and dependencies.
  In PowerShell from this folder, run:
    .\mvnw.cmd test
    .\mvnw.cmd javafx:run
  The Maven Wrapper downloads Maven automatically if it is not already installed.
  In VS Code, use Terminal > Run Task > Run Banking System (JavaFX).
  Do not use the Java extension's plain Run Java action on BankApp; it omits the JavaFX runtime modules.

GitHub Codespaces
  Use the Java 25 environment for this project.
  In VS Code, open Terminal > Run Task and choose Build Banking System or Run Banking Integration Tests.
  The JavaFX run task uses the Maven Wrapper on Linux and macOS, and mvnw.cmd on Windows.
  A Codespace without a graphical display can build and test the application, but it cannot show the JavaFX window.
  Use desktop VS Code on a machine with a display when you need to demonstrate the GUI.

Demo logins
  teller / 1234
  john / 1234
  customer2 through customer10 / 1234

Demo data
  On an empty database, the app creates ten sample customers and eleven sample accounts.
  John owns AC1001 (Savings) and AC1002 (Investment); AC1003 belongs to another customer.
  File-backed H2 data is stored under ./data/banking.mv.db and is retained between runs.
  Override the database URL with the JVM property -Dbanking.jdbc.url=... if needed.

Business rules
  Savings accounts accrue 0.05% monthly interest and do not permit withdrawals.
  Investment accounts require an opening deposit of at least BWP500.00 and accrue 5% monthly interest.
  Cheque accounts allow deposits and withdrawals and are available only to employed individual customers.
  Interest is applied automatically once per calendar month when the app starts and is recorded as a transaction.
  Every transaction updates the balance and transaction history in one JDBC transaction.
  Customers can access only their own accounts. Teller-only actions are checked in the controller.

Design
  Views collect input and display results. BankController coordinates the UI, domain model, and DAO layer.
  Account subclasses implement behavior through inheritance and the InterestBearing interface.
  BigDecimal is used for balances and transaction amounts.
  Passwords are stored as salted PBKDF2 hashes. The simple demo passwords are for classroom use only.

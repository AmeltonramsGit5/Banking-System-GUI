import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Boundary class for account actions and read-only account information.
public class AccountView {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final BankApp app;
    private final BankController controller;
    private final ComboBox<String> cmbAccountType = new ComboBox<>();
    private final TextField txtInitialDeposit = new TextField();
    private final TextField txtAccountNo = new TextField();
    private final TableView<Account> tblAccounts = new TableView<>();
    private final TableView<BankingTransaction> tblTransactions = new TableView<>();
    private final TabPane outputTabs = new TabPane();
    private final Tab accountsTab = new Tab("Account summary");
    private final Tab transactionsTab = new Tab("Transaction history");
    private final Label lblAccountTotal = new Label("Total balance: BWP 0.00");
    private final Label lblMessage = new Label();

    public AccountView(BankApp app, BankController controller) {
        this.app = app;
        this.controller = controller;
    }

    public Scene getScene() {
        Label title = new Label("Accounts");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #244c70;");

        cmbAccountType.getItems().setAll("Savings", "Investment", "Cheque");
        cmbAccountType.setValue("Savings");
        cmbAccountType.setMaxWidth(Double.MAX_VALUE);
        txtInitialDeposit.setPromptText("0.00 or more");
        txtAccountNo.setPromptText("For example AC1001");

        Button btnOpen = new Button("Open Account");
        GridPane openForm = new GridPane();
        openForm.setHgap(10);
        openForm.setVgap(8);
        openForm.setPadding(new Insets(10));
        openForm.add(new Label("Account type:"), 0, 0);
        openForm.add(cmbAccountType, 1, 0);
        openForm.add(new Label("Initial deposit (BWP):"), 0, 1);
        openForm.add(txtInitialDeposit, 1, 1);
        openForm.add(btnOpen, 1, 2);
        TitledPane openPane = new TitledPane("Open a new account", openForm);
        openPane.setCollapsible(false);

        Button btnBalance = new Button("Check Balance");
        Button btnSummary = new Button("Show Summary");
        Button btnHistory = new Button("Transaction History");
        GridPane lookup = new GridPane();
        lookup.setHgap(10);
        lookup.setVgap(8);
        lookup.setPadding(new Insets(10));
        lookup.add(new Label("Account number:"), 0, 0);
        lookup.add(txtAccountNo, 1, 0);
        HBox lookupButtons = new HBox(8, btnBalance, btnSummary, btnHistory);
        lookupButtons.setAlignment(Pos.CENTER_LEFT);
        lookup.add(lookupButtons, 1, 1);
        TitledPane lookupPane = new TitledPane("Account information", lookup);
        lookupPane.setCollapsible(false);

        configureTables();
        VBox accountResults = new VBox(8, tblAccounts, lblAccountTotal);
        VBox.setVgrow(tblAccounts, Priority.ALWAYS);
        VBox transactionResults = new VBox(tblTransactions);
        VBox.setVgrow(tblTransactions, Priority.ALWAYS);
        accountsTab.setContent(accountResults);
        accountsTab.setClosable(false);
        transactionsTab.setContent(transactionResults);
        transactionsTab.setClosable(false);
        outputTabs.getTabs().setAll(accountsTab, transactionsTab);
        outputTabs.setPrefHeight(280);

        lblMessage.setWrapText(true);
        lblMessage.setStyle("-fx-text-fill: #9b3030;");
        Button btnBack = new Button("Back");
        HBox footer = new HBox(12, btnBack, lblMessage);
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox page = new VBox(12, title, openPane, lookupPane, outputTabs, footer);
        page.setPadding(new Insets(18));
        ScrollPane scroll = new ScrollPane(page);
        scroll.setFitToWidth(true);

        btnOpen.setOnAction(e -> {
            try {
                lblMessage.setText(controller.openAccount(
                        cmbAccountType.getValue(), new BigDecimal(txtInitialDeposit.getText().trim())));
            } catch (NumberFormatException ex) {
                lblMessage.setText("Please enter a valid deposit amount.");
            }
        });
        btnBalance.setOnAction(e -> lblMessage.setText(controller.getBalance(txtAccountNo.getText())));
        btnSummary.setOnAction(e -> showAccountSummary());
        btnHistory.setOnAction(e -> showTransactionHistory());
        btnBack.setOnAction(e -> app.showMenu());

        return new Scene(scroll, 900, 700);
    }

    private void configureTables() {
        tblAccounts.getColumns().setAll(
                accountColumn("Account Number", Account::getAccountNumber),
                accountColumn("Account Type", account -> account.getType().getLabel()),
                accountColumn("Branch", Account::getBranch),
                accountColumn("Balance (BWP)", account -> money(account.getBalance())));
        tblAccounts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblAccounts.setPlaceholder(new Label("Select Show Summary to display your accounts."));
        tblAccounts.setPrefHeight(235);

        tblTransactions.getColumns().setAll(
                transactionColumn("Date and time", tx -> tx.getOccurredAt().format(DATE_FORMAT)),
                transactionColumn("Type", tx -> tx.getType().toString().replace('_', ' ')),
                transactionColumn("Amount (BWP)", tx -> money(tx.getAmount())),
                transactionColumn("Balance after (BWP)", tx -> money(tx.getBalanceAfter())),
                transactionColumn("Description", BankingTransaction::getDescription));
        tblTransactions.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblTransactions.setPlaceholder(new Label("Enter an account number and select Transaction History."));
        tblTransactions.setPrefHeight(235);
    }

    private TableColumn<Account, String> accountColumn(String title, Function<Account, String> value) {
        TableColumn<Account, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private TableColumn<BankingTransaction, String> transactionColumn(
            String title, Function<BankingTransaction, String> value) {
        TableColumn<BankingTransaction, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private void showAccountSummary() {
        try {
            List<Account> accounts = controller.getVisibleAccounts();
            tblAccounts.setItems(FXCollections.observableArrayList(accounts));
            BigDecimal total = accounts.stream().map(Account::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
            lblAccountTotal.setText("Total balance: BWP " + money(total));
            outputTabs.getSelectionModel().select(accountsTab);
            lblMessage.setText(accounts.isEmpty() ? "No accounts found." : accounts.size() + " account(s) loaded.");
        } catch (RuntimeException ex) {
            tblAccounts.getItems().clear();
            lblMessage.setText(ex.getMessage());
        }
    }

    private void showTransactionHistory() {
        try {
            List<BankingTransaction> transactions = controller.getTransactionsForAccount(txtAccountNo.getText());
            tblTransactions.setItems(FXCollections.observableArrayList(transactions));
            outputTabs.getSelectionModel().select(transactionsTab);
            lblMessage.setText(transactions.isEmpty() ? "No transactions recorded." : transactions.size() + " transaction(s) loaded.");
        } catch (RuntimeException ex) {
            tblTransactions.getItems().clear();
            lblMessage.setText(ex.getMessage());
        }
    }

    private String money(BigDecimal amount) {
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}

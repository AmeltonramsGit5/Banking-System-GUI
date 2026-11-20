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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// Boundary class for deposits and withdrawals; the controller owns all rules.
public class TransactionView {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final BankApp app;
    private final BankController controller;
    private final TextField txtAccountNo = new TextField();
    private final TextField txtAmount = new TextField();
    private final TableView<BankingTransaction> tblTransactions = new TableView<>();
    private final Label lblMessage = new Label();

    public TransactionView(BankApp app, BankController controller) {
        this.app = app;
        this.controller = controller;
    }

    public Scene getScene() {
        Label title = new Label("Deposit / Withdraw");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #244c70;");
        txtAccountNo.setPromptText("For example AC1001");
        txtAmount.setPromptText("Amount in BWP");
        lblMessage.setWrapText(true);

        GridPane form = new GridPane();
        form.setAlignment(Pos.CENTER_LEFT);
        form.setHgap(10);
        form.setVgap(12);
        form.setPadding(new Insets(18));
        form.add(new Label("Account number:"), 0, 0);
        form.add(txtAccountNo, 1, 0);
        form.add(new Label("Amount (BWP):"), 0, 1);
        form.add(txtAmount, 1, 1);

        Button btnDeposit = new Button("Deposit");
        Button btnWithdraw = new Button("Withdraw");
        Button btnHistory = new Button("Show History");
        Button btnBack = new Button("Back");
        HBox actions = new HBox(8, btnDeposit, btnWithdraw, btnHistory);
        actions.setAlignment(Pos.CENTER_LEFT);

        configureTable();
        VBox.setVgrow(tblTransactions, Priority.ALWAYS);
        Label outputTitle = new Label("Saved transactions");
        outputTitle.setStyle("-fx-font-weight: bold;");

        btnDeposit.setOnAction(e -> transact(true));
        btnWithdraw.setOnAction(e -> transact(false));
        btnHistory.setOnAction(e -> loadHistory());
        btnBack.setOnAction(e -> app.showMenu());

        HBox footer = new HBox(12, btnBack, lblMessage);
        footer.setAlignment(Pos.CENTER_LEFT);

        VBox page = new VBox(12, title, form, actions, outputTitle, tblTransactions, footer);
        page.setPadding(new Insets(20));
        VBox.setVgrow(tblTransactions, Priority.ALWAYS);
        return new Scene(page, 850, 620);
    }

    private void configureTable() {
        tblTransactions.getColumns().setAll(
                column("Date and time", tx -> tx.getOccurredAt().format(DATE_FORMAT)),
                column("Type", tx -> tx.getType().toString().replace('_', ' ')),
                column("Amount (BWP)", tx -> money(tx.getAmount())),
                column("Balance after (BWP)", tx -> money(tx.getBalanceAfter())),
                column("Description", BankingTransaction::getDescription));
        tblTransactions.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblTransactions.setPrefHeight(280);
        tblTransactions.setPlaceholder(new Label("Enter an account number and select Show History."));
    }

    private TableColumn<BankingTransaction, String> column(
            String title, Function<BankingTransaction, String> value) {
        TableColumn<BankingTransaction, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

    private void transact(boolean deposit) {
        try {
            BigDecimal amount = new BigDecimal(txtAmount.getText().trim());
            String result = deposit
                    ? controller.deposit(txtAccountNo.getText(), amount)
                    : controller.withdraw(txtAccountNo.getText(), amount);
            lblMessage.setText(result);
            if (result.startsWith("Deposited.") || result.startsWith("Withdrawn.")) {
                loadHistory();
                lblMessage.setText(result);
            }
        } catch (NumberFormatException ex) {
            lblMessage.setText("Please enter a valid amount.");
        }
    }

    private void loadHistory() {
        try {
            List<BankingTransaction> transactions = controller.getTransactionsForAccount(txtAccountNo.getText());
            tblTransactions.setItems(FXCollections.observableArrayList(transactions));
            if (transactions.isEmpty()) lblMessage.setText("No transactions recorded.");
            else lblMessage.setText(transactions.size() + " transaction(s) loaded.");
        } catch (RuntimeException ex) {
            tblTransactions.getItems().clear();
            lblMessage.setText(ex.getMessage());
        }
    }

    private String money(BigDecimal amount) {
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}

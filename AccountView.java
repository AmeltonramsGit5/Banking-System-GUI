import java.math.BigDecimal;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

// Boundary class for account actions and read-only account information.
public class AccountView {
    private final BankApp app;
    private final BankController controller;
    private final ComboBox<String> cmbAccountType = new ComboBox<>();
    private final TextField txtInitialDeposit = new TextField();
    private final TextField txtAccountNo = new TextField();
    private final TextArea txtSummary = new TextArea();
    private final TextArea txtTransactions = new TextArea();
    private final Label lblMessage = new Label();

    public AccountView(BankApp app, BankController controller) { this.app = app; this.controller = controller; }

    public Scene getScene() {
        Label title = new Label("Accounts");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        cmbAccountType.getItems().addAll("Savings", "Investment", "Cheque");
        cmbAccountType.setValue("Savings");
        txtSummary.setEditable(false); txtSummary.setPrefRowCount(4);
        txtTransactions.setEditable(false); txtTransactions.setPrefRowCount(5);
        txtInitialDeposit.setPromptText("0.00 or more");
        txtAccountNo.setPromptText("For example AC1001");
        lblMessage.setWrapText(true);

        Button btnOpen = new Button("Open Account");
        Button btnBalance = new Button("Check Balance");
        Button btnSummary = new Button("Show Summary");
        Button btnHistory = new Button("Transaction History");
        Button btnBack = new Button("Back");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10); grid.setVgap(8); grid.setPadding(new Insets(18));
        grid.add(title, 0, 0, 2, 1);
        grid.add(new Label("Account type:"), 0, 1); grid.add(cmbAccountType, 1, 1);
        grid.add(new Label("Initial deposit (BWP):"), 0, 2); grid.add(txtInitialDeposit, 1, 2);
        grid.add(btnOpen, 1, 3);
        grid.add(new Label("Account number:"), 0, 4); grid.add(txtAccountNo, 1, 4);
        grid.add(btnBalance, 0, 5); grid.add(btnHistory, 1, 5);
        grid.add(btnSummary, 1, 6);
        grid.add(new Label("Account summary:"), 0, 7, 2, 1); grid.add(txtSummary, 0, 8, 2, 1);
        grid.add(new Label("Transactions:"), 0, 9, 2, 1); grid.add(txtTransactions, 0, 10, 2, 1);
        grid.add(lblMessage, 0, 11, 2, 1); grid.add(btnBack, 1, 12);

        btnOpen.setOnAction(e -> {
            try { lblMessage.setText(controller.openAccount(cmbAccountType.getValue(), new BigDecimal(txtInitialDeposit.getText().trim()))); }
            catch (NumberFormatException ex) { lblMessage.setText("Please enter a valid deposit amount."); }
        });
        btnBalance.setOnAction(e -> lblMessage.setText(controller.getBalance(txtAccountNo.getText())));
        btnSummary.setOnAction(e -> txtSummary.setText(controller.getSummary()));
        btnHistory.setOnAction(e -> txtTransactions.setText(controller.getTransactionHistory(txtAccountNo.getText())));
        btnBack.setOnAction(e -> app.showMenu());
        return new Scene(grid, 550, 720);
    }
}

import java.math.BigDecimal;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

// Boundary class for deposits and withdrawals; the controller owns all rules.
public class TransactionView {
    private final BankApp app;
    private final BankController controller;
    private final TextField txtAccountNo = new TextField();
    private final TextField txtAmount = new TextField();
    private final Label lblMessage = new Label();

    public TransactionView(BankApp app, BankController controller) { this.app = app; this.controller = controller; }

    public Scene getScene() {
        Label title = new Label("Deposit / Withdraw");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        txtAccountNo.setPromptText("For example AC1001");
        txtAmount.setPromptText("Amount in BWP");
        lblMessage.setWrapText(true);
        Button btnDeposit = new Button("Deposit");
        Button btnWithdraw = new Button("Withdraw");
        Button btnBack = new Button("Back");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER); grid.setHgap(10); grid.setVgap(12); grid.setPadding(new Insets(25));
        grid.add(title, 0, 0, 2, 1);
        grid.add(new Label("Account number:"), 0, 1); grid.add(txtAccountNo, 1, 1);
        grid.add(new Label("Amount (BWP):"), 0, 2); grid.add(txtAmount, 1, 2);
        grid.add(btnDeposit, 0, 3); grid.add(btnWithdraw, 1, 3);
        grid.add(btnBack, 1, 4); grid.add(lblMessage, 0, 5, 2, 1);
        btnDeposit.setOnAction(e -> transact(true));
        btnWithdraw.setOnAction(e -> transact(false));
        btnBack.setOnAction(e -> app.showMenu());
        return new Scene(grid, 470, 340);
    }

    private void transact(boolean deposit) {
        try {
            BigDecimal amount = new BigDecimal(txtAmount.getText().trim());
            lblMessage.setText(deposit ? controller.deposit(txtAccountNo.getText(), amount)
                    : controller.withdraw(txtAccountNo.getText(), amount));
        } catch (NumberFormatException ex) { lblMessage.setText("Please enter a valid amount."); }
    }
}

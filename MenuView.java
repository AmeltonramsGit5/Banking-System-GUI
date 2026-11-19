import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

// Boundary class for navigation; role checks are repeated by the controller.
public class MenuView {
    private final BankApp app;
    private final BankController controller;

    public MenuView(BankApp app, BankController controller) { this.app = app; this.controller = controller; }

    public Scene getScene() {
        Label welcome = new Label("Welcome, " + controller.getCurrentUser() + " (" + controller.getRole() + ")");
        welcome.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label interestNote = new Label("Monthly interest is processed automatically once each month.");
        Button btnRegister = new Button("Register Customer");
        Button btnAccount = new Button("Accounts");
        Button btnTransaction = new Button("Deposit / Withdraw");
        Button btnLogout = new Button("Logout");
        btnRegister.setOnAction(e -> app.showCustomer());
        btnAccount.setOnAction(e -> app.showAccount());
        btnTransaction.setOnAction(e -> app.showTransaction());
        btnLogout.setOnAction(e -> { controller.logout(); app.showLogin(); });

        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(25));
        box.getChildren().add(welcome);
        if ("TELLER".equals(controller.getRole())) box.getChildren().add(btnRegister);
        box.getChildren().addAll(btnAccount, btnTransaction, interestNote, btnLogout);
        return new Scene(box, 470, 390);
    }
}

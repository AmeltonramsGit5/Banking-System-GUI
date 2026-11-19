import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

// BOUNDARY CLASS: only shows the login screen and collects input.
public class LoginView {

    private BankApp app;
    private BankController controller;

    private TextField txtUsername = new TextField();
    private PasswordField txtPassword = new PasswordField();
    private Button btnLogin = new Button("Login");
    private Label lblMessage = new Label();

    public LoginView(BankApp app, BankController controller) {
        this.app = app;
        this.controller = controller;
    }

    public Scene getScene() {
        Label title = new Label("Bank Login");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        lblMessage.setStyle("-fx-text-fill: red;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(25));

        grid.add(title, 0, 0, 2, 1);
        grid.add(new Label("Username:"), 0, 1);
        grid.add(txtUsername, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(txtPassword, 1, 2);
        grid.add(btnLogin, 1, 3);
        grid.add(lblMessage, 0, 4, 2, 1);

        // The view just passes the input to the controller and shows the answer.
        btnLogin.setOnAction(e -> {
            String result = controller.login(txtUsername.getText(), txtPassword.getText());
            if (result.equals("OK")) {
                app.showMenu();
            } else {
                lblMessage.setText(result);
            }
        });

        return new Scene(grid, 400, 300);
    }
}

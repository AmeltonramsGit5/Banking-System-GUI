import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class BankApp extends Application {
    private Stage stage;
    private BankController controller;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Banking System");
        try {
            controller = new BankController();
            showLogin();
        } catch (RuntimeException ex) {
            Label error = new Label("Could not start the banking system.\n" + ex.getMessage());
            error.setWrapText(true);
            stage.setScene(new Scene(new StackPane(error), 540, 220));
        }
        stage.show();
    }

    public void showLogin() { stage.setScene(new LoginView(this, controller).getScene()); }
    public void showMenu() { stage.setScene(new MenuView(this, controller).getScene()); }
    public void showCustomer() { stage.setScene(new CustomerView(this, controller).getScene()); }
    public void showAccount() { stage.setScene(new AccountView(this, controller).getScene()); }
    public void showTransaction() { stage.setScene(new TransactionView(this, controller).getScene()); }

    public static void main(String[] args) { launch(args); }
}

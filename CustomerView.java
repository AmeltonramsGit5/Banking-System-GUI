import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

// Boundary class for teller-led customer registration.
public class CustomerView {
    private final BankApp app;
    private final BankController controller;
    private final TextField txtName = new TextField();
    private final TextField txtAddress = new TextField();
    private final ComboBox<String> cmbType = new ComboBox<>();
    private final CheckBox chkEmployed = new CheckBox("Customer is employed");
    private final TextField txtEmployer = new TextField();
    private final TextField txtEmployerAddress = new TextField();
    private final Label lblMessage = new Label();

    public CustomerView(BankApp app, BankController controller) { this.app = app; this.controller = controller; }

    public Scene getScene() {
        Label title = new Label("Register Customer");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        cmbType.getItems().addAll("Individual", "Company");
        cmbType.setValue("Individual");
        txtName.setPromptText("Full name or company name");
        txtAddress.setPromptText("Customer address");
        txtEmployer.setPromptText("Employer / company");
        txtEmployerAddress.setPromptText("Employer address");
        txtEmployer.setDisable(true);
        txtEmployerAddress.setDisable(true);
        cmbType.setOnAction(e -> updateEmploymentFields());
        chkEmployed.setOnAction(e -> updateEmploymentFields());

        Button btnRegister = new Button("Register");
        Button btnBack = new Button("Back");
        lblMessage.setWrapText(true);
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(22));
        grid.add(title, 0, 0, 2, 1);
        grid.add(new Label("Name:"), 0, 1); grid.add(txtName, 1, 1);
        grid.add(new Label("Address:"), 0, 2); grid.add(txtAddress, 1, 2);
        grid.add(new Label("Customer type:"), 0, 3); grid.add(cmbType, 1, 3);
        grid.add(chkEmployed, 1, 4);
        grid.add(new Label("Employer:"), 0, 5); grid.add(txtEmployer, 1, 5);
        grid.add(new Label("Employer address:"), 0, 6); grid.add(txtEmployerAddress, 1, 6);
        grid.add(btnRegister, 1, 7); grid.add(btnBack, 1, 8); grid.add(lblMessage, 0, 9, 2, 1);
        btnRegister.setOnAction(e -> lblMessage.setText(controller.registerCustomer(txtName.getText(), txtAddress.getText(),
                cmbType.getValue(), chkEmployed.isSelected(), txtEmployer.getText(), txtEmployerAddress.getText())));
        btnBack.setOnAction(e -> app.showMenu());
        return new Scene(grid, 520, 500);
    }

    private void updateEmploymentFields() {
        boolean available = "Individual".equals(cmbType.getValue());
        chkEmployed.setDisable(!available);
        if (!available) chkEmployed.setSelected(false);
        boolean required = available && chkEmployed.isSelected();
        txtEmployer.setDisable(!required);
        txtEmployerAddress.setDisable(!required);
    }
}

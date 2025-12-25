package org.example.project.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.project.dao.UserDAO;
import org.example.project.model.User;

public class LoginView extends StackPane {

    private static final String MAIN = "#f5c5cd";
    private static final String ACCENT = "#a53d38";

    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;

    public LoginView() {
        setBackground(new Background(new BackgroundFill(Color.web(MAIN), CornerRadii.EMPTY, Insets.EMPTY)));

        Label title = new Label("Login");
        title.setFont(Font.font("Poppins", FontWeight.EXTRA_BOLD, 34));
        title.setTextFill(Color.web(ACCENT));

        Label subtitle = new Label("Welcome back! Please enter your details");
        subtitle.setFont(Font.font("Poppins", 13));
        subtitle.setTextFill(Color.web(ACCENT));
        subtitle.setOpacity(0.8);

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        HBox userRow = inputWithIcon("👤", usernameField);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        HBox passRow = inputWithIcon("🔒", passwordField);

        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setTextFill(Color.web(ACCENT));
        rememberMe.setOpacity(0.85);

        Label forgot = new Label("Forgot password?");
        forgot.setTextFill(Color.web(ACCENT));
        forgot.setOpacity(0.85);
        forgot.setStyle("-fx-underline: true; -fx-cursor: hand;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(10, rememberMe, spacer, forgot);
        row.setAlignment(Pos.CENTER_LEFT);

        loginButton = new Button("Login");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        stylePrimaryButton(loginButton);

        Label register = new Label("Don’t have an account?  Sign up");
        register.setTextFill(Color.web(ACCENT));
        register.setStyle("-fx-underline: true; -fx-cursor: hand;");
        register.setOnMouseEntered(e -> register.setOpacity(0.75));
        register.setOnMouseExited(e -> register.setOpacity(1));
        register.setOnMouseClicked(e -> getScene().setRoot(new SignupView()));

        VBox card = new VBox(14, title, subtitle, userRow, passRow, row, loginButton, register);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28));
        card.setMaxWidth(430);

        // Glass card style (like the example)
        card.setStyle("""
            -fx-background-color: rgba(255,255,255,0.45);
            -fx-background-radius: 22;
            -fx-border-radius: 22;
            -fx-border-color: rgba(165,61,56,0.20);
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 30, 0.2, 0, 10);
        """);

        getChildren().add(card);

        // Enter triggers login
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(e -> doLogin());
        usernameField.setOnAction(e -> doLogin());
        passwordField.setOnAction(e -> doLogin());
    }

    private HBox inputWithIcon(String icon, TextField tf) {
        Label ic = new Label(icon);
        ic.setFont(Font.font(16));
        ic.setTextFill(Color.web(ACCENT));
        ic.setOpacity(0.75);

        tf.setPrefHeight(44);
        tf.setStyle("""
            -fx-background-color: rgba(255,255,255,0.9);
            -fx-background-radius: 16;
            -fx-border-radius: 16;
            -fx-border-color: rgba(165,61,56,0.25);
            -fx-prompt-text-fill: rgba(165,61,56,0.55);
            -fx-font-family: 'Poppins';
            -fx-font-size: 13px;
            -fx-padding: 10 12;
        """);

        HBox box = new HBox(10, ic, tf);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(6, 12, 6, 12));
        box.setStyle("""
            -fx-background-color: rgba(255,255,255,0.25);
            -fx-background-radius: 18;
            -fx-border-radius: 18;
            -fx-border-color: rgba(165,61,56,0.10);
        """);

        HBox.setHgrow(tf, Priority.ALWAYS);
        return box;
    }

    private void stylePrimaryButton(Button b) {
        b.setPrefHeight(46);
        b.setStyle("""
            -fx-background-color: #a53d38;
            -fx-text-fill: white;
            -fx-font-weight: 900;
            -fx-background-radius: 16;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
            -fx-font-size: 14px;
        """);
        b.setOnMouseEntered(e -> b.setStyle("""
            -fx-background-color: #8f332f;
            -fx-text-fill: white;
            -fx-font-weight: 900;
            -fx-background-radius: 16;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
            -fx-font-size: 14px;
        """));
        b.setOnMouseExited(e -> b.setStyle("""
            -fx-background-color: #a53d38;
            -fx-text-fill: white;
            -fx-font-weight: 900;
            -fx-background-radius: 16;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
            -fx-font-size: 14px;
        """));
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password.");
            return;
        }

        try {
            User user = UserDAO.login(username, password);
            if (user != null) {
                getScene().setRoot(new MainView(user));
            } else {
                showAlert("Error", "Invalid username or password.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "An error occurred while logging in: " + ex.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Button getLoginButton() { return loginButton; }
    public TextField getUsernameField() { return usernameField; }
    public PasswordField getPasswordField() { return passwordField; }
}

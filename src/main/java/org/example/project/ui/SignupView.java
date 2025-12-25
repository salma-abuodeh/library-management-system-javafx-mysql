package org.example.project.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.project.dao.UserDAO;

public class SignupView extends StackPane {

    private static final String MAIN = "#f5c5cd";
    private static final String ACCENT = "#a53d38";

    public SignupView() {
        setBackground(new Background(new BackgroundFill(Color.web(MAIN), CornerRadii.EMPTY, Insets.EMPTY)));

        Label title = new Label("Create Account");
        title.setFont(Font.font("Poppins", FontWeight.EXTRA_BOLD, 30));
        title.setTextFill(Color.web(ACCENT));

        Label subtitle = new Label("Create a strong password to secure your account");
        subtitle.setFont(Font.font("Poppins", 13));
        subtitle.setTextFill(Color.web(ACCENT));
        subtitle.setOpacity(0.8);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        HBox userRow = inputWithIcon("👤", usernameField);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        HBox emailRow = inputWithIcon("📧", emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        HBox passRow = inputWithIcon("🔒", passwordField);

        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm Password");
        HBox confirmRow = inputWithIcon("✅", confirmPassword);

        Label hint = new Label("8+ chars, include: A-Z, a-z, 0-9, symbol");
        hint.setFont(Font.font("Poppins", 11));
        hint.setTextFill(Color.web(ACCENT));
        hint.setOpacity(0.7);

        Label strength = new Label("");
        strength.setFont(Font.font("Poppins", FontWeight.SEMI_BOLD, 12));

        passwordField.textProperty().addListener((obs, ov, nv) -> {
            String s = passwordStrengthLabel(nv);
            strength.setText(s);
            strength.setTextFill(s.startsWith("Strong") ? Color.web("#1a7f4b")
                    : s.startsWith("Medium") ? Color.web("#b7791f")
                    : Color.web("#b42318"));
        });

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("student", "staff");
        roleBox.setPromptText("Select role");
        roleBox.setPrefHeight(44);
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setStyle("""
            -fx-background-radius: 16;
            -fx-background-color: rgba(255,255,255,0.85);
            -fx-border-radius: 16;
            -fx-border-color: rgba(165,61,56,0.25);
            -fx-font-family: 'Poppins';
            -fx-font-size: 13px;
        """);

        Button signUp = new Button("Create Account");
        signUp.setMaxWidth(Double.MAX_VALUE);
        stylePrimaryButton(signUp);

        Button back = new Button("Back");
        back.setMaxWidth(Double.MAX_VALUE);
        styleSecondaryButton(back);
        back.setOnAction(e -> getScene().setRoot(new LoginView()));

        HBox buttons = new HBox(10, signUp, back);
        HBox.setHgrow(signUp, Priority.ALWAYS);
        HBox.setHgrow(back, Priority.ALWAYS);

        VBox card = new VBox(14,
                title, subtitle,
                userRow, emailRow,
                passRow, confirmRow,
                hint, strength,
                roleBox,
                buttons
        );
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28));
        card.setMaxWidth(460);
        card.setStyle("""
            -fx-background-color: rgba(255,255,255,0.45);
            -fx-background-radius: 22;
            -fx-border-radius: 22;
            -fx-border-color: rgba(165,61,56,0.20);
            -fx-border-width: 1;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 30, 0.2, 0, 10);
        """);

        getChildren().add(card);

        signUp.setDefaultButton(true);
        signUp.setOnAction(e -> doSignup(usernameField, emailField, passwordField, confirmPassword, roleBox));
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

    private void styleSecondaryButton(Button b) {
        b.setPrefHeight(46);
        b.setStyle("""
            -fx-background-color: rgba(255,255,255,0.55);
            -fx-text-fill: #a53d38;
            -fx-font-weight: 900;
            -fx-background-radius: 16;
            -fx-border-color: rgba(165,61,56,0.25);
            -fx-border-radius: 16;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
            -fx-font-size: 14px;
        """);
    }

    private void doSignup(TextField usernameField,
                          TextField emailField,
                          PasswordField passwordField,
                          PasswordField confirmField,
                          ComboBox<String> roleBox) {

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmField.getText();
        String role = roleBox.getValue();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty() || role == null) {
            showAlert("Error", "Please fill all fields.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("Error", "Please enter a valid email address.");
            return;
        }

        if (!password.equals(confirm)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }

        if (!isStrongPassword(password)) {
            showAlert("Error", "Password is weak. It must be 8+ chars and include A-Z, a-z, 0-9, and a symbol.");
            return;
        }

        try {
            if (UserDAO.exists(username)) {
                showAlert("Error", "Username already exists.");
                return;
            }

            if (UserDAO.signup(username, email, password, role)) {
                showAlert("Success", "User registered successfully!");
                getScene().setRoot(new LoginView());
            } else {
                showAlert("Error", "Could not register user. Please try again.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", ex.getMessage());
        }
    }

    private boolean isStrongPassword(String p) {
        if (p == null || p.length() < 8) return false;
        boolean upper = p.matches(".*[A-Z].*");
        boolean lower = p.matches(".*[a-z].*");
        boolean digit = p.matches(".*\\d.*");
        boolean symbol = p.matches(".*[^A-Za-z0-9].*");
        return upper && lower && digit && symbol;
    }

    private String passwordStrengthLabel(String p) {
        if (p == null || p.isEmpty()) return "";
        int score = 0;
        if (p.length() >= 8) score++;
        if (p.matches(".*[A-Z].*")) score++;
        if (p.matches(".*[a-z].*")) score++;
        if (p.matches(".*\\d.*")) score++;
        if (p.matches(".*[^A-Za-z0-9].*")) score++;
        if (score >= 5) return "Strong password ✅";
        if (score >= 3) return "Medium password ⚠";
        return "Weak password ❌";
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(regex);
    }
}

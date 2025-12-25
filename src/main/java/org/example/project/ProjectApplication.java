package org.example.project;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.project.dao.UserDAO;
import org.example.project.model.User;
import org.example.project.ui.Dialogs;
import org.example.project.ui.LoginView;
import org.example.project.ui.MainView;
import org.example.project.ui.WelcomeView;

public class ProjectApplication extends Application {

    private static Stage primaryStage;
    private static Scene mainScene;

    private static final String MAIN_COLOR = "#f5c5cd";

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        mainScene = new Scene(new StackPane(), 1280, 720);
        mainScene.setFill(javafx.scene.paint.Color.web(MAIN_COLOR));

        try {
            mainScene.getStylesheets().add(
                    getClass().getResource("/app.css").toExternalForm()
            );
        } catch (Exception ignored) {
        }

        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Library Management System");
        primaryStage.setMaximized(true);
        primaryStage.setResizable(true);

        mainScene.setRoot(new WelcomeView(primaryStage, mainScene));

        primaryStage.show();
    }

    public static void attachLoginHandler(LoginView loginView) {
        loginView.getLoginButton().setOnAction(e -> {
            String uname = loginView.getUsernameField().getText().trim();
            String pwd = loginView.getPasswordField().getText();

            if (uname.isEmpty() || pwd.isEmpty()) {
                Dialogs.showInfo("Error", "Please enter username and password.");
                return;
            }

            try {
                User user = UserDAO.login(uname, pwd);

                if (user == null) {
                    Dialogs.showInfo("Error", "Invalid username or password");
                    return;
                }

                mainScene.setRoot(new MainView(user));

            } catch (Exception ex) {
                ex.printStackTrace();
                Dialogs.showInfo("Error", ex.getMessage());
            }
        });
    }

    // ---------- SWITCH TO LOGIN ----------
    public static void showLoginScreen() {
        LoginView loginView = new LoginView();
        mainScene.setRoot(loginView);
        attachLoginHandler(loginView);
    }

    public static void showWelcomeScreen() {
        mainScene.setRoot(new WelcomeView(primaryStage, mainScene));
    }

    public static Scene getMainScene() {
        return mainScene;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

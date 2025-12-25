package org.example.project.ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.project.ProjectApplication;

public class WelcomeView extends StackPane {

    public WelcomeView(Stage stage, Scene mainScene) {

        Image bgImage = new Image(
                getClass().getResource("/main.png").toExternalForm()
        );

        ImageView bgView = new ImageView(bgImage);
        bgView.setPreserveRatio(true);
        bgView.setSmooth(true);
        bgView.setCache(true);

        mainScene.widthProperty().addListener((obs, o, n) ->
                resizeCover(bgView, n.doubleValue(), mainScene.getHeight()));
        mainScene.heightProperty().addListener((obs, o, n) ->
                resizeCover(bgView, mainScene.getWidth(), n.doubleValue()));

        resizeCover(bgView, mainScene.getWidth(), mainScene.getHeight());

        Button startButton = new Button("Get Started →");
        startButton.setStyle(primaryBtnStyle(false));

        StackPane.setAlignment(startButton, Pos.CENTER);

        startButton.translateYProperty()
                .bind(mainScene.heightProperty().multiply(0.38));

        startButton.setOnMouseEntered(e ->
                startButton.setStyle(primaryBtnStyle(true)));

        startButton.setOnMouseExited(e ->
                startButton.setStyle(primaryBtnStyle(false)));

        startButton.setOnAction(e ->
                ProjectApplication.showLoginScreen());

        getChildren().addAll(bgView, startButton);
    }

    private void resizeCover(ImageView iv, double w, double h) {
        if (iv.getImage() == null) return;

        double imgW = iv.getImage().getWidth();
        double imgH = iv.getImage().getHeight();

        if (imgW <= 0 || imgH <= 0 || w <= 0 || h <= 0) return;

        double scale = Math.max(w / imgW, h / imgH);

        iv.setFitWidth(imgW * scale);
        iv.setFitHeight(imgH * scale);
    }

    private String primaryBtnStyle(boolean hover) {
        return """
            -fx-font-family: "Poppins";
            -fx-font-size: 18px;
            -fx-font-weight: 800;
            -fx-text-fill: #a53d38;

            -fx-background-color: linear-gradient(
                to bottom,
                %s,
                %s
            );

            -fx-padding: 14 46;
            -fx-background-radius: 28;

            -fx-border-color: rgba(165,61,56,0.25);
            -fx-border-width: 1;
            -fx-border-radius: 28;

            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0.15, 0, 6);
        """.formatted(
                hover ? "#e9aab4" : "#f5c5cd",
                hover ? "#f0b8c2" : "#f8d5db"
        );
    }
}

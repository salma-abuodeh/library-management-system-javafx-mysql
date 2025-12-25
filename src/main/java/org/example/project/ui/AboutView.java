package org.example.project.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class AboutView extends StackPane {

    private static final String MAIN_BG  = "#f5c5cd";
    private static final String CARD_BG  = "rgba(255,255,255,0.80)";
    private static final String ACCENT   = "#a53d38";

    public AboutView() {
        setPadding(new Insets(28));
        setStyle("-fx-background-color: " + MAIN_BG + ";");

        BorderPane page = new BorderPane();
        page.setMaxWidth(980);

        // ---- Header (icon + title) ----
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 18, 12, 18));

        Label icon = new Label("📚");
        icon.setStyle("""
            -fx-font-size: 26px;
            -fx-background-color: rgba(165,61,56,0.10);
            -fx-padding: 10 12;
            -fx-background-radius: 14;
            -fx-border-color: rgba(165,61,56,0.25);
            -fx-border-radius: 14;
        """);

        VBox titleBox = new VBox(3);
        Label title = new Label("About This App");
        title.setFont(Font.font("Poppins", FontWeight.EXTRA_BOLD, 22));
        title.setTextFill(Color.web(ACCENT));

        Label subtitle = new Label("Library Management System • JavaFX");
        subtitle.setFont(Font.font("Poppins", FontWeight.SEMI_BOLD, 12));
        subtitle.setTextFill(Color.web("#6b2b29"));

        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(icon, titleBox);

        // ---- Content card ----
        VBox card = new VBox(14);
        card.setPadding(new Insets(18));
        card.setStyle("""
            -fx-background-color: %s;
            -fx-background-radius: 18;
            -fx-border-color: rgba(165,61,56,0.18);
            -fx-border-radius: 18;
        """.formatted(CARD_BG));
        card.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.18)));

        Label dev = infoLine("Developed by:", "Salma Mahmoud Abu Odeh");
        Label uni = infoLine("Bethlehem University |", "Software Engineering");
        Label contact = infoLine("Contact:", "salmamahmoudao@gmail.com");

        FlowPane chips = new FlowPane(10, 10);
        chips.getChildren().addAll(
                chip("JavaFX UI"),
                chip("MySQL / JDBC"),
                chip("CRUD + Reports"),
                chip("Charts")
        );

        Label footer = new Label("Thank you for using the system 💗");
        footer.setFont(Font.font("Poppins", FontWeight.SEMI_BOLD, 12));
        footer.setTextFill(Color.web("#6b2b29"));

        card.getChildren().addAll(dev, uni, contact, spacer(), chips, spacer(), footer);

        // ---- Outer layout (center card) ----
        VBox center = new VBox(14, header, card);
        center.setAlignment(Pos.TOP_LEFT);

        page.setCenter(center);

        StackPane decor = new StackPane(page);
        decor.setPadding(new Insets(12));
        decor.setStyle("""
            -fx-background-color: rgba(255,255,255,0.22);
            -fx-background-radius: 22;
        """);

        getChildren().add(decor);
    }

    private Label infoLine(String left, String right) {
        Label l = new Label(left + " " + right);
        l.setWrapText(true);
        l.setFont(Font.font("Poppins", FontWeight.SEMI_BOLD, 14));
        l.setTextFill(Color.web("#2b2b2b"));
        return l;
    }

    private Node chip(String text) {
        Label c = new Label(text);
        c.setFont(Font.font("Poppins", FontWeight.BOLD, 12));
        c.setTextFill(Color.web(ACCENT));
        c.setStyle("""
            -fx-background-color: rgba(165,61,56,0.10);
            -fx-padding: 8 12;
            -fx-background-radius: 999;
            -fx-border-color: rgba(165,61,56,0.18);
            -fx-border-radius: 999;
        """);
        return c;
    }

    private Region spacer() {
        Region r = new Region();
        r.setMinHeight(2);
        return r;
    }
}

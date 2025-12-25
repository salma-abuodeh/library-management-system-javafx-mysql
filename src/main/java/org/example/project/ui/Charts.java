package org.example.project.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class Charts {

    // Pink palette
    private static final String[] PALETTE = {
            "#b63b36", // deep red
            "#d24b67", // rose
            "#f06b8a", // pink
            "#f39c6b", // peach
            "#ffcc66", // warm yellow
            "#7bbf9f", // soft green
            "#6aa9e9", // soft blue
            "#9a7bd6"  // soft purple
    };

    private static Scene wrap(Stage stage, String title, javafx.scene.Node chart) {
        stage.setTitle(title);

        Label header = new Label(title);
        header.getStyleClass().add("chart-title");

        VBox top = new VBox(header);
        top.setPadding(new Insets(14));
        top.getStyleClass().add("chart-header");

        BorderPane root = new BorderPane(chart);
        root.setTop(top);
        root.getStyleClass().add("chart-root");

        Scene scene = new Scene(root, 900, 540);
        scene.getStylesheets().add(Charts.class.getResource("/chart-style.css").toExternalForm());
        return scene;
    }

    private static void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private static void colorBars(XYChart.Series<String, Number> series) {
        Platform.runLater(() -> {
            for (int i = 0; i < series.getData().size(); i++) {
                XYChart.Data<String, Number> d = series.getData().get(i);
                Node n = d.getNode();
                if (n != null) {
                    String c = PALETTE[i % PALETTE.length];
                    n.setStyle(
                            "-fx-bar-fill: " + c + ";" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-border-radius: 8;"
                    );
                }
            }
        });
    }

    private static void colorLine(LineChart<String, Number> chart, XYChart.Series<String, Number> series) {
        Platform.runLater(() -> {
            Node line = series.getNode().lookup(".chart-series-line");
            if (line != null) {
                line.setStyle("-fx-stroke: #b63b36; -fx-stroke-width: 3.2px;");
            }

            // symbols
            for (XYChart.Data<String, Number> d : series.getData()) {
                Node sym = d.getNode();
                if (sym != null) {
                    sym.setStyle(
                            "-fx-background-color: #b63b36, white;" +
                                    "-fx-background-insets: 0, 2;" +
                                    "-fx-background-radius: 8px;" +
                                    "-fx-padding: 6px;"
                    );
                }
            }
        });
    }

    private static void colorPie(PieChart chart) {
        Platform.runLater(() -> {
            for (int i = 0; i < chart.getData().size(); i++) {
                PieChart.Data d = chart.getData().get(i);
                Node n = d.getNode();
                if (n != null) {
                    String c = PALETTE[i % PALETTE.length];
                    n.setStyle("-fx-pie-color: " + c + ";");
                }
            }
        });
    }

    /** 1) Books per category (bar): expects keys: category, cnt */
    public static void showCategoryChart(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            info("No data found for Books per category.");
            return;
        }

        Stage stage = new Stage();

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);

        xAxis.setLabel("Category");
        yAxis.setLabel("Books");
        chart.setLegendVisible(false);
        chart.setAnimated(true);
        chart.setCategoryGap(18);
        chart.setBarGap(6);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map<String, Object> row : data) {
            Object cat = row.get("category");
            Object cnt = row.get("cnt");
            if (cat == null || cnt == null) continue;

            series.getData().add(new XYChart.Data<>(
                    String.valueOf(cat),
                    ((Number) cnt).intValue()
            ));
        }

        chart.getData().add(series);
        colorBars(series);

        stage.setScene(wrap(stage, "Books Distribution by Category", chart));
        stage.show();
    }

    /** 2) Availability split (pie): expects keys: available_cnt, borrowed_cnt */
    public static void showAvailabilityPie(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            info("No data found for Availability overview.");
            return;
        }
        Map<String, Object> r = data.get(0);

        int available = ((Number) r.get("available_cnt")).intValue();
        int borrowed = ((Number) r.get("borrowed_cnt")).intValue();

        PieChart chart = new PieChart(FXCollections.observableArrayList(
                new PieChart.Data("Available", available),
                new PieChart.Data("Borrowed", borrowed)
        ));
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);

        chart.setLabelLineLength(18);
        chart.setStartAngle(90);

        colorPie(chart);

        Stage stage = new Stage();
        stage.setScene(wrap(stage, "Availability Overview", chart));
        stage.show();
    }

    /** 3) Loans per month (line): expects keys: month_label, cnt */
    public static void showLoansPerMonthLine(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            info("No data found for Loans per month.");
            return;
        }

        Stage stage = new Stage();

        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(x, y);

        chart.setLegendVisible(false);
        chart.setCreateSymbols(true);
        chart.setAnimated(true);

        x.setLabel("Month");
        y.setLabel("Loans");

        XYChart.Series<String, Number> s = new XYChart.Series<>();
        for (Map<String, Object> row : data) {
            Object m = row.get("month_label");
            Object cnt = row.get("cnt");
            if (m == null || cnt == null) continue;

            s.getData().add(new XYChart.Data<>(
                    String.valueOf(m),
                    ((Number) cnt).intValue()
            ));
        }

        chart.getData().add(s);
        colorLine(chart, s);

        stage.setScene(wrap(stage, "Loans Per Month", chart));
        stage.show();
    }

    /** 4) Sales per month (bar): expects keys: month_label, revenue */
    public static void showSalesRevenueBar(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            info("No data found for Sales revenue per month.");
            return;
        }

        Stage stage = new Stage();

        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);

        chart.setLegendVisible(false);
        chart.setAnimated(true);
        chart.setCategoryGap(18);
        chart.setBarGap(6);

        x.setLabel("Month");
        y.setLabel("Revenue");

        XYChart.Series<String, Number> s = new XYChart.Series<>();
        for (Map<String, Object> row : data) {
            Object m = row.get("month_label");
            Object rev = row.get("revenue");
            if (m == null || rev == null) continue;

            s.getData().add(new XYChart.Data<>(
                    String.valueOf(m),
                    ((Number) rev).doubleValue()
            ));
        }

        chart.getData().add(s);
        colorBars(s);

        stage.setScene(wrap(stage, "Sales Revenue Per Month", chart));
        stage.show();
    }

    /** 5) Top borrowers (bar): expects keys: borrower, cnt */
    public static void showTopBorrowersBar(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            info("No data found for Top borrowers.");
            return;
        }

        Stage stage = new Stage();

        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);

        chart.setLegendVisible(false);
        chart.setAnimated(true);
        chart.setCategoryGap(20);
        chart.setBarGap(6);

        x.setLabel("Borrower");
        y.setLabel("Loans");

        XYChart.Series<String, Number> s = new XYChart.Series<>();
        for (Map<String, Object> row : data) {
            Object name = row.get("borrower");
            Object cnt = row.get("cnt");
            if (name == null || cnt == null) continue;

            s.getData().add(new XYChart.Data<>(
                    String.valueOf(name),
                    ((Number) cnt).intValue()
            ));
        }

        chart.getData().add(s);
        colorBars(s);

        stage.setScene(wrap(stage, "Top Borrowers", chart));
        stage.show();
    }
}

package org.example.project.ui;

import javafx.scene.image.Image;
import javafx.scene.layout.*;

public class BackgroundUtil {

    public static void applyBackground(Pane pane, String imageFileName) {
        Image bg = new Image(BackgroundUtil.class.getResource("/" + imageFileName).toExternalForm());

        BackgroundSize bgSize = new BackgroundSize(
                BackgroundSize.AUTO, BackgroundSize.AUTO,
                true, true, true, false
        );
        BackgroundImage bgImage = new BackgroundImage(
                bg,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                bgSize
        );
        pane.setBackground(new Background(bgImage));
    }
}

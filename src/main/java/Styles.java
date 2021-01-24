import javafx.geometry.Insets;
import javafx.scene.control.Button;

public class Styles {

    public static final String IDLE_BUTTON_STYLE = "-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: transparent; -fx-border-radius:5;";
    public static final String HOVERED_BUTTON_STYLE = "-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius:5; ";

    static Button makeBtn(String title) {
        Button btn = new Button(title);
        btn.setPadding(new Insets(10, 20, 10, 20));
        btn.setStyle(IDLE_BUTTON_STYLE);
        btn.setOnMouseEntered(e -> btn.setStyle(HOVERED_BUTTON_STYLE));
        btn.setOnMouseExited(e -> btn.setStyle(IDLE_BUTTON_STYLE));
        return btn;
    }
}

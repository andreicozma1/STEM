import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class OptionsMenu {
    public OptionsMenu(){
        Stage optionsStage = new Stage();

        //handle this window before moving on
        optionsStage.initModality(Modality.APPLICATION_MODAL);
        optionsStage.setTitle("Options");
        optionsStage.setMinWidth(100);

        Label label = new Label("Options Menu");

        CheckBox enableScrollBars = Styles.makeCheckBox("Enable Scroll Bars?");
        enableScrollBars.setSelected(true);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, enableScrollBars);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        optionsStage.setScene(scene);
        optionsStage.showAndWait();

    }
}

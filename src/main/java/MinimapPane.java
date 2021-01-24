import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class MinimapPane extends Pane {
    private StackPane root;
    private Pane target;
    private Node scaleNode;
    public MinimapPane(Pane target) {
        super();

        root = new StackPane();

        this.target = target;
        this.scaleNode = new Group(target);

        this.setMaxHeight(250);
        this.setMaxWidth(250);
        this.setStyle("-fx-background-color: transparent;-fx-border-color: black;-fx-border-width:1px");
        this.getChildren().addAll(root);
    }
}

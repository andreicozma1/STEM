package Types.Changes;

import Editor.Editor;
import Types.State;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class AcceptAddition implements Change {

    private State state;
    private Editor editor;

    public AcceptAddition(State state, Editor editor) {
        this.state = state;
        this.editor = editor;
    }

    @Override
    public Change undo() {
        this.state.setAccept(false);

        this.editor.getEditorSpace().getChildren().remove(state.getAcceptCircle());

        this.state.setAcceptCircle(null);
        return this;
    }

    @Override
    public Change apply() {
        this.state.setAccept(true);

        Circle circle = new Circle(this.state.getCircle().getCenterX(), this.state.getCircle().getCenterY(),
                this.editor.getCircleRadius() * 1.25, null);
        circle.setStrokeWidth(2);
        circle.setStroke(Color.BLACK);

        this.state.setAcceptCircle(circle);
        this.editor.getEditorSpace().getChildren().add(circle);
        circle.toBack();
        return this;
    }

    @Override
    public String toString() {
        return "Accept Addition";
    }

}

package Types.Changes;

import java.util.ArrayList;

import Editor.Editor;
import Types.Machine;
import Types.State;
import javafx.scene.shape.Rectangle;

public class StateManyMove extends StateMove {

    private ArrayList<StateMove> moves;
    // these are all for moving the selected rectangle, not the states
    private Rectangle rectangle;
    private double oldX;
    private double oldY;
    private double newX;
    private double newY;

    public StateManyMove(Rectangle selectedRectangle, Machine machine, ArrayList<State> states, double offsetX,
            double offsetY, Editor editor) {
        this.moves = new ArrayList<StateMove>();
        for (State state : states) {
            this.moves.add(new StateMove(machine, state, state.getX() - offsetX, state.getY() - offsetY, editor));
        }
        this.rectangle = selectedRectangle;
        this.oldX = rectangle.getX() - offsetX;
        this.oldY = rectangle.getY() - offsetY;
        this.newX = rectangle.getX();
        this.newY = rectangle.getY();
    }

    @Override
    public Change undo() {
        for (StateMove change : moves) {
            change.undo();
        }
        this.rectangle.setX(oldX);
        this.rectangle.setY(oldY);
        return this;
    }

    @Override
    public Change apply() {
        for (StateMove change : moves) {
            change.apply();
        }
        this.rectangle.setX(newX);
        this.rectangle.setY(newY);
        return this;
    }

    @Override
    public String toString() {
        return "State Many Move";
    }

}

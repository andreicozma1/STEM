package Types.Changes;

import java.util.ArrayList;

import Editor.Editor;
import Types.Machine;
import Types.State;
import Types.Transition;

public class StateMove implements Change {

    private State state;
    private Machine machine;
    private double newX;
    private double newY;
    private double oldX;
    private double oldY;
    private Editor editor;

    protected StateMove() {
    }

    public StateMove(Machine machine, State state, double oldX, double oldY, Editor editor) {
        this.machine = machine;
        this.state = state;
        this.editor = editor;
        this.oldX = oldX;
        this.oldY = oldY;
        this.newX = state.getX();
        this.newY = state.getY();
    }

    @Override
    public Change undo() {
        // move the state to newX
        state.getCircle().setCenterX(oldX);
        state.getCircle().setCenterY(oldY);
        state.getLabel().setX(oldX - (state.getLabel().getLayoutBounds().getWidth() / 2));
        state.getLabel().setY(oldY + (state.getLabel().getLayoutBounds().getHeight() / 4));
        state.setX(oldX);
        state.setY(oldY);

        // update the accept circle if needed
        if (state.isAccept()) {
            state.getAcceptCircle().setCenterX(oldX);
            state.getAcceptCircle().setCenterY(oldY);
        }

        // if the state is a start state, redraw it
        if (state.isStart()) {
            this.editor.drawStartTriangle(state);
        }

        if (state.isSelected()) {
            this.state.getSelctedCircle().setCenterX(oldX);
            this.state.getSelctedCircle().setCenterY(oldY);
        }

        // update all transitions that were affected
        ArrayList<Transition> transitions = new ArrayList<Transition>();
        for (Transition transition : this.machine.getTransitions()) {
            if (transition.getToState() == this.state || transition.getFromState() == this.state) {
                transitions.add(transition);
            }
        }
        this.editor.redrawPaths(transitions);

        return this;
    }

    @Override
    public Change apply() {
        // move the state to newX
        state.getCircle().setCenterX(newX);
        state.getCircle().setCenterY(newY);
        state.getLabel().setX(newX - (state.getLabel().getLayoutBounds().getWidth() / 2));
        state.getLabel().setY(newY + (state.getLabel().getLayoutBounds().getHeight() / 4));
        state.setX(newX);
        state.setY(newY);

        // update the accept circle if needed
        if (state.isAccept()) {
            state.getAcceptCircle().setCenterX(newX);
            state.getAcceptCircle().setCenterY(newY);
        }

        // if the state is a start state, redraw it
        if (state.isStart()) {
            this.editor.drawStartTriangle(state);
        }

        if (state.isSelected()) {
            this.state.getSelctedCircle().setCenterX(newX);
            this.state.getSelctedCircle().setCenterY(newY);
        }

        // update all transitions that were affected
        ArrayList<Transition> transitions = new ArrayList<Transition>();
        for (Transition transition : this.machine.getTransitions()) {
            if (transition.getToState() == this.state || transition.getFromState() == this.state) {
                transitions.add(transition);
            }
        }
        this.editor.redrawPaths(transitions);
        return this;
    }

    @Override
    public String toString() {
        return "State Moved";
    }
}

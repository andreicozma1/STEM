package Types.Changes;

import Types.State;

import java.util.ArrayList;

import Editor.Editor;
import Types.Machine;
import Types.Transition;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import Types.Path;

public class StateDelete implements Change {

    private State state;
    private Editor editor;
    private Machine machine;
    private ArrayList<Transition> transitions;
    private boolean wasStart;

    protected StateDelete() {
    }

    public StateDelete(State state, Machine machine, Editor editor) {
        this.state = state;
        this.editor = editor;
        this.machine = machine;
        this.transitions = new ArrayList<Transition>();
    }

    @Override
    public Change undo() {
        // add back the state
        this.machine.addState(this.state);
        this.editor.getEditorSpace().getChildren().addAll(this.state.getCircle(), this.state.getLabel());

        // add back each transition
        for (Transition transition : this.transitions) {
            this.machine.getTransitions().add(transition);
            // add transition to the state
            transition.getFromState().addNewTransition(transition);
        }
        this.editor.redrawPaths(transitions);

        // add back the start triangle if need be
        if (wasStart) {
            this.machine.setStartState(this.state);
            this.editor.drawStartTriangle(state);
        }

        // add back the accept circle if need be
        if (this.state.isAccept()) {
            Circle circle = new Circle(this.state.getCircle().getCenterX(), this.state.getCircle().getCenterY(),
                    this.editor.getCircleRadius() * 1.25, null);
            circle.setStrokeWidth(2);
            circle.setStroke(Color.BLACK);

            this.state.setAcceptCircle(circle);
            this.editor.getEditorSpace().getChildren().add(circle);
            circle.toBack();
        }

        // make this state number invalid again
        if (this.editor.getDeletedValues().contains(Integer.parseInt(this.state.getName()))) {
            this.editor.getDeletedValues()
                    .remove(this.editor.getDeletedValues().indexOf(Integer.parseInt(this.state.getName())));
        }

        return this;
    }

    @Override
    public Change apply() {
        ArrayList<Transition> deleteTransitions = new ArrayList<>();
        ArrayList<Path> deletePaths = new ArrayList<>();

        for (Transition t : this.machine.getTransitions()) {
            if (t.getToState() == this.state) {

                ArrayList<Node> nodes = t.getPath().getAllNodes();
                if (!nodes.isEmpty())
                    this.editor.getEditorSpace().getChildren().removeAll(t.getPath().getAllNodes());
                t.getFromState().getTransition().remove(t);

                deletePaths.add(t.getPath());
                deleteTransitions.add(t);
                transitions.add(t);
            } else if (t.getFromState() == this.state) {
                transitions.add(t);
            }
        }
        this.machine.getPaths().removeAll(deletePaths);
        this.machine.getTransitions().removeAll(deleteTransitions);

        this.editor.getEditorSpace().getChildren().removeAll(state.getCircle(), state.getLabel());

        this.machine.getTransitions().removeAll(state.getTransition());

        for (Transition t : state.getTransition()) {
            this.editor.getEditorSpace().getChildren().removeAll(t.getPath().getAllNodes());
            this.machine.getPaths().remove(t.getPath());
            t.setPath(null);
        }
        this.state.getTransition().clear();

        if (this.machine.getStartState() == state) {
            wasStart = true;
            System.out.printf("State %s is start removing...", state.getName());
            this.machine.setStartState(null);
            this.editor.getEditorSpace().getChildren().remove(this.editor.getStartTriangle());
        }

        if (state.isAccept()) {
            this.editor.getEditorSpace().getChildren().remove(state.getAcceptCircle());
        }

        if (state.isSelected()) {
            this.editor.getEditorSpace().getChildren().remove(state.getSelectedCircle());
            state.setSelected(false);
            state.setSelctedCircle(null);
        }

        this.machine.deleteState(state);
        this.editor.getDeletedValues().add(Integer.parseInt(state.getName()));
		this.editor.getSelectedStates().remove(state);
        return this;
    }

    @Override
    public String toString() {
        return "State Delete";
    }

}

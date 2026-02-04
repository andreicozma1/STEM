package Types.Changes;

import Types.State;
import Types.Transition;
import java.util.ArrayList;

import Editor.Editor;
import Types.Machine;

public class StateAddition implements Change {

    private State state;
    private Machine machine;
    private Editor editor;
    private ArrayList<Transition> transitions;

    public StateAddition(State state, ArrayList<Transition> transitions, Machine machine, Editor editor) {
        this.state = state;
        this.machine = machine;
        this.editor = editor;
        this.transitions = transitions;
    }

    @Override
    public Change undo() {
        editor.getEditorSpace().getChildren().removeAll(state.getCircle(), state.getLabel());
        machine.deleteState(state);
        this.editor.getDeletedValues().add(Integer.parseInt(state.getName()));
        if (this.state.isAccept()) {
            this.editor.getEditorSpace().getChildren().remove(this.state.getAcceptCircle());
        }
        if (transitions != null) {
            for (Transition transition : transitions) {
                transition.getFromState().getTransition().remove(transition);
                this.machine.deleteTransition(transition);
                this.machine.deletePath(transition.getPath());
                this.editor.getEditorSpace().getChildren().removeAll(transition.getPath().getAllNodes());
            }
        }
        return this;
    }

    @Override
    public Change apply() {
        machine.addState(state);
        editor.getEditorSpace().getChildren().addAll(state.getCircle(), state.getLabel());
        // add the transitions
        if (transitions != null) {
            for (Transition transition : transitions) {
                transition.getFromState().addNewTransition(transition);
                this.machine.getTransitions().add(transition);
            }
            this.editor.redrawPaths(transitions);
        }
        if (this.state.isAccept()) {
            this.editor.getEditorSpace().getChildren().add(this.state.getAcceptCircle());
        }
        return this;
    }

    @Override
    public String toString() {
        return "State Addition";
    }
}

package Types.Changes;

import Types.State;

import java.util.ArrayList;

import Editor.Editor;
import Types.Machine;
import Types.Transition;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

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
		int number = Integer.parseInt(this.state.getName());
		int index = this.editor.getDeletedValues().indexOf(number);
        if (index != -1) {
            this.editor.getDeletedValues().remove(index);
        }

		this.transitions.clear();
        return this;
    }

    @Override
    public Change apply() {
		ArrayList<Transition> transitionsToRemove = new ArrayList<Transition>();
		for(int i = 0; i < this.machine.getTransitions().size(); i++){
			Transition transition = this.machine.getTransitions().get(i);
            if (transition.getToStateName().equals(this.state.getName()) || transition.getFromStateName().equals(this.state.getName())) {
				// remove the transition's path
				if(transition.getPath() != null){
	                ArrayList<Node> nodes = transition.getPath().getAllNodes();
	                if (!nodes.isEmpty())
	                    this.editor.getEditorSpace().getChildren().removeAll(nodes);
					this.machine.deletePath(transition.getPath());
				}
				// remove the transition from the states
                transition.getFromState().removeTransition(transition);
                transition.getToState().removeTransition(transition);

				// save the transition to add back on undo
                this.transitions.add(transition);
				transitionsToRemove.add(transition);
            }
        }
		// delete the transitions from the machine
		transitionsToRemove.forEach(transition -> this.machine.deleteTransition(transition));

        this.editor.getEditorSpace().getChildren().removeAll(state.getCircle(), state.getLabel());

        if (this.machine.getStartState() == state) {
            wasStart = true;
            this.machine.setStartState(null);
            this.editor.getEditorSpace().getChildren().remove(this.editor.getStartTriangle());
        }

        if (state.isAccept()) {
            this.editor.getEditorSpace().getChildren().remove(state.getAcceptCircle());
        }

        if (state.isSelected()) {
            this.editor.getEditorSpace().getChildren().remove(state.getSelectedCircle());
            state.setSelected(false);
            state.setSelectedCircle(null);
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

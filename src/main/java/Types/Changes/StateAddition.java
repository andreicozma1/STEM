package Types.Changes;

import Types.State;
import Types.Transition;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

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
                this.machine.getTransitions().remove(transition);
                if (transition.getFromState() != this.state) {
                    transition.getFromState().getTransition().remove(transition);
                }
                this.machine.getPaths().remove(transition.getPath());
                this.editor.getEditorSpace().getChildren().removeAll(transition.getPath().getAllNodes());
            }
        }
        return this;
    }

    @Override
    public Change apply() {
		// ????? what is this
		// EventHandler<MouseEvent> controlClickHandler = event -> {
		// 	System.out.println("entering");
		// 	if(!this.editor.isToggleSelected() && event.isControlDown()){
		// 		System.out.println("entering again");
		// 		if(state.isSelected()){
		// 			System.out.println("unselecting state");
		// 			this.editor.getSelectedStates().remove(state);
		// 			state.setSelected(false);
		// 			this.editor.getEditorSpace().getChildren().remove(state.getSelctedCircle());
		// 			state.setSelctedCircle(null);
		// 		}
		// 		else{
		// 			System.out.println("selecting state");
		// 			this.editor.getSelectedStates().add(state);
		// 			state.setSelected(true);
		// 			Circle selectedCircle = new Circle(state.getX(), state.getY(), this.editor.getCircleRadius() * 1.5,
		// 					Color.TRANSPARENT);
		// 			selectedCircle.setStrokeWidth(2);
		// 			selectedCircle.setStroke(Color.BLUE);
		// 			state.setSelctedCircle(selectedCircle);
		// 			this.editor.getEditorSpace().getChildren().add(selectedCircle);
		// 		}
		// 	}
		// };

		// state.getCircle().addEventHandler(MouseEvent.MOUSE_CLICKED, controlClickHandler);
		// state.getLabel().addEventHandler(MouseEvent.MOUSE_CLICKED, controlClickHandler);
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

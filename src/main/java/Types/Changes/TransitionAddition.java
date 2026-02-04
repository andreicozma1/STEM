package Types.Changes;

import java.util.ArrayList;

import Editor.Editor;
import Types.Machine;
import Types.Transition;
import Types.Path;
import javafx.scene.Node;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Line;

public class TransitionAddition implements Change {

    Transition transition;
    Editor editor;
    Machine machine;

    public TransitionAddition(Transition transition, Machine machine, Editor editor) {
        this.transition = transition;
        this.editor = editor;
        this.machine = machine;
    }

    @Override
    public Change undo() {
		if(this.transition.getPath() != null){
	        ArrayList<Node> nodes = this.transition.getPath().removeTransition(this.transition);

	        if (!nodes.isEmpty()) {
	            this.editor.getEditorSpace().getChildren().removeAll(nodes);
	        }

	    	if (!this.transition.getPath().getAllNodes().isEmpty()) {
	            this.machine.getPaths().remove(this.transition.getPath());
	        }
		}

        this.transition.getFromState().getTransition().remove(this.transition);
        this.machine.getTransitions().remove(this.transition);
        return this;
    }

    @Override
    public Change apply() {
        this.machine.getTransitions().add(transition);
        transition.getFromState().getTransition().add(transition);

        Path path = null;
        for (Path p : this.machine.getPaths()) {
            if (p.compareTo(this.transition.getFromState(), this.transition.getToState())) {
                path = p;
                System.out.println("Found Path");
                break;
            }
        }

        if (path == null) {
            path = new Path(this.transition.getFromState(), this.transition.getToState());
            System.out.println("New Path");
            this.machine.getPaths().add(path);
        }

        this.transition.setPath(path);
        ArrayList<Node> nodes = path.addTransition(transition);
        this.editor.getEditorSpace().getChildren().addAll(nodes);

        for (Node node : nodes)
            if (node instanceof Line || node instanceof CubicCurve)
                node.toBack();
        return this;
    }

    @Override
    public String toString() {
        return "Transition Addition";
    }
}

package Types.Changes;

import Editor.Editor;
import Types.Transition;
import Types.Transition.Direction;

public class TransitionDirectionChange implements Change {

    Transition transition;
    Editor editor;
	char newDirection;
    char oldDirection;

    public TransitionDirectionChange(Transition transition, char newDirection, char oldDirection, Editor editor) {
        this.transition = transition;
		this.newDirection = newDirection;
		this.oldDirection = oldDirection;
        this.editor = editor;
    }

    @Override
    public Change undo() {
		this.transition.setDirectionChar(oldDirection);
        this.editor.redrawPath(this.transition);
        return this;
    }

    @Override
    public Change apply() {
		this.transition.setDirectionChar(newDirection);
        this.editor.redrawPath(this.transition);
        return this;
    }

    @Override
    public String toString() {
        return "Transition Direction Change";
    }
}

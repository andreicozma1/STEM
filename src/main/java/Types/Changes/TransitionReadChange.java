package Types.Changes;

import Editor.Editor;
import Types.Transition;

public class TransitionReadChange implements Change {

    Transition transition;
    Editor editor;
    char oldChar;
    char newChar;

    public TransitionReadChange(Transition transition, char oldChar, char newChar, Editor editor) {
        this.transition = transition;
        this.oldChar = oldChar;
        this.newChar = newChar;
        this.editor = editor;
    }

    @Override
    public Change undo() {
		this.transition.getPath().removeTransition(this.transition);
        this.transition.setReadChar(this.oldChar);
		this.transition.getPath().addTransition(this.transition);
        this.editor.redrawPath(this.transition);
        return this;
    }

    @Override
    public Change apply() {
		this.transition.getPath().removeTransition(this.transition);
        this.transition.setReadChar(this.newChar);
		this.transition.getPath().addTransition(this.transition);
        this.editor.redrawPath(this.transition);
        return this;
    }

    @Override
    public String toString() {
        return "Transition Read Change";
    }
}

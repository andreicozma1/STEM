package Types.Changes;

import Editor.Editor;
import Types.Transition;

public class TransitionWriteChange implements Change {

    Transition transition;
    Editor editor;
    char oldChar;
    char newChar;

    public TransitionWriteChange(Transition transition, char oldChar, char newChar, Editor editor) {
        this.transition = transition;
        this.oldChar = oldChar;
        this.newChar = newChar;
        this.editor = editor;
    }

    @Override
    public Change undo() {
        this.transition.setWriteChar(oldChar);
        this.editor.redrawPath(this.transition);
        return this;
    }

    @Override
    public Change apply() {
        this.transition.setWriteChar(newChar);
        this.editor.redrawPath(this.transition);
        return this;
    }

    @Override
    public String toString() {
        return "Transition Write Change";
    }
}

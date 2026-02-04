package Types.Changes;

import Editor.Editor;
import Types.Machine;
import javafx.scene.control.TextArea;

public class CommentDelete implements Change {

    TextArea comment;
    Machine machine;
    Editor editor;

    public CommentDelete(TextArea comment, Machine machine, Editor editor) {
        this.comment = comment;
        this.machine = machine;
        this.editor = editor;
    }

    @Override
    public Change undo() {
        this.machine.getComments().add(comment);
        this.editor.getEditorSpace().getChildren().add(comment);
        return this;
    }

    @Override
    public Change apply() {
        this.machine.getComments().remove(comment);
        this.editor.getEditorSpace().getChildren().remove(comment);
        return this;
    }

    @Override
    public String toString() {
        return "Comment Delete";
    }

}

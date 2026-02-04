package Types.Changes;

import Editor.Editor;
import Types.Machine;
import javafx.scene.shape.Rectangle;

public class CommentBoxDelete implements Change {

    Rectangle box;
    Editor editor;
    Machine machine;

    public CommentBoxDelete(Rectangle commentBox, Machine machine, Editor editor) {
        this.box = commentBox;
        this.editor = editor;
        this.machine = machine;
    }

    @Override
    public Change undo() {
        if (!this.editor.getEditorSpace().getChildren().contains(this.box)) {
            this.editor.getEditorSpace().getChildren().add(this.box);
            this.box.toBack();
        }

        if (!this.machine.getcBoxes().contains(this.box)) {
            this.machine.getcBoxes().add(this.box);
        }
        return this;
    }

    @Override
    public Change apply() {
        this.editor.getEditorSpace().getChildren().remove(this.box);
        this.machine.getcBoxes().remove(this.box);
        return this;
    }

    @Override
    public String toString() {
        return "Comment Box Delete";
    }

}

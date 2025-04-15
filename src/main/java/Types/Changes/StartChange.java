package Types.Changes;

import Types.State;
import Editor.Editor;
import Types.Machine;

public class StartChange implements Change {

    private State originalStart;
    private State newStart;
    private Machine machine;
    private Editor editor;

    public StartChange(Machine machine, State originalStart, State newStart, Editor editor) {
        this.machine = machine;
        this.originalStart = originalStart;
        this.newStart = newStart;
        this.editor = editor;
    }

    @Override
    public Change undo() {
        if (this.originalStart != null) {
            machine.setStartState(originalStart);
            originalStart.setStart(true);
        } else {
            machine.setStartState(null);
        }
        newStart.setStart(false);
        this.editor.drawStartTriangle(this.originalStart);
        return this;
    }

    @Override
    public Change apply() {
        machine.setStartState(newStart);
        newStart.setStart(true);
        return this;
    }

    @Override
    public String toString() {
        return "Start Change";
    }
}

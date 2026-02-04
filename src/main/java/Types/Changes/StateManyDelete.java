package Types.Changes;

import java.util.ArrayList;

import Editor.Editor;
import Types.Machine;
import Types.State;

public class StateManyDelete extends StateDelete {

    private ArrayList<StateDelete> deletes;

    public StateManyDelete(ArrayList<State> states, Machine machine, Editor editor) {
        this.deletes = new ArrayList<StateDelete>();
        for (State state : states) {
            this.deletes.add(new StateDelete(state, machine, editor));
        }
    }

    @Override
    public Change undo() {
        for (StateDelete change : deletes) {
            change.undo();
        }
        return this;
    }

    @Override
    public Change apply() {
        for (StateDelete change : deletes) {
            change.apply();
        }
        return this;
    }

}

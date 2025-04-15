package Types.Changes;

import Types.State;
import javafx.scene.paint.Color;

public class BreakpointDelete implements Change {

    private State state;

    public BreakpointDelete(State state) {
        this.state = state;
    }

    @Override
    public Change undo() {
        state.setDebug(true);
        state.getCircle().setStroke(Color.RED);
        return this;
    }

    @Override
    public Change apply() {
        state.setDebug(false);
        state.getCircle().setStroke(Color.BLACK);
        return this;
    }

    @Override
    public String toString() {
        return "Breakpoint Addition";
    }
}

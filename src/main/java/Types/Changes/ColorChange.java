package Types.Changes;

import Types.State;
import javafx.scene.paint.Color;

public class ColorChange implements Change {

    private State state;
    private Color newColor;
    private Color oldColor;

    public ColorChange(State state, Color newColor) {
        this.state = state;
        this.newColor = newColor;
        this.oldColor = this.state.getBaseColor();
    }

    @Override
    public Change undo() {
        state.setColor(oldColor);
        state.getCircle().setFill(oldColor);
        return this;
    }

    @Override
    public Change apply() {
        state.setColor(newColor);
        state.getCircle().setFill(newColor);
        return this;
    }

    @Override
    public String toString() {
        return "Color Change";
    }

}

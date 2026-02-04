package Types.Changes;

public interface Change {
    public Change undo();

    public Change apply();
}

package BackEnd;

public class Action {
    public enum Type { FOLD, CHECK, CALL, RAISE, ALL_IN }

    public Type type;
    public int amount;

    public Action(Type type, int amount) {
        this.type = type;
        this.amount = amount;
    }
}

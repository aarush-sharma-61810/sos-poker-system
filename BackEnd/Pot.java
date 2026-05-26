package BackEnd;

import java.util.ArrayList;
import java.util.List;

public class Pot {
    public int amount;
    public List<String> eligible;

    public Pot() {
        amount = 0;
        eligible = new ArrayList<>();
    }
}

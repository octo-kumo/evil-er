package utils;

import java.util.HashMap;
import java.util.List;

public class Utils {
    public static List<String> replaceDuplicates(List<String> input) {
        HashMap<String, Integer> hash = new HashMap<>();
        for (int i = 0; i < input.size(); i++) {
            if (!hash.containsKey(input.get(i)))
                hash.put(input.get(i), 1);
            else {
                int count = hash.get(input.get(i));
                hash.put(input.get(i), hash.get(input.get(i)) + 1);
                input.set(i, input.get(i) + "_" + count);
            }
        }
        return input;
    }
}

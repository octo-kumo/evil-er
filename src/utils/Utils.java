package utils;

import java.util.Arrays;
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

    public static <T> T[] prepend(T[] elements, T... to_prepend) {
        T[] newArray = Arrays.copyOf(elements, elements.length + to_prepend.length);
        System.arraycopy(to_prepend, 0, newArray, 0, to_prepend.length);
        System.arraycopy(elements, 0, newArray, to_prepend.length, elements.length);
        return newArray;
    }
}

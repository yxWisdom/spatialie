package edu.nju.ws.spatialie.utils;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtils {

    // 根据delimiter将list分割为若干sub list
    public static<T> List<List<T>> split(List<T> list, T delimiter) {
        List<List<T>> res = new ArrayList<>();
        List<T> tmp = new ArrayList<>();
        for (T element: list) {
            if (!tmp.isEmpty() && (element == null && delimiter == null || element != null && element.equals(delimiter))) {
                res.add(tmp);
                tmp = new ArrayList<>();
            } else {
                tmp.add(element);
            }
        }
        if (!tmp.isEmpty())
            res.add(tmp);
        return res;
    }
}

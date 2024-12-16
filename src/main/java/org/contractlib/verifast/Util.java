package org.contractlib.verifast;

import org.contractlib.ast.Datatype;
import org.contractlib.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Util {
    public static <A, B> List<Pair<A,B>> zip(List<A> a, List<B> b) {
        if (a.size() != b.size()) {
            throw new IllegalArgumentException("Lists must have the same length");
        }
        var result = new ArrayList<Pair<A,B>>();
        Iterator<A> it1 = a.iterator();
        Iterator<B> it2 = b.iterator();
        while(it1.hasNext() && it2.hasNext()) {
            result.add(new Pair<>(it1.next(), it2.next()));
        }
        return result;
    }
}

package org.contractlib.exporter;

import org.contractlib.util.Pair;

import java.util.*;
import java.util.function.Function;

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

    public static <E> List<E> concated(List<E> l1, List<E> l2) {
        return new ConcatedList<E>(l1, l2);
    }

    public static String timestamp() {
        return new Date().toString();
    }

    public static String commasep(String s, String t) {
        return s + ", " + t;
    }

    public static <E> CharSequence commatize(Iterable<E> list, Function<E, String> f) {
        return commatize(list, f, ", ");
    }

    public static <E> CharSequence commatize(Iterable<E> list, Function<E, String> f, String delimiter) {
        var result = new StringBuilder();
        boolean first = true;
        for (E e : list) {
            if (first) {
                first = false;
            } else {
                result.append(delimiter);
            }
            result.append(f.apply(e));
        }
        return result;
    }



    public static String removeLeading_(String s) {
        if (s.startsWith("_")) {
            return s.substring(1);
        } else {
            return s;
        }
    }

    public static String parenNonEmpty(CharSequence s) {
        if(s.isEmpty()) return "";
        return "(" + s + ")";
    }


    private static class ConcatedList<E> extends AbstractList<E> {
        private final List<E> l1;
        private final List<E> l2;

        public ConcatedList(List<E> l1, List<E> l2) {
            this.l1 = l1;
            this.l2 = l2;
        }

        @Override
        public E get(int index) {
            if (index < l1.size()) {
                return l1.get(index);
            } else {
                return l2.get(index - l1.size());
            }
        }

        @Override
        public int size() {
            return l1.size() + l2.size();
        }
    }
}

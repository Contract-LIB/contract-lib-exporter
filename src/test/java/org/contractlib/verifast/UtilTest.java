package org.contractlib.verifast;

import org.contractlib.exporter.Util;
import org.contractlib.util.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void testZip() {
        var a = List.of(1, 2, 3);
        var b = List.of("a", "b", "c");
        var result = Util.zip(a, b);
        assertEquals(3, result.size());
        assertEquals(new Pair<>(1, "a"), result.get(0));
        assertEquals(new Pair<>(2, "b"), result.get(1));
        assertEquals(new Pair<>(3, "c"), result.get(2));
    }

}
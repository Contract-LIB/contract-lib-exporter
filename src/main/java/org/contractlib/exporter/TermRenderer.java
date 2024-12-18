package org.contractlib.exporter;

import java.util.List;

public interface TermRenderer {
    String render(String functionName, List<String> args);
}

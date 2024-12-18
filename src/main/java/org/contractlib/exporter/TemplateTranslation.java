package org.contractlib.exporter;

import java.io.IOException;

public interface TemplateTranslation {
    String getName();

    void translate(ContractLib contractLib) throws IOException;
}

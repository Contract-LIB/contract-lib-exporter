package org.contractlib.templated;

import java.io.IOException;

public interface TemplateTranslation {
    String getName();

    void translate(ContractLib contractLib) throws IOException;
}

package org.contractlib.templated;

import java.io.IOException;

public interface TemplateTranslation {
    String getName();

    void translate(Main main, ContractLib contractLib) throws IOException;
}

package org.contractlib.dafny;

import org.contractlib.ast.Term;
import org.contractlib.templated.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DafnyTranslation extends AbstractTemplateTranslation {
    public DafnyTranslation(Main main) {
        super(main);
    }

    @Override
    public String getName() {
        return "dafny";
    }

    @Override
    public void translate(ContractLib contractLib) throws IOException {
        debug("Translating to KeY");
        debug("Contract Lib: %s", contractLib);

        StringBuilder sb = new StringBuilder();
        sb.append(timeStamp);

        sb.append(handleDatatypes(contractLib.datatypes()));

        debug("Abstractions:");
        for (ContractLib.Abstraction abstraction : contractLib.abstractions()) {
            var contracts = contractLib.getContracts(abstraction);
            var absString = handleAbstraction(abstraction, contracts);
            sb.append(absString);
        }

        Files.write(Paths.get(options.directory.getPath(), "dafny.dfy"), sb.toString().getBytes());
    }

    private String handleAbstraction(ContractLib.Abstraction abstraction, List<ContractLib.Contract> contracts) {

        boolean innerMode = options.options.contains("inner");

        StringBuilder sb = new StringBuilder();
        sb.append("trait ").append(abstraction.name()).append("\n{\n\n");

        CharSequence locset;
        if(!innerMode) {
            sb.append("  ghost var set<object> footprint;\n");
            locset = "footprint";
        } else {
            locset = "this";
        }

        for (ContractLib.TypedName selector : abstraction.selectors()) {
            sb.append("  ghost var ").append(selector.name()).append(": ").append(renderType(selector.type())).append(";\n");
        }
        sb.append("\n");

        var selectorNames = abstraction.getSelectorNames();
        for (ContractLib.Contract contract : contracts) {
            sb.append("  method ").append(contract.operationName()).append("(");
            sb.append(Util.commatize(contract.inputParams(), x -> x.name() + ": " + renderType(x.type())));
            sb.append(")");
            var retParam = contract.getReturnParameter();
            retParam.ifPresent(modeTypedName -> sb.append(" returns (").append(modeTypedName.name()).append(": ").append(renderType(modeTypedName.type())).append(")"));
            sb.append(")\n");
            sb.append("    requires ").append(renderTerm(contract.pre(), selectorNames)).append(";\n");
            sb.append("    ensures ").append(renderTerm(contract.post(), selectorNames)).append(";\n");
            if(contract.modifier()) {
                sb.append("    modifies ").append(locset).append(";\n");
            } else {
                sb.append("    reads ").append(locset).append(";\n");
            }
            sb.append("\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String handleDatatypes(List<ContractLib.Datatype> datatypes) {
        StringBuilder sb = new StringBuilder();
        for (ContractLib.Datatype datatype : datatypes) {
            sb.append("datatype ").append(datatype.name()).append(" = ");
            sb.append(Util.commatize(datatype.constructors(), this::handleConstructor, " | "));
            sb.append(";\n");
        }
        return sb.toString();
    }

    private String handleConstructor(ContractLib.Constructor constructor) {
        return constructor.name() + Util.parenNonEmpty(Util.commatize(constructor.selectors(), this::handleSelector));
    }

    private String handleSelector(ContractLib.TypedName typedName) {
        return typedName.name() + ": " + renderType(typedName.type());
    }

    @Override
    protected String renderOld(List<String> selectorNames, Term.Old old) {
        return "old(" + super.renderOld(selectorNames, old) + ")";
    }
}

package org.contractlib.verifast;

import org.contractlib.ast.Term;
import org.contractlib.templated.*;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class VerifastTranslation extends AbstractTemplateTranslation {

    public VerifastTranslation(Main main) throws FileNotFoundException {
        super(main);
    }

    @Override
    public String getName() {
        return "verifast";
    }

    @Override
    public void translate(ContractLib contractLib) throws IOException {

        debug("Translating to Verifast");
        debug("Contract Lib: %s", contractLib);

        debug("Data types:");
        var datatypeString = timeStamp + contractLib.datatypes().stream().map(this::handleDatatype).reduce(String::concat).orElse("");
        Files.write(Paths.get(options.directory.getPath(), "_datatypes.java"), datatypeString.getBytes());

        debug("Abstractions:");
        for (ContractLib.Abstraction abstraction : contractLib.abstractions()) {
            var contracts = contractLib.getContracts(abstraction);
            var absString = handleAbstraction(abstraction, contracts);
            Files.write(Paths.get(options.directory.getPath(), abstraction.name() + ".java"), absString.getBytes());
        }
    }

    private String handleAbstraction(ContractLib.Abstraction abstraction, List<ContractLib.Contract> contracts) {
        debug("Abstraction: %s", abstraction);
        debug("Contracts: %s", contracts);
        StringBuilder sb = new StringBuilder();
        sb.append(timeStamp);
        sb.append("interface ").append(abstraction.name()).append(" {\n\n");
        sb.append("  //@ predicate valid(");

        sb.append(Util.commatize(abstraction.selectors(), x ->
            renderType(x.type()) + " " + x.name()));
        sb.append(");\n\n");

        var selectorNames = abstraction.getSelectorNames();

        for (ContractLib.Contract contract : contracts) {
            sb.append("  ").append(renderType(contract.resultType())).append(" ").append(contract.operationName()).append("(");
            sb.append(Util.commatize(contract.inputParams(), x -> renderType(x.type()) + " " + x.name()));
            sb.append(");\n");
            sb.append("    //@ requires valid(");
            sb.append(Util.commatize(abstraction.selectors(), x -> "?" + x.name()));
            sb.append(") &*& ");
            sb.append(renderTerm(contract.pre(), selectorNames));
            sb.append(";\n");

            sb.append("    //@ ensures valid(");
            List<String> selectorNamesPost;
            if(contract.modifier()) {
                sb.append(Util.commatize(abstraction.selectors(), x -> "?_" + x.name()));
                selectorNamesPost = abstraction.selectors().stream().map(x -> "_" + x.name()).toList();
            } else {
                sb.append(Util.commatize(abstraction.selectors(), ContractLib.TypedName::name));
                selectorNamesPost = selectorNames;
            }
            sb.append(") &*& ");
            sb.append(renderTerm(contract.post(), selectorNames));
            sb.append(";\n\n");
        }

        sb.append("}");
        var result = sb.toString();
        debug(result);
        return result;
    }

    private String handleDatatype(ContractLib.Datatype datatype) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\ninductive %s =%n", datatype.name()));
        String indent = "    ";
        for (ContractLib.Constructor constructor : datatype.constructors()) {
            sb.append(indent).append(constructor.name()).append("(");
            sb.append(Util.commatize(constructor.selectors(), x -> renderType(x.type()) + " " + x.name()));
            sb.append(");\n");
            indent = "  | ";
        }
        var result = sb.toString();
        debug("Datatype: %s", result);
        return result;
    }

    protected String renderOld(List<String> selectorNames, Term.Old old) {
        var selectorNamesOld = selectorNames.stream().map(Util::removeLeading_).toList();
        return renderTerm(old.argument(), selectorNamesOld);
    }

}

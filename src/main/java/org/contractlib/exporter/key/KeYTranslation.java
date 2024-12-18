package org.contractlib.exporter.key;

import org.contractlib.ast.Term;
import org.contractlib.exporter.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class KeYTranslation extends AbstractTemplateTranslation {

    @Override
    public String getName() {
        return "key";
    }

    public KeYTranslation(Main options) {
        super(options);
    }

    @Override
    public void translate(ContractLib contractLib) throws IOException {

        debug("Translating to KeY");
        debug("Contract Lib: %s", contractLib);

        debug("Data types:");
        var datatypeString = timeStamp + handleDatatypes(contractLib.datatypes());
        Files.write(Paths.get(options.directory.getPath(), "datatypes.key"), datatypeString.getBytes());

        debug("Abstractions:");
        for (ContractLib.Abstraction abstraction : contractLib.abstractions()) {
            var contracts = contractLib.getContracts(abstraction);
            var absString = handleAbstraction(abstraction, contracts);
            Files.write(Paths.get(options.directory.getPath(), abstraction.name() + ".java"), absString.getBytes());
        }
    }

    private String handleAbstraction(ContractLib.Abstraction abstraction, List<ContractLib.Contract> contracts) {

        boolean innerMode = options.options.contains("inner");

        StringBuilder sb = new StringBuilder();
        sb.append(timeStamp);
        sb.append("interface ").append(abstraction.name()).append(" {\n\n");

        CharSequence locset;
        if(!innerMode) {
            sb.append("  //@ instance ghost \\locset footprint;\n");
            locset = "footprint";
        } else {
            locset = Util.commatize(abstraction.selectors(), ContractLib.TypedName::name);
        }

        for (ContractLib.TypedName selector : abstraction.selectors()) {
            sb.append("  //@ instance ghost ").append(renderType(selector.type())).append(" ").append(selector.name()).append(";\n");
        }
        sb.append("\n");

        var selectorNames = abstraction.getSelectorNames();
        for (ContractLib.Contract contract : contracts) {
            sb.append("  /*@ public normal_behaviour\n");
            sb.append("    @  requires ").append(renderTerm(contract.pre(), selectorNames)).append(";\n");
            sb.append("    @  ensures ").append(renderTerm(contract.post(), selectorNames)).append(";\n");
            if(contract.modifier()) {
                sb.append("    @  assignable ").append(locset).append(";\n");
            } else {
                sb.append("    @  assignable \\nothing;\n");
                sb.append("    @  accessible ").append(locset).append(";\n");
            }
            sb.append("    @*/\n");
            sb.append("  ").append(renderType(contract.resultType())).append(" ").append(contract.operationName()).append("(");
            sb.append(Util.commatize(contract.inputParams(), x -> renderType(x.type()) + " " + x.name()));
            sb.append(");\n\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String handleDatatypes(List<ContractLib.Datatype> datatypes) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\datatypes {\n\n");
        for (ContractLib.Datatype datatype : datatypes) {
            sb.append("    ").append(datatype.name()).append(" = ");
            sb.append(Util.commatize(datatype.constructors(), this::handleConstructor, " | "));
            sb.append(";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String handleConstructor(ContractLib.Constructor constructor) {
        return constructor.name() + "(" + Util.commatize(constructor.selectors(), this::handleSelector) + ")";
    }

    private String handleSelector(ContractLib.TypedName typedName) {
        return renderType(typedName.type()) + " " + typedName.name();
    }

    @Override
    protected Map<String, String> makeTypeMap() {
        var result = super.makeTypeMap();
        result.put("Seq", "\\seq");
        return result;
    }

    @Override
    protected Map<String, TermRenderer> makeTermRendererMap() {
        var result = super.makeTermRendererMap();
        result.put("len", (f, args) -> args.getFirst() + ".length");
        result.put("select", (f, args) -> args.getFirst() + "[" + args.getLast() + "]");
        result.put("store", funAppRenderer("\\seqUpd"));
        return result;
    }

    @Override
    protected String renderOld(List<String> selectorNames, Term.Old old) {
        return "\\old(" + super.renderOld(selectorNames, old) + ")";
    }
}

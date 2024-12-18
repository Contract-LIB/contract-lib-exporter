package org.contractlib.verifast;

import org.contractlib.parser.ContractLibANTLRParser;
import org.contractlib.templated.ContractLib;
import org.contractlib.templated.Main;
import org.contractlib.templated.TemplateTranslation;
import org.contractlib.templated.Util;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.contractlib.antlr4parser.ContractLIBLexer;
import org.contractlib.antlr4parser.ContractLIBParser;
import org.contractlib.ast.*;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;


public class VerifastTranslation implements TemplateTranslation {

    private final String timeStamp = "// This file is auto-generated by the ContractLib tool. Do not modify\n// "
             + Util.timestamp() + "\n\n";

    private final STGroup templates;

    private Main options;

    public VerifastTranslation() throws FileNotFoundException {
        URL url = getClass().getResource("templates.st");
        if(url == null) {
            throw new FileNotFoundException("Could not find the template file in resources");
        }
        this.templates = new STGroupFile(url);
        templates.registerModelAdaptor(Record.class, new RecordAdaptor());
        templates.registerRenderer(Type.class, new TypeRenderer());
    }

    @Override
    public String getName() {
        return "verifast";
    }

    private void debug(String message, Object... args) {
        if(options.verbose) {
            System.out.printf(message + "\n", args);
        }
    }

    public void translate(Main main, ContractLib contractLib) throws IOException {
        this.options = main;

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
            printType(x.type()) + " " + x.name()));
        sb.append(");\n\n");

        for (ContractLib.Contract contract : contracts) {
            sb.append("  " + printType(contract.resultType()) + " " + contract.operationName() + "(");
            sb.append(Util.commatize(contract.inputParams(), x -> printType(x.type()) + " " + x.name()));
            sb.append(");\n");
            sb.append("    //@ requires valid(");
            sb.append(Util.commatize(abstraction.selectors(), x -> "?" + x.name()));
            sb.append(") &*& ");
            sb.append(contract.renderedPre());
            sb.append(";\n");

            sb.append("    //@ ensures valid(");
            sb.append(Util.commatize(abstraction.selectors(), x -> x.name()));
            sb.append(") &*& ");
            // sb.append(contract.renderedPost());
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
            sb.append(Util.commatize(constructor.selectors(), x -> printType(x.type()) + " " + x.name()));
            sb.append(");\n");
            indent = "  | ";
        }
        var result = sb.toString();
        debug("Datatype: %s", result);
        return result;
    }

    private String printType(Type value) {
        if(value == null)
            return "void";
        return switch(value) {
            case Type.Sort sort -> correctKnown(sort.name()) +
                    sort.arguments().stream().map(this::printType).reduce((a, b) -> a + ", " + b).map(a -> "<" + a + ">").orElse("");
            case Type.Param param -> correctKnown(param.name());
        };
    }

    public String correctKnown(String name) {
        return switch(name) {
            case "Int" -> "int";
            case "Bool" -> "boolean";
            case "string" -> "string";
            default -> name;
        };
    }
}
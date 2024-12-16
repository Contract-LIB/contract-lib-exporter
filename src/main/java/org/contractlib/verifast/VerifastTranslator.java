package org.contractlib.verifast;

import org.contractlib.parser.ContractLibANTLRParser;
import org.contractlib.util.Pair;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.contractlib.antlr4parser.ContractLIBLexer;
import org.contractlib.antlr4parser.ContractLIBParser;
import org.contractlib.ast.*;
import org.contractlib.factory.Mode;
import org.contractlib.parser.ContractLibANTLRParser;
import org.contractlib.util.Pair;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.ErrorManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class VerifastTranslator {

    private final STGroup templates;

    private VerifastTranslator() throws FileNotFoundException {
        URL url = getClass().getResource("templates.st");
        if(url == null) {
            throw new FileNotFoundException("Could not find the template file in resources");
        }
        this.templates = new STGroupFile(url);
        templates.registerModelAdaptor(Record.class, new RecordAdaptor());
    }

    public static void main(String[] args) throws IOException {
        System.out.println(translate(Files.readString(Path.of("t1.smt2"))));
    }

    public static String translate(String contractLibInput) throws FileNotFoundException {

        CharStream charStream = CharStreams.fromString(contractLibInput);
        ContractLIBLexer lexer = new ContractLIBLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ContractLIBParser parser = new ContractLIBParser(tokens);

        ContractLIBParser.ScriptContext ctx = parser.script();
        Factory factory = new Factory();
        ContractLibANTLRParser<Term, Type, Abstraction, Datatype, FunDec, Command> converter = new ContractLibANTLRParser<>(factory);
        converter.visit(ctx);

        List<Command> commands = converter.getCommands();

        VerifastTranslator translator = new VerifastTranslator();
        translator.handleCommands(commands);
        return translator.getString();
    }

    private String getString() {
        return "";
    }

    private void handleCommands(List<Command> commands) {
        for (Command command : commands) {
            handleCommand(command);
        }
    }

    private void handleCommand(Command command) {
        if (command instanceof Command.DeclareDatatypes declare) {
            handleDeclareDatatypes(declare);
//        } else if (command instanceof Command.Define define) {
//            handleDefine(define);
//        } else if (command instanceof Command.Function function) {
//            handleFunction(function);
//        } else if (command instanceof Command.Include include) {
//            handleInclude(include);
//        } else if (command instanceof Command.Type type) {
//            handleType(type);
        } else {
            throw new RuntimeException("Commands of type " + command.getClass() + " currently not supported for VerifastExport");
        }
    }


    private void handleDeclareDatatypes(Command.DeclareDatatypes declare) {
        System.out.println(declare);

        var datatypes = Util.zip(declare.arities(), declare.datatypes());
        System.out.println(datatypes);
        var st = templates.getInstanceOf("adt_decl").add("dts", datatypes);
        System.out.println(st.render());
        for (var datatype : datatypes) {
            var name = datatype.first().first();
            var typeAr = datatype.first().second();
            if(typeAr != 0) {
                throw new RuntimeException("VerifastExport does not support datatypes with parameters");
            }
//            var constructors = datatype.second().constrs().stream().map(
//                    constr -> new STConstr(constr.first(), constr.second().size())).collect(Collectors.toList());

        }
    }

}

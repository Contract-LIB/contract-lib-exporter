package org.contractlib.templated;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.contractlib.antlr4parser.ContractLIBLexer;
import org.contractlib.antlr4parser.ContractLIBParser;
import org.contractlib.ast.*;
import org.contractlib.parser.ContractLibANTLRParser;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


public class PreTranslator {

    private List<ContractLib.Datatype> datatypes = new ArrayList<>();
    private List<ContractLib.Abstraction> abstractions = new ArrayList<>();
    private List<ContractLib.Contract> contracts = new ArrayList<>();

    private PreTranslator() {
    }

    public static ContractLib translate(CharStream charStream) throws FileNotFoundException {
        ContractLIBLexer lexer = new ContractLIBLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ContractLIBParser parser = new ContractLIBParser(tokens);

        ContractLIBParser.ScriptContext ctx = parser.script();
        Factory factory = new Factory();
        ContractLibANTLRParser<Term, Type, Abstraction, Datatype, FunDecl, Command> converter = new ContractLibANTLRParser<>(factory);
        converter.visit(ctx);

        List<Command> commands = converter.getCommands();

        PreTranslator translator = new PreTranslator();
        translator.handleCommands(commands);
        return translator.getResult();
    }

    private ContractLib getResult() {
        return new ContractLib(datatypes, abstractions, contracts);
    }

    private void handleCommands(List<Command> commands) {
        for (Command command : commands) {
            handleCommand(command);
        }
    }

    private void handleCommand(Command command) {
        switch (command) {
            case Command.DeclareDatatypes dt:
                handleDeclareDatatypes(dt);
                break;

            case Command.DeclareAbstractions da:
                handleDeclAbstraction(da);
                break;

            case Command.DefineContract dc:
                handleDefineContract(dc);
                break;

            default:
                throw new RuntimeException("Commands of type " + command.getClass() + " currently not supported for export");
        }
    }

    private void handleDefineContract(Command.DefineContract dc) {
        System.out.println(dc);
        var qname = dc.name();
        var parts = qname.split("\\.");
        if(parts.length != 2) {
            throw new RuntimeException("Contract name must be fully qualified");
        }
        var abstractionName = parts[0];
        var operationName = parts[1];
        var params = dc.formal().stream().map(x -> new ContractLib.ModeTypedName(x.first(), x.second().first(), x.second().second())).toList();
        var contracts = dc.contracts();
        if (contracts.size() != 1) {
            throw new RuntimeException("Currently only one contract per operation is supported");
        }
        var contract = contracts.getFirst();
        var pre = contract.first();
        var post = contract.second();
        this.contracts.add(new ContractLib.Contract(abstractionName, operationName, params, pre, post));

    }

    private void handleDeclareDatatypes(Command.DeclareDatatypes dt) {
        Util.zip(dt.arities(), dt.datatypes()).forEach(p -> {
            var name = p.first().first();
            var typeAr = p.first().second();
            var params = p.second().params();
            if (params.size() != typeAr) {
  //              throw new RuntimeException("inconsistent number of type parameters for " + name);
            }

            var constructors = p.second().constrs().stream().map(
                    constr -> new ContractLib.Constructor(constr.first(),
                    constr.second().stream().map(x -> new ContractLib.TypedName(x.first(), x.second())).toList())).toList();

            datatypes.add(new ContractLib.Datatype(name, params, constructors));
        });
    }


    private void handleDeclAbstraction(Command.DeclareAbstractions da) {
        System.out.println(da);
        Util.zip(da.arities(), da.abstractions()).forEach(p -> {
            var name = p.first().first();
            var typeAr = p.first().second();
            var params = p.second().params();
            if (params.size() != typeAr) {
          //      throw new RuntimeException("inconsistent number of type parameters for " + name);
            }

            var constructors = p.second().constrs();
            if(constructors.size() != 1) {
                throw new RuntimeException("Abstractions must have exactly one constructor");
            }

            var constr = constructors.getFirst();
            if(!constr.first().equals(name)) {
                throw new RuntimeException("Abstraction constructor must have the same name as the abstraction");
            }

            var selectors = constr.second().stream().map(x -> new ContractLib.TypedName(x.first(), x.second())).toList();
            abstractions.add(new ContractLib.Abstraction(name, params, selectors));
        });
    }
}


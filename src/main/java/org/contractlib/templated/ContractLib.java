package org.contractlib.templated;

import org.contractlib.ast.Term;
import org.contractlib.ast.Type;
import org.contractlib.factory.Mode;

import java.util.List;

public class ContractLib {

    private final List<Datatype> datatypes;
    private final List<Abstraction> abstractions;
    private final List<Contract> contracts;

    public ContractLib(List<Datatype> datatypes, List<Abstraction> abstractions, List<Contract> contracts) {
        this.datatypes = datatypes;
        this.abstractions = abstractions;
        this.contracts = contracts;
    }

    public static ContractLib merge(ContractLib cl1, ContractLib cl2) {
        if(cl1 == null) {
            return cl2;
        } else if(cl2 == null) {
            return cl1;
        } else {
            return new ContractLib(
                    Util.concated(cl1.datatypes, cl2.datatypes),
                    Util.concated(cl1.abstractions, cl2.abstractions),
                    Util.concated(cl1.contracts, cl2.contracts));
        }
    }

    @Override
    public String toString() {
        return "ContractLib:\n  datatypes:\n    " + datatypes.stream().map(Object::toString).reduce((x,y) -> x + "\n    " + y).orElse("") +
                "\n  abstractions:\n    " + abstractions.stream().map(Object::toString).reduce((x,y) -> x + "\n    " + y).orElse("") +
                "\n  contracts:\n    " +  contracts.stream().map(Object::toString).reduce((x,y) -> x + "\n    " + y).orElse("");
    }

    public List<Contract> getContracts(Abstraction abstraction) {
        return contracts.stream().filter(c -> c.abstractionName.equals(abstraction.name)).toList();
    }

    public record Contract(String abstractionName, String operationName, List<ModeTypedName> params,
                           Term pre, Term post) {
        // todo check there is but one output, check 1st is this

        public Type resultType() {
            return params.stream().filter(p -> p.mode == Mode.OUT).findFirst().map(x->x.type).orElse(null);
        }

        public List<ModeTypedName> inputParams() {
            return params.stream().filter(p -> p.mode == Mode.IN && !p.name.equals("this")).toList();
        }

        public boolean modifies() {
            return params.stream().anyMatch(p -> p.mode == Mode.INOUT && p.name.equals("this"));
        }

        public String renderedPre() {
            return render(pre, false);
        }

        // In post mode, we need to render the postcondition with the result variable
        private String render(Term pre, boolean postMode) {
            switch(pre) {
                case Term.Application app:
                    var args = app.arguments().stream().map(a -> render(a, postMode)).toList();
                    return handleApp(app.function(), args);
                case Term.Variable var:
                    return var.name();
                case Term.Literal lit:
                    return lit.value().toString();
                case Term.Old old:
                    return render(old.argument(), false);
                case Term.Binder binder:
                    throw new RuntimeException("Binders are currently not supported in contracts");
            }
        }

        private String handleApp(String app, List<String> args) {
            return switch (app) {
                case "and" ->
                        "(" + args.stream().reduce((a, b) -> a + " && " + b).orElse("true") + ")";
                case "or" ->
                        "(" + args.stream().reduce((a, b) -> a + " || " + b).orElse("false") + ")";
                case "not" -> "!" + args.getFirst();
                case "=", "<", "<=", ">", ">=" ->
                        "(" + args.getFirst() + " " + app + " " + args.get(1) + ")";
                default ->
                        app + "(" + args.stream().reduce((a, b) -> a + ", " + b).orElse("") + ")";
            };
        }
    }



    public record Datatype(String name, List<String> typeParameters, List<Constructor> constructors) {
    }

    public record Constructor(String name, List<TypedName> selectors) {
    }

    public record TypedName(String name, Type type) {
    }

    public record ModeTypedName(String name, Mode mode, Type type) {
    }

    public record Abstraction(String name, List<String> params, List<TypedName> selectors) {
    }



    public List<Datatype> datatypes() {
        return datatypes;
    }

    public List<Abstraction> abstractions() {
        return abstractions;
    }

    public List<Contract> contracts() {
        return contracts;
    }
}

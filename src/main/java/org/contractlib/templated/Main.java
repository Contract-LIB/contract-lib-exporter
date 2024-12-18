package org.contractlib.templated;

import org.antlr.v4.runtime.CharStreams;
import org.contractlib.dafny.DafnyTranslation;
import org.contractlib.key.KeYTranslation;
import org.contractlib.verifast.VerifastTranslation;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public List<TemplateTranslation> translations = new ArrayList<>();

    private Main() throws FileNotFoundException {
        // todo eventually that should be a service loader
        // The following were all (fully automatically) suggested by github copilot ;)
        translations.add(new VerifastTranslation(this));
        translations.add(new KeYTranslation(this));
//        translations.add(new BoogieTranslation());
//        translations.add(new Why3Translation());
//        translations.add(new ViperTranslation());
        translations.add(new DafnyTranslation(this));
//        translations.add(new FramaCTranslation());
//        translations.add(new JMLTranslation());
//        translations.add(new SpecSharpTranslation());
    }

    @Option(names = "-d", description = "target directory")
    public File directory = new File(".");

//    @Option(names = { "-i", "--include" }, paramLabel = "abstraction", description = "comma-separated list of abstractions to include")
//    public String toInclude = null;

    @Option(names = { "-t", "--target" }, paramLabel = "language", description = "target formal verification language")
    public String language = "verifast";

    @Option(names = { "-l", "--list" }, description = "list available target languages")
    private boolean listLanguages = false;

    @Parameters(paramLabel = "files", description = "input smt2 files")
    public File[] files;

    @Option(names = { "-v", "--verbose" }, description = "be verbose")
    public boolean verbose = false;

    @Option(names = { "-o", "--option" }, description = "language-specific option", arity = "0..*")
    public List<String> options = new ArrayList<>();

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
    private boolean helpRequested = false;


    public static void main(String[] args) throws IOException {
        Main main = new Main();
        new CommandLine(main).parseArgs(args);
        main.run();
    }

    private void run() throws IOException {
        if (listLanguages) {
            System.out.println("Available target languages:");
            for (TemplateTranslation translation : translations) {
                System.out.println("  " + translation.getName());
            }
            return;
        }

        TemplateTranslation tt = null;
        for (TemplateTranslation translation : translations) {
            if (translation.getName().equalsIgnoreCase(language)) {
                tt = translation;
                break;
            }
        }

        if(tt == null) {
            throw new RuntimeException("Unknown target language: " + language);
        }

        ContractLib contractLib = null;
        if (files == null || files.length == 0) {
            var charStream = CharStreams.fromStream(System.in);
            contractLib = PreTranslator.translate(charStream);
        } else {
            for (File file : files) {
                ContractLib newCL = PreTranslator.translate(CharStreams.fromPath(file.toPath()));
                contractLib = ContractLib.merge(contractLib, newCL);
            }
        }

        tt.translate(contractLib);
    }

}

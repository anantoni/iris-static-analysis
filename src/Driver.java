/**
 * Created by anantoni on 1/5/2015.
 */
import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.KnowledgeBase;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.compiler.Parser;
import org.deri.iris.compiler.ParserException;
import org.deri.iris.optimisations.magicsets.MagicSets;
import org.deri.iris.storage.IRelation;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Driver {

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws EvaluationException {
        if (args.length != 1) {
            System.err.println("Please give input file");
            System.exit(-1);
        }

        File factsSubDir = new File(args[0]);
        String rootFactsDir = "../generated-facts/";
        String rootAnalysisLogicDir = "../analysis-logic/";
        String rootQueriesDir = "../queries/";
        String projectFactsDir = rootFactsDir + factsSubDir;

        Parser parser = new Parser();
        Map<IPredicate, IRelation> factMap = new HashMap<>();

        System.out.println("Scanning directory: " + projectFactsDir);
        final File factsDirectory = new File(projectFactsDir);
        if (factsDirectory.isDirectory())
            for (final File fileEntry : factsDirectory.listFiles()) {
            if (fileEntry.isDirectory())
                System.out.println("Omitting directory " + fileEntry.getPath());

            else {
                try {
                    System.out.println("Transforming file: " + fileEntry.getAbsolutePath());
                    BufferedReader factsReader = new BufferedReader(new FileReader(fileEntry));
                    PrintWriter writer = new PrintWriter(projectFactsDir + "/" + fileEntry.getName().replace(".facts", ".iris"), "UTF-8");
                    String line;
                    String predicateName = fileEntry.getName().replace(".facts", "").replace("-", ":");
                    StringBuilder transformedArgs = new StringBuilder();

                    while ((line = factsReader.readLine()) != null) {
                        String[] predicateArgs = line.split("\t");

                        for(int i = 0; i < predicateArgs.length; i++) {
                            if (i == 0) {
                                if (isInteger(predicateArgs[i]))
                                    transformedArgs.append(predicateArgs[i]);
                                else
                                    transformedArgs.append(predicateArgs[i]);
                            }
                            else {
                                if (isInteger(predicateArgs[i]))
                                    transformedArgs.append("," + predicateArgs[i]);
                                else
                                    transformedArgs.append(",\'" + predicateArgs[i] + "\'");
                            }

                        }
                        writer.println(predicateName + "(" + transformedArgs.toString() + ").");
                    }
                    writer.close();
                    factsReader.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Retrieve the facts and put all of them in factMap
                //factMap.putAll(parser.getFacts());
            }
        }
        else {
            System.err.println("Invalid facts directory path: " + projectFactsDir);
            System.exit(-1);
        }

        if (factsDirectory.isDirectory()) for (final File fileEntry : factsDirectory.listFiles()) {
            if (fileEntry.isDirectory())
                System.out.println("Omitting directory " + fileEntry.getPath());

            else {
                Reader factsReader;
                try {
                    factsReader = new FileReader(fileEntry);
                    parser.parse(factsReader);
                } catch (ParserException e) {
                    System.err.println("Parse exception in file: " + fileEntry.getName());
                    e.printStackTrace();
                    System.exit(-1);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                // Retrieve the facts and put all of them in factMap
                factMap.putAll(parser.getFacts());
            }
        }
        else {
            System.err.println("Invalid facts directory path: " + projectFactsDir);
            System.exit(-1);
        }

        File copyPropagationRuleFile = new File(rootAnalysisLogicDir + "micro-doop.iris");
        Reader rulesReader;
        try {
            rulesReader = new FileReader(copyPropagationRuleFile);
            parser.parse(rulesReader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }
        List<IRule> rules = parser.getRules();

        File queriesFile = new File(rootQueriesDir + "queries.iris");
        Reader queriesReader;
        try {
            queriesReader = new FileReader(queriesFile);
            parser.parse(queriesReader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParserException e) {
            e.printStackTrace();
        }
        // Retrieve the queries from the parsed file.
        List<IQuery> queries = parser.getQueries();

        // Create a default configuration.
        Configuration configuration = new Configuration();

        // Enable Magic Sets together with rule filtering.
        configuration.programOptmimisers.add(new MagicSets());
        for (IRule rule : rules)
            configuration.ruleSafetyProcessor.process(rule);

        // Create the knowledge base.
        IKnowledgeBase knowledgeBase = new KnowledgeBase(factMap, rules, configuration);

        // Evaluate all queries over the knowledge base.
        for (IQuery query : queries) {
            List<IVariable> variableBindings = new ArrayList<>();
            IRelation relation = knowledgeBase.execute(query, variableBindings);

            // Output the variables.
            System.out.println("\n" + query.toString() + "\n" + variableBindings);

            // Output each tuple in the relation, where the term at position i
            // corresponds to the variable at position i in the variable
            // bindings list.
            for (int i = 0; i < relation.size(); i++) {
                System.out.println(relation.get(i));
            }
        }

    }
}

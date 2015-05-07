import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by anantoni on 7/5/2015.
 */
public class TransformerThread implements Runnable {
    private File fileEntry;
    private String projectFactsDir;
    private Set<String> libraryFiles;

    public TransformerThread(File fileEntry, String projectFactsDir) {
        this.fileEntry = fileEntry;
        this.projectFactsDir = projectFactsDir;
        this.libraryFiles = new HashSet<>();
        libraryFiles.add("InitializedClass.facts");
        libraryFiles.add("ClassInitializer.facts");
        libraryFiles.add("AssignCompatible.facts");
        libraryFiles.add("MethodLookup.facts");
        libraryFiles.add("AssignNormalHeapAllocation.facts");
        libraryFiles.add("AssignAuxiliaryHeapAllocation.facts");
        libraryFiles.add("AssignContextInsensitiveHeapAllocation.facts");
        libraryFiles.add("MainMethodDeclaration.facts");
        libraryFiles.add("ImplicitReachable.facts");

    }

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

    @Override
    public void run() {
        System.out.println("Transforming file: " + fileEntry.getAbsolutePath());
        BufferedReader factsReader = null;
        try {
            factsReader = new BufferedReader(new FileReader(fileEntry));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(projectFactsDir + "/" + fileEntry.getName().replace(".facts", ".iris"), "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String line;
        String predicateName = fileEntry.getName().replace(".facts", "").replace("-", "");


        try {
            int groups = 0;
            while ((line = factsReader.readLine()) != null) {
                line = line.trim();

                String[] predicateArgs;
                if (libraryFiles.contains(fileEntry.getName()))
                    predicateArgs = line.split("\t");
                else
                    predicateArgs = line.split(", ");
                if (predicateArgs.length > groups || predicateArgs.length < groups && groups > 0)
                    continue;
                else if (groups == 0)
                    groups = predicateArgs.length;

                StringBuilder transformedArgs = new StringBuilder();
                for(int i = 0; i < predicateArgs.length; i++) {
                    if (i == 0) {
                        if (isInteger(predicateArgs[i]))
                            transformedArgs.append(predicateArgs[i]);
                        else
                            transformedArgs.append("\"" + predicateArgs[i] + "\"");
                    }
                    else {
                        if (isInteger(predicateArgs[i]))
                            transformedArgs.append("," + predicateArgs[i]);
                        else
                            transformedArgs.append(",\"" + predicateArgs[i] + "\"");
                    }

                }
                writer.println(predicateName + "(" + transformedArgs.toString() + ").");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.close();
        try {
            factsReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

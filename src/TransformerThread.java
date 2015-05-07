import java.io.*;

/**
 * Created by anantoni on 7/5/2015.
 */
public class TransformerThread implements Runnable {
    private File fileEntry;
    private String projectFactsDir;

    public TransformerThread(File fileEntry, String projectFactsDir) {
        this.fileEntry = fileEntry;
        this.projectFactsDir = projectFactsDir;
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
        String predicateName = fileEntry.getName().replace(".facts", "").replace("-", ":");
        StringBuilder transformedArgs = new StringBuilder();

        try {
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

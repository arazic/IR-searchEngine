import javax.print.Doc;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.stream.Stream;

public class ReadFile {

    private String corpusPath;
    private String outPath;
    private boolean stemming;
    private static volatile int totalDocNum;
    private Parse Parse;

    public ReadFile(String corpusPath, String outPath, boolean stemming) {
        this.corpusPath = corpusPath;
        this.outPath = outPath;
        this.stemming = stemming;
        this.totalDocNum = 0;
    }

    public ReadFile(String corpusPath, Parse parse) {
        this.corpusPath = corpusPath;
        this.outPath = "";
        this.stemming = false;
        this.totalDocNum = 0;
        Parse= parse;

    }


    public void createDocuments(){
        File path= new File(this.corpusPath);
        getToFile(path);
    }

    public void getToFile(File path){
        for ( File fileEntry : path.listFiles()) {
            if (fileEntry.isDirectory()) {
                getToFile(fileEntry);
            } else {
                System.out.println(fileEntry.getName());
                StringBuilder sb = new StringBuilder();

                try (BufferedReader br = new BufferedReader(new FileReader(fileEntry))) {
                    String line;
                    Document Document;
                    while ((line = br.readLine()) != null) {
                        if(line.equals("</DOC>")){
                            sb.append(line).append("\n");
                            Document= new Document(sb);
                            Parse.add(Document);
                            sb.setLength(0);
                        }
                        else
                            sb.append(line).append("\n");
                    }

                } catch (IOException e) {
                    System.err.format("IOException: %s%n", e);
                }

                System.out.println(sb);

            }
        }
    }

}

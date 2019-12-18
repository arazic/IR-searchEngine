package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ReadFile {

    private String corpusPath;
    private String outPath;
    private boolean stemming;
    private static volatile int totalDocNum;
    private Parse parse;

    public ReadFile(String corpusPath, String outPath, boolean stemming) {
        this.corpusPath = corpusPath;
        this.outPath = outPath;
        this.stemming = stemming;
        this.totalDocNum = 0;
    }

    public ReadFile(String corpusPath,Parse parse)
    {
        this.corpusPath = corpusPath;
        this.outPath = "";
        this.stemming = false;
        this.totalDocNum = 0;
        this.parse = parse;
    }

    public void readStopWords()
    {
        String sPath = this.corpusPath+"/stopWords.txt";
        File path= new File(sPath);
        String line;
        List <String> stopWords = new LinkedList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(path)))
        {
            while ((line = br.readLine()) != null)
            {
                stopWords.add(line);
            }
        }
        catch (IOException i)
        {
            i.printStackTrace();
        }
        parse.createStopWords(stopWords);
    }


    public void createDocuments(){
        readStopWords();
        File path= new File(this.corpusPath);
        getToFile(path);
        parse.setFinishDoc(true);
    }

    public void getToFile(File path){
        for ( File fileEntry : path.listFiles())
        {
            if (fileEntry.isDirectory()) {
                getToFile(fileEntry);
            } else {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(fileEntry)))
                {
                    String line;
                    while ((line = br.readLine()) != null)
                    {
                        if(line.equals("</DOC>")){
                            sb.append(line).append("\n");
                            parse.createDocument(sb);
                            sb.setLength(0);
                        }
                        else
                            sb.append(line).append("\n");
                    }
                } catch (IOException e) {
                    System.err.format("IOException: %s%n", e);
                }
            }
        }
    }
}

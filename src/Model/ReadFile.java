package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * read each document in the corpus and transform it to parse
 * create a collection of stop words ans transfer to parse
 *
 */
public class ReadFile {

    private String corpusPath;
    private static int totalDocNum;
    private Parse parse;



    public ReadFile(String corpusPath,Parse parse)
    {
        this.corpusPath = corpusPath;
        this.totalDocNum = 0;
        this.parse = parse;
    }

    /**
     * read all stop-words and transfer to parse
     */

    private void readStopWords()
    {
        String sPath = this.corpusPath+"/stopWords.txt";
        File path= new File(sPath);
        String line;
        HashSet<String> sWords = new HashSet<>();
        try(BufferedReader br = new BufferedReader(new FileReader(path)))
        {
            while ((line = br.readLine()) != null)
            {
                sWords.add(line);
            }
        }
        catch (IOException i)
        {
            i.printStackTrace();
        }
        parse.createStopWords(sWords);
    }

    /**
     * the main functionality
     * create stop words collection'
     * read all document and transfer it to parse document by document
     * sends an end message at the end of the process
     */
    public void createDocuments(){
        readStopWords();
        File path= new File(this.corpusPath);
        getToFile(path);
        parse.setFinishDoc(true);
    }

    /**
     * read all files in corpus
     * @param path
     */
    private void getToFile(File path){
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
                            totalDocNum++;
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

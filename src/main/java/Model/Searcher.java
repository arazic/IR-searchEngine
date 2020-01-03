package Model;

import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.*;
import java.util.*;

public class Searcher {
    Ranker ranker;
    Parse parser;
    boolean stemming;
    boolean semantic;
    TreeMap<String, TreeMap<String, Integer>> tremsInDoc ; // docName, <Term-"tf">
    TreeMap<String, Integer> termsDf ; // Term, df-how manyDocs
    TreeMap<String, Integer> allRelevantDocs ; // docName, size - |d|
    int totalCurposDoc ;
    double averageDocLength ;

    public Searcher(Parse parser) {
        ranker = new Ranker();
        this.parser = parser;
    }

    public void readQueriesFromData(String pathToQueries)
    {
        File path= new File(pathToQueries);
        String line;
        StringBuilder sb = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new FileReader(path)))
        {
            while ((line = br.readLine()) != null)
            {
                if(line.equals("</DOC>")){
                    sb.append(line).append("\n");
                    sb.setLength(0);
                }
                else
                    sb.append(line).append("\n");
            }
        }
        catch (IOException i)
        {
            i.printStackTrace();
        }
    }


    private void parseQueryFromData(StringBuilder stringBuilder)
    {
        String text =stringBuilder.toString();
        String [] tokens=text.split(" ");
        StringBuilder query = new StringBuilder();
        int index=0;
        int queryId=0;
        while(index<tokens.length)
        {
            if(tokens[index].equals("<num>"))
            {

            }
        }
    }

    public void search(String postingPath, String query, boolean stemming, boolean semantic) {
        this.stemming=stemming;
        this.semantic=semantic;
        TreeMap<String, Integer> termsQuery = parser.parseQuery(query, stemming); //tremName, tf in query
        if (semantic) {
            TreeMap<String, Integer> semanticTerms = getWords(termsQuery);
            termsQuery.putAll(semanticTerms);
        }
        infoFromPosting(postingPath, termsQuery);
        LinkedList<String> rankedDicuments=ranker.rankDocuments();
    }




    public String getEntities(String docName) {
        return null;
    }


    private TreeMap<String, Integer> getWords(TreeMap<String, Integer> termsQuery) {
        for (String terms : termsQuery.keySet()) {
        }
        return null;
    }

    public void infoFromPosting(String postingPath, TreeMap<String, Integer> termsQuery) {
        try {
            tremsInDoc= new TreeMap<>();
            termsDf= new TreeMap<>();// Term, df-how manyDocs
            allRelevantDocs = new TreeMap<>(); // docNam
            totalCurposDoc=0;
            averageDocLength=0;

            String  s="";
            if(stemming)
               s="With";
            else
                s="No";

            FileReader numberFile = new FileReader((postingPath) + "/finalPostingNumbers"+s+"Stemming.txt");
            FileReader capitalFile = new FileReader((postingPath) + "/finalPostingCapital"+s+"Stemming.txt");
            FileReader lowerFile1 = new FileReader((postingPath) + "/finalPostingLowert"+s+"StemmingD.txt");
            FileReader lowerFile2 = new FileReader((postingPath) + "/finalPostingLowert"+s+"StemmingP.txt");
            FileReader lowerFile3 = new FileReader((postingPath) + "/finalPostingLowert"+s+"StemmingZ.txt");

            BufferedReader lowerReader1 = new BufferedReader(lowerFile1);
            BufferedReader lowerReader2 = new BufferedReader(lowerFile2);
            BufferedReader lowerReader3 = new BufferedReader(lowerFile3);
            BufferedReader numberReader = new BufferedReader(numberFile);
            BufferedReader capitalReader = new BufferedReader(capitalFile);


            for (String term : termsQuery.keySet()) {
                if (term.toUpperCase().equals(term)){
                    String line= capitalReader.readLine();
                    while (line!=null){
                        String termName= line.substring(0,line.indexOf("!"));
                        if(termName.equals(term)){
                            rePostingTerms(line);
                            System.out.println(term);
                            break;
                        }
                        line=capitalReader.readLine();
                    }
                }
                else if (term.charAt(0) > '9' || term.charAt(0) < '0') {
                    if (term.charAt(0) <= 'd') {
                        String line= lowerReader1.readLine();
                        while (line!=null){
                            String termName= line.substring(0,line.indexOf("!"));
                            if(termName.equals(term)){
                                rePostingTerms(line);
                                System.out.println(term);
                                break;
                            }
                            line=lowerReader1.readLine();
                        }
                    } else if (term.charAt(0) <= 'p') {
                        String line= lowerReader2.readLine();
                        while (line!=null){
                            String termName= line.substring(0,line.indexOf("!"));
                            if(termName.equals(term)){
                                rePostingTerms(line);
                                System.out.println(term);
                                break;
                            }
                            line=lowerReader2.readLine();
                        }
                    }
                    else {
                        String line= lowerReader3.readLine();
                        while (line!=null){
                            String termName= line.substring(0,line.indexOf("!"));
                            if(termName.equals(term)){
                                rePostingTerms(line);
                                System.out.println(term);
                                break;
                            }
                            line=lowerReader3.readLine();
                        }
                    }
                } else {

                    String line= numberReader.readLine();
                    while (line!=null){
                        String termName= line.substring(0,line.indexOf("!"));
                        if(termName.equals(term)){
                            rePostingTerms(line);
                            System.out.println(term);
                            break;
                        }
                        line=numberReader.readLine();
                    }
                }
            }

            rePostingDocs(postingPath);
            ranker.setData(tremsInDoc, termsDf, allRelevantDocs, totalCurposDoc, averageDocLength);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void rePostingDocs(String postingPath) {
        try {

        String  s="";
        if(stemming) {
            s="With";
        }
        else {
            s="No";
        }

        FileReader Documents = new FileReader((postingPath) + "/postingDocuments"+s+"Stemming.txt");
        BufferedReader bufferedDocs= new BufferedReader(Documents);
        String line= bufferedDocs.readLine();
        while (line!=null) {
        Set set = allRelevantDocs.entrySet();
        Iterator iterator = set.iterator();
        Map.Entry entry = (Map.Entry) iterator.next();
        while (entry!=null) {
            String docInPosting= line.substring(0,line.indexOf("!"));
            if (entry.getKey().equals(docInPosting)){
                String [] parseDoc=  StringUtils.split(line,"!");
                allRelevantDocs.put(docInPosting, parseDoc[parseDoc.length-1]);
                if(iterator.hasNext())
                    entry = (Map.Entry) iterator.next();
                else
                    entry=null;
            }
            line=bufferedDocs.readLine();
            if(line.charAt(0)=='~'){
                totalCurposDoc= Integer.parseInt(line.substring(1));
                line=bufferedDocs.readLine();
                averageDocLength= Integer.parseInt(line.substring(1));
                line=bufferedDocs.readLine();
                break;
            }
          }
        }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void rePostingTerms(String term) {

        String[] miniParse= StringUtils.split(term,"!");
        String termName= miniParse[0];

        termsDf.put(termName,miniParse[2]); //insert df of term

        String[] splitDocs= StringUtils.split(miniParse[1],",");
        for (int i=0; i<splitDocs.length; i++){
            String docName= StringUtils.substring(splitDocs[i],0,splitDocs[i].indexOf(":"));
            String tfInDoc=StringUtils.substring(splitDocs[i],splitDocs[i].indexOf(":")+1);
            allRelevantDocs.put(docName, "toUpdate");
            if(tremsInDoc.containsKey(docName)){
                TreeMap<String,String> temp= tremsInDoc.get(docName);
                temp.put(termName,tfInDoc);
                tremsInDoc.put(docName,temp);
            }
            else{
                TreeMap<String,String> temp= new TreeMap<>();
                temp.put(termName,tfInDoc);
                tremsInDoc.put(docName,temp);
            }

        }
    }
}

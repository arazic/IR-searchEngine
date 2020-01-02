package Model;

import java.awt.*;
import java.io.*;
import java.util.TreeMap;
import java.util.TreeSet;

public class Searcher {
    Ranker ranker;
    Parse parser;


    public Searcher(Parse parser) {
        ranker = new Ranker();
        this.parser = parser;
    }

    public void search(String postingPath, String query, boolean isStemming, boolean semantic) {
        TreeMap<String, Integer> termsQuery = parser.parseQuery(query, isStemming); //tremName, tf in query
        if (semantic) {
            TreeMap<String, Integer> semanticTerms = getWords(termsQuery);
            termsQuery.putAll(semanticTerms);
        }
        infoFromPosting(postingPath, termsQuery);
        List top50Docs = ranker.rank();
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
            FileReader numberFile = new FileReader((postingPath) + "/finalPostingNumbersWithStemming" + ".txt");
            FileReader capitalFile = new FileReader((postingPath) + "/finalPostingCapitalWithStemming" + ".txt");
            FileReader lowerFile1 = new FileReader((postingPath) + "/finalPostingLowerWithStemmingD" + ".txt");
            FileReader lowerFile2 = new FileReader((postingPath) + "/finalPostingLowerWithStemmingP" + ".txt");
            FileReader lowerFile3 = new FileReader((postingPath) + "/finalPostingLowerWithStemmingZ" + ".txt");
            BufferedReader lowerWriter1 = new BufferedReader(lowerFile1);
            BufferedReader lowerWriter2 = new BufferedReader(lowerFile2);
            BufferedReader lowerWriter3 = new BufferedReader(lowerFile3);
            BufferedReader numberWriter = new BufferedReader(numberFile);
            BufferedReader capitalWriter = new BufferedReader(capitalFile);


            for (String term : termsQuery.keySet()) {

            }

            TreeMap<String, TreeMap<String, String>> tremsInDoc = null; // docName, <Term-"tf">

            TreeMap<String, String> termsDf = null; // Term, df-how manyDocs

            TreeMap<String, String> allRelevantDocs = null; // docName, size - |d|
            int totalCurposDoc = 0;
            double averageDocLength = 0;


            ranker.setData(tremsInDoc, termsDf, allRelevantDocs, totalCurposDoc, averageDocLength);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }
}

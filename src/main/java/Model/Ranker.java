package Model;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class Ranker {

    private int corpusSize;
    private double avergeDocSize;
    private TreeMap<String,Integer> termsDocumentFrequency;
    private TreeMap<String,Integer> termsQueryFrequency;
    private TreeMap<String,TreeMap<String,Integer>> documentTermsFrequency;
    private TreeMap<String,Integer> documentsSize;
    private int k;
    private double b;

    private double rank(int docLength,TreeMap<String,Integer> termsTF)
    {
        double rank=0;
        for (Map.Entry entry:termsTF.entrySet())
        {
            int termInQueryfreq= termsQueryFrequency.get(entry.getKey());
            int termInDocFreq=(int)entry.getValue();
            int termDocumentsFreq=termsDocumentFrequency.get(entry.getKey());
            rank+=termInQueryfreq*((k+1)*termInDocFreq)/(termInDocFreq+k*(1-b+b*(docLength/avergeDocSize)))*Math.log10((corpusSize+1)/termDocumentsFreq);
        }
        return rank;
    }

    public LinkedList<String> rankDocuments()
    {
        TreeMap<Double,LinkedList<String>> rankDocuments = new TreeMap<>();
        LinkedList<String> documentsByRank= new LinkedList<>();
        for (Map.Entry entry:documentsSize.entrySet())
        {
            String docName=(String)entry.getKey();
            int docSize=(int)entry.getValue();
            double rank =rank(docSize,documentTermsFrequency.get(docName));
            if(rankDocuments.containsKey(rank))
            {
                LinkedList<String> documents=rankDocuments.get(rank);
                documents.add(docName);
                rankDocuments.replace(rank,documents);
            }
            else
            {
                LinkedList<String> documents = new LinkedList<>();
                rankDocuments.put(rank,documents);
            }
        }
        int totalDocuments=0;
        for (double rank:rankDocuments.keySet())
        {
            LinkedList<String> rankList=rankDocuments.get(rank);
            for (String document:rankList)
            {
                if(totalDocuments<50)
                {
                    documentsByRank.addLast(document);
                    totalDocuments++;
                }
            }
        }
        return documentsByRank;
    }



    public void setData(TreeMap<String, TreeMap<String,Integer>> tremsInDoc, // docName, <Term-"tf">
            TreeMap<String, Integer> termsDf, // Term, df-how manyDocs
            TreeMap<String, Integer> allRelevantDocs , TreeMap<String,Integer> termsQueryFrequency) // docName, size - |d|
    {
        documentTermsFrequency=tremsInDoc;
        termsDocumentFrequency=termsDf;
        documentsSize=allRelevantDocs;
        this.termsQueryFrequency=termsQueryFrequency;
    }

}





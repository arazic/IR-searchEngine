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
    private double k;
    private double b;

    public Ranker ()
    {
        k=45;
        b=0.25;
    }

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
        TreeMap<String,Double> rankDocuments = new TreeMap<>();
        TreeMap<String,Double> documentsByRank= new TreeMap<>();
        for (Map.Entry entry:documentsSize.entrySet())
        {
            String docName=(String)entry.getKey();
            int docSize=(int)entry.getValue();
            double rank =rank(docSize,documentTermsFrequency.get(docName));
            rankDocuments.put(docName,rank);
        }
        double maxRank=0;
        String maxdoc="";
        for(int i=0;i<50;i++)
        {
            for (Map.Entry entry:rankDocuments.entrySet())
            {
                if((double)entry.getValue()>maxRank)
                {
                    maxdoc=(String)entry.getKey();
                    maxRank=(double) entry.getValue();
                }
            }
            documentsByRank.put(maxdoc,maxRank);
            rankDocuments.remove(maxdoc);
            maxdoc="";
            maxRank=0;
        }
        LinkedList<String> ans = new LinkedList<>(documentsByRank.keySet());
        return ans;
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

    public void setCorpusSize(int size)
    {
        corpusSize=size;
    }

    public void setAvergeDocSize(double avergeDocSize)
    {
        this.avergeDocSize=avergeDocSize;

    }

}





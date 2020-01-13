package Model;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class Ranker {

    private int corpusSize;
    private double averageDocSize;
    private TreeMap<String,Integer> termsDocumentFrequency;
    private TreeMap<String,Integer> termsQueryFrequency;
    private TreeMap<String,TreeMap<String,Integer>> documentTermsFrequency;
    private TreeMap<String,Integer> documentsSize;
    private double k;
    private double b;

    public Ranker ()
    {
        k=1.25;
        b=0.55;
    }

    /**
     *
     */
    public void cleanRanker()
    {
        termsDocumentFrequency.clear();
        termsQueryFrequency.clear();
        documentTermsFrequency.clear();
        documentsSize.clear();
    }

    /**
     *  run the BM25 algorithm to find the rank of one doc
     * @param docLength
     * @param termsTF
     * @return
     */
    private double rank(int docLength,TreeMap<String,Integer> termsTF)
    {
        double rank=0;
        for (Map.Entry entry:termsTF.entrySet())
        {
            int termInQueryfreq= termsQueryFrequency.get(entry.getKey());
            int termInDocFreq=(int)entry.getValue();
            int termDocumentsFreq=termsDocumentFrequency.get(entry.getKey());
            rank+=termInQueryfreq*((k+1)*termInDocFreq)/(termInDocFreq+k*(1-b+b*(docLength/ averageDocSize)))*Math.log10((corpusSize+1)/termDocumentsFreq);
        }
        return rank;
    }

    /**
     * rank all the documents and returns the top 50
     * @return
     */
    public LinkedList<String> rankDocuments()
    {
        TreeMap<String,Double> rankDocuments = new TreeMap<>();
        LinkedList<String> documentsByRank= new LinkedList<>();
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
            documentsByRank.addLast(maxdoc);
            rankDocuments.remove(maxdoc);
            maxdoc="";
            maxRank=0;
        }
        cleanRanker();
        return documentsByRank;
    }

    /**
     * set all the needed info to do the rank action
     * @param tremsInDoc
     * @param termsDf
     * @param allRelevantDocs
     * @param termsQueryFrequency
     */
    public void setData(TreeMap<String, TreeMap<String,Integer>> tremsInDoc, // docName, <Term-"tf">
            TreeMap<String, Integer> termsDf, // Term, df-how manyDocs
            TreeMap<String, Integer> allRelevantDocs ,  // docName, size - |d|
                        TreeMap<String,Integer> termsQueryFrequency ) // freq of term in the query
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

    public void setAverageDocSize(double averageDocSize)
    {
        this.averageDocSize = averageDocSize;
    }

}





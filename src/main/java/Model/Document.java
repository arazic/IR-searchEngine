package Model;


import java.util.List;

/**
 * document class represent document in the corpus
 * the document save max frequency, length, number of unique terms
 */
public class Document {

    private String docName;
    private int maxTerm;
    private int uniqeTermsNum;
    private int totalTerms;


    private String[] topEntities;

    public Document()
    {
        totalTerms=0;
    }


    public String getDocName()
    {
        return this.docName;
    }

    public int getMaxTerm() {
        return maxTerm;
    }

    public int getUniqeTermsNum() {
        return uniqeTermsNum;
    }


    public int getTotalTerms()
    {
        return totalTerms;
    }

    public void setTotalTerms(int terms)
    {
        totalTerms=terms;
    }

    public void setDocName(String name)
    {
        if(name!=null && name !="")
        {
            docName =name;
        }
    }


    public void setMaxTerm(int max)
    {
     this.maxTerm=max;
    }

    public void setUniqeTermsNum(int uniqeTermsNum){
        this.uniqeTermsNum= uniqeTermsNum;
    }

    public void setEntities(String[] entities)
    {
       topEntities=entities;
    }

    public String getTopEntitiesToPosting() {
        String ans="";
        for (int i=0; i<this.topEntities.length; i++){
            if(topEntities[i]!=null){
                ans= ans+ topEntities[i] +"|";
            }
        }
        return ans;

    }
}

package Model;



public class Document {

    private String docName;
    private int maxTerm;
    private int uniqeTermsNum;
    private int totalTerms;

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
}

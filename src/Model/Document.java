package Model;

import java.util.*;

public class Document {

    private String docName;
    private String header;
    private String popularTerm;
    private int maxTerm;
    private int uniqeTermsNum;



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
    public void setHeader(String header)
    {
        this.header=header;
    }


}

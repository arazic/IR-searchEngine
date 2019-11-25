import java.util.*;

public class Document {

    private String docName;
    private String header;
    private String popularTerm;
    private int maxTerm;
    private int uniqeTermsNum;
    private TreeMap <String,Integer> docTerms;

    public Document()
    {
        docTerms = new TreeMap<>();
    }

    public void addTermsToDoc (HashMap<String,Integer> allterms)
    {
        Term currTerm;
        Iterator it = allterms.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            currTerm=new Term((String)(pair.getKey()),(int)pair.getValue());
            currTerm.addDocToTerm(docName);
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public void setDocName(String name)
    {
        if(name!=null && name !="")
        {
            docName =name;
        }
    }

    public void setHt(String ht)
    {

    }

    public void setMaxTerm()
    {
        String maxString="";
        int maxFrequency=0;
        for (HashMap.Entry<String,Integer> entry : docTerms.entrySet())
        {
            if(entry.getValue()>maxFrequency)
            {
                maxString=entry.getKey();
                maxFrequency=entry.getValue();
            }
        }
        this.maxTerm=maxFrequency;
        this.popularTerm=maxString;


    }

    public void setHeader(String header)
    {
        this.header=header;
    }

}

import java.util.HashMap;
import java.util.Set;

public class Document {

    private String docName;
    private String header;
    private String popularTerm;
    private int maxTerm;
    private int uniqeTermsNum;
    private HashMap <String,Integer> termsFrequency;



    public Document()
    {
        termsFrequency = new HashMap<>();
    }

    public void add (String term)
    {
        if(termsFrequency.containsKey(term))
        {
            termsFrequency.replace(term,termsFrequency.get(term)+1);
            return;
        }
        termsFrequency.put(term,1);
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
        for (HashMap.Entry<String,Integer> entry : termsFrequency.entrySet())
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

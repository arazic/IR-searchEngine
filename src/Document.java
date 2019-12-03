import java.util.*;

public class Document {

    private String docName;
    private String header;
    private String popularTerm;
    private int maxTerm;
    private int uniqeTermsNum;
    private StringBuilder docLang;
    private StringBuilder articleType;
    private TreeMap <String,Integer> docTerms;

    public Document()
    {
        docTerms = new TreeMap<>();
        docLang= new StringBuilder();
        articleType= new StringBuilder();
    }


    public String getDocName()
    {
        return this.docName;
    }

    public void printDoc()
    {
        System.out.println("<DOCNO> "+docName+" </DOCNO");
        System.out.println("<T1> "+header+ " </T1>");
        System.out.println("<TERMS>");
        Iterator<Map.Entry<String, Integer>> itr = docTerms.entrySet().iterator();
        while(itr.hasNext())
        {
            Map.Entry<String, Integer> entry = itr.next();
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
    }

    public void setDocLang(StringBuilder docLang) {
        this.docLang = docLang;
    }

    public void setArticleType(StringBuilder articleType) {
        this.articleType = articleType;
    }


    public void setTerms(HashMap<String,Integer> notOrderedMap)
    {
      notOrderedMap.forEach((key,value) -> docTerms.put(key,value));
    }

    public void addTermsToDoc (HashMap<String,Integer> allterms)
    {
        Term currTerm;
        Iterator it = allterms.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
           // System.out.println(pair.getKey() + " = " + pair.getValue());
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

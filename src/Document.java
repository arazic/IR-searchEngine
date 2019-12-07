import java.util.*;

public class Document {

    private String docName;
    private String header;
    private String popularTerm;
    private int maxTerm;
    private int uniqeTermsNum;
    private StringBuilder docLang;
    private StringBuilder articleType;
  //  private TreeMap <String,Integer> docTerms;


    public Document()
    {
        //docTerms = new TreeMap<>();
        docLang= new StringBuilder();
        articleType= new StringBuilder();
    }

   // public TreeMap<String, Integer> getDocTerms() {
    //    return docTerms;
    //}


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
//    public void printDoc()
//    {
//        System.out.println("<DOCNO> "+docName+" </DOCNO");
//        System.out.println("<T1> "+header+ " </T1>");
//        System.out.println("<TERMS>");
//        Iterator<Map.Entry<String, Integer>> itr = docTerms.entrySet().iterator();
//        while(itr.hasNext())
//        {
//            Map.Entry<String, Integer> entry = itr.next();
//            System.out.println(entry.getKey() + " " + entry.getValue());
//        }
//    }

    public void setDocLang(StringBuilder docLang) {
        this.docLang = docLang;
    }

    public void setArticleType(StringBuilder articleType) {
        this.articleType = articleType;
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

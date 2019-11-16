import java.util.HashMap;

public class Document {

    private String docNo;
    private String Ht;
    private String header;
    private int maxTerm;
    private int uniqeTermsNum;
    private HashMap <Term,String> map= new HashMap<>();
    private StringBuilder content;


    public Document(StringBuilder content) {
        this.content = content;
    }

    public StringBuilder getContent() {
        return content;
    }

    public void setDocNo(String DocNo) {
        docNo= DocNo;
    }

    public void setHt(String ht) {
        Ht = ht;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}

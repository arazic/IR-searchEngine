import java.util.HashMap;

public class Document {

    String id;
    String header;
    int maxTerm;
    int uniqeTermsNum;
    HashMap <Term,String> map= new HashMap<>();
    StringBuilder content;


    public Document(StringBuilder content) {
        this.content = content;
    }


}

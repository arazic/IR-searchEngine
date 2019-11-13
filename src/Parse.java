import java.util.HashSet;

public class Parse {

    Document currDoc;
    HashSet<Document> collection;

    public Parse() {
        collection= new HashSet<>();
    }

    public void add(Document currDoc){
        collection.add(currDoc);

    }


}

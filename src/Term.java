import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Term
{
    private String term;
    private int freq;
    private Set<String> documents;


    public Term( String term, int freq) {
        this.term = term;
        this.freq = freq;
        this.documents=new TreeSet<>();
    }

    public void addDocToTerm( String doc) {
        documents.add(doc);
    }


}

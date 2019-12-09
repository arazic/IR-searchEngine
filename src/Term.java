import java.util.Set;
import java.util.TreeSet;

public class Term implements Comparable<Term>
{
    private String stringTerm;
    private int freq;
    private Set<String> documents;


    public Term(String term, int freq, String document) {
        this.stringTerm = term;
        this.freq = freq;
        this.documents=new TreeSet<>();
        documents.add(document+":"+freq);
    }

    public Term(String term, int freq) {
        this.stringTerm = term;
        this.freq = freq;
        this.documents=new TreeSet<>();
    }

    public String getStringTerm() {
        return stringTerm;
    }

    public Set<String> getDocuments() {
        return documents;
    }

    public void addDocToTerm(String sDoc, int frequency)
    {
        documents.add(sDoc+":"+frequency);
    }

    public int getFreq()
    {
        return this.freq;
    }

    public int compareTo(Term term)
    {
        return this.stringTerm.compareTo(term.stringTerm);
    }

    public int compareTo(String stringTerm)
    {
        return this.stringTerm.compareTo(stringTerm);
    }

    public void setFreq(int freq)
    {
        this.freq = freq;
    }


    public boolean equals(Term term, String sTerm)
    {
        if(term.stringTerm.equals(sTerm))
        {
            return true;
        }
        return false;
    }


}

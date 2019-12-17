import java.util.Set;
import java.util.TreeSet;

public class Term implements Comparable<Term>
{
    private String stringTerm;
    private int freq;// df- number of doc is appear in
    private int totalTf;// how many times in all the corpus
    private String title;
    private Set<String> documents;


    public Term(String term, int freq, String document) {
        this.stringTerm= term;
        this.freq = freq;
        this.documents=new TreeSet<>();
        this.totalTf = freq;
        documents.add(document+":"+freq);

    }


    public String getStringTerm() {
        return stringTerm;
    }

    public Set<String> getDocuments() {
        return documents;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addDocToTerm(String sDoc, int frequency)
    {
        documents.add(sDoc+":"+frequency);
        totalTf = totalTf +frequency;
    }

    public void setTotalTf(int totalTf) {
        this.totalTf = totalTf;
    }

    public int getTotalTf() {
        return totalTf;
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
    public int compareNoSensitivity(String stringTerm)
    {
        return this.stringTerm.toLowerCase().compareTo(stringTerm.toLowerCase());
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

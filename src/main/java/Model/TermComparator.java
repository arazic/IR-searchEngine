package Model;

import java.util.Comparator;

public class TermComparator implements Comparator<Term>
{

    @Override
    public int compare(Term t1, Term t2)
    {
        if(t1.getFreq()>t2.getFreq())
        {
            return 1;
        }
        return -1;
    }
}

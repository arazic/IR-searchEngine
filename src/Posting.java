import java.util.*;

public class Posting {


    private int docFILE= 400;
    private int postingSIZE= 8;
    private static TreeSet<Term> mergeTerms = new TreeSet<>();
    private int docCounter; // how many docs are merging in the memory;
    private int docFileCounter; // how many doc in a posting file;
    private String pointerDocPosting; //<postingFileName><postingPlaceInFile>

    //create posting file
    public void postingDoc(Document document) {
        //docDetailes.writeToFile("/path");
        //document.writeToFile("/path");
        Indexer.addDoc(document.getDocName(),pointerDocPosting);
    }

    public void postingTerms(HashMap<String, Integer> docTerms, String docName) {
        if(docCounter<=postingSIZE){
            TreeMap<String,Integer> mapTerms = new TreeMap<>(docTerms);
            Set<Term> tempTerms= new TreeSet<>();
            Iterator<String> itNew = mapTerms.keySet().iterator();
            Iterator <Term> itOld = mergeTerms.iterator();
            Term newTerm;
            Term term = itOld.next();
            String termString = itNew.next();
            boolean finish =false;
            int freq=0;
            while (itNew.hasNext() && itOld.hasNext())
            {
                if(term.compareTo(termString)==0)
                {
                    freq=docTerms.get(termString);
                    term.setFreq(term.getFreq()+freq);
                    term.addDocToTerm(docName,freq); // <docName:freq>
                    term = itOld.next();
                    termString = itNew.next();
                }
                else if(term.compareTo(termString)==-1)
                {
                    term = itOld.next();
                }
                else if(term.compareTo(termString)==1)
                {
                    tempTerms.add(newTerm = new Term(termString,docTerms.get(termString),docName));
                    termString = itNew.next();
                }
            }
            while (itNew.hasNext()|| !finish)
            {
                tempTerms.add(newTerm = new Term(termString,docTerms.get(termString),docName));
                if(itNew.hasNext())
                {
                    termString = itNew.next();
                }
                else
                {
                    finish=true;
                }

            }
            mergeTerms.addAll(tempTerms);
            docCounter++;
        }
        if(docCounter>postingSIZE)
        {
            //write terms to postig file
            docCounter=0;
            //empaty mergeTerms for a new start merging
            // create a new posting file
        }
    }








/*
    public void test ()
    {
        Term t1 = new Term("t1",1,"doc1");
        Term t2 = new Term(("t2"),1,"doc1");
        Term t3 = new Term("t3",1,"doc3");
        Term t4 = new Term(("t4"),1,"doc4");
        terms.add(t1);
        terms.add(t2);
        terms.add(t3);
        terms.add(t4);
        allDocTerms.put("t5",4);
        allDocTerms.put("t",2);
        allDocTerms.put("t2",5);
        currDoc = new Document();
        currDoc.setDocName("doc2");
        mergeTerms();
    }

*/


}

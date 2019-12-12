import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Posting {

    private int docFILE= 50; // how many doc in a postingDoc file
    private int chunkPostingSIZE = 20; // how many doc in a postingTerm file
    private int chunksCount;
    private static TreeSet<Term> mergeTerms;
    private int docCounter; // how many docs are merging in the memory;
    private int docFileCounter; // how many doc in a posting file;
    private String pointerDocPosting; //<postingFileName><postingPlaceInFile>
    private String postingPath;
    private BufferedWriter writerToPostingDoc;


    public Posting(String postingPath){
        mergeTerms = new TreeSet<>();
        docFileCounter=1;
        pointerDocPosting="";
        this.postingPath=postingPath;
        chunksCount=1;
        try {
            writerToPostingDoc = new BufferedWriter(new FileWriter(postingPath + "/documents/postingDoc" + chunksCount + ".txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //create posting file
    public void postingDoc(Document document) {

        try {
            if(docFileCounter<=docFILE) {
                writerToPostingDoc.append(document.getMaxTerm() + " " + document.getUniqeTermsNum());
                writerToPostingDoc.append('\n');
                pointerDocPosting = String.valueOf(chunksCount) + "," + docFileCounter;
                docFileCounter++;
                //docDetailes.writeToFile("/path");
                //document.writeToFile("/path");
                Indexer.addDoc(document.getDocName(), pointerDocPosting);

            }
            else {
                writerToPostingDoc.flush();
                chunksCount++;
                docFileCounter=1;
                writerToPostingDoc = new BufferedWriter(new FileWriter(postingPath + "/documents/postingDoc" + chunksCount + ".txt"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void postingTerms(HashMap<String, Integer> docTerms, String docName) {
        if(docCounter<= chunkPostingSIZE){
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
        if(docCounter> chunkPostingSIZE)
        {
            //write terms to postig file
            docCounter=0;
            //empaty mergeTerms for a new start merging
            // create a new posting file
        }
    }
}

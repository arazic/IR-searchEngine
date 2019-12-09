import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Posting {

    private int docFILE= 100; // how many doc in a postingDoc file
    private int chunkPostingSIZE = 130; // how many doc in a postingTerm file
    private int chunksCount;
    private static TreeSet<Term> mergeTerms;
    private int docCounter; // how many docs are merging in the memory;
    private int docFileCounter; // how many doc in a posting file;
    private int termFileCounter; // how many term in a posting file;
    private String pointerDocPosting; //<postingFileName><postingPlaceInFile>
    private String pointerTermPosting; //<postingFileName><postingPlaceInFile>
    private String postingPath;
    private BufferedWriter writerToPostingDoc;
    private BufferedWriter writerToPostingTerm;


    public Posting(String postingPath){
        mergeTerms = new TreeSet<>();
        docFileCounter=1;
        termFileCounter=1;
        pointerDocPosting="";
        pointerTermPosting="";
        this.postingPath=postingPath;
        chunksCount=1;
        try {
            writerToPostingDoc = new BufferedWriter(new FileWriter(postingPath + "/documents/postingDoc" + chunksCount + ".txt"));
            writerToPostingTerm = new BufferedWriter(new FileWriter(postingPath + "/terms/postingTerm" + chunksCount + ".txt"));
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
        if (!docTerms.isEmpty()){
            if (docCounter <= chunkPostingSIZE) {
                if (mergeTerms.isEmpty())
                    for (Map.Entry entry : docTerms.entrySet())
                        mergeTerms.add(new Term((String) entry.getKey(), (int) entry.getValue(), docName));

                else {

                    TreeMap<String, Integer> mapTerms = new TreeMap<>(docTerms);
                    Iterator<String> itNew = mapTerms.keySet().iterator();
                    Iterator<Term> itOld = mergeTerms.iterator();
                    Set<Term> tempTerms = new TreeSet<>();
                    Term existTerm = itOld.next();
                    String newTermString = itNew.next();
                    boolean finish = false;
                    int freq = 0;
                    while (itNew.hasNext() && itOld.hasNext()) {
                        int option = existTerm.compareTo(newTermString);
                        if (option == 0) {
                            freq = docTerms.get(newTermString);
                            existTerm.setFreq(existTerm.getFreq() + freq);
                            existTerm.addDocToTerm(docName, freq); // <docName:freq>
                            existTerm = itOld.next();
                            newTermString = itNew.next();
                        } else if (option == -1) {
                            existTerm = itOld.next();
                        } else {
                            tempTerms.add(new Term(newTermString, docTerms.get(newTermString), docName));
                            newTermString = itNew.next();
                        }
                    }
                    while (itNew.hasNext() || !finish) {
                        tempTerms.add(new Term(newTermString, docTerms.get(newTermString), docName));
                        if (itNew.hasNext()) {
                            newTermString = itNew.next();
                        } else {
                            finish = true;
                        }

                    }
                    mergeTerms.addAll(tempTerms);
                    docCounter++;
                }
            }
        if (docCounter > chunkPostingSIZE) {
            try {
                Iterator entry = mergeTerms.iterator();
                int counter = 0;
                for (Term term : mergeTerms) {
                    //pointerTermPosting = String.valueOf(chunksCount) + "," + termFileCounter;
                    String put = term.getStringTerm() + "," + term.getDocuments().toString() +
                            "," + term.getDocuments().size();
                    if (term.getStringTerm().equals("youngster") && term.getDocuments().toString().equals(" LA011290-0024:1")) {
                        System.out.println("jump");
                    }
                    writerToPostingTerm.append(put);
                    //Banna,#d1:2#d4:6#d9:11,3
                    //name,#d1:tf#d4:tf#d9:tf,idf

                    writerToPostingTerm.append('\n');
                    // termFileCounter++;
                    // Indexer.addTerm();

                }
                docCounter = 0;
                writerToPostingTerm.flush();
                mergeTerms.clear();
                chunksCount++;
                writerToPostingTerm = new BufferedWriter(new FileWriter(postingPath + "/terms/postingTerm" + chunksCount + ".txt"));


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    }

    private void margeToMainPostingFile() {
        // after the merge we need to write to dic.

        //   Indexer.addTerm(term.getStringTerm(), (term.getFreq() +
        //           "|" + term.getDocuments().size() + "|" + pointerTermPosting));
        //   //name|Freq|Df|chunkPointer,linePointer
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

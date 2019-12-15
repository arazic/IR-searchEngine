import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Posting {

    private int docFILE= 100; // how many doc in a postingDoc file
    private int chunkPostingSIZE =300; // how many doc in a postingTerm file
    private int chunksCount;
    private static TreeSet<Term> mergeTerms;
    private int docCounter; // how many docs are merging in the memory;
    private int docFileCounter; // how many doc in a posting file;
    private int termFileCounter; // how many term in a posting file;
    private String pointerDocPosting; //<postingFileName><postingPlaceInFile>
    private String pointerTermPosting; //<postingFileName><postingPlaceInFile>
    private String postingPath;
   // private BufferedWriter writerToPostingDoc;
    private BufferedReader readFromTmpPostingTerm1;
    private BufferedReader readFromTmpPostingTerm2;
    private BufferedWriter writerToPostingTerm;
    private BufferedWriter writerToMargeTmpPosting;
    private BufferedWriter writerToPostingDoc;


    public Posting(String postingPath){
        mergeTerms = new TreeSet<>();
        docFileCounter=1;
        termFileCounter=1;
        pointerDocPosting="";
        pointerTermPosting="";
        this.postingPath=postingPath;
        chunksCount=1;
        try {
            writerToPostingDoc = new BufferedWriter(new FileWriter(postingPath + "/documents/postingDoc.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //create posting file
    public void postingDoc(Document document) {
        try {

                String toAdd= document.getDocName()+"!"+document.getMaxTerm()+"!"+document.getUniqeTermsNum();
                writerToPostingDoc.append(toAdd);
                writerToPostingDoc.append('\n');
                Indexer.addDoc(document.getDocName(), String.valueOf(docFileCounter));
                docFileCounter++;
                writerToPostingDoc.flush();

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
                    int freq;
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
                writerToPostingTerm = new BufferedWriter(new FileWriter(postingPath + "/terms/postingTerm" + chunksCount + ".txt"));
                for (Term term : mergeTerms) {
                    String toUp= term.getStringTerm().toUpperCase();
                    String strTerm= term.getStringTerm();
                    String  strTermToLower= strTerm.toLowerCase();
                    String strDocName= term.getDocuments().toString();
                    int strDocsSize=  term.getDocuments().size();
                    int totalTf =term.getTotalTf();



/*                    if( (term.getStringTerm().equals("anc")|| term.getStringTerm().equals("ANC")| term.getStringTerm().equals("Anc")))
                        System.out.println("jump");*/
                    if((!strTerm.equals("")) && (toUp.charAt(0)==strTerm.charAt(0)) &&
                         (!(strTerm.charAt(0) >= '0' && strTerm.charAt(0) <= '9'))){// the term is not number
                        if (toUp.compareTo(strTerm) == 0){
                            if(strTerm.contains(" ")){
                               writerToPostingTerm.append(strTermToLower + "!" + strDocName +
                                     "!" + strDocsSize + "!" + totalTf + "^s");}
                            else{ writerToPostingTerm.append(strTermToLower + "!" + strDocName +
                                    "!" + strDocsSize + "!" + totalTf+ "^u"); }
                        }//Banna,#d1:2#d4:6#d9:11,3,19 //name,#d1:tf#d4:tf#d9:tf,idf,totalTF
                        else{
                            writerToPostingTerm.append(strTermToLower + "!" + strDocName +
                                    "!" +strDocsSize + "!" +totalTf+ "^u"); }//Banna,#d1:2#d4
                    }
                    else {
                        writerToPostingTerm.append(strTermToLower + "!" + strDocName +
                                "!" + strDocsSize + "!" +totalTf); //Banna,#d1:2#d4:6#d9:11,3,19 //name,#d1:tf#d4:tf#d9:tf,idf,totalTF
                    }
                    writerToPostingTerm.append('\n');
                }
                writerToPostingTerm.flush();
                docCounter = 0;
                mergeTerms.clear();
                chunksCount++;
                writerToPostingTerm.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    }

    public void margeToMainPostingFile() {

        try {
            String m = "m"; // First round of merge
            String h="";
            int cur=chunksCount-1;
            int totalNewMarge = 0;
            while(cur!=1) {
            int countPointer=0;
                int countMarge = 1;

                for (int i = 1; i < cur; i += 2) {
                    if(cur%2==0)
                        totalNewMarge = cur / 2;
                    else
                        totalNewMarge= (cur/2)+1;

                    FileReader f1 = new FileReader((postingPath + "/terms/postingTerm" + (i)+h + ".txt"));
                    FileReader f2 = new FileReader((postingPath + "/terms/postingTerm" + (i + 1)+h + ".txt"));
                    readFromTmpPostingTerm1 = new BufferedReader(f1);
                    readFromTmpPostingTerm2 = new BufferedReader(f2);
                    writerToMargeTmpPosting = new BufferedWriter(new FileWriter((postingPath) + "/terms/postingTerm" + countMarge + m + ".txt"));

                    String firstFileLine = readFromTmpPostingTerm1.readLine();
                    String secondFileLine = readFromTmpPostingTerm2.readLine();

                    while (firstFileLine != null && secondFileLine != null) {
                        String[] split1 = splitLine(firstFileLine);
                        String[] split2 = splitLine(secondFileLine);

                        if(split1[0].equals("a")|| split2[0].equals("a"))
                            System.out.println("jump");

                        int option = split1[0].toLowerCase().compareTo(split2[0].toLowerCase());
                        if (option == 0) {
                            int df = Integer.parseInt(split1[2]) + Integer.parseInt(split2[2]);
                            int freq= Integer.parseInt(split1[3]) + Integer.parseInt(split2[3]);
                            String toAdd= split1[0] + "!" + mergeDocs(split1[1], split2[1]) + "!"+df+"!"+freq;
                            if((split1[4]!=null&& split2[4]!=null)){
                                if(split1[4].equals(split2[4] )){// both start with upper letter
                                  if((split1[4].equals("u"))){
                                    toAdd= toAdd +"^"+ split1[4];}
                                  else if((split1[4].equals("s"))){
                                      toAdd= toAdd +"^"+ "e";
                                }
                            }
                            }
                            writerToMargeTmpPosting.append(toAdd);
                            writerToMargeTmpPosting.append('\n');

                            if(cur==2){
                                countPointer++;
                                if(toAdd.contains("^u")){
                                    String theFirstLetter= String.valueOf(split1[0].charAt(0));
                                    String rest=split1[0].substring(1);
                                   // Indexer.addTerm((theFirstLetter.toUpperCase()+rest), (mergeDocs(split1[1], split2[1]) + "!" +df+"!"+ freq));
                                    Indexer.addTerm(split1[0], df+"!"+ freq+"!" + countPointer);
                                }
                                else if(toAdd.contains("^e")){
                                   // Indexer.addTermToEntity(split1[0], (mergeDocs(split1[1], split2[1]) + "!" +df+"!"+ freq));
                                    Indexer.addTermToEntity(split1[0], df+"!"+ freq+"!" + countPointer);
                                }
                                else
                                   // Indexer.addTerm(split1[0], (mergeDocs(split1[1], split2[1])  + "!" +df+"!"+ freq));
                                    Indexer.addTerm(split1[0], df+"!"+ freq+"!" + countPointer);

                            }
                            firstFileLine = readFromTmpPostingTerm1.readLine();
                            secondFileLine = readFromTmpPostingTerm2.readLine();

                        } else if (option <= -1) {
                            writerToMargeTmpPosting.append(firstFileLine);
                            writerToMargeTmpPosting.append('\n');
                            if(cur==2){
                                countPointer++;
                                String theFirstLetter= String.valueOf(split1[0].charAt(0));
                                String rest=split1[0].substring(1);
                                if(firstFileLine.contains("^u")){
                                    //Indexer.addTerm((theFirstLetter.toUpperCase()+rest), split1[1] + "!" + split1[2]+"!"+split1[3]);
                                    Indexer.addTerm(split1[0], split1[2]+"!"+split1[3]+"!"+countPointer);
                                }
                                else if(firstFileLine.contains("^e")){
                                   // Indexer.addTermToEntity(split1[0], split1[1] + "!" + split1[2]+"!"+split1[3]);
                                    Indexer.addTermToEntity(split1[0], split1[2]+"!"+split1[3]+"!"+countPointer);
                                }
                                else
                                    //Indexer.addTerm(split1[0], split1[1]+ "!" + split1[2]+ "!" + split1[3]);
                                    Indexer.addTerm(split1[0],  split1[2]+ "!" + split1[3]+"!"+countPointer);
                            }
                            firstFileLine = readFromTmpPostingTerm1.readLine();
                        } else {
                            writerToMargeTmpPosting.append(secondFileLine);
                            writerToMargeTmpPosting.append('\n');
                            if(cur==2) {
                                countPointer++;
                                String theFirstLetter = String.valueOf(split2[0].charAt(0));
                                String rest = split2[0].substring(1);
                                if (secondFileLine.contains("^u")) {
                                   // Indexer.addTerm((theFirstLetter.toUpperCase() + rest), split2[1] + "!" + split2[2] + "!" + split2[3]);
                                    Indexer.addTerm(split2[0],split2[2] + "!" + split2[3]+"!"+countPointer);
                                }
                                else if(firstFileLine.contains("^e")){
                                   // Indexer.addTermToEntity(split2[0], split2[1] + "!" + split2[2]+"!"+split2[3]);
                                    Indexer.addTermToEntity(split2[0], split2[2]+"!"+split2[3]+"!"+countPointer);
                                }
                                else
                                   // Indexer.addTerm(split2[0], split2[1] + "!" + split2[2] + "!" + split2[3] );
                                    Indexer.addTerm(split2[0],  split2[2] + "!" + split2[3]+"!"+countPointer );
                            }
                            secondFileLine = readFromTmpPostingTerm2.readLine();
                        }
                    }
                    while (firstFileLine != null) {
                        writerToMargeTmpPosting.append(firstFileLine);
                        writerToMargeTmpPosting.append('\n');
                        if(cur==2) {
                            countPointer++;
                            String[] split1 = splitLine(firstFileLine);
                            if (firstFileLine.contains("^u")) {
                                String theFirstLetter = String.valueOf(split1[0].charAt(0));
                                String rest = split1[0].substring(1);
                                Indexer.addTerm(split1[0], split1[2] + "!" + split1[3]+"!"+countPointer);
                               // Indexer.addTerm((theFirstLetter.toUpperCase() + rest), split1[1] + "!" + split1[2] + "!" + split1[3]);
                            }
                            else if(firstFileLine.contains("^e")){
                                Indexer.addTermToEntity(split1[0], split1[2]+"!"+split1[3]+"!"+countPointer);
                               // Indexer.addTermToEntity(split1[0], split1[1] + "!" + split1[2]+"!"+split1[3]);
                            }
                            else
                                Indexer.addTerm(split1[0], split1[2] + "!" + split1[3] +"!"+countPointer);
                               // Indexer.addTerm(split1[0], split1[1] + "!" + split1[2] + "!" + split1[3] );
                        }
                        firstFileLine = readFromTmpPostingTerm1.readLine();
                    }


                    while (secondFileLine != null) {
                        writerToMargeTmpPosting.append(secondFileLine);
                        writerToMargeTmpPosting.append('\n');
                            if(cur==2) {
                                countPointer++;
                                String[] split2 = splitLine(secondFileLine);
                                if (secondFileLine.contains("^u")) {
                                    String theFirstLetter = String.valueOf(split2[0].charAt(0));
                                    String rest = split2[0].substring(1);
                                    Indexer.addTerm(split2[0], split2[2] + "!" + split2[3]+"!"+countPointer);
                                   // Indexer.addTerm((theFirstLetter.toUpperCase() + rest), split2[1] + "!" + split2[2] + "!" + split2[3]);
                                }
                                else if(secondFileLine.contains("^e")){
                                    Indexer.addTermToEntity(split2[0], split2[1] + "!" + split2[2]+"!"+split2[3]+"!"+countPointer);
                                   // Indexer.addTermToEntity(split2[0], split2[1] + "!" + split2[2]+"!"+split2[3]);
                                }
                                else
                                    Indexer.addTerm(split2[0], split2[1] + "!" + split2[2] + "!" + split2[3]+"!"+countPointer );
                                   // Indexer.addTerm(split2[0], split2[1] + "!" + split2[2] + "!" + split2[3] );
                            }
                        secondFileLine = readFromTmpPostingTerm2.readLine();
                    }
                    writerToMargeTmpPosting.flush();
                    writerToMargeTmpPosting.close();
                    f1.close();
                    f2.close();
                    readFromTmpPostingTerm1.close();
                    readFromTmpPostingTerm2.close();

                    Path path = Paths.get((postingPath + "/terms/postingTerm" + (i)+h + ".txt"));
                    File f = path.toFile();
                    f.delete();
                    Path path2 = Paths.get((postingPath + "/terms/postingTerm" + (i+1)+h + ".txt"));
                    File ff = path2.toFile();
                    ff.delete();

                    countMarge++;

                }
                if (totalNewMarge-(countMarge-1) > 0) {

                    FileReader f1 = new FileReader((postingPath + "/terms/postingTerm" + (cur)+h + ".txt"));
                    FileWriter f2 = new FileWriter((postingPath) + "/terms/postingTerm" + countMarge + m + ".txt");
                    readFromTmpPostingTerm1 = new BufferedReader(f1);
                    writerToMargeTmpPosting = new BufferedWriter(f2);
                    String firstFileLine = readFromTmpPostingTerm1.readLine();
                    while (firstFileLine != null) {
                        writerToMargeTmpPosting.append(firstFileLine);
                        writerToMargeTmpPosting.append("\n");
                        if(cur==2) {
                            countPointer++;
                            String[] split1 = splitLine(firstFileLine);
                            if (firstFileLine.contains("^u")) {
                                String theFirstLetter = String.valueOf(split1[0].charAt(0));
                                String rest = split1[0].substring(1);
                                Indexer.addTerm(split1[0],  split1[2] + "!" + split1[3]+"!"+ countPointer);
                                //Indexer.addTerm((theFirstLetter.toUpperCase() + rest), split1[1] + "!" + split1[2] + "!" + split1[3]);
                            }
                            else if(firstFileLine.contains("^e")){
                                Indexer.addTermToEntity(split1[0],  split1[2]+"!"+split1[3]+"!"+ countPointer);
                               // Indexer.addTermToEntity(split1[0], split1[1] + "!" + split1[2]+"!"+split1[3]);
                            }
                            else
                                Indexer.addTerm(split1[0],  split1[2] + "!" + split1[3] + "!" + split1[4]+"!"+ countPointer);
                               // Indexer.addTerm(split1[0], split1[1] + "!" + split1[2] + "!" + split1[3] + "!" + split1[4]);
                        }
                        firstFileLine = readFromTmpPostingTerm1.readLine();
                    }
                    writerToMargeTmpPosting.flush();
                    f1.close();
                    f2.close();
                    writerToMargeTmpPosting.close();
                    readFromTmpPostingTerm1.close();

                    Path path = Paths.get((postingPath + "/terms/postingTerm" + (cur)+h + ".txt"));
                    File f = path.toFile();
                    f.delete();

                    countMarge++;
                }
//                for (int i = 1; i <= cur; i++) {
//                    Path path = Paths.get((postingPath + "/terms/postingTerm" + (i)+h + ".txt"));
//                    File f = path.toFile();
//                    f.delete();
//
//                }
                cur= totalNewMarge;
                h=m;
                m = m + "m";
            }

            Indexer.printTerms();
            Indexer.printDocs();

        } catch ( IOException e) {
            e.printStackTrace();
        }



        // after the merge we need to write to dic.

        //   Indexer.addTerm(term.getStringTerm(), (term.getFreq() +
        //           "|" + term.getDocuments().size() + "|" + pointerTermPosting));
        //   //name|Freq|Df|chunkPointer,linePointer



    }

    private String mergeDocs(String s1, String s2) {
        String mergeDocs="";
        String []firstDocs= s1.split(",");
        String []secondDocs= s2.split(",");
        int i=0;
        int j=0;
        while (i<firstDocs.length && j<secondDocs.length){
            String docName1= firstDocs[i].substring(0,firstDocs[i].indexOf(":"));
            String docFreq1= firstDocs[i].substring(firstDocs[i].indexOf(":")+1);
            String docName2= secondDocs[i].substring(0,secondDocs[i].indexOf(":"));
            String docFreq2= secondDocs[i].substring(secondDocs[i].indexOf(":")+1);

            int option= docName1.compareTo(docName2);
            if(option==0){ // I dont sure we need it!
                int freqMerge= Integer.parseInt(docFreq1)+Integer.parseInt(docFreq2);
                mergeDocs= mergeDocs+ docName1+" :"+ freqMerge+",";
                i++;
                j++;
            }
            else if(option<=-1){
                mergeDocs= mergeDocs+ firstDocs[i]+","+secondDocs[i];
                i++;
                j++;
            }
            else{
                mergeDocs= mergeDocs+ secondDocs[i]+", "+firstDocs[i];
                i++;
                j++;
            }
        }
        while(i<firstDocs.length){
            mergeDocs= mergeDocs+ firstDocs[i];
            i++;
        }
        while(j<secondDocs.length){
            mergeDocs= mergeDocs+ secondDocs[j];
            j++;
        }
        return "["+mergeDocs+"]";
    }



    /**
     * parsing to posting file, each term has array-
     * array[0] is the string term
     * array[1] are the documents the term exist in and the tf in each document
     * array[2] is the df- in how many docs the term exist
     * array[3] is the total tf- in all the corpus
     * array[4] is one (or less to nothing) from the flags: s- suspect to be entity, e- entity, u- upper letter, or nothing
     * @param fileLine
     * @return array
     */
    private String[] splitLine(String fileLine) {
        String []ans= new String[5];
        ans[0]=fileLine.substring(0,fileLine.indexOf("!"));
        ans[1]=fileLine.substring(fileLine.indexOf("[")+1,fileLine.indexOf("]"));
        String dfTf=fileLine.substring(fileLine.indexOf("]!")+2);
        ans[3]=dfTf;
        ans[2]= dfTf.substring(0, dfTf.indexOf("!"));
        if(dfTf.contains("^")){
            ans[4]= dfTf.substring(dfTf.length()-1);
            ans[3]= ans[3].substring(ans[3].indexOf("!")+1, ans[3].length()-2);
        }

        else
            ans[3]= ans[3].substring(dfTf.indexOf("!")+1);

        return ans;
    }





}

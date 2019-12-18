package Model;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Posting {

    private int docFILE= 900; // how many doc in a postingDoc file
    private int chunkPostingSIZE =300; // how many doc in a postingTerm file
    private int writeToBuff=400;
    private int chunksCount;
    private static HashMap<String,String> mergeTerms;
    private int docCounter; // how many docs are merging in the memory;
    private int docFileCounter; // how many doc in a posting file;
    private String postingPath;
    private BufferedReader readFromTmpPostingTerm1;
    private BufferedReader readFromTmpPostingTerm2;
    private BufferedWriter writerToPostingTerm;
    private BufferedWriter writerToMargeTmpPosting;
    private BufferedWriter writerToPostingDoc;
    private boolean finishDoc;

    public Posting(String postingPath){
        mergeTerms = new HashMap<>();
        docFileCounter=0;
        this.postingPath=postingPath;
        chunksCount=1;


    }

    public void postingDoc(Document document) {
        try {
            if(writerToPostingDoc==null){
                writerToPostingDoc = new BufferedWriter(new FileWriter(postingPath + "/postingDoc.txt"));
            }
                        String toAdd = document.getDocName() + "!" + document.getMaxTerm() + "!" + document.getUniqeTermsNum();
                        writerToPostingDoc.write(toAdd);
                        writerToPostingDoc.write('\n');
                        Indexer.addDoc(document.getDocName(), String.valueOf(docFileCounter+1));
                        docFileCounter++;
           // writerToPostingDoc.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void postingTerms(HashMap<String, Integer> docTerms, String docName) {
        if (!docTerms.isEmpty()) {
            if (docCounter <= chunkPostingSIZE) {
                    for (Map.Entry entry : docTerms.entrySet()) {

                        String original = String.valueOf(entry.getKey());
                        String newTerm = original.toLowerCase();
                        int newFreq = (int) entry.getValue();
                        String title;

                        if (original.charAt(0) > 'Z' || original.charAt(0) < 'A')
                            title = "l";
                        else if (original.contains(" "))
                            title = "s";
                        else
                            title = "u";

                        String info = "";
                        if (mergeTerms.containsKey(newTerm)) {
                            info = mergeTerms.get(newTerm);
                            String[] infoArray = mergeInfo(info);
                            String docs = mergeDocs(infoArray[0], (docName + ":" + newFreq));
                            int df = Integer.parseInt(infoArray[1]) + 1;
                            int freq = newFreq + Integer.parseInt(infoArray[2]);
                            if (!title.equals(infoArray[3])) {// both start with upper letter
                                if (infoArray[3].equals("e"))
                                    title = "e";
                                else
                                    title = "l";
                            } else if (title.equals("s")) {
                                title = "e";
                            }

                            mergeTerms.put(newTerm, "!" + docs + "!" + df + "!" + freq + "^" + title);

                        } else {
                         //   System.out.println(newTerm+"![" + docName + ":" + newFreq + "]!" + 1 + "!" + newFreq + "^" + title);
                            mergeTerms.put(newTerm, "![" + docName + ":" + newFreq + "]!" + 1 + "!" + newFreq + "^" + title);
                        }

                    }

                docCounter++;
            }

            if (docCounter > chunkPostingSIZE) {
                try {
                    //LocalDateTime myDateObj = LocalDateTime.now();
                    //System.out.println("time start chunk :+ "+ myDateObj);
                    int counterWriter=0;
                    writerToPostingTerm = new BufferedWriter(new FileWriter(postingPath + "/postingTerm" + chunksCount + ".txt"));
                    TreeMap<String,String> sortedTerms= new TreeMap<>(mergeTerms);

                    for (Map.Entry entry : sortedTerms.entrySet()) {
                        writerToPostingTerm.append(entry.getKey().toString()+ entry.getValue().toString());
                        writerToPostingTerm.append('\n');
                        counterWriter++;
                        if(counterWriter>= writeToBuff) {
                            writerToPostingTerm.flush();
                            counterWriter=0;
                        }
                    }
                    if(counterWriter< writeToBuff) {
                        writerToPostingTerm.flush();
                    }
                    docCounter = 0;
                    mergeTerms.clear();
                    chunksCount++;
                    writerToPostingTerm.close();
                    // myDateObj = LocalDateTime.now();
                    //System.out.println("time end chunk :+ "+ myDateObj);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void margeToMainPostingFile() {

            String m = "m"; // First round of merge
            String h="";
            int cur=chunksCount-1;
            int totalNewMarge = 0;
            int countMarge=1;
            while(cur>2)
            {
                try {
                for (int i = 1; i < cur; i += 2) {
                    if(cur%2==0)
                        totalNewMarge = cur / 2;
                    else
                        totalNewMarge= (cur/2)+1;

                    FileReader f1 = new FileReader((postingPath + "/postingTerm" + (i)+h + ".txt"));
                    FileReader f2 = new FileReader((postingPath + "/postingTerm" + (i + 1)+h + ".txt"));
                    readFromTmpPostingTerm1 = new BufferedReader(f1);
                    readFromTmpPostingTerm2 = new BufferedReader(f2);
                    writerToMargeTmpPosting = new BufferedWriter(new FileWriter((postingPath) + "/postingTerm" + countMarge + m + ".txt"));
                    int counterWriter=0;

                    String firstFileLine = readFromTmpPostingTerm1.readLine();
                    String secondFileLine = readFromTmpPostingTerm2.readLine();

                    while (firstFileLine != null && secondFileLine != null) {
                        String[] split1 = splitLine(firstFileLine);
                        String[] split2 = splitLine(secondFileLine);

                        int option = split1[0].compareTo(split2[0]);
                        if (option == 0) {
                            int df = Integer.parseInt(split1[2]) + Integer.parseInt(split2[2]);
                            int freq= Integer.parseInt(split1[3]) + Integer.parseInt(split2[3]);
                            String toAdd= split1[0] + "!" + mergeDocs(split1[1], split2[1]) + "!"+df+"!"+freq;
                            if(split1[4].equals(split2[4])) {// e,e  s,s  u,u  l,l
                                if (split1[4].equals("s")||split1[4].equals("e"))
                                    toAdd = toAdd + "^e";
                                else if(split1[4].equals("u"))
                                    toAdd= toAdd+"^u";
                                else if(split1[4].equals("l"))
                                    toAdd= toAdd+"^l";
                            }
                            else if(split1[4].equals("e")||split2[4].equals("e"))
                                toAdd= toAdd +"^e";
                            else
                                toAdd= toAdd+"^l";

                            writerToMargeTmpPosting.append(toAdd);
                            writerToMargeTmpPosting.append('\n');
                            counterWriter++;
                            firstFileLine = readFromTmpPostingTerm1.readLine();
                            secondFileLine = readFromTmpPostingTerm2.readLine();

                        } else if (option <= -1) {

                            writerToMargeTmpPosting.append(firstFileLine);
                            writerToMargeTmpPosting.append('\n');
                            counterWriter++;
                            firstFileLine = readFromTmpPostingTerm1.readLine();

                        }
                        else
                            {
                            writerToMargeTmpPosting.append(secondFileLine);
                            writerToMargeTmpPosting.append('\n');
                            counterWriter++;
                            secondFileLine = readFromTmpPostingTerm2.readLine();
                        }

                        if(counterWriter>=writeToBuff){
                            writerToMargeTmpPosting.flush();
                            counterWriter=0;
                        }

                    }

                    while (firstFileLine != null) {
                        writerToMargeTmpPosting.append(firstFileLine);
                        writerToMargeTmpPosting.append('\n');
                        counterWriter++;
                        if(counterWriter>=writeToBuff){
                            writerToMargeTmpPosting.flush();
                            counterWriter=0;
                        }
                        firstFileLine = readFromTmpPostingTerm1.readLine();

                    }

                    while (secondFileLine != null) {
                        writerToMargeTmpPosting.append(secondFileLine);
                        writerToMargeTmpPosting.append('\n');
                        counterWriter++;
                        if(counterWriter>=writeToBuff){
                            writerToMargeTmpPosting.flush();
                            counterWriter=0;
                        }
                        secondFileLine = readFromTmpPostingTerm2.readLine();
                    }

                    if(counterWriter!=0){
                        writerToMargeTmpPosting.flush();
                    }

                   // writerToMargeTmpPosting.flush();
                    writerToMargeTmpPosting.close();
                    readFromTmpPostingTerm1.close();
                    readFromTmpPostingTerm2.close();
                    f1.close();
                    f2.close();

                    Path path = Paths.get((postingPath + "/postingTerm" + (i)+h + ".txt"));
                    File f = path.toFile();
                    f.delete();
                    Path path2 = Paths.get((postingPath + "/postingTerm" + (i+1)+h + ".txt"));
                    File ff = path2.toFile();
                    ff.delete();

                    countMarge++;

                }
                if (totalNewMarge-(countMarge-1) > 0) {

                    FileReader f1 = new FileReader((postingPath + "/postingTerm" + (cur)+h + ".txt"));
                    FileWriter f2 = new FileWriter((postingPath) + "/postingTerm" + countMarge + m + ".txt");
                    readFromTmpPostingTerm1 = new BufferedReader(f1);
                    writerToMargeTmpPosting = new BufferedWriter(f2);
                    String firstFileLine = readFromTmpPostingTerm1.readLine();
                    int counterWriter=0;

                    Path pathToRemove = Paths.get((postingPath + "/postingTerm" + (cur)+h + ".txt"));
                    File fToRemove = pathToRemove .toFile();

                    //if(fToRemove.renameTo(new File(postingPath + "/terms/postingTerm" + countMarge + m + ".txt"))){
                    //   System.out.println("remane to"+ postingPath + "/terms/postingTerm" + countMarge + m + ".txt");
                    //}

                    while (firstFileLine != null) {
                        writerToMargeTmpPosting.append(firstFileLine);
                        writerToMargeTmpPosting.append("\n");
                        countMarge++;
                        firstFileLine = readFromTmpPostingTerm1.readLine();
                        if(counterWriter>=writeToBuff){
                            writerToMargeTmpPosting.flush();
                            counterWriter=0;
                        }
                    }

                    if(counterWriter!=0){
                        writerToMargeTmpPosting.flush();
                    }

                    //    writerToMargeTmpPosting.flush();
                    writerToMargeTmpPosting.close();
                    readFromTmpPostingTerm1.close();
                    f1.close();
                    f2.close();

                    Path path = Paths.get((postingPath + "/postingTerm" + (cur)+h + ".txt"));
                    File f = path.toFile();
                    f.delete();

                    countMarge++;
                }

                countMarge=1;
                cur= totalNewMarge;
                h=m;
                m = m + "m";
            } catch ( IOException e) {
                e.printStackTrace();
            }
            }


            if(cur==2){
                int countPointer=0;
                try {
                    FileReader f1 = new FileReader((postingPath + "/postingTerm" + (1)+h + ".txt"));
                    FileReader f2 = new FileReader((postingPath + "/postingTerm" + (2)+h + ".txt"));
                    readFromTmpPostingTerm1 = new BufferedReader(f1);
                    readFromTmpPostingTerm2 = new BufferedReader(f2);
                    writerToMargeTmpPosting = new BufferedWriter(new FileWriter((postingPath) + "/postingTerm" + countMarge + m + ".txt"));
                    int counterWriter=0;

                    String firstFileLine = readFromTmpPostingTerm1.readLine();
                    String secondFileLine = readFromTmpPostingTerm2.readLine();

                    while (firstFileLine != null && secondFileLine != null) {
                        String[] split1 = splitLine(firstFileLine);
                        String[] split2 = splitLine(secondFileLine);

                        int option = split1[0].compareTo(split2[0]);
                        if (option == 0) {
                            int df = Integer.parseInt(split1[2]) + Integer.parseInt(split2[2]);
                            int freq= Integer.parseInt(split1[3]) + Integer.parseInt(split2[3]);
                            String docs=mergeDocs(split1[1], split2[1]);
                            String toAdd= split1[0] + "!" + docs + "!"+df+"!"+freq;
                            if(split1[4].equals(split2[4])) {// e,e  s,s  u,u  l,l
                                if (split1[4].equals("s")||split1[4].equals("e"))
                                    toAdd = toAdd + "^e";
                                else if(split1[4].equals("u"))
                                    toAdd= toAdd+"^u";
                                else if(split1[4].equals("l"))
                                    toAdd= toAdd+"^l";
                            }
                            else if(split1[4].equals("e")||split2[4].equals("e"))
                                toAdd= toAdd +"^e";
                            else
                                toAdd= toAdd+"^l";

                            countPointer++;
                            if(toAdd.contains("^u")){
                                Indexer.addTerm(split1[0].toUpperCase(), df+"!"+ freq+"!" + countPointer);
                                writerToMargeTmpPosting.append(split1[0].toUpperCase()+"!"+docs+"!"+df+"!"+freq);
                                counterWriter++;

                            }
                            else if(toAdd.contains("^e")){
                                Indexer.addTermToEntity(split1[0], df+"!"+ freq+"!" + countPointer);
                                writerToMargeTmpPosting.append(split1[0].toUpperCase()+"!"+docs+"!"+df+"!"+freq);
                                counterWriter++;

                            }
                            else {
                                Indexer.addTerm(split1[0], df + "!" + freq + "!" + countPointer);
                                writerToMargeTmpPosting.append(split1[0]+"!"+docs+"!"+df+"!"+freq);
                                counterWriter++;
                            }

                            writerToMargeTmpPosting.append('\n');

                            firstFileLine = readFromTmpPostingTerm1.readLine();
                            secondFileLine = readFromTmpPostingTerm2.readLine();

                        } else if (option <= -1) {

                            countPointer++;
                            if(firstFileLine.contains("^u")){
                                Indexer.addTerm(split1[0].toUpperCase(), split1[2]+"!"+ split1[3]+"!" + countPointer);
                                writerToMargeTmpPosting.append(split1[0].toUpperCase()+"!"+split1[1]+"!"+split1[2]+"!"+ split1[3]);
                                writerToMargeTmpPosting.append('\n');
                                counterWriter++;

                            }
                            else if(firstFileLine.contains("^e")){
                                Indexer.addTermToEntity(split1[0].toUpperCase(), split1[2]+"!"+ split1[3]+"!" + countPointer);
                                writerToMargeTmpPosting.append(split1[0].toUpperCase()+"!"+split1[1]+"!"+split1[2]+"!"+ split1[3]);
                                writerToMargeTmpPosting.append('\n');
                                counterWriter++;
                            }
                            else if(firstFileLine.contains("^l")){
                                Indexer.addTerm(split1[0], split1[2]+"!"+ split1[3]+"!" + countPointer);
                                writerToMargeTmpPosting.append(split1[0]+"!"+split1[1]+"!"+split1[2]+"!"+ split1[3]);
                                writerToMargeTmpPosting.append('\n');
                                counterWriter++;
                            }
                            firstFileLine = readFromTmpPostingTerm1.readLine();

                        }
                        else
                        {
                            countPointer++;
                            if(secondFileLine.contains("^u")){
                                Indexer.addTerm(split2[0].toUpperCase(), split2[2]+"!"+ split2[3]+"!" + countPointer);
                                writerToMargeTmpPosting.append(split2[0].toUpperCase()+"!"+split2[1]+"!"+split2[2]+"!"+ split2[3]);
                                writerToMargeTmpPosting.append('\n');
                                counterWriter++;
                            }
                            else if(secondFileLine.contains("^e")){
                                Indexer.addTermToEntity(split2[0].toUpperCase(), split2[2]+"!"+ split2[3]+"!" + countPointer);
                                writerToMargeTmpPosting.append(split2[0].toUpperCase()+"!"+split2[1]+"!"+split2[2]+"!"+ split2[3]);
                                writerToMargeTmpPosting.append('\n');
                                counterWriter++;
                            }
                            else if(secondFileLine.contains("^l")){
                                Indexer.addTerm(split2[0], split2[2]+"!"+ split2[3]+"!" + countPointer);
                                writerToMargeTmpPosting.append(split2[0]+"!"+split2[1]+"!"+split2[2]+"!"+ split2[3]);
                                writerToMargeTmpPosting.append('\n');
                                counterWriter++;
                            }
                            secondFileLine = readFromTmpPostingTerm2.readLine();
                        }
                        if(counterWriter>=writeToBuff){
                            writerToMargeTmpPosting.flush();
                            counterWriter=0;
                        }
                    }
                    while (firstFileLine != null) {
                        countPointer++;
                        String[] split1 = splitLine(firstFileLine);
                        if(firstFileLine.contains("^u")){
                            Indexer.addTerm(split1[0].toUpperCase(), split1[2]+"!"+ split1[3]+"!" + countPointer);
                            writerToMargeTmpPosting.append(split1[0].toUpperCase()+"!"+split1[1]+"!"+split1[2]+"!"+ split1[3]);
                            writerToMargeTmpPosting.append('\n');
                            counterWriter++;

                        }
                        else if(firstFileLine.contains("^e")){
                            Indexer.addTermToEntity(split1[0].toUpperCase(), split1[2]+"!"+ split1[3]+"!" + countPointer);
                            writerToMargeTmpPosting.append(split1[0].toUpperCase()+"!"+split1[1]+"!"+split1[2]+"!"+ split1[3]);
                            writerToMargeTmpPosting.append('\n');
                            counterWriter++;

                        }
                        else if(firstFileLine.contains("^l")){
                            Indexer.addTerm(split1[0], split1[2]+"!"+ split1[3]+"!" + countPointer);
                            writerToMargeTmpPosting.append(split1[0]+"!"+split1[1]+"!"+split1[2]+"!"+ split1[3]);
                            writerToMargeTmpPosting.append('\n');
                            counterWriter++;

                        }
                        if(counterWriter>=writeToBuff){
                            writerToMargeTmpPosting.flush();
                            counterWriter=0;
                        }
                        firstFileLine = readFromTmpPostingTerm1.readLine();
                    }

                    while (secondFileLine != null) {
                        countPointer++;
                        String[] split2 = splitLine(secondFileLine);
                        if(secondFileLine.contains("^u")){
                            Indexer.addTerm(split2[0].toUpperCase(), split2[2]+"!"+ split2[3]+"!" + countPointer);
                            writerToMargeTmpPosting.append(split2[0].toUpperCase()+"!"+split2[1]+"!"+split2[2]+"!"+ split2[3]);
                            writerToMargeTmpPosting.append('\n');
                            counterWriter++;

                        }
                        else if(secondFileLine.contains("^e")){
                            Indexer.addTermToEntity(split2[0].toUpperCase(), split2[2]+"!"+ split2[3]+"!" + countPointer);
                            writerToMargeTmpPosting.append(split2[0].toUpperCase()+"!"+split2[1]+"!"+split2[2]+"!"+ split2[3]);
                            writerToMargeTmpPosting.append('\n');
                            counterWriter++;

                        }
                        else if(secondFileLine.contains("^l")){
                            Indexer.addTerm(split2[0], split2[2]+"!"+ split2[3]+"!" + countPointer);
                            writerToMargeTmpPosting.append(split2[0]+"!"+split2[1]+"!"+split2[2]+"!"+ split2[3]);
                            writerToMargeTmpPosting.append('\n');
                            counterWriter++;

                        }
                        if(counterWriter>=writeToBuff){
                            writerToMargeTmpPosting.flush();
                            counterWriter=0;
                        }
                        secondFileLine = readFromTmpPostingTerm2.readLine();
                    }
                    if( counterWriter!=0){
                        writerToMargeTmpPosting.flush();
                    }

                    // writerToMargeTmpPosting.flush();
                    writerToMargeTmpPosting.close();
                    readFromTmpPostingTerm1.close();
                    readFromTmpPostingTerm2.close();
                    f1.close();
                    f2.close();

                    Path path = Paths.get((postingPath + "/postingTerm" + (1)+h + ".txt"));
                    File f = path.toFile();
                    f.delete();
                    Path path2 = Paths.get((postingPath + "/postingTerm" + (2)+h + ".txt"));
                    File ff = path2.toFile();
                    ff.delete();


                } catch ( IOException e) {
                    e.printStackTrace();
                }
            }

            Indexer.print();

    }

    private String mergeDocs(String s1, String s2) {
        StringBuilder mergeDocs= new StringBuilder();
        String []firstDocs= s1.split(",");
        String []secondDocs= s2.split(",");
        int i=0;
        int j=0;
        while (i<firstDocs.length && j<secondDocs.length)
        {
            String docName1= firstDocs[i].substring(0,firstDocs[i].indexOf(":"));
            String docName2= secondDocs[j].substring(0,secondDocs[j].indexOf(":"));
            int option= docName1.compareTo(docName2);

            if(option<0)
            {
                mergeDocs= mergeDocs.append(firstDocs[i]+",");
                i++;
            }
            else
                {
                mergeDocs= mergeDocs.append(secondDocs[j]+",");
                j++;
            }
        }
        while(i<firstDocs.length){
            mergeDocs= mergeDocs.append(firstDocs[i]+",");
            i++;
        }
        while(j<secondDocs.length){
            mergeDocs= mergeDocs.append(secondDocs[j]+",");
            j++;
        }
        return "["+mergeDocs.substring(0,mergeDocs.length()-1)+"]";
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


    /**
     * parsing to posting file, each term has array-
     * array[0] are the documents the term exist in and the tf in each document
     * array[1] is the df- in how many docs the term exist
     * array[2] is the total tf- in all the corpus
     * array[3] is one (or less to nothing) from the flags: s- suspect to be entity, e- entity, u- upper letter, or nothing
     * @param info
     * @return
     */
    private String[] mergeInfo(String info) {
        String []ans= new String[4];
        ans[0]=info.substring(info.indexOf("[")+1,info.indexOf("]"));
        String dfTf=info.substring(info.indexOf("]!")+2);
        ans[2]=dfTf;
        ans[1]= dfTf.substring(0, dfTf.indexOf("!"));
        if(dfTf.contains("^")){
            ans[3]= dfTf.substring(dfTf.length()-1);
            ans[2]= ans[2].substring(ans[2].indexOf("!")+1, ans[2].length()-2);
        }

        else
            ans[2]= ans[2].substring(dfTf.indexOf("!")+1);

        return ans;
    }


    public void setFinishDoc(boolean b) {
        this.finishDoc=b;
    }
}

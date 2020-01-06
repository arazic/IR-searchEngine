package Model;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class Posting {

    private static HashMap<String,String> mergeTerms; // all terms from difrrent documents
    private String postingPath;
    private boolean finishDoc;
    private boolean isStemming;

    private int chunkPostingSIZE =300; // how many doc in a temporary postingTerm file
    private int writeToBuff=400;// how many tersms to writw each write to disk
    private int chunksCount; // how many docs beeen posting
    private int docCounter; // how many docs are merging in the memory;
    private int docFileCounter; // how many doc in a posting file;
    private int sumLengthAllDoc;
    private TreeSet<String> documents;


    private BufferedReader readFromTmpPostingTerm1;
    private BufferedReader readFromTmpPostingTerm2;
    private BufferedWriter writerToPostingTerm;
    private BufferedWriter writerToMargeTmpPosting;
    private BufferedWriter writerToPostingDoc;

    private boolean empty;

    public Posting(String postingPath, boolean isStemming){
        this.isStemming= isStemming;
        mergeTerms = new HashMap<>();
        docFileCounter=0;
        this.postingPath=postingPath;
        chunksCount=1;
        sumLengthAllDoc=0;
        documents = new TreeSet<>();
    }

    public void postingDoc(Document document) {
        try {
            if(writerToPostingDoc==null){
                if(isStemming)
                    writerToPostingDoc = new BufferedWriter(new FileWriter(postingPath + "/postingDocumentsWithStemming.txt"));
                else
                    writerToPostingDoc = new BufferedWriter(new FileWriter(postingPath + "/postingDocumentsNoStemming.txt"));
            }
                        String toAdd = document.getDocName() + "!" + document.getMaxTerm() + "!" + document.getUniqeTermsNum()+ "!"+ document.getTotalTerms()+ "!"+document.getTopEntitiesToPosting();
                       // writerToPostingDoc.write(toAdd);
                        //writerToPostingDoc.newLine();
                        documents.add(toAdd);
                        sumLengthAllDoc= sumLengthAllDoc+ document.getTotalTerms();
                        docFileCounter++;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void postingTerms(HashMap<String, Integer> docTerms, String docName) {
        if (!docTerms.isEmpty())
        {
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
                    if (mergeTerms.containsKey(newTerm))
                    {
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
                        if(title!="l")
                        {
                            mergeTerms.put(newTerm, "!" + docs + "!" + df + "!" + freq + "^" + title);
                        }
                        else
                        {
                            mergeTerms.put(original, "!" + docs + "!" + df + "!" + freq + "^" + title);
                        }
                    }
                    else
                    {
                        if(title!="l")
                        {
                            mergeTerms.put(newTerm, "![" + docName + ":" + newFreq + "]!" + 1 + "!" + newFreq + "^" + title);
                        }
                        else
                        {
                            mergeTerms.put(original, "![" + docName + ":" + newFreq + "]!" + 1 + "!" + newFreq + "^" + title);
                        }
                    }
                }
                docCounter++;
            }
            if (docCounter > chunkPostingSIZE) {
                try {
                    int counterWriter=0;
                    if(isStemming)
                        writerToPostingTerm = new BufferedWriter(new FileWriter(postingPath + "/postingTerm!S" + chunksCount + ".txt"));
                    else
                        writerToPostingTerm = new BufferedWriter(new FileWriter(postingPath + "/postingTerm!R" + chunksCount + ".txt"));

                    TreeMap<String,String> sortedTerms= new TreeMap<>(mergeTerms);
                    for (Map.Entry entry : sortedTerms.entrySet()) {
                        writerToPostingTerm.append(entry.getKey().toString()+ entry.getValue().toString());
                        writerToPostingTerm.newLine();
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void margeToMainPostingFile() {
        String m;
        String h;
        String k;
        if (isStemming) {
            m = "S"; // First round of merge
            h = "";
            k = "!S";
        } else {
            m = "R"; // First round of merge
            h = "";
            k = "!R";
        }
        int cur = chunksCount - 1;

        if (cur == 0) {
            handleLittleCorpus(k, h, m);
        } else {
            int totalNewMarge = 0;
            int countMarge = 1;
            while (cur > 2) {
                try {
                    for (int i = 1; i < cur; i += 2) {
                        if (cur % 2 == 0)
                            totalNewMarge = cur / 2;
                        else
                            totalNewMarge = (cur / 2) + 1;

                        FileReader f1 = new FileReader((postingPath + "/postingTerm" + k + (i) + h + ".txt"));
                        FileReader f2 = new FileReader((postingPath + "/postingTerm" + k + (i + 1) + h + ".txt"));
                        readFromTmpPostingTerm1 = new BufferedReader(f1);
                        readFromTmpPostingTerm2 = new BufferedReader(f2);
                        writerToMargeTmpPosting = new BufferedWriter(new FileWriter((postingPath) + "/postingTerm" + k + countMarge + m + ".txt"));
                        int counterWriter = 0;

                        String firstFileLine = readFromTmpPostingTerm1.readLine();
                        String secondFileLine = readFromTmpPostingTerm2.readLine();

                        while (firstFileLine != null && secondFileLine != null) {
                            String[] split1 = splitLine(firstFileLine);
                            String[] split2 = splitLine(secondFileLine);

                            int option = split1[0].compareTo(split2[0]);
                            if (option == 0) {
                                int df = Integer.parseInt(split1[2]) + Integer.parseInt(split2[2]);
                                int freq = Integer.parseInt(split1[3]) + Integer.parseInt(split2[3]);
                                String toAdd = split1[0] + "!" + mergeDocs(split1[1], split2[1]) + "!" + df + "!" + freq;
                                if (split1[4].equals(split2[4])) {// e,e  s,s  u,u  l,l
                                    if (split1[4].equals("s") || split1[4].equals("e"))
                                        toAdd = toAdd + "^e";
                                    else if (split1[4].equals("u"))
                                        toAdd = toAdd + "^u";
                                    else if (split1[4].equals("l"))
                                        toAdd = toAdd + "^l";
                                } else if (split1[4].equals("e") || split2[4].equals("e"))
                                    toAdd = toAdd + "^e";
                                else
                                    toAdd = toAdd + "^l";

                                writerToMargeTmpPosting.append(toAdd);
                                writerToMargeTmpPosting.newLine();
                                counterWriter++;
                                firstFileLine = readFromTmpPostingTerm1.readLine();
                                secondFileLine = readFromTmpPostingTerm2.readLine();

                            } else if (option <= -1) {

                                writerToMargeTmpPosting.append(firstFileLine);
                                writerToMargeTmpPosting.newLine();
                                counterWriter++;
                                firstFileLine = readFromTmpPostingTerm1.readLine();

                            } else {
                                writerToMargeTmpPosting.append(secondFileLine);
                                writerToMargeTmpPosting.newLine();
                                counterWriter++;
                                secondFileLine = readFromTmpPostingTerm2.readLine();
                            }

                            if (counterWriter >= writeToBuff) {
                                writerToMargeTmpPosting.flush();
                                counterWriter = 0;
                            }

                        }

                        while (firstFileLine != null) {
                            writerToMargeTmpPosting.append(firstFileLine);
                            writerToMargeTmpPosting.newLine();
                            counterWriter++;
                            if (counterWriter >= writeToBuff) {
                                writerToMargeTmpPosting.flush();
                                counterWriter = 0;
                            }
                            firstFileLine = readFromTmpPostingTerm1.readLine();

                        }

                        while (secondFileLine != null) {
                            writerToMargeTmpPosting.append(secondFileLine);
                            writerToMargeTmpPosting.newLine();
                            counterWriter++;
                            if (counterWriter >= writeToBuff) {
                                writerToMargeTmpPosting.flush();
                                counterWriter = 0;
                            }
                            secondFileLine = readFromTmpPostingTerm2.readLine();
                        }

                        if (counterWriter != 0) {
                            writerToMargeTmpPosting.flush();
                        }

                        writerToMargeTmpPosting.close();
                        readFromTmpPostingTerm1.close();
                        readFromTmpPostingTerm2.close();
                        f1.close();
                        f2.close();

                        Path path = Paths.get((postingPath + "/postingTerm" + k + (i) + h + ".txt"));
                        File f = path.toFile();
                        f.delete();
                        Path path2 = Paths.get((postingPath + "/postingTerm" + k + (i + 1) + h + ".txt"));
                        File ff = path2.toFile();
                        ff.delete();

                        countMarge++;

                    }
                    if (totalNewMarge - (countMarge - 1) > 0) {

                        FileReader f1 = new FileReader((postingPath + "/postingTerm" + k + (cur) + h + ".txt"));
                        FileWriter f2 = new FileWriter((postingPath) + "/postingTerm" + k + countMarge + m + ".txt");
                        readFromTmpPostingTerm1 = new BufferedReader(f1);
                        writerToMargeTmpPosting = new BufferedWriter(f2);
                        String firstFileLine = readFromTmpPostingTerm1.readLine();
                        int counterWriter = 0;

                        Path pathToRemove = Paths.get((postingPath + "/postingTerm" + k + (cur) + h + ".txt"));
                        File fToRemove = pathToRemove.toFile();

                        //if(fToRemove.renameTo(new File(postingPath + "/terms/postingTerm" + countMarge + m + ".txt"))){
                        //   System.out.println("remane to"+ postingPath + "/terms/postingTerm" + countMarge + m + ".txt");
                        //}

                        while (firstFileLine != null) {
                            writerToMargeTmpPosting.append(firstFileLine);
                            writerToMargeTmpPosting.append("\n");
                            countMarge++;
                            firstFileLine = readFromTmpPostingTerm1.readLine();
                            if (counterWriter >= writeToBuff) {
                                writerToMargeTmpPosting.flush();
                                counterWriter = 0;
                            }
                        }

                        if (counterWriter != 0) {
                            writerToMargeTmpPosting.flush();
                        }

                        //    writerToMargeTmpPosting.flush();
                        writerToMargeTmpPosting.close();
                        readFromTmpPostingTerm1.close();
                        f1.close();
                        f2.close();

                        Path path = Paths.get((postingPath + "/postingTerm" + k + (cur) + h + ".txt"));
                        File f = path.toFile();
                        f.delete();

                        countMarge++;
                    }

                    countMarge = 1;
                    cur = totalNewMarge;
                    h = m;
                    if (isStemming)
                        m = m + "S";
                    else
                        m = m + "R";

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (cur == 2) {
                int numberPointer = 0;
                int capitalPointer = 0;
                int lowerPointer1 = 0;
                int lowerPointer2 = 0;
                int lowerPointer3 = 0;
                try {
                    FileReader f1 = new FileReader((postingPath + "/postingTerm" + k + (1) + h + ".txt"));
                    FileReader f2 = new FileReader((postingPath + "/postingTerm" + k + (2) + h + ".txt"));
                    readFromTmpPostingTerm1 = new BufferedReader(f1);
                    readFromTmpPostingTerm2 = new BufferedReader(f2);
                    BufferedWriter numberWriter = null;
                    BufferedWriter capitalWriter = null;
                    BufferedWriter lowerWriter1 = null;
                    BufferedWriter lowerWriter2 = null;
                    BufferedWriter lowerWriter3 = null;
                    FileWriter numberFile = null;
                    FileWriter capitalFile = null;
                    FileWriter lowerFile1 = null;
                    FileWriter lowerFile2 = null;
                    FileWriter lowerFile3 = null;
                    if (k == "!R") {
                        numberFile = new FileWriter((postingPath) + "/finalPostingNumbersNoStemming" + ".txt");
                        capitalFile = new FileWriter((postingPath) + "/finalPostingCapitalNoStemming" + ".txt");
                        lowerFile1 = new FileWriter((postingPath) + "/finalPostingLowertNoStemmingD" + ".txt");
                        lowerFile2 = new FileWriter((postingPath) + "/finalPostingLowertNoStemmingP" + ".txt");
                        lowerFile3 = new FileWriter((postingPath) + "/finalPostingLowertNoStemmingZ" + ".txt");
                        lowerWriter1 = new BufferedWriter(lowerFile1);
                        lowerWriter2 = new BufferedWriter(lowerFile2);
                        lowerWriter3 = new BufferedWriter(lowerFile3);
                        numberWriter = new BufferedWriter(numberFile);
                        capitalWriter = new BufferedWriter(capitalFile);

                    } else {
                        numberFile = new FileWriter((postingPath) + "/finalPostingNumbersWithStemming" + ".txt");
                        capitalFile = new FileWriter((postingPath) + "/finalPostingCapitalWithStemming" + ".txt");
                        lowerFile1 = new FileWriter((postingPath) + "/finalPostingLowerWithStemmingD" + ".txt");
                        lowerFile2 = new FileWriter((postingPath) + "/finalPostingLowerWithStemmingP" + ".txt");
                        lowerFile3 = new FileWriter((postingPath) + "/finalPostingLowerWithStemmingZ" + ".txt");
                        lowerWriter1 = new BufferedWriter(lowerFile1);
                        lowerWriter2 = new BufferedWriter(lowerFile2);
                        lowerWriter3 = new BufferedWriter(lowerFile3);
                        numberWriter = new BufferedWriter(numberFile);
                        capitalWriter = new BufferedWriter(capitalFile);
                    }
                    int counterLowerWriter1 = 0;
                    int counterLowerWriter2 = 0;
                    int counterLowerWriter3 = 0;
                    int counterCapitalWriter = 0;
                    int counterNumberWriter = 0;
                    String firstFileLine = readFromTmpPostingTerm1.readLine();
                    String secondFileLine = readFromTmpPostingTerm2.readLine();
                    while (firstFileLine != null && secondFileLine != null) {
                        String[] split1 = splitLine(firstFileLine);
                        String[] split2 = splitLine(secondFileLine);
                        int option = split1[0].compareTo(split2[0]);
                        if (option == 0) {
                            int df = Integer.parseInt(split1[2]) + Integer.parseInt(split2[2]);
                            int freq = Integer.parseInt(split1[3]) + Integer.parseInt(split2[3]);
                            String docs = mergeDocs(split1[1], split2[1]);
                            String toAdd = split1[0] + "!" + docs + "!" + df + "!" + freq;
                            if (split1[4].equals(split2[4])) {// e,e  s,s  u,u  l,l
                                if (split1[4].equals("s") || split1[4].equals("e"))
                                    toAdd = toAdd + "^e";
                                else if (split1[4].equals("u"))
                                    toAdd = toAdd + "^u";
                                else if (split1[4].equals("l"))
                                    toAdd = toAdd + "^l";
                            } else if (split1[4].equals("e") || split2[4].equals("e"))
                                toAdd = toAdd + "^e";
                            else
                                toAdd = toAdd + "^l";
                            if (toAdd.contains("^u") || toAdd.contains("^e")) {
                                capitalPointer++;
                                counterCapitalWriter++;
                                Indexer.addTerm(split1[0].toUpperCase(), df + "!" + freq + "!" + capitalPointer);
                                capitalWriter.append(split1[0].toUpperCase() + "!" + docs + "!" + df + "!" + freq);
                                capitalWriter.newLine();
                                if (counterCapitalWriter > writeToBuff / 2) {
                                    capitalWriter.flush();
                                    counterCapitalWriter = 0;
                                }
                            } else if (toAdd.contains("^l")) {
                                if (split1[0].charAt(0) > '9' || split1[0].charAt(0) < '0') {
                                    if (split1[0].charAt(0) <= 'd') {
                                        lowerPointer1++;
                                        counterLowerWriter1++;
                                        Indexer.addTerm(split1[0].toLowerCase(), df + "!" + freq + "!" + lowerPointer1);
                                        lowerWriter1.append(split1[0].toLowerCase() + "!" + docs + "!" + df + "!" + freq);
                                        lowerWriter1.newLine();
                                        if (counterLowerWriter1 > writeToBuff / 2) {
                                            lowerWriter1.flush();
                                            counterLowerWriter1 = 0;
                                        }
                                    } else if (split1[0].charAt(0) <= 'p') {
                                        lowerPointer2++;
                                        counterLowerWriter2++;
                                        Indexer.addTerm(split1[0].toLowerCase(), df + "!" + freq + "!" + lowerPointer2);
                                        lowerWriter2.append(split1[0].toLowerCase() + "!" + docs + "!" + df + "!" + freq);
                                        lowerWriter2.newLine();
                                        if (counterLowerWriter2 > writeToBuff / 2) {
                                            lowerWriter2.flush();
                                            counterLowerWriter2 = 0;
                                        }
                                    } else {
                                        lowerPointer3++;
                                        counterLowerWriter3++;
                                        Indexer.addTerm(split1[0].toLowerCase(), df + "!" + freq + "!" + lowerPointer3);
                                        lowerWriter3.append(split1[0].toLowerCase() + "!" + docs + "!" + df + "!" + freq);
                                        lowerWriter3.newLine();
                                        if (counterLowerWriter3 > writeToBuff / 2) {
                                            lowerWriter3.flush();
                                            counterLowerWriter3 = 0;
                                        }
                                    }

                                } else {
                                    numberPointer++;
                                    counterNumberWriter++;
                                    Indexer.addTerm(split1[0], df + "!" + freq + "!" + numberPointer);
                                    numberWriter.append(split1[0] + "!" + docs + "!" + df + "!" + freq);
                                    numberWriter.newLine();
                                    if (counterNumberWriter > writeToBuff / 2) {
                                        numberWriter.flush();
                                        counterNumberWriter = 0;
                                    }
                                }
                            }
                            firstFileLine = readFromTmpPostingTerm1.readLine();
                            secondFileLine = readFromTmpPostingTerm2.readLine();
                        } else if (option <= -1) {
                            if (firstFileLine.contains("^u") || firstFileLine.contains("^e")) {
                                capitalPointer++;
                                counterCapitalWriter++;
                                Indexer.addTerm(split1[0].toUpperCase(), split1[2] + "!" + split1[3] + "!" + capitalPointer);
                                capitalWriter.append(split1[0].toUpperCase() + "!" + split1[1] + "!" + split1[2] + "!" + split1[3]);
                                capitalWriter.newLine();
                                if (counterCapitalWriter > writeToBuff / 2) {
                                    capitalWriter.flush();
                                    counterCapitalWriter = 0;
                                }
                            } else if (firstFileLine.contains("^l")) {
                                if (split1[0].charAt(0) > '9' || split1[0].charAt(0) < '0') {
                                    if (split1[0].charAt(0) <= 'd') {
                                        lowerPointer1++;
                                        counterLowerWriter1++;
                                        Indexer.addTerm(split1[0].toLowerCase(), split1[2] + "!" + split1[3] + "!" + lowerPointer1);
                                        lowerWriter1.append(split1[0].toLowerCase() + "!" + split1[1] + "!" + split1[2] + "!" + split1[3]);
                                        lowerWriter1.newLine();
                                        if (counterLowerWriter1 > writeToBuff / 2) {
                                            counterLowerWriter1 = 0;
                                            lowerWriter1.flush();
                                        }
                                    } else if (split1[0].charAt(0) <= 'p') {
                                        lowerPointer2++;
                                        counterLowerWriter2++;
                                        Indexer.addTerm(split1[0].toLowerCase(), split1[2] + "!" + split1[3] + "!" + lowerPointer2);
                                        lowerWriter2.append(split1[0].toLowerCase() + "!" + split1[1] + "!" + split1[2] + "!" + split1[3]);
                                        lowerWriter2.newLine();
                                        if (counterLowerWriter2 > writeToBuff / 2) {
                                            counterLowerWriter2 = 0;
                                            lowerWriter2.flush();
                                        }
                                    } else {
                                        lowerPointer3++;
                                        counterLowerWriter3++;
                                        Indexer.addTerm(split1[0].toLowerCase(), split1[2] + "!" + split1[3] + "!" + lowerPointer3);
                                        lowerWriter3.append(split1[0].toLowerCase() + "!" + split1[1] + "!" + split1[2] + "!" + split1[3]);
                                        lowerWriter3.newLine();
                                        if (counterLowerWriter3 > writeToBuff / 2) {
                                            counterLowerWriter3 = 0;
                                            lowerWriter3.flush();
                                        }
                                    }

                                } else {
                                    numberPointer++;
                                    counterNumberWriter++;
                                    Indexer.addTerm(split1[0], split1[2] + "!" + split1[3] + "!" + numberPointer);
                                    numberWriter.append(split1[0] + "!" + split1[1] + "!" + split1[2] + "!" + split1[3]);
                                    numberWriter.newLine();
                                    if (counterNumberWriter > writeToBuff / 2) {
                                        counterNumberWriter = 0;
                                        numberWriter.flush();
                                    }
                                }
                            }
                            firstFileLine = readFromTmpPostingTerm1.readLine();
                        } else {
                            if (secondFileLine.contains("^u") || secondFileLine.contains("^e")) {
                                capitalPointer++;
                                counterCapitalWriter++;
                                Indexer.addTerm(split2[0].toUpperCase(), split2[2] + "!" + split2[3] + "!" + capitalPointer);
                                capitalWriter.append(split2[0].toUpperCase() + "!" + split2[1] + "!" + split2[2] + "!" + split2[3]);
                                capitalWriter.newLine();
                                if (counterCapitalWriter > writeToBuff / 2) {
                                    counterCapitalWriter = 0;
                                    capitalWriter.flush();
                                }
                            } else if (secondFileLine.contains("^l")) {
                                if (split2[0].charAt(0) < '0' || split2[0].charAt(0) > '9') {
                                    if (split2[0].charAt(0) <= 'd') {
                                        lowerPointer1++;
                                        counterLowerWriter1++;
                                        Indexer.addTerm(split2[0].toLowerCase(), split2[2] + "!" + split2[3] + "!" + lowerPointer1);
                                        lowerWriter1.append(split2[0].toLowerCase() + "!" + split2[1] + "!" + split2[2] + "!" + split2[3]);
                                        lowerWriter1.newLine();
                                        if (lowerPointer1 > writeToBuff / 2) {
                                            lowerWriter1.flush();
                                            counterLowerWriter1 = 0;
                                        }
                                    } else if (split2[0].charAt(0) <= 'p') {
                                        lowerPointer2++;
                                        counterLowerWriter2++;
                                        Indexer.addTerm(split2[0].toLowerCase(), split2[2] + "!" + split2[3] + "!" + lowerPointer2);
                                        lowerWriter2.append(split2[0].toLowerCase() + "!" + split2[1] + "!" + split2[2] + "!" + split2[3]);
                                        lowerWriter2.newLine();
                                        if (lowerPointer2 > writeToBuff / 2) {
                                            lowerWriter2.flush();
                                            counterLowerWriter2 = 0;
                                        }
                                    } else {
                                        lowerPointer3++;
                                        counterLowerWriter3++;
                                        Indexer.addTerm(split2[0].toLowerCase(), split2[2] + "!" + split2[3] + "!" + lowerPointer3);
                                        lowerWriter3.append(split2[0].toLowerCase() + "!" + split2[1] + "!" + split2[2] + "!" + split2[3]);
                                        lowerWriter3.newLine();
                                        if (lowerPointer3 > writeToBuff / 2) {
                                            lowerWriter3.flush();
                                            counterLowerWriter3 = 0;
                                        }
                                    }

                                } else {
                                    numberPointer++;
                                    counterNumberWriter++;
                                    Indexer.addTerm(split2[0], split2[2] + "!" + split2[3] + "!" + numberPointer);
                                    numberWriter.append(split2[0] + "!" + split2[1] + "!" + split2[2] + "!" + split2[3]);
                                    numberWriter.newLine();
                                    if (counterNumberWriter > writeToBuff / 2) {
                                        counterNumberWriter = 0;
                                        numberWriter.flush();
                                    }
                                }

                            }
                            secondFileLine = readFromTmpPostingTerm2.readLine();
                        }
                    }
                    while (firstFileLine != null) {
                        String[] split1 = splitLine(firstFileLine);
                        if (firstFileLine.contains("^u") || firstFileLine.contains("^e")) {
                            capitalPointer++;
                            counterCapitalWriter++;
                            Indexer.addTerm(split1[0].toUpperCase(), split1[2] + "!" + split1[3] + "!" + capitalPointer);
                            capitalWriter.append(split1[0].toUpperCase() + "!" + split1[1] + "!" + split1[2] + "!" + split1[3]);
                            capitalWriter.newLine();
                            if (counterCapitalWriter > writeToBuff / 2) {
                                capitalWriter.flush();
                                counterCapitalWriter = 0;
                            }
                        } else if (firstFileLine.contains("^l")) {
                            if (split1[0].charAt(0) > '9' || split1[0].charAt(0) < '0') {
                                if (split1[0].charAt(0) <= 'd') {
                                    lowerPointer1++;
                                    counterLowerWriter1++;
                                    Indexer.addTerm(split1[0].toLowerCase(), split1[2] + "!" + split1[3] + "!" + lowerPointer1);
                                    lowerWriter1.append(split1[0].toLowerCase() + "!" + split1[1] + "!" + split1[2] + "!" + split1[3]);
                                    lowerWriter1.newLine();
                                    if (counterLowerWriter1 > writeToBuff / 2) {
                                        lowerWriter1.flush();
                                        counterLowerWriter1 = 0;
                                    }
                                } else if (split1[0].charAt(0) <= 'p') {
                                    lowerPointer2++;
                                    counterLowerWriter2++;
                                    Indexer.addTerm(split1[0].toLowerCase(), split1[2] + "!" + split1[3] + "!" + lowerPointer2);
                                    lowerWriter2.append(split1[0].toLowerCase() + "!" + split1[1] + "!" + split1[2] + "!" + split1[3]);
                                    lowerWriter2.newLine();
                                    if (counterLowerWriter2 > writeToBuff / 2) {
                                        lowerWriter2.flush();
                                        counterLowerWriter2 = 0;
                                    }
                                } else {
                                    lowerPointer3++;
                                    counterLowerWriter3++;
                                    Indexer.addTerm(split1[0].toLowerCase(), split1[2] + "!" + split1[3] + "!" + lowerPointer3);
                                    lowerWriter3.append(split1[0].toLowerCase() + "!" + split1[1] + "!" + split1[2] + "!" + split1[3]);
                                    lowerWriter3.newLine();
                                    if (counterLowerWriter3 > writeToBuff / 2) {
                                        lowerWriter3.flush();
                                        counterLowerWriter3 = 0;
                                    }
                                }
                            } else {
                                numberPointer++;
                                counterNumberWriter++;
                                Indexer.addTerm(split1[0], split1[2] + "!" + split1[3] + "!" + numberPointer);
                                numberWriter.append(split1[0] + "!" + split1[1] + "!" + split1[2] + "!" + split1[3]);
                                numberWriter.newLine();
                                if (counterNumberWriter > writeToBuff / 2) {
                                    counterNumberWriter = 0;
                                    numberWriter.flush();
                                }
                            }
                        }
                        firstFileLine = readFromTmpPostingTerm1.readLine();
                    }
                    while (secondFileLine != null) {
                        String[] split2 = splitLine(secondFileLine);
                        if (secondFileLine.contains("^u") || secondFileLine.contains("^e")) {
                            capitalPointer++;
                            counterCapitalWriter++;
                            Indexer.addTerm(split2[0].toUpperCase(), split2[2] + "!" + split2[3] + "!" + capitalPointer);
                            capitalWriter.append(split2[0].toUpperCase() + "!" + split2[1] + "!" + split2[2] + "!" + split2[3]);
                            capitalWriter.newLine();
                            if (counterCapitalWriter > writeToBuff / 2) {
                                capitalWriter.flush();
                                counterCapitalWriter = 0;
                            }
                        } else if (secondFileLine.contains("^l")) {
                            if (split2[0].charAt(0) < '0' || split2[0].charAt(0) > '9') {
                                if (split2[0].charAt(0) <= 'd') {
                                    lowerPointer1++;
                                    counterLowerWriter1++;
                                    Indexer.addTerm(split2[0].toLowerCase(), split2[2] + "!" + split2[3] + "!" + lowerPointer1);
                                    lowerWriter1.append(split2[0].toLowerCase() + "!" + split2[1] + "!" + split2[2] + "!" + split2[3]);
                                    lowerWriter1.newLine();
                                    if (counterLowerWriter1 > writeToBuff / 2) {
                                        counterLowerWriter1 = 0;
                                        lowerWriter1.flush();
                                    }
                                } else if (split2[0].charAt(0) <= 'p') {
                                    lowerPointer2++;
                                    counterLowerWriter2++;
                                    Indexer.addTerm(split2[0].toLowerCase(), split2[2] + "!" + split2[3] + "!" + lowerPointer2);
                                    lowerWriter2.append(split2[0].toLowerCase() + "!" + split2[1] + "!" + split2[2] + "!" + split2[3]);
                                    lowerWriter2.newLine();
                                    if (counterLowerWriter2 > writeToBuff / 2) {
                                        counterLowerWriter2 = 0;
                                        lowerWriter2.flush();
                                    }
                                } else {
                                    lowerPointer2++;
                                    counterLowerWriter2++;
                                    Indexer.addTerm(split2[0].toLowerCase(), split2[2] + "!" + split2[3] + "!" + lowerPointer2);
                                    lowerWriter2.append(split2[0].toLowerCase() + "!" + split2[1] + "!" + split2[2] + "!" + split2[3]);
                                    lowerWriter2.newLine();
                                    if (counterLowerWriter2 > writeToBuff / 2) {
                                        counterLowerWriter2 = 0;
                                        lowerWriter2.flush();
                                    }
                                }

                            } else {
                                numberPointer++;
                                counterNumberWriter++;
                                Indexer.addTerm(split2[0], split2[2] + "!" + split2[3] + "!" + numberPointer);
                                numberWriter.append(split2[0] + "!" + split2[1] + "!" + split2[2] + "!" + split2[3]);
                                numberWriter.newLine();
                                if (counterNumberWriter > writeToBuff / 2) {
                                    counterNumberWriter = 0;
                                    numberWriter.flush();
                                }
                            }
                        }
                        secondFileLine = readFromTmpPostingTerm2.readLine();
                    }
                    if (counterLowerWriter1 != 0) {
                        lowerWriter1.flush();
                    }
                    if (counterLowerWriter2 != 0) {
                        lowerWriter2.flush();
                    }
                    if (counterLowerWriter3 != 0) {
                        lowerWriter3.flush();
                    }
                    if (counterCapitalWriter != 0) {
                        capitalWriter.flush();
                    }
                    if (counterNumberWriter != 0) {
                        numberWriter.flush();
                    }
                    lowerWriter1.close();
                    lowerWriter2.close();
                    lowerWriter3.close();
                    numberWriter.close();
                    capitalWriter.close();
                    lowerFile1.close();
                    lowerFile2.close();
                    lowerFile3.close();
                    numberFile.close();
                    capitalFile.close();
                    readFromTmpPostingTerm1.close();
                    readFromTmpPostingTerm2.close();
                    f1.close();
                    f2.close();
                    Path path = Paths.get((postingPath + "/postingTerm" + k + (1) + h + ".txt"));
                    File f = path.toFile();
                    f.delete();
                    Path path2 = Paths.get((postingPath + "/postingTerm" + k + (2) + h + ".txt"));
                    File ff = path2.toFile();
                    ff.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Indexer.print();
        }
    }
        private void handleLittleCorpus (String k, String h, String m){
            if (docCounter != 0) {
                try {
                    if (isStemming)
                        writerToPostingTerm = new BufferedWriter(new FileWriter(postingPath + "/postingTerm!S" + chunksCount + ".txt"));
                    else
                        writerToPostingTerm = new BufferedWriter(new FileWriter(postingPath + "/postingTerm!R" + chunksCount + ".txt"));

                    TreeMap<String, String> sortedTerms = new TreeMap<>(mergeTerms);
                    for (Map.Entry entry : sortedTerms.entrySet()) {
                        writerToPostingTerm.write(entry.getKey().toString() + entry.getValue().toString());
                        writerToPostingTerm.write('\n');

                    }

                    docCounter = 0;
                    mergeTerms.clear();
                    chunksCount++;
                    writerToPostingTerm.close();

                    FileReader f1 = new FileReader((postingPath + "/postingTerm" + k + (1) + h + ".txt"));
                    readFromTmpPostingTerm1 = new BufferedReader(f1);

                    if (isStemming)
                        writerToMargeTmpPosting = new BufferedWriter(new FileWriter((postingPath) + "/finalPostingWithStemming.txt"));
                    else
                        writerToMargeTmpPosting = new BufferedWriter(new FileWriter((postingPath) + "/finalPostingNoStemming.txt"));


                    String firstFileLine = readFromTmpPostingTerm1.readLine();
                    int countPointer = 0;

                    while (firstFileLine != null) {
                        countPointer++;
                        String[] split1 = splitLine(firstFileLine);
                        if (firstFileLine.contains("^u")) {
                            Indexer.addTerm(split1[0].toUpperCase(), split1[2] + "!" + split1[3] + "!" + countPointer);
                            writerToMargeTmpPosting.write(split1[0].toUpperCase() + "!" + split1[1] + "!" + split1[2] + "!" + split1[3]);
                            writerToMargeTmpPosting.write('\n');

                        } else if (firstFileLine.contains("^e")) {
                            Indexer.addTerm(split1[0].toUpperCase(), split1[2] + "!" + split1[3] + "!" + countPointer);
                            writerToMargeTmpPosting.write(split1[0].toUpperCase() + "!" + split1[1] + "!" + split1[2] + "!" + split1[3]);
                            writerToMargeTmpPosting.write('\n');

                        } else if (firstFileLine.contains("^l")) {
                            Indexer.addTerm(split1[0], split1[2] + "!" + split1[3] + "!" + countPointer);
                            writerToMargeTmpPosting.write(split1[0] + "!" + split1[1] + "!" + split1[2] + "!" + split1[3]);
                            writerToMargeTmpPosting.write('\n');

                        }

                        firstFileLine = readFromTmpPostingTerm1.readLine();
                    }


                    writerToMargeTmpPosting.close();
                    readFromTmpPostingTerm1.close();
                    f1.close();

                    Path path = Paths.get((postingPath + "/postingTerm" + k + (1) + h + ".txt"));
                    File f = path.toFile();
                    f.delete();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String mergeDocs (String s1, String s2)
        {
            StringBuilder mergeDocs = new StringBuilder();
            String[] firstDocs = s1.split(",");
            String[] secondDocs = s2.split(",");
            int i = 0;
            int j = 0;
            while (i < firstDocs.length && j < secondDocs.length) {
                String docName1 = firstDocs[i].substring(0, firstDocs[i].indexOf(":"));
                String docName2 = secondDocs[j].substring(0, secondDocs[j].indexOf(":"));
                int option = docName1.compareTo(docName2);
                if (option < 0) {
                    mergeDocs = mergeDocs.append(firstDocs[i] + ",");
                    i++;
                } else {
                    mergeDocs = mergeDocs.append(secondDocs[j] + ",");
                    j++;
                }
            }
            while (i < firstDocs.length) {
                mergeDocs = mergeDocs.append(firstDocs[i] + ",");
                i++;
            }
            while (j < secondDocs.length) {
                mergeDocs = mergeDocs.append(secondDocs[j] + ",");
                j++;
            }
            return "[" + mergeDocs.substring(0, mergeDocs.length() - 1) + "]";
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
        private String[] splitLine (String fileLine){
            String[] ans = new String[5];
            ans[0] = fileLine.substring(0, fileLine.indexOf("!"));
            ans[1] = fileLine.substring(fileLine.indexOf("[") + 1, fileLine.indexOf("]"));
            String dfTf = fileLine.substring(fileLine.indexOf("]!") + 2);
            ans[3] = dfTf;
            ans[2] = dfTf.substring(0, dfTf.indexOf("!"));
            if (dfTf.contains("^")) {
                ans[4] = dfTf.substring(dfTf.length() - 1);
                ans[3] = ans[3].substring(ans[3].indexOf("!") + 1, ans[3].length() - 2);
            } else
                ans[3] = ans[3].substring(dfTf.indexOf("!") + 1);

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
        try {
            for (String document:documents
                 ) {

                writerToPostingDoc.append(document);
                writerToPostingDoc.newLine();
            }
            writerToPostingDoc.flush();
            writerToPostingDoc.write("~"+docFileCounter);
            writerToPostingDoc.newLine();
            writerToPostingDoc.write("~"+(sumLengthAllDoc/docFileCounter));
            writerToPostingDoc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setIsStemming(boolean isStemming) {
        this.isStemming=isStemming;
    }

    public void reset() {
        try {
            writerToPostingDoc.close();

            Path path = Paths.get((postingPath + "/indexerWithStemming.txt"));
            File  f = path.toFile();
            if(f.exists())
                f.delete();
            path = Paths.get((postingPath + "/postingDocumentsWithStemming.txt"));
            f = path.toFile();
            if(f.exists())
                f.delete();

            path = Paths.get((postingPath + "/indexerNoStemming.txt"));
            f = path.toFile();
            if(f.exists())
                f.delete();
            path = Paths.get((postingPath + "/postingDocumentsNoStemming.txt"));
            f = path.toFile();
            if(f.exists())
                f.delete();

            path = Paths.get((postingPath + "/finalPostingCapitalNoStemming.txt"));
            f = path.toFile();
            if(f.exists())
                f.delete();

            path = Paths.get((postingPath + "/finalPostingLowertNoStemmingD.txt"));
            f = path.toFile();
            if(f.exists())
                f.delete();

           path = Paths.get((postingPath + "/finalPostingLowertNoStemmingP.txt"));
            f = path.toFile();
            if(f.exists())
                f.delete();
            path = Paths.get((postingPath + "/finalPostingLowertNoStemmingZ.txt"));
            f = path.toFile();
            if(f.exists())
                f.delete();
            path = Paths.get((postingPath + "/finalPostingNumbersNoStemming.txt"));
            f = path.toFile();
            if(f.exists())
                f.delete();

            path = Paths.get((postingPath + "/finalPostingCapitalWithStemming.txt"));
            f = path.toFile();
            if(f.exists())
                f.delete();

            path = Paths.get((postingPath + "/finalPostingLowerWithStemmingD.txt"));
            f = path.toFile();
            if(f.exists())
                f.delete();

            path = Paths.get((postingPath + "/finalPostingLowerWithStemmingP.txt"));
            f = path.toFile();
            if(f.exists())
                f.delete();
            path = Paths.get((postingPath + "/finalPostingLowerWithStemmingZ.txt"));
            f = path.toFile();
            if(f.exists())
                f.delete();
            path = Paths.get((postingPath + "/finalPostingNumbersWithStemming.txt"));
            f = path.toFile();
            if(f.exists())
                f.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

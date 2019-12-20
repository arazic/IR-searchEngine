package Model;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Indexer {

        private static TreeMap<String, String> termsDic; // margePostingTerm
        private static HashMap<String, String> docDic; // docName, <postingFileName><postingPlaceInFile>
        private static String postingPath;
        private static boolean isStemming;

        private static int termsNumber=0;
        public static int containsNumber=0;
        public static int pureNumbera=0;
        private static PriorityQueue<Term> frequencyMap= new PriorityQueue<Term>(new TermComparator());
        private static Pattern containsNumberPattern= Pattern.compile(".*[0-9].*");
        private static Pattern pureNumberPattern= Pattern.compile("((([0-9]*)"+"[.,])*)"+"([0-9]*)");

        public static void initIndexer(String post, boolean isStemm){
            termsDic= new TreeMap<>();
            docDic= new HashMap<>();
            postingPath=post;
            isStemming=isStemm;
        }


        public static void addDoc(String docName, String pointer){
            docDic.put(docName,pointer);
        }

         public static void termsFrequency(String term,int frequency)
         {
             Term t= new Term(term,frequency);
             frequencyMap.add(t);
         }

         public static void printFrequency()
         {
             try
             {
                 int rank=1;
                 BufferedWriter writer = new BufferedWriter(new FileWriter("C:/Users/gal/Desktop/FB396018/documents/frequency.txt"));
                 for (Term term:frequencyMap
                      ) {
                     writer.write(String.valueOf(rank)+"           "+ String.valueOf(term.getFreq()));
                     writer.newLine();
                 }
             }
             catch (IOException e){

             }

         }
         public static void addTerm(String stringTerm, String info)
         {
             if(!termsDic.containsKey(stringTerm))
                {
                    termsDic.put(stringTerm,info);
                     termsNumber++;
                }
             if(containsNumberPattern.matcher(stringTerm).matches())
             {
                containsNumber++;
                if(pureNumberPattern.matcher(stringTerm).matches())
                {
                    pureNumbera++;
                }
             }
         }

    public static void print() {
        try {

            BufferedWriter index;
            if(isStemming)
                 index= new BufferedWriter(new FileWriter(postingPath+ "/indexerWithStemming.txt"));
            else
                 index= new BufferedWriter(new FileWriter(postingPath+ "/indexerNoStemming.txt"));
            Set set= termsDic.entrySet();
            Iterator it = set.iterator();
            while(it.hasNext())
            {
                Map.Entry cur = (Map.Entry)it.next();
                index.append(cur.getKey()+","+ cur.getValue());
                index.append("\n");
            }
            index.flush();
            System.out.println(termsDic.size());
            index.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static int getSizeWithSteem() {
        return termsDic.size();
    }

    public static int getTermsNumber()
    {
        return termsNumber;
    }

    public static boolean loadData() {
        try {
            FileReader fr;
            if(isStemming)
                 fr = new FileReader(postingPath+ "/indexerWithStemming.txt");
            else
                 fr = new FileReader(postingPath+ "/indexerNoStemming.txt");
            BufferedReader readerIndex= new BufferedReader(fr);
            String line= readerIndex.readLine();

            while (line != null) {

                String term= line.substring(0,line.indexOf(","));
                String info= line.substring(line.indexOf(",")+1);
                Indexer.addTerm(term,info);
                line= readerIndex.readLine();
            }

        return true;
        } catch (FileNotFoundException e) {
            // e.printStackTrace();
        } catch (IOException e) {
           // e.printStackTrace();
        }
        return false;
    }

    public static void setIsStemming(boolean isStemm) {
        isStemming=isStemm;
    }

    public static TreeMap<String, String> getTermsDic(){
        return termsDic;
    }
}

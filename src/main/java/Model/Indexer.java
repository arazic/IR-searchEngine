package Model;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Indexer
{

        private static TreeMap<String, String> termsDic; // margePostingTerm
        private static String postingPath;
        private static boolean isStemming;

        private static int termsNumber=0;

        public static void initIndexer(String post, boolean isStemm){
            termsDic= new TreeMap<>();
            postingPath=post;
            isStemming=isStemm;
        }


    /**
     * add term to the final dictionary
     */
         public static void addTerm(String stringTerm, String info)
         {
             if(!termsDic.containsKey(stringTerm))
                {
                     termsDic.put(stringTerm,info);
                     termsNumber++;
                }
         }

    /**
     * create indexer file with all the terms in the dictionary
     */
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
                index.newLine();
            }
            index.flush();
           // System.out.println(termsDic.size());
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

    /**
     * get file with final indexer details and load it to exist indexer
     * for testing the engine
     * @return
     */
    public static TreeMap<String, String> loadData() {
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

        return Indexer.getTermsDic();
        } catch (FileNotFoundException e) {
            // e.printStackTrace();
        } catch (IOException e) {
           // e.printStackTrace();
        }
        return null;

    }

    public static void setIsStemming(boolean isStemm) {
        isStemming=isStemm;
    }

    public static TreeMap<String, String> getTermsDic(){
        return termsDic;
    }
}

package Model;

import java.io.*;
import java.util.*;

public class Indexer {

        private static TreeMap<String, String> termsDic; // margePostingTerm
        private static TreeMap<String, String> entityDic; // margePostingTerm
        private static HashMap<String, String> docDic; // docName, <postingFileName><postingPlaceInFile>
        private static int sizeWithSteem;
        private static int sizeWithpoutSteem;
        private static String postingPath;
        private static boolean isStemming;

        public static void initIndexer(String post, boolean isStemm){
            termsDic= new TreeMap<>();
            entityDic= new TreeMap<>();
            docDic= new HashMap<>();
            postingPath=post;
            isStemming=isStemm;
        }


        public static void addDoc(String docName, String pointer){
            docDic.put(docName,pointer);
          //  System.out.println(docDic);
        }

    public static void addTerm(String stringTerm, String info) {
            // if the term did not exist- we need to add with info from mearged posting file.
        if(!termsDic.containsKey(stringTerm))
            termsDic.put(stringTerm,info);
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

            while(it.hasNext()) {
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


/*
    public static void print() {
        System.out.println(termsDic.size());
        System.out.println(entityDic.size());
    }
*/

    public static void addTermToEntity(String entityName, String info) {
        entityDic.put(entityName,info);
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

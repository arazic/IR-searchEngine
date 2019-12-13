import java.io.*;
import java.util.*;

public class Indexer {

        private static TreeMap<String, String> termsDic; // margePostingTerm
        private static TreeMap<String, String> entityDic; // margePostingTerm
        private static HashMap<String, String> docDic; // docName, <postingFileName><postingPlaceInFile>
        private static int sizeWithSteem;
        private static int sizeWithpoutSteem;

        public static void initIndexer(){
            termsDic= new TreeMap<>();
            entityDic= new TreeMap<>();
            docDic= new HashMap<>();
        }


        public static void addDoc(String docName, String pointer){
            docDic.put(docName,pointer);
            System.out.println(docDic);
        }

    public static void addTerm(String stringTerm, String info) {
            // if the term did not exist- we need to add with info from mearged posting file.
        if(!termsDic.containsKey(stringTerm))
            termsDic.put(stringTerm,info);
    }

    public static void print() {
        try {
            BufferedWriter index= new BufferedWriter(new FileWriter("C:/Users/user/engine/posting/terms/indexer.txt"));
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

             index= new BufferedWriter(new FileWriter("C:/Users/user/engine/posting/terms/indexerEntity.txt"));
             set= entityDic.entrySet();
             it = set.iterator();

            while(it.hasNext()) {
                Map.Entry cur = (Map.Entry)it.next();
                index.append(cur.getKey()+","+ cur.getValue());
                index.append("\n");
            }

            index.flush();
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


}

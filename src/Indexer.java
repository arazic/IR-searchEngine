import java.util.HashMap;
import java.util.TreeMap;

public class Indexer {

        private static TreeMap<String, String> termsDic; // margePostingTerm
        private static HashMap<String, String> docDic; // docName, <postingFileName><postingPlaceInFile>
        private static int sizeWithSteem;
        private static int sizeWithpoutSteem;

        public static void initIndexer(){
            termsDic= new TreeMap<>();
            docDic= new HashMap<>();
        }


        public static void addDoc(String docName, String pointer){
            docDic.put(docName,pointer);
            System.out.println(docDic);
        }

    public static void addTerm(String stringTerm, String info) {
            // if the term did not exist- we need to add with info from mearged posting file.
            termsDic.put(stringTerm,info);
    }

    //public void margePostingTermsFile();

}

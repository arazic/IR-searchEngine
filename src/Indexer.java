import java.util.HashMap;
import java.util.TreeMap;

public class Indexer {

        private static TreeMap<String, String> termsDic; // margePostingTerm
        private static HashMap<String, String> docDic; // docName, <postingFileName><postingPlaceInFile>
        private static int sizeWithSteem;
        private static int sizeWithpoutSteem;

        public static void addDoc(String docName, String pointer){
            docDic.put(docName,pointer);
        }

        //public void margePostingTermsFile();

}

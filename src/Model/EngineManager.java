package Model;


import java.util.TreeMap;

public class EngineManager {

    private String corpusPath;
    private String postingPath;
    private boolean stemming;
    private Parse parse;
    private Posting posting;
    private ReadFile readFile;

    public EngineManager(String corpusPath, String postingPath, boolean isStemming){
        this.corpusPath=corpusPath;
        this.postingPath=postingPath;
        this.stemming=isStemming;
        this.posting= new Posting(postingPath, isStemming);
        this.parse= new Parse(posting, isStemming);
        this.readFile= new ReadFile(corpusPath, parse);
        Indexer.initIndexer(postingPath,isStemming);

    }

    public EngineManager(String postingPath, boolean isStemming){
        this.corpusPath=null;
        this.postingPath=postingPath;
        this.stemming=isStemming;
        this.posting= new Posting(postingPath, isStemming);
        this.parse= new Parse(posting, isStemming);
        this.readFile= new ReadFile(corpusPath, parse);
        Indexer.initIndexer(postingPath,isStemming);

    }

    public void startEngine() {
        readFile.createDocuments();  //create also tmpPosting files
        posting.margeToMainPostingFile();
    }

/*
    public static void main(String[] args) {
        System.out.println("We are Google!");
        long millis=System.currentTimeMillis();
        java.util.Date date=new java.util.Date(millis);
        System.out.println("start "+ date);
        // EngineManager engineManager= new EngineManager("C:/Users/user/engine/corpus", "C:/Users/user/engine/posting",false);
        EngineManager engineManager= new EngineManager("C:/Users/gal/Desktop/FB396018/corpus", "C:/Users/gal/Desktop/FB396018/documents/terms",false);

        engineManager.startEngine();
        System.out.println("chen is my queen! time" );

        long millis2=System.currentTimeMillis();
        java.util.Date date2=new java.util.Date(millis2);
        System.out.println("end "+ date2);
    }
*/

    public boolean existCorpusPath() {
        if(this.corpusPath==null)
            return false;
        return true;
    }

    public void setCorpusPath(String corpusPath) {
        this.corpusPath=corpusPath;
    }

    public static boolean setIndexer() {
        return Indexer.loadData();
    }

    public void setIsStemming(boolean isStemming) {
        posting.setIsStemming(isStemming);
        parse.setIsStemming(isStemming);
        Indexer.setIsStemming(isStemming);

    }

    public static TreeMap<String, String> getTermsDic(){
        return Indexer.getTermsDic();
    }

    public void reset() {
        posting.reset();
    }
}


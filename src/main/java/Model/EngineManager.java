package Model;


import javafx.util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

// TODO: the docs are sorted by importance?
// TODO: to add just one sys term to semantic
// TODO: use indexer to find posting files
public class EngineManager {

    private String corpusPath;
    private String postingPath;
    private boolean stemming;
    private Parse parse;
    private Posting posting;
    private ReadFile readFile;
    private Searcher searcher;

    public EngineManager(String corpusPath, String postingPath, boolean isStemming){
        this.corpusPath=corpusPath;
        this.postingPath=postingPath;
        this.stemming=isStemming;
        this.posting= new Posting(postingPath, isStemming);
        this.parse= new Parse(posting, isStemming);
        this.readFile= new ReadFile(corpusPath, parse);
        this.searcher= new Searcher(parse,isStemming,postingPath);
        Indexer.initIndexer(postingPath,isStemming);
        readFile.readStopWordsNoRunning();

    }

/*    public EngineManager(String postingPath, boolean isStemming){
        this.corpusPath=null;
        this.postingPath=postingPath;
        this.stemming=isStemming;
        this.posting= new Posting(postingPath, isStemming);
        this.parse= new Parse(posting, isStemming);
        this.readFile= new ReadFile(corpusPath, parse);
        this.searcher= new Searcher(parse,isStemming,postingPath);
        Indexer.initIndexer(postingPath,isStemming);
        readFile.readStopWordsNoRunning();

    }*/

    public void startEngine() {
        readFile.createDocuments();  //create also tmpPosting files
        posting.margeToMainPostingFile();
    }


/*
    public static void main(String[] args) {
        System.out.println("We are Google!");
        long millis=System.currentTimeMillis();
        Date date1=new java.util.Date(millis);
        System.out.println("start "+ date1);

        Instant start = java.time.Instant.now();

         EngineManager engineManager= new EngineManager("C:/Users/user/engine/lXcorpus", "C:/Users/user/engine/posting",false);
        //EngineManager engineManager= new EngineManager("C:/Users/gal/Desktop/FB396018/corpus", "C:/Users/gal/Desktop/FB396018/documents/terms",false);
        engineManager.startEngine();
        System.out.println("chen is my queen! time" );

        millis=System.currentTimeMillis();
        Date date2=new java.util.Date(millis);
        System.out.println(date2);
        Instant end = java.time.Instant.now();
        Duration between = java.time.Duration.between(start, end);
              System.out.println("total time:\n" +between.toMinutes()+" minutes, "+
                between.getSeconds()+" seconds and "+ between.toMillis()+" millis");
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

    public static TreeMap<String, String> getIndexer() {
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
    public int totalDocNum(){
        return readFile.getTotalDocNum();
    }
    public LinkedList<Pair<String,String>> search(String currentQuery, boolean isStemming, boolean isSemantic) {
        return searcher.search(currentQuery,isStemming, isSemantic);
    }
    public HashMap<Integer, LinkedList<Pair<String,String>>> searchQueriesFile(String path, boolean isStemming, boolean isSemantic) {
       return searcher.readQueriesFromData(path,isStemming,isSemantic);
    }

    public void setQueriesResultPath(String queriesResultPath) {
        this.searcher.setQueriesResultPath(queriesResultPath);
    }
}


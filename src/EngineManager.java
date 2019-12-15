import java.util.Date;

public class EngineManager {

    private String corpusPath;
    private String postingPath;
    private boolean stemming;
    private Parse parse;
    private Posting posting;
    private ReadFile readFile;

    public static void main(String[] args) {
        System.out.println("We are Google!");
        long millis=System.currentTimeMillis();
        java.util.Date date=new java.util.Date(millis);
        System.out.println("start "+ date);
        EngineManager engineManager= new EngineManager("C:/Users/user/engine/lXcorpus", "C:/Users/user/engine/posting",false);
       // EngineManager engineManager= new EngineManager("C:/Users/gal/Desktop/FB396018", "C:/Users/gal/Desktop/FB396018/documents",false);

        engineManager.startEngine();
        System.out.println("chen is my queen! time" );

        long millis2=System.currentTimeMillis();
        java.util.Date date2=new java.util.Date(millis2);
        System.out.println("end "+ date2);
    }


    public EngineManager(String corpusPath, String postingPath, boolean stemming){
        this.corpusPath=corpusPath;
        this.postingPath=postingPath;
        this.stemming=stemming;
        this.posting= new Posting(postingPath);
        this.parse= new Parse(posting);
        this.readFile= new ReadFile(corpusPath, parse);
    }

    private void startEngine() {
        Indexer.initIndexer();
        readFile.createDocuments();  //create also tmpPosting files
        posting.margeToMainPostingFile();
    }



}


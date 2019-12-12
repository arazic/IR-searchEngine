
public class EngineManager {

    private String corpusPath;
    private String postingPath;
    private boolean stemming;
    private Parse parse;
    private Posting posting;
    private ReadFile readFile;

    public static void main(String[] args)
    {

        long millis=System.currentTimeMillis();
        java.util.Date date=new java.util.Date(millis);
        System.out.println("start "+ date);

        EngineManager engineManager= new EngineManager("C:/Users/gal/Desktop/FB396001/corpus", "C:/Users/gal/Desktop/FB396001",false);
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
        this.readFile= new ReadFile("C:/Users/gal/Desktop/FB396018", parse);
    }

    private void startEngine() {
        Indexer.initIndexer();
        readFile.createDocuments();

    }

}


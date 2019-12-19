package Model;//package PACKAGE_NAME;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Observable;
import java.util.TreeMap;

public class Model extends Observable
{
    EngineManager engineManager;
    TreeMap<String, String> terms;
    public void startEngine(String corpusPath,String postingPath,boolean isSteeming){
        System.out.println("We are Google!");
        long millis=System.currentTimeMillis();
        java.util.Date date=new java.util.Date(millis);
        System.out.println("start "+ date);
        this.engineManager= new EngineManager(corpusPath, postingPath,isSteeming);
        if(!engineManager.existCorpusPath()){
            engineManager.setCorpusPath(corpusPath);
        }
        engineManager.startEngine();
        System.out.println("chen is my queen! time" );

        long millis2=System.currentTimeMillis();
        java.util.Date date2=new java.util.Date(millis2);
        System.out.println("end "+ date2);
    }

    public boolean loadDictionary(String postingPath, boolean isStemming) {

        if(engineManager==null){
            this.engineManager= new EngineManager(postingPath,isStemming);
            return engineManager.setIndexer();
        }
        else {
            engineManager.setIsStemming(isStemming);
            return engineManager.setIndexer();
        }
    }

    public void showDictionary() {

          this.terms=engineManager.getTermsDic();
          setChanged();
          notifyObservers("showDictionary");
    }


    public TreeMap<String, String> getTerms() {
        return terms;
    }

    public void reset() {
        if(engineManager!=null){
             engineManager.reset();
            this.engineManager= null;
        }

    }
}

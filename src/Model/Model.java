package Model;//package PACKAGE_NAME;

import java.util.Observable;

public class Model extends Observable
{
    EngineManager engineManager;
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

    public void loadDictionary(String postingPath, boolean isSteeming) {

        System.out.println(postingPath);
        if(engineManager==null){
            this.engineManager= new EngineManager(postingPath,isSteeming);
            engineManager.setIndexer();
        }
        else {
            engineManager.setIndexer();
        }
    }
}

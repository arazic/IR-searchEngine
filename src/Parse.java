import com.sun.deploy.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;

public class Parse {

    private String currText;
    private HashSet<String>  months;
    private HashSet<String> stopWords;
    private HashSet<Character>  delimeters;
    private String []tokens;
    private HashMap<String, Integer> terms;


    public Parse() {
    }

    public void parse(Document currDoc){

        currText= currDoc.getContent().toString();
        String [] info; //info[0]= place, info[1]= phrase
        currText= currText.replaceAll("\n"," ");
        if(currText==null || currText.isEmpty())
            return;
       tokens= StringUtils.splitString(currText," :?[]");
       for(int i=0; i<tokens.length; i++){

           if(tokens[i].equals("<DOC>"))
               continue;

           if(tokens[i].equals("<DOCNO>")){
               info= extractOneWordExpression(i, "</DOCNO>");
               currDoc.setDocNo(info[1]);
               i=Integer.valueOf(info[0]);
               continue;
           }

           if(tokens[i].equals("<HT>")){
               info= extractOneWordExpression(i, "</HT>");
               currDoc.setHt(info[1]);
               i=Integer.valueOf(info[0]);
               continue;
           }

            if(tokens[i].equals("<HEADER>")){
               while(!tokens[i].equals("</HEADER>"))
                   i++;


             while (!tokens[i].equals("<TEXT>"))
                i++;
             i++;

             String currentToken= tokens[i];
             String currTerm= becomeTerm(currentToken);
             terms.put(currTerm, terms.get(currTerm)+1);

            }


       }

    }

    private String becomeTerm(String token) {
        String pureToken= clearTheToken(token);
        String type= getType(pureToken);
        String term="";

        switch(type) {
            case "word":
                // code block
                break;
            case "number":
                // code block
                break;
            case "date":
                // code block
                break;
            case "nameOrEntity":
                // code block
                break;
            case "stopWord":
                // code block
                break;
            default:
                // code block
        }
    return term;
    }

    private String getType(String pureToken) {
        return "";
    }


    private String clearTheToken(String token) {
        String pureToken= token.replaceAll(":","");
        pureToken= token.replaceAll(";","");

        return pureToken;
    }

    private String[] extractOneWordExpression(int place, String kind) {
        String []info=new String[2];
        place++;
        String phrase="";
        while(!tokens[place].equals(kind)) {
            phrase = phrase + tokens[place];
            place++;}
        info[0]= Integer.toString(place);
        info[1]=phrase;
        return info;
    }


}

import com.sun.deploy.util.StringUtils;
import org.omg.PortableInterceptor.INACTIVE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parse {

    private String currText;
    private String currTerm;
    private HashSet<String>  months;
    private HashSet<String> stopWords;
    private HashSet<Character>  delimeters;
    private String []tokens;
    private HashMap<String, Integer> terms;
    private HashMap<String, Integer>transperToFormat;
    private String [] info; //info[0]= place, info[1]= phrase
    private Document currDoc;
    private String newTerm;
    private int place;

    private Pattern priceLength4= Pattern.compile("(million|billion|trillion)"+ " "+"U.S."+ " "+"dollars");
    private Pattern intPattern= Pattern.compile("^[0-9]*$");
    private Pattern digitPattern= Pattern.compile("\\d");
    private Pattern doublePattern= Pattern.compile("^[0-9]*$"+"."+"^[0-9]*$");
    private Pattern fractionPattern= Pattern.compile("^[0-9]*$"+" "+"^[0-9]*$"+"/"+"^[0-9]*$");

    public Parse() {
        info= new String[2];
        transperToFormat= new HashMap<>();
        terms= new HashMap<>();
        transperToFormat.put("million",1);
        transperToFormat.put("billion",1000);
        transperToFormat.put("trillion",1000000);

    }

    public void parse(Document currDoc){
        this.currDoc= currDoc;
        currText= currDoc.getContent().toString();
        currText= currText.replaceAll("\n"," ");
        if(currText==null || currText.isEmpty())
            return;
       tokens= StringUtils.splitString(currText," ():?[]");
       place= parseBeforeText();
       for(int i=place; i<tokens.length; i++){

           if (tokens[i].equals("</TEXT>"))
               break;

           becomeTerm(i);
           i = Integer.valueOf(info[0]); //the last i that did not handle yet
           currTerm = info[1]; // the prepared term!
           System.out.println(currTerm);
           if(terms.containsKey(currTerm))
               terms.put(currTerm, terms.get(currTerm) + 1);
           else
               terms.put(currTerm, 1);
            }


       }




    private void  becomeTerm(int place) {

        newTerm="";

            if(matchToFour(place)){
                return;
            }

            else if(matchToThree()){
                return;
            }

            else if(matchToTwo()){
                return;
            }

            else if(oneTerm()){
                info[0]= Integer.toString(place+1);
                  return;
            }
    }

    private boolean oneTerm() {
        return true;
    }

    private boolean matchToTwo() {
        return false;
    }

    private boolean matchToThree() {
        return false;
    }

    private boolean matchToFour(int i) {
        while (i + 3 <= tokens.length) { // can check terms in size 4
            String firstToken = tokens[i];
            String secondToken = tokens[i + 1];
            String thirdToken = tokens[i + 2];
            String fourthToken = tokens[i + 3];

            Matcher matcherPrice4 = priceLength4.matcher(" " + secondToken.toLowerCase() + " " + thirdToken + " " + fourthToken);
            if (isNum(firstToken) && matcherPrice4.find()) {
                int num = Integer.valueOf(firstToken) * transperToFormat.get(secondToken);
                newTerm = Integer.toString(num) + " M Dollars";
                info[0] = Integer.toString(i + 3);
                info[1] = newTerm;
                return true;
            }

            if (firstToken.toLowerCase().equals("between") && isNum(secondToken)
                    && (thirdToken.toLowerCase().equals("and") || thirdToken.toLowerCase().equals("to")) && isNum(fourthToken)) {
                newTerm = firstToken + " " + secondToken + " " + thirdToken + " " + fourthToken;
                info[0] = Integer.toString(i + 3);
                info[1] = newTerm;
                return true;
            }
            return false;
        }
        return false;

    }

    private Boolean isNum(String val){
         return (intPattern.matcher(val).find()|| digitPattern.matcher(val).find()||
         doublePattern.matcher(val).find()|| fractionPattern.matcher(val).find());
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
    private int parseBeforeText() {
        int i = 0;
        for (; i < tokens.length; i++) {

            if (tokens[i].equals("<DOC>"))
                continue;

            if (tokens[i].equals("<DOCNO>")) {
                info = extractOneWordExpression(i, "</DOCNO>");
                currDoc.setDocNo(info[1]);
                i = Integer.valueOf(info[0]);
                continue;
            }

            if (tokens[i].equals("<HT>")) {
                info = extractOneWordExpression(i, "</HT>");
                currDoc.setHt(info[1]);
                i = Integer.valueOf(info[0]);
                continue;
            }

            if (tokens[i].equals("<HEADER>")) {
                while (!tokens[i].equals("</HEADER>"))
                    i++;
            }

            while (!tokens[i].equals("<TEXT>"))
                i++;
            i++;
            break;
        }
        return i;
    }


}

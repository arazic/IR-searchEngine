import com.sun.deploy.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parse {

    private static HashSet<String> stopWords = new HashSet<>();
    private static HashMap<String,String> monthMap = new HashMap<>();
    private static HashMap<String,String> unitMap = new HashMap<>();
    private static HashMap<String, String> symbols=new HashMap<>();

/*
    private HashMap<String, Integer>transperToFormat;
*/
    private HashMap<String,Integer> allDocTerms;
    private StringBuilder docLang;
    private StringBuilder articleType;
    private List<String> quotes;
    private HashMap<String,Integer> entity;
    private int maxFreqTermInDoc;

    private String[] tokens;
    private String text;
    private Document currDoc;
    private Posting posting;

    private String header;
    private int currIndex;
    private boolean debug1;
    private boolean debug2;

    //private Pattern priceLength4= Pattern.compile("(million|billion|trillion)"+ " "+"U.S."+ " "+"dollars");
    private Pattern intPattern= Pattern.compile("[0-9]");
    private Pattern digitPattern= Pattern.compile("\\d");
    private Pattern doublePattern= Pattern.compile("[0-9]*"+"."+"[0-9]*");
    private Pattern fractionPattern= Pattern.compile("[0-9]*"+"/"+"[0-9]*");


    public Parse(Posting posting)
    {
        debug1=false;
        debug2=false;
        allDocTerms = new HashMap<>();
        quotes = new LinkedList<>();
        entity=new HashMap<>();
        loadMonthMap();
        loadUnits();
        loadSymbols();
        this.posting=posting;

    }

    private void loadMonthMap()
    {
        monthMap.put("january","01");
        monthMap.put("jan","01");
        monthMap.put("february","02");
        monthMap.put("feb","02");
        monthMap.put("march","03");
        monthMap.put("mar","03");
        monthMap.put("april","04");
        monthMap.put("apr","04");
        monthMap.put("may","05");
        monthMap.put("june","06");
        monthMap.put("jun","06");
        monthMap.put("july","07");
        monthMap.put("jul","07");
        monthMap.put("august","08");
        monthMap.put("aug","08");
        monthMap.put("septmber","09");
        monthMap.put("sep","09");
        monthMap.put("october","10");
        monthMap.put("oct","10");
        monthMap.put("novmber","11");
        monthMap.put("nov","11");
        monthMap.put("december","12");
        monthMap.put("dec","12");
    }

    private void loadSymbols()
    {
        symbols.put("u.s","");
        symbols.put("u.s.","");
        symbols.put("us","");
        symbols.put("US","");
        symbols.put("dollars"," Dollars");
        symbols.put("dollar"," Dollars");
        symbols.put("%","%");
        symbols.put("percent","%");
        symbols.put("percentage","%");
        symbols.put("$"," Dollars");
    }

    private void loadUnits()
    {
        unitMap.put("million","M");
        unitMap.put("billion","B");
        unitMap.put("trillion","T");
        unitMap.put("m","M");
        unitMap.put("bn","B");
        unitMap.put("thousand","K");
    }




    public void createDocument(StringBuilder content)
    {
        currDoc = new Document();
        text = content.toString();
        text = text.replaceAll("\n"," ");
        getDocName();
        getHeader();
        getText();
        // find qoutes in text
        findQuotes();
        // split text by delimters
        tokens= StringUtils.splitString(text," ():?[]!; "); // to add ""
        parseText();
        updateDoc();
    }

    private void updateDoc() {

        int maxFrequency=0;
        for (HashMap.Entry<String,Integer> entry : allDocTerms.entrySet())
        {
            if(entry.getValue()>maxFrequency)
            {
                maxFrequency=entry.getValue();
            }
        }

        //currDoc.addTermsToDoc(allDocTerms);
        currDoc.setDocLang(docLang);
        currDoc.setArticleType(articleType);
        currDoc.setMaxTerm(maxFrequency);
        currDoc.setUniqeTermsNum(allDocTerms.size());

        if (debug1)
            System.out.println("///////////////// finish to "+currDoc.getDocName() +" maxTerm:"+currDoc.getMaxTerm()+" unique:"+currDoc.getUniqeTermsNum());

        //posting.postingDoc(currDoc);
        posting.postingTerms(allDocTerms, currDoc.getDocName());


        maxFreqTermInDoc=0;
        allDocTerms.clear();
    }



    private void findQuotes()
    {
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(text);
        while (m.find())
        {
            quotes.add(m.group(1));
        }
    }


    private void parseText() {
        currIndex=0;
        handleLanguage();
        handleArticleType();

        while (currIndex < tokens.length) {
            String concat = "";
            boolean fractionFlag = false;

            if (stopWords.contains(tokens[currIndex].toLowerCase())) {
                if (tokens[currIndex].toLowerCase().equals("between")) {
                    concat = concat + "between";
                    currIndex++;
                    if (currIndex + 3 < tokens.length &&  // there more 3 tokens in the array
                            isPureNum(tokens[currIndex]) &&
                            ((tokens[currIndex + 1].equals("to") || (tokens[currIndex + 1].equals("and"))) &&
                                    isPureNum(tokens[currIndex + 2]))) {
                        concat = tokens[currIndex] + "-" + tokens[currIndex + 2];
                        currIndex = currIndex + 3;
                    }
                } else
                    currIndex++;

            // containsNumber "10-part"/ "10bn" / "US$12.88"
            } else if (containsNumber(tokens[currIndex])) {
                if (isPureNum(tokens[currIndex])) {
                    concat += tokens[currIndex];
                    currIndex++;

                    //fractionPattern- "22 2/3"
                    if ((currIndex < tokens.length) && fractionPattern.matcher(tokens[currIndex]).find()) {
                        fractionFlag = true;
                        concat += " " + tokens[currIndex];
                        currIndex++;
                        if ((currIndex < tokens.length) && symbols.containsKey(tokens[currIndex].toLowerCase())) { //dollar, precent, %
                            concat = concat + symbols.get(tokens[currIndex].toLowerCase());
                            currIndex++;
                            if ((currIndex < tokens.length) && symbols.containsKey(tokens[currIndex].toLowerCase())) { //dollar, precent, %
                                concat = concat + symbols.get(tokens[currIndex].toLowerCase());
                                currIndex++;
                            }
                        }
                    }


                    String unitToken = "";
                    if ((currIndex < tokens.length) && unitMap.containsKey(tokens[currIndex].toLowerCase())) {
                        unitToken = tokens[currIndex];
                        currIndex++;
                    }

                    if ((currIndex < tokens.length) && symbols.containsKey(tokens[currIndex].toLowerCase()))
                        // if true is a price- there is "dollars"
                    {
                        if ((currIndex<tokens.length)&&symbols.get(tokens[currIndex]) == "%") {
                            concat = concat + "%";
                            currIndex++;
                        } else {
                            currIndex++;
                            if ((currIndex<tokens.length)&&symbols.containsKey(tokens[currIndex].toLowerCase())) {
                                currIndex++;
                            }
                            if (unitToken != "") {
                                concat = handlePriceNumbersAndUnits(concat, unitToken);
                            } else {
                                concat = handlePriceNumbers(concat);
                            }
                        }

                    } else if ((currIndex < tokens.length) && (!fractionFlag) && monthMap.containsKey(tokens[currIndex].toLowerCase())) {
                        concat = monthHandler(concat, monthMap.get(tokens[currIndex].toLowerCase()));

                    } else if (fractionFlag == false){ // its just a number
                        if (unitToken != "") {
                            concat = handleSimpleNumbersAndUnits(concat, unitToken);
                        } else if (fractionFlag == false) {
                            concat = handleSimpleNumbers(concat);
                        }
                    }
                } else // its not a pure number
                {
                    concat = tokens[currIndex];
                    currIndex++;
                    // if contains $
                    if (concat.contains("$")) {
                        if (concat.charAt(0) == '$') {
                            concat = concat.substring(1);

                        } else if (concat.charAt(concat.length() - 1) == '$') {
                            concat = concat.substring(0, concat.length() - 1);
                        }
                        else {
                            if(concat.contains("US$")) {
                                concat = concat.substring(3);
                            }
                            else
                                continue;
                        }
                        if ((currIndex<tokens.length)&&unitMap.containsKey(tokens[currIndex].toLowerCase())) {
                            concat = handlePriceNumbersAndUnits(concat, tokens[currIndex]);
                            currIndex++;
                        } else if (fractionFlag == false ) {
                            concat = handlePriceNumbers(concat);
                        }
                    }
                    // if contains bn/m
                    else if (concat.contains("m") || concat.contains("bn")) {
                        if ((currIndex<tokens.length)&&symbols.containsKey(tokens[currIndex].toLowerCase())) {
                            currIndex++;
                            if ((currIndex<tokens.length)&& symbols.containsKey(tokens[currIndex].toLowerCase())) {
                                currIndex++;
                            }
                            if (concat.contains("m")) {
                                String num = concat.substring(0, concat.length() - 1);
                                concat = handlePriceNumbersAndUnits(num, "million");
                            } else {
                                String num = concat.substring(0, concat.length() - 2);
                                concat = handlePriceNumbersAndUnits(num, "billion");
                            }
                        } else {
                            if (concat.contains("m")) {
                                String num = concat.substring(0, concat.length() - 1);
                                concat = handleSimpleNumbersAndUnits(num, "million");
                            } else {
                                String num = concat.substring(0, concat.length() - 2);
                                concat = handleSimpleNumbersAndUnits(num, "billion");
                            }
                        }
                    } else if (concat.contains("-")) {

                    }
                    // if contains %
                }
            } else if ((currIndex<tokens.length)&&monthMap.containsKey(tokens[currIndex].toLowerCase())) {
                concat = monthMap.get(tokens[currIndex].toLowerCase());
                currIndex++;
                if ((currIndex<tokens.length) &&isMonthNumber(tokens[currIndex]) != -1) {
                    concat = monthHandler(tokens[currIndex], concat);
                    currIndex++;
                }
            } else if ((currIndex<tokens.length)&& tokens[currIndex].charAt(0) >= 'A' && tokens[currIndex].charAt(0) <= 'Z')// is a entity
            {
                concat = entityHandler();
            } else {
                // to think what happens is concat is junk. like "--"
                concat = tokens[currIndex];
                currIndex++;
            }

            if (!concat.isEmpty()) {
                concat = cleanWord(concat);

                if(debug2)
                    System.out.println(concat);


                if (allDocTerms.containsKey(concat)) {
                    allDocTerms.replace(concat, allDocTerms.get(concat) + 1);
                    if(maxFreqTermInDoc<allDocTerms.get(concat) + 1)
                        maxFreqTermInDoc=allDocTerms.get(concat) + 1;
                }
                else
                    allDocTerms.put(concat, 1);
            }
        }
    }

    private void handleArticleType() {
        articleType= new StringBuilder();
        if (currIndex < tokens.length) {
            if(tokens[currIndex].toLowerCase().equals("article")){
                currIndex++;
                if(tokens[currIndex].toLowerCase().contains("type")){
                    currIndex++;
                   articleType.append(tokens[currIndex]);
                   currIndex++;
                }
            }
        }
    }

    private void handleLanguage() {
        docLang= new StringBuilder();
        if (currIndex<tokens.length) {
            if(tokens[currIndex].toLowerCase().equals("language")){
                currIndex++;
                if(tokens[currIndex].equals("<F")&&tokens[currIndex+1].equals("P=105>")) {
                    docLang.append(tokens[currIndex+2]);
                    currIndex=currIndex + 4;
                }
            }
        }
    }


    private String handlePriceNumbers(String sNum)
    {
        String ans=handleSimpleNumbers(sNum);
        if(ans.charAt(ans.length()-1)=='K')
        {
            return sNum+" Dollars";
        }
        if(ans.charAt(ans.length()-1)=='B')
        {
            return handlePriceNumbersAndUnits(sNum.substring(0,sNum.length()-2),"billion");
        }
        if(ans.charAt(ans.length()-1)=='T')
        {
            return handlePriceNumbersAndUnits(sNum.substring(0,sNum.length()-2),"trillion");
        }
        if(ans.charAt(ans.length()-1)=='M')
        {
            return ans.substring(0,ans.length()-1)+" M"+ " Dollars";
        }
        return sNum+ " Dollars";
    }

    private String handleSimpleNumbersAndUnits(String sNum,String unit)
    {
        String ans=sNum+unitMap.get(unit.toLowerCase());
        return  ans;
    }

    private String handlePriceNumbersAndUnits(String sNumb,String unit)
    {
        String ans="";
        String sNum=cleanPureNumber(sNumb);
        if(unitMap.get(unit)=="B" || unitMap.get(unit)=="T" )
        {
            double number;
            try {
                number = Double.parseDouble(sNum);
            }
            catch (Exception E){
                return sNum;
            }
            if(unitMap.get(unit)=="B")
            {
                number=number*1000;
            }
            else if(unitMap.get(unit)=="T")
            {
                number=number*1000000;
            }
            int check=(int)number;
            if(check==number)
            {
                ans=String.valueOf(check)+" M"+" Dollars";
                return ans;
            }
            ans=String.valueOf(number)+" M"+" Dollars";
            return ans;
        }
        ans=sNum+" M"+" Dollars";
        return ans;
    }


    private String handleSimpleNumbers(String sNumb)
    {
       String sNum= cleanPureNumber(sNumb);
       double number=0;
        try {
            number=Double.parseDouble(sNum);
       }
       catch (Exception  e){
           return sNum;
       }
       String unit="";
       if(number<9999)
       {
          String ans = String.valueOf(number);
          return ans;
       }
       else if(number<1000000)
       {
           number=number/1000;
           unit="K";
       }
       else if(number<1000000000)
       {
           number=number/1000000;
           unit="M";
       }
       else if((number/1000)<1000000000)
       {
           number=number/1000000000;
           unit="B";
       }
       else
       {
           unit="T";
       }
       String sNumber=String.valueOf(number);
       int position=sNumber.indexOf('.');
       int diff=sNumber.length()-position;
       if(diff>=3&&(position+4<=diff))
       {
           String ans = sNumber.substring(0,position+4)+unit;
           return ans;
       }
       else
       {
           String ans = sNumber.substring(0,position)+unit;
           return ans;
       }

    }


    private boolean containsNumber(String word)
    {
        for(int i=0; i<10; i++)
        {
            String s=String.valueOf(i);
            if(word.contains(s))
            {
                return true;
            }
        }
        return false;
    }


    private Boolean isPureNum(String val){
       int countSlech=0;
       int countPoints=0;
       for (int i=0; i<val.length();i++)
       {
           if((val.charAt(i)<='9' && val.charAt(i)>='0') || val.charAt(i)==','||val.charAt(i)=='.'||val.charAt(i)=='/')
           {
               if(val.charAt(i)=='/')
                   countSlech++;
               if(val.charAt(i)=='.')
                   countPoints++;
           }
           else
                return false;
       }
       if(countSlech<=1 || countPoints<=1)
            return true;
       return false;
    }

    private String cleanPureNumber(String number)
    {
        if(number.contains(","))
        {
            number=number.replaceAll(",","");
        }
        return number;
    }

    private String entityHandler()
    {
        String concat="";
        concat=tokens[currIndex++];
        if(concat.equals("Text"))
            return "";
        while((currIndex<tokens.length)&& (tokens[currIndex].charAt(0)>='A' && tokens[currIndex].charAt(0)<='Z' &&
                (!monthMap.containsKey(tokens[currIndex].toLowerCase()))))
        {
            concat=concat+" "+tokens[currIndex++];
        }
        concat=cleanWord(concat);
        if(entity.containsKey(concat))
        {
            entity.replace(concat,entity.get(concat)+1);
        }
        else
        {
            entity.put(concat,1);
        }
        return concat;
    }
    private String monthHandler(String number, String month)
    {
        String ans="";
        if(number.length()<=2)
        {
            if(number.length()==2)
            {
                ans=month+"-"+number;
                return ans;
            }

            ans=month+"-0"+number;
            return ans;
        }
        ans=number+"-"+month;
        return ans;
    }


    private int isMonthNumber(String word)
    {
       word=cleanWord(word);
       try {
           int number=Integer.parseInt(word);
           if(number<=31 && number>0)
           {
               return 0;
           }
           return 1;
       }
       catch (Exception e)
       {
           return -1;
       }
    }



    public String cleanWord(String word)
    {
        if(word.charAt(0)=='.'|| word.charAt(0)==','|| word.charAt(0)=='-')
        {
            word=word.substring(1);
        }
        if(word.isEmpty()==false &&(word.charAt(word.length()-1)=='.' || word.charAt(word.length()-1)==','||
                word.charAt(word.length()-1)=='-'))
        {
            word=word.substring(0,word.length()-1);
        }
        return word;
    }


    public void createStopWords( List<String> stringList) // create stop words hash
    {
        for (String word:stringList)
        {
            stopWords.add(word);
        }
    }



    private void getText()
    {
        Pattern p = Pattern.compile("<TEXT>(.*)</TEXT>");
        Matcher m = p.matcher(text);
        while (m.find()) {
            text =m.group(1);
        }
    }

    private void getHeader()
    {
        Pattern p = Pattern.compile("<TI>(.*)</TI>");
        Matcher m = p.matcher(text);
        while (m.find()) {
            header =m.group(1);
        }
        //parse header like text and insert him to header doc

    }

    private void getDocName()
    {
        Pattern p = Pattern.compile("<DOCNO>(.*)</DOCNO>");
        Matcher m = p.matcher(text);
        String docName="";
        while (m.find()) {
            docName =m.group(1);
        }
        currDoc.setDocName(docName);
    }

}

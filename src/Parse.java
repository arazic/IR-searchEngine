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
    private HashMap<String,Integer> entity;
    private HashMap<String,Integer> termsWithCapitalLetters;
    private StringBuilder docLang;
    private StringBuilder articleType;
    private List<String> quotes;
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
    private Pattern fractionPattern= Pattern.compile("[0-9]"+"/"+"[0-9]");


    public Parse(Posting posting)
    {
        debug1=true;
        debug2=true;
        allDocTerms = new HashMap<>();
        quotes = new LinkedList<>();
        entity=new HashMap<>();
        termsWithCapitalLetters=new HashMap<>();
        loadMonthMap();
        loadUnits();
        loadSymbols();
        this.posting=posting;

    }


    private void cleanParser()
    {
        allDocTerms.clear();
        quotes.clear();
        entity.clear();
        termsWithCapitalLetters.clear();
        currIndex=0;
    }

    private void loadMonthMap()
    {
        monthMap.put("January","01");
        monthMap.put("Jan","01");
        monthMap.put("February","02");
        monthMap.put("Feb","02");
        monthMap.put("March","03");
        monthMap.put("Mar","03");
        monthMap.put("April","04");
        monthMap.put("Apr","04");
        monthMap.put("May","05");
        monthMap.put("June","06");
        monthMap.put("Jun","06");
        monthMap.put("July","07");
        monthMap.put("Jul","07");
        monthMap.put("August","08");
        monthMap.put("Aug","08");
        monthMap.put("Septmber","09");
        monthMap.put("Sep","09");
        monthMap.put("October","10");
        monthMap.put("Oct","10");
        monthMap.put("November","11");
        monthMap.put("Nov","11");
        monthMap.put("December","12");
        monthMap.put("Dec","12");
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
        unitMap.put("thousands","K");
    }




    public void createDocument(StringBuilder content)
    {
        currIndex=0;
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
        handleLanguage();
        handleArticleType();
        if(tokens.length==0)
        {
            cleanParser();
            return;
        }
        parseText();

        handleCapitalTerms();
        cleanParser();
        //entityHandle();
        //updateDoc();
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

        posting.postingDoc(currDoc);
     //   posting.postingTerms(allDocTerms, currDoc.getDocName());


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


    private void parseText()
    {
        if(tokens.length==0)
        {
            return;
        }
        if(tokens[currIndex].equals("Text"))
            currIndex++;
        while (currIndex < tokens.length)
        {
            boolean cunterFlag=false;
            if(currIndex==tokens.length-2)
            {
                int x=0;
            }
            String concat = cleanWord(tokens[currIndex]);
            if(concat.isEmpty()||concat.contains("<")||concat.contains(">"))
            {
                currIndex++;
                continue;
            }
            boolean fractionFlag = false;
            if (stopWords.contains(concat))
            {
                if (concat.toLowerCase().equals("between"))
                {
                    concat ="between";
                    currIndex++;
                    if (currIndex + 3 < tokens.length &&  // there more 3 tokens in the array
                            isPureNum(tokens[currIndex]) &&
                            ((tokens[currIndex + 1].equals("to") || (tokens[currIndex + 1].equals("and"))) &&
                                    isPureNum(tokens[currIndex + 2]))) {
                        concat = tokens[currIndex] + "-" + tokens[currIndex + 2];
                        currIndex = currIndex + 3;
                    }
                }
                else
                {
                    currIndex++;
                    continue;
                }
            }
            else if (containsNumber(concat)) // handle numbers
            {
                // handle pure numbers
                if (isPureNum(concat))
                {
                    currIndex++;
                    //fractionPattern- "22 2/3"
                    if(currIndex<tokens.length)
                    {
                        if (fractionPattern.matcher(tokens[currIndex]).find())
                        {
                            fractionFlag = true;
                            concat += " " + tokens[currIndex];
                            currIndex++;
                            if(currIndex<tokens.length)
                            {
                                String temp=cleanWord(tokens[currIndex]).toLowerCase();
                                if (symbols.containsKey(temp)) //dollar, precent, %
                                {
                                    concat = concat + symbols.get(temp);
                                    currIndex++;
                                    if(currIndex<tokens.length)
                                    {
                                        temp=cleanWord(tokens[currIndex]).toLowerCase();
                                        if( symbols.containsKey(temp)) //dollar, precent, %
                                        {
                                            concat = concat + symbols.get(temp);
                                            currIndex++;
                                        }
                                    }
                                }
                            }
                            else
                            {
                                currIndex--;
                                cunterFlag=true;
                            }
                        }
                        String unitToken = cleanWord(tokens[currIndex]).toLowerCase();
                        if (unitMap.containsKey(unitToken))
                        {
                            currIndex++;
                        }
                        else
                        {
                            unitToken="";
                        }
                        boolean flag=false;
                        if(currIndex<tokens.length)
                        {
                            String symbol=cleanWord(tokens[currIndex].toLowerCase());
                            if ( symbols.containsKey(symbol))// if true is a price- there is "dollars"F
                            {
                                flag=true;
                                if (symbols.get(symbol) == "%")
                                {
                                    concat = concat + "%";
                                    currIndex++;
                                }
                                else
                                {
                                    if (unitToken != "")
                                    {
                                        concat = handlePriceNumbersAndUnits(concat, unitToken);
                                    }
                                    else
                                    {
                                        concat = handlePriceNumbers(concat);
                                    }
                                    currIndex++;
                                    if(currIndex<tokens.length)
                                    {
                                        String symbol2=cleanWord(tokens[currIndex]).toLowerCase();
                                        if (symbols.containsKey(symbol2))
                                        {
                                            currIndex++;
                                        }
                                    }
                                }
                            }
                            else if ( (!fractionFlag) && monthMap.containsKey(symbol))
                            {
                                concat = monthHandler(concat, monthMap.get(symbol));
                            }
                            if (!fractionFlag && !flag)// its just a number
                            {
                                if (unitToken != "")
                                {
                                    concat = handleSimpleNumbersAndUnits(concat, unitToken);
                                }
                                else if (fractionFlag == false)
                                {
                                    concat = handleSimpleNumbers(concat);
                                }
                            }
                        }
                        else
                        {
                            concat = handleSimpleNumbersAndUnits(concat,unitToken);
                        }
                    }
                    else
                    {
                        concat = handleSimpleNumbers(concat);
                    }
                }
                // finish handle pure numbers

                else // its not a pure number
                {
                    if(concat.contains("-"))
                    {
                        if(concat.charAt(0)!='-') // its not negative number
                        {
                            String[] words = concat.split("-");
                            concat=words[0];
                            int i;
                            for (i=1; i<words.length;i++)
                            {
                                String word=cleanWord(words[i]);
                                if (allDocTerms.containsKey(word))
                                {
                                    allDocTerms.replace(word, allDocTerms.get(word) + 1);
                                    if(maxFreqTermInDoc<allDocTerms.get(word) + 1)
                                        maxFreqTermInDoc=allDocTerms.get(word) + 1;
                                }
                                else
                                    allDocTerms.put(word, 1);
                                concat=concat+"-"+words[i];
                            }
                        }
                        currIndex++;
                    }
                    else if (concat.contains("$"))
                    {
                        if (concat.charAt(0) == '$')
                        {
                            concat = concat.substring(1);

                        }
                        else if (concat.charAt(concat.length() - 1) == '$')
                        {
                            concat = concat.substring(0, concat.length() - 1);
                        }
                        else
                        {
                            if(concat.contains("US$"))
                            {
                                concat = concat.substring(3);
                            }
                            else if(concat.contains("U.S.$"))
                            {
                                concat = concat.substring(5);
                            }
                            else
                            {
                                //System.out.println(concat+" maybe we have a problem");
                                currIndex++;
                                continue;
                            }
                        }
                        currIndex++;
                        if(currIndex<tokens.length)
                        {
                            String temp=cleanWord(tokens[currIndex]).toLowerCase();
                            if (unitMap.containsKey(temp))
                            {
                                concat = handlePriceNumbersAndUnits(concat, temp);
                                currIndex++;
                            } else if (fractionFlag == false )
                            {
                                concat = handlePriceNumbers(concat);
                            }
                        }
                        else
                        {
                            concat = handlePriceNumbers(concat);
                        }
                    }
                    // if contains bn/m
                    else if (concat.contains("m") || concat.contains("bn"))
                    {
                        currIndex++;
                        if(currIndex<tokens.length)
                        {
                            String temp=cleanWord(tokens[currIndex]).toLowerCase();
                            if (symbols.containsKey(temp))
                            {
                                currIndex++;
                                if(currIndex<tokens.length)
                                {
                                    if (symbols.containsKey(cleanWord(tokens[currIndex]).toLowerCase())) {
                                        currIndex++;
                                    }
                                }
                                if (concat.contains("m"))
                                {
                                    String num = concat.substring(0, concat.length() - 1);
                                    concat = handlePriceNumbersAndUnits(num, "million");
                                }
                                else
                                {
                                    String num = concat.substring(0, concat.length() - 2);
                                    concat = handlePriceNumbersAndUnits(num, "billion");
                                }
                            }
                            else
                            {
                                if (concat.contains("m")) {
                                    String num = concat.substring(0, concat.length() - 1);
                                    concat = handleSimpleNumbersAndUnits(num, "million");
                                } else {
                                    String num = concat.substring(0, concat.length() - 2);
                                    concat = handleSimpleNumbersAndUnits(num, "billion");
                                }
                            }
                        }
                        else
                        {
                            if (concat.contains("m"))
                            {
                                String num = concat.substring(0, concat.length() - 1);
                                concat = handleSimpleNumbersAndUnits(num, "million");
                            } else {
                                String num = concat.substring(0, concat.length() - 2);
                                concat = handleSimpleNumbersAndUnits(num, "billion");
                            }
                        }
                    }
                    else
                    {
                        currIndex++;
                    }
                }
            }
            //finish handle numbers

            // check if its month

            else if (monthMap.containsKey(concat))
            {
                if((currIndex+1<tokens.length)&&isMonthNumber(tokens[currIndex+1]) != -1)
                {
                    concat = monthMap.get(concat);
                    currIndex++;
                    String temp=cleanWord(tokens[currIndex]);
                    concat = monthHandler(temp, concat);
                }
                else
                {
                    if(termsWithCapitalLetters.containsKey(concat))
                    {
                        termsWithCapitalLetters.put(concat,termsWithCapitalLetters.get(concat)+1);
                    }
                    else
                    {
                        termsWithCapitalLetters.put(concat,1);
                    }
                    currIndex++;
                    continue;
                }
                currIndex++;
            }
            //finish handle month

            else if (concat.charAt(0) >= 'A' && concat.charAt(0) <= 'Z')// is a entity or termWithCapitalLetter
            {
                if(stopWords.contains(concat.toLowerCase()))
                {
                    currIndex++;
                    continue;
                }
                if(currIndex+1<tokens.length)
                {
                    if(tokens[currIndex+1].charAt(0)>='A' && tokens[currIndex+1].charAt(0)<='Z' &&!( tokens[currIndex].contains(",")||tokens[currIndex].contains(".")))
                    {
                        currIndex++;
                        entityHandler(concat);
                        continue;
                    }
                }
                if(termsWithCapitalLetters.containsKey(concat))
                {
                    termsWithCapitalLetters.replace(concat,termsWithCapitalLetters.get(concat)+1);
                }
                else
                {
                    termsWithCapitalLetters.put(concat,1);
                }
                currIndex++;
                continue;
            }
            // finish handle entities
            // handle words contains -
            else if(concat.contains("-"))
            {
                int j=0;
                while (j<concat.length())
                {
                    if(concat.charAt(j)=='-')
                    {
                        j++;
                    }
                    else
                    {
                        break;
                    }
                }
                concat=concat.substring(j);
                if(concat.contains("-"))
                {
                    String[] words = tokens[currIndex].split("-");
                    if(words.length==0)
                    {
                        currIndex++;
                        continue;
                    }
                    concat=words[0];
                    int i;
                    for (i=1; i<words.length;i++)
                    {
                        if(words[i].isEmpty())
                        {
                            continue;
                        }
                        String word=cleanWord(words[i]);
                        if (allDocTerms.containsKey(word))
                        {
                            allDocTerms.replace(word, allDocTerms.get(word) + 1);
                            if(maxFreqTermInDoc<allDocTerms.get(word) + 1)
                                maxFreqTermInDoc=allDocTerms.get(word) + 1;
                        }
                        else
                            allDocTerms.put(word, 1);
                        concat=concat+"-"+words[i];
                    }
                }
                else if(concat!="")
                {
                    if(stopWords.contains(concat))
                    {
                        currIndex++;
                        continue;
                    }
                }
                concat=cleanWord(concat);
                currIndex++;
            }
            else // its a regular word
            {
                currIndex++;
            }

            if (!concat.isEmpty())
            {
                if(cunterFlag)
                    currIndex++;
                if((concat.length()==1)&& (concat.contains("*")||concat.contains("|")||concat.contains("/")||concat.contains("=")||concat.contains("%")||concat.contains("+")))
                {
                    continue;
                }
                else if (allDocTerms.containsKey(concat))
                {
                    //System.out.println(concat);
                    allDocTerms.replace(concat, allDocTerms.get(concat) + 1);
                    if(maxFreqTermInDoc<allDocTerms.get(concat) + 1)
                        maxFreqTermInDoc=allDocTerms.get(concat) + 1;
                }
                else
                {
                    //System.out.println(concat);
                    allDocTerms.put(concat, 1);
                }

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

    private void entityHandle()
    {
        for (String entityTerm:entity.keySet()
             ) {
            String []tokens=entityTerm.split(" ");
            String entityString="";
            for(int i=0; i<tokens.length;i++)
            {
                String lowerCase=tokens[i].toLowerCase();
                if(allDocTerms.containsKey(lowerCase))
                {
                    allDocTerms.replace(lowerCase,allDocTerms.get(lowerCase)+entity.get(tokens[i]));
                }
                else
                {
                    entityString+=tokens[i]+" ";
                }
            }
            String temp=entityString.substring(0,entityString.length()-1);
            if(!temp.equals(entityTerm))
            {
                int num=entity.get(entityTerm);
                entity.remove(entityTerm);
                entity.put(entityString,num);
            }
        }
        // asks chen what to do maybe merge and where to store
    }

    private void handleCapitalTerms()
    {
        for (String term:termsWithCapitalLetters.keySet()
             ) {
            if(allDocTerms.containsKey(term.toLowerCase()))
            {
                allDocTerms.replace(term.toLowerCase(),allDocTerms.get(term.toLowerCase())+termsWithCapitalLetters.get(term));
               // termsWithCapitalLetters.remove(term);
            }
        }
        if(!termsWithCapitalLetters.isEmpty())
        {
            // asks chen how to save them correctly;
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
        String ans="";
       double number=0;
        try {
            number=Double.parseDouble(sNum);
       }
       catch (Exception  e){
           return sNum;
       }
       String unit="";
       if(number<1000)
       {
          ans=sNum;
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
       int position =sNumber.indexOf(".");
       if(sNumber.length()>=position+4&& sNumber.charAt(position+3)!='0')
       {
           ans=sNumber.substring(0,position+4);
       }
       else if(sNumber.length()>=position+3&& sNumber.charAt(position+2)!='0')
       {
           ans=sNumber.substring(0,position+3);
       }
       else if(sNumber.length()>=position+2 && sNumber.charAt(position+1)!='0')
       {
           ans=sNumber.substring(0,position+2);
       }
       else
           ans=sNumber.substring(0,position);
       return ans+unit;
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
           if((val.charAt(i)<='9' && val.charAt(i)>='0') || val.charAt(i)==','||val.charAt(i)=='.')
           {
               if(val.charAt(i)=='.')
                   countPoints++;
           }
           else
                return false;
       }
       if(countPoints<=1)
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

    private void entityHandler(String cleanToken)
    {
        String concat=cleanToken+" ";
        while((currIndex<tokens.length)&& (tokens[currIndex].charAt(0)>='A' && tokens[currIndex].charAt(0)<='Z' && (!monthMap.containsKey(tokens[currIndex]))))
        {
            String token=tokens[currIndex];
            if(token.charAt(token.length()-1)=='.' || token.charAt(token.length()-1)==',')
            {
                String clean=cleanWord(token);
                concat+=clean;
                currIndex++;
                break;
            }
            else
            {
                concat+=cleanWord(tokens[currIndex])+" ";
                currIndex++;
            }
        }
        concat=cleanWord(concat);
        if(entity.containsKey(concat))
        {
            entity.put(concat,entity.get(concat)+1);
        }
        else
        {
            entity.put(concat,1);
        }
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
        int j=0;
        for(int i=0;i<word.length();i++)
        {
            if(word.charAt(i)=='.'|| word.charAt(i)==',' || word.charAt(i)==' ' || word.charAt(i)=='"'|| word.charAt(i)=='\'')
            {
                j++;
            }
            else
            {
                break;
            }
        }
        word=word.substring(j);
        j=word.length();
        if(word.isEmpty())
        {
            return word;
        }
        for(int i=word.length()-1;i>0;i--)
        {
            if(word.charAt(i)=='.'|| word.charAt(i)==',' || word.charAt(i)==' ' || word.charAt(i)=='"'|| word.charAt(i)=='\'')
            {
                j--;
            }
            else
            {
                break;
            }
        }
        word=word.substring(0,j);
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

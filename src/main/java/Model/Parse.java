package Model;

//import com.sun.deploy.util.StringUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parse {

    private static HashSet<String> stopWords;
    private static HashMap<String,String> monthMap = new HashMap<>();
    private static HashMap<String,String> unitMap = new HashMap<>();
    private static HashMap<String, String> symbols=new HashMap<>();
    private HashMap<String,Integer> allDocTerms;
    private HashMap<String,Integer> entities;
    private boolean isStemming;
    private Stemmer stemmer;
    private int maxFreqTermInDoc;
    private int totalTermsInDoc;
    private String[] tokens;
    private String text;
    private Document currDoc;
    private Posting posting;
    private int currIndex;
    private boolean debug1;
    private boolean debug2;
    private boolean FinishDoc;


    Pattern containsNumber= Pattern.compile(".*[0-9].*");
    Pattern pureNumberPattern= Pattern.compile("((([0-9]*)"+"[.,])*)"+"([0-9]*)");
    private Pattern fractionPattern= Pattern.compile("[0-9]"+"/"+"[0-9]");


    public Parse(Posting posting, boolean isStemming)
    {
        debug1=false;
        debug2=false;
        stemmer= new Stemmer();
        this.isStemming =isStemming;
        allDocTerms = new HashMap<>();
        entities= new HashMap<>();
        maxFreqTermInDoc=1;
        loadMonthMap();
        loadUnits();
        loadSymbols();
        this.posting=posting;
        FinishDoc=false;
        totalTermsInDoc=0;
    }

    private void insertToAllDocTerms(String term)
    {
        if (term.isEmpty() || term.contains(">")||term.contains("<")) {
            return;
        }
        totalTermsInDoc++;
        if (term.charAt(0) > 'Z' || term.charAt(0) < 'A') // is lowerCase
        {
            if (allDocTerms.containsKey(term)) {
                int freq = allDocTerms.get(term) + 1;
                allDocTerms.replace(term, freq);
                if (freq > maxFreqTermInDoc)
                    maxFreqTermInDoc = freq;
                return;
            }
            String upper = term.toUpperCase();
            if (upper.charAt(0) >= 'A' && upper.charAt(0) <= 'Z') {
                if (allDocTerms.containsKey(upper)) {
                    int freq = allDocTerms.get(upper) + 1;
                    allDocTerms.remove(upper);
                    allDocTerms.put(term, freq);
                    if (freq > maxFreqTermInDoc)
                        maxFreqTermInDoc = freq;
                    return;
                }
            }
            allDocTerms.put(term, 1);
            return;
        }
        String lowerCase = term.toLowerCase();
        if (allDocTerms.containsKey(lowerCase)) {
            int freq = allDocTerms.get(lowerCase) + 1;
            allDocTerms.replace(lowerCase, freq);
            if (freq > maxFreqTermInDoc)
                maxFreqTermInDoc = freq;
            return;
        }
        String upperCase = term.toUpperCase();
        if (allDocTerms.containsKey(upperCase)) {
            int freq = allDocTerms.get(upperCase) + 1;
            allDocTerms.replace(upperCase, freq);
            if (freq > maxFreqTermInDoc)
                maxFreqTermInDoc = freq;
            return;
        }
        allDocTerms.put(upperCase, 1);
    }

    private void cleanParser()
    {
        allDocTerms.clear();
        currIndex=0;
    }

    private void loadMonthMap()
    {
        // regular case
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

        //lowerCase
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
        monthMap.put("november","11");
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
        unitMap.put("thousands","K");
    }


    public void createDocument(StringBuilder content)
    {
        currIndex=0;
        currDoc = new Document();
        text = content.toString();
        text = text.replaceAll("\n"," ");
        getDocName();
        getText();
        tokens= StringUtils.split(text," ():?[]!;*#+| "); // to add ""
        if(tokens.length==0)
        {
            cleanParser();
            return;
        }
        parseText();
        if(isStemming)
        {
            HashMap<String,Integer> termsAfterStemming= new HashMap<>();
            for (String beforeStem:allDocTerms.keySet()
                    ) {
                if(containsNumber.matcher(beforeStem).matches()|| beforeStem.contains(" "))
                {
                    termsAfterStemming.put(beforeStem,allDocTerms.get(beforeStem));
                }
                else
                {
                    String afterStem=stemmer.stem(beforeStem);
                    if(termsAfterStemming.containsKey(afterStem))
                    {
                        termsAfterStemming.replace(afterStem,termsAfterStemming.get(afterStem)+allDocTerms.get(beforeStem));
                    }
                    else
                    {
                        termsAfterStemming.put(afterStem,allDocTerms.get(beforeStem));
                    }
                }
            }
            allDocTerms=termsAfterStemming;
        }
        setTopEntities();
        updateDoc();
    }





    private void updateDoc() {
        currDoc.setTotalTerms(totalTermsInDoc);
        currDoc.setMaxTerm(maxFreqTermInDoc);
        currDoc.setUniqeTermsNum(allDocTerms.size());
        currDoc.setEntities(setTopEntities());
        posting.postingDoc(currDoc);
        posting.postingTerms(allDocTerms, currDoc.getDocName());
        maxFreqTermInDoc=0;
        allDocTerms.clear();
    }


    private String[]  setTopEntities()
    {
       String[] topEntities = new String[5];
       int counter=0;
       while (counter<5 || entities.isEmpty())
       {
           int maxFrequency=0;
           String entity="";
           for (Map.Entry entry:entities.entrySet()
                ) {
               if((int)entry.getValue()>maxFrequency)
               {
                   entity=(String)entry.getKey();
                   maxFrequency=(int)entry.getValue();
               }
           }
           topEntities[counter]=entity;
           counter++;
           entities.remove(entity);
       }
       return topEntities;
    }

    private void rankEntitis()
    {
        for (String entity:entities.keySet()
             ) {
            String[] splitName=entity.split(" ");
            int addFrequency=0;
            for (int i=0; i<splitName.length;i++)
            {
                if(allDocTerms.containsKey(splitName[i]))
                {
                    addFrequency+=allDocTerms.get(splitName[i]);
                }
            }
            entities.replace(entity,entities.get(entity)+addFrequency);
        }
    }



    private void parseText()
    {

        if(tokens[currIndex].equals("Text"))
            currIndex++;
        while (currIndex < tokens.length)
        {
            boolean cunterFlag=false;
            boolean fractionFlag = false;
            String concat = cleanWord(tokens[currIndex]);
            if(concat.isEmpty()||concat.contains("<")||concat.contains(">"))
            {
                currIndex++;
                continue;
            }
            if (stopWords.contains(concat))
            {
                if (concat.equals("between"))
                {
                    currIndex++;
                    if (currIndex + 3 < tokens.length &&  // there more 3 tokens in the array
                            pureNumberPattern.matcher(tokens[currIndex]).matches() &&
                            ((tokens[currIndex + 1].equals("to") || (tokens[currIndex + 1].equals("and"))) &&
                                    pureNumberPattern.matcher(tokens[currIndex + 2]).matches()))
                    {
                        handleBetweenCase();
                        continue;
                    }
                }
                else
                {
                    currIndex++;
                    continue;
                }
            }
            else if (containsNumber.matcher(concat).matches()) // handle numbers
            {
                // handle pure numbers
                if (pureNumberPattern.matcher(concat).matches())
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

                        String unitToken="";
                        if(currIndex<tokens.length)
                        {
                            unitToken = cleanWord(tokens[currIndex]).toLowerCase();
                        }
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
                            if(symbol.isEmpty()&& currIndex+1<tokens.length)
                            {
                                currIndex++;
                                symbol=cleanWord(tokens[currIndex].toLowerCase());
                            }
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
                                if(currIndex+1<tokens.length)
                                {
                                    String temp=cleanWord(tokens[currIndex+1]);
                                    if(pureNumberPattern.matcher(temp).matches())
                                    {
                                        insertToAllDocTerms(monthHandler(temp,monthMap.get(symbol)));
                                        currIndex++;
                                    }
                                }
                                concat = monthHandler(concat, monthMap.get(symbol));
                                currIndex++;
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
                            String[] splirWords=concat.split("-");
                            if(splirWords.length>1)
                            {
                                String first=cleanWord(splirWords[0]);
                                String second=cleanWord(splirWords[1]);
                                if(pureNumberPattern.matcher(first).matches())
                                {
                                    if(unitMap.containsKey(second.toLowerCase()))
                                    {
                                        concat=handleSimpleNumbersAndUnits(first,second);
                                    }
                                    else if(monthMap.containsKey(second))
                                    {
                                        concat=monthHandler(first,monthMap.get(second));
                                    }
                                    else if(symbols.containsKey(second.toLowerCase()))
                                    {
                                        concat=first+symbols.get(second.toLowerCase());
                                        if(splirWords.length>3)
                                        {
                                            if(symbols.containsKey(splirWords[2].toLowerCase()))
                                            {
                                                concat=concat+symbols.get(splirWords[2].toLowerCase());
                                            }
                                        }
                                    }
                                    else
                                    {
                                        concat=handleSimpleNumbers(first)+"-"+second;
                                    }
                                }
                                else if(containsNumber.matcher(first).matches())
                                {
                                    if(first.contains("$"))
                                    {
                                        if(first.charAt(0)=='$')
                                        {
                                            first=first.substring(1);
                                            if(pureNumberPattern.matcher(first).matches())
                                            {
                                                if(unitMap.containsKey(second.toLowerCase()))
                                                {
                                                    concat=handlePriceNumbersAndUnits(first,unitMap.get(second.toLowerCase()));
                                                }
                                                else
                                                {
                                                    concat=handlePriceNumbers(first);
                                                }
                                            }
                                        }
                                        else if(first.charAt(first.length()-1)=='$')
                                        {
                                            first=first.substring(0,first.length()-1);
                                            if(pureNumberPattern.matcher(first).matches())
                                            {
                                                if(unitMap.containsKey(second.toLowerCase()))
                                                {
                                                    concat=handlePriceNumbersAndUnits(first,unitMap.get(second.toLowerCase()));
                                                }
                                                else
                                                {
                                                    concat=handlePriceNumbers(first);
                                                }
                                            }
                                        }
                                    }
                                    else if(first.charAt(first.length()-1)=='m')
                                    {
                                        first=first.substring(0,first.length()-1);
                                        if(pureNumberPattern.matcher(first).matches())
                                        {
                                            concat=handleSimpleNumbersAndUnits(first,"million");
                                        }
                                    }
                                    else if(first.contains("bn"))
                                    {
                                        first=first.substring(0,first.length()-2);
                                        if(pureNumberPattern.matcher(first).matches())
                                        {
                                            concat=handleSimpleNumbersAndUnits(first,"billion");
                                        }
                                    }
                                }
                                else if (monthMap.containsKey(first))
                                {
                                    if(pureNumberPattern.matcher(second).matches())
                                    {
                                        concat=monthHandler(second,monthMap.get(first));
                                    }
                                }
                            }
                            else if(splirWords.length>-1)
                            {
                                String first=splirWords[0];
                                if(first.charAt(0)=='$' && first.length()>1)
                                {
                                    first=first.substring(1);
                                    if(pureNumberPattern.matcher(first).matches())
                                    {
                                        concat=handlePriceNumbers(first);
                                    }
                                }
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
                                insertToAllDocTerms(concat);
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
                    insertToAllDocTerms(concat);
                    currIndex++;
                    continue;
                }
                currIndex++;
            }
            //finish handle month

            else if (concat.charAt(0) >= 'A' && concat.charAt(0) <= 'Z')// is a entity or termWithCapitalLetter
            {
                if(currIndex+1<tokens.length)
                {
                    if (concat.equals("Between"))
                    {
                        currIndex++;
                        if (currIndex + 3 < tokens.length &&  // there more 3 tokens in the array
                                pureNumberPattern.matcher(tokens[currIndex]).matches() &&
                                ((tokens[currIndex + 1].equals("to") || (tokens[currIndex + 1].equals("and"))) &&
                                        pureNumberPattern.matcher(tokens[currIndex + 2]).matches()))
                        {
                            handleBetweenCase();
                            continue;
                        }
                    }
                    if(tokens[currIndex+1].charAt(0)>='A' && tokens[currIndex+1].charAt(0)<='Z' )
                    {
                        if((!tokens[currIndex].contains(",")&&!tokens[currIndex].contains("."))||(tokens[currIndex].equals("Mr.")||tokens[currIndex].equals("Mrs.")))
                        {
                            currIndex++;
                            entityHandler(concat);
                            continue;
                        }
                    }
                    else
                    {
                        if(stopWords.contains(concat.toLowerCase()))
                        {
                            currIndex++;
                            continue;
                        }
                        insertToAllDocTerms(concat);
                        currIndex++;
                        continue;
                    }
                }
                if(stopWords.contains(concat.toLowerCase()))
                {
                    currIndex++;
                    continue;
                }
                insertToAllDocTerms(concat);
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
                        if(!word.isEmpty())
                        {
                            insertToAllDocTerms(word);
                            concat=concat+"-"+words[i];
                        }
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
                else
                {
                    insertToAllDocTerms(concat);
                }
            }
        }
    }


    private void handleBetweenCase()
    {
        String concat="";
        String token1=cleanWord(tokens[currIndex]);
        String token2=cleanWord(tokens[currIndex+2]);
        boolean flag=false;
        if(currIndex+4<tokens.length)
        {
            String temp=cleanWord(tokens[currIndex+3]).toLowerCase();
            if(symbols.containsKey(temp))
            {
                token1=token1+symbols.get(temp);
                token2=token2+symbols.get(temp);
                concat=token1+"-"+token2;
                currIndex=currIndex+4;
            }
            else if(monthMap.containsKey(temp))
            {
                token1=monthHandler(token1,monthMap.get(temp));
                concat=token1+"-"+token2;
                token2=monthHandler(token2,monthMap.get(temp));
                currIndex=currIndex+4;
            }
            else if(unitMap.containsKey(temp))
            {
                token1=handleSimpleNumbersAndUnits(token1,temp);
                token2=handleSimpleNumbersAndUnits(token2,temp);
                concat=token1+"-"+token2;
                currIndex=currIndex+4;
            }
            else
                flag=true;
        }
        if(flag)
        {
            concat = token1 + "-" + token2;
            currIndex = currIndex + 3;
        }

        insertToAllDocTerms(token1);
        insertToAllDocTerms(token2);
        insertToAllDocTerms(concat);
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
            String clean=cleanWord(token);
            if(!stopWords.contains(clean.toLowerCase()))
            {
                insertToAllDocTerms(clean);
            }
            if(token.charAt(token.length()-1)=='.' || token.charAt(token.length()-1)==',')
            {
                concat+=clean;
                insertToAllDocTerms(clean);
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
        insertToAllDocTerms(concat);
        if(entities.containsKey(concat))
        {
            entities.replace(concat,entities.get(concat)+1);
        }
        else
        {
            entities.put(concat,1);
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

    public void createStopWords(HashSet<String> sWords)
    {
        stopWords=sWords;
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
        if(word.isEmpty())
        {
            return "";
        }
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


    private void getText()
    {
        Pattern p = Pattern.compile("<TEXT>(.*)</TEXT>");
        Matcher m = p.matcher(text);
        while (m.find()) {
            text =m.group(1);
        }
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

    public void setFinishDoc(boolean b) {
        this.FinishDoc= b;
        posting.setFinishDoc(b);
    }

    public void setIsStemming(boolean isStemming) {
            this.isStemming=isStemming;
    }

    public TreeMap<String,Integer> parseQuery(String query, boolean isStemming) {
        currIndex=0;
        tokens= query.split(" ");
        parseText();
        if(isStemming)
        {
            HashMap<String,Integer> termsAfterStemming= new HashMap<>();
            for (String beforeStem:allDocTerms.keySet()
            ) {
                if(containsNumber.matcher(beforeStem).matches()|| beforeStem.contains(" "))
                {
                    termsAfterStemming.put(beforeStem,allDocTerms.get(beforeStem));
                }
                else
                {
                    String afterStem=stemmer.stem(beforeStem);
                    if(termsAfterStemming.containsKey(afterStem))
                    {
                        termsAfterStemming.replace(afterStem,termsAfterStemming.get(afterStem)+allDocTerms.get(beforeStem));
                    }
                    else
                    {
                        termsAfterStemming.put(afterStem,allDocTerms.get(beforeStem));
                    }
                }
            }
            allDocTerms=termsAfterStemming;
        }
        TreeMap<String,Integer> terms= new TreeMap<>(allDocTerms);
        return terms;
    }
}

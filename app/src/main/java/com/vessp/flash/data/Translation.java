package com.vessp.flash.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import flash.vessp.com.flash.R;

/*
 *  A word or phrase with direct translation.  Can be tested in multiple ways, ex. kanjiToEigo or kanaToEigo.
 */
public class Translation extends FlashData implements IKnowledge
{
    public int _id = -1;

    public int id()
    {
        return _id;
    }

    public GType gType;
    public GSubType gSubType;

    public String kanji;
    public String kana;
    public String eigo;

    public String notes;

    private transient Question kanjiToEigo = new Question();
    private transient Question kanaToEigo = new Question();
    private transient Question eigoToKana = new Question();

    /**
     * Compares this object to the specified object to determine their relative
     * order.
     *
     * @param another the object to compare to this instance.
     * @return a negative integer if this instance is less than {@code another};
     * a positive integer if this instance is greater than
     * {@code another}; 0 if this instance has the same order as
     * {@code another}.
     * @throws ClassCastException if {@code another} cannot be converted into something
     *                            comparable to {@code this} instance.
     */
    @Override
    public int compareTo(IKnowledge another)
    {
        if(needsTesting() == another.needsTesting())
            return another.getPriorityQuestion().value() - getPriorityQuestion().value();
        else if(!needsTesting())
            return 1;
        return -1;
    }

    public enum GType{
        NONE, NOUN(R.color.grammer_noun), VERB(R.color.grammer_verb), ADJECTIVE(R.color.grammer_adj), ADVERB(R.color.grammer_adv), PREPOSITION, CONJUNCTION, PHRASE;

        public int colorRes;

        GType()
        {
            this(R.color.grammer_base);
        }

        GType(int colorRes)
        {
            this.colorRes = colorRes;
        }
    }

    public enum GSubType{
        NONE, TRANS, INTRANS;
    }

    public Translation()
    {

    }

    @Override
    public List<IQuestion> getQuestions()
    {
        List<IQuestion> qs = new ArrayList<>();
        if(kanji != null && kanji.length() > 0 && kanaToEigo.maruCount() > 5)
            qs.add(kanjiToEigo);
        qs.add(kanaToEigo);
//        qs.add(eigoToKana);
        return qs;
    }

    @Override
    public AttrMap getAttrMap() throws IllegalAccessException
    {
        AttrMap map = super.getAttrMap();
        map.putAll(kanjiToEigo.getAttrMap(), "kanjiToEigo");
        map.putAll(kanaToEigo.getAttrMap(), "kanaToEigo");
        map.putAll(eigoToKana.getAttrMap(), "eigoToKana");
        return map;
    }

    public void readAttrs(AttrMap attrs) throws IllegalAccessException
    {
        super.readAttrs(attrs);
        kanjiToEigo.readAttrs(attrs, "kanjiToEigo");
        kanaToEigo.readAttrs(attrs, "kanaToEigo");
        eigoToKana.readAttrs(attrs, "eigoToKana");
    }

    @Override
    public String toString()
    {
        return kana + " - " + eigo;
    }

    @Override
    public IQuestion getPriorityQuestion()
    {
        List<IQuestion> qs = getQuestions();
        Collections.sort(qs);

        IQuestion pq = null;
        for(IQuestion q : qs)
        {
            if(q.needsTesting())
                pq = q;//get highest score which needs testing
        }

        if(pq == null)
            pq = qs.get(0);//if none need testing then just get highest score

        return pq;
    }

    @Override
    public String getQuestionText(IQuestion q)
    {
        if(q == kanjiToEigo)
            return kanji;
        else if(q == kanaToEigo)
            return kana;
        return eigo;
    }

    @Override
    public String getCorrectAnswer(IQuestion q)
    {
        if(q == kanjiToEigo)
            return eigo;
        else if(q == kanaToEigo)
            return eigo;
        return kana;
    }

    @Override
    public boolean isCorrectAnswer(IQuestion q, String answerText)
    {
//        String correctAnswerText = "tree (in a), house (asdf), cat";
        String correctAnswerText = getCorrectAnswer(q);
        if(q == kanjiToEigo || q == kanaToEigo)
        {
            while(true)
            {
                int openBracketIndex = correctAnswerText.indexOf("(", 0);
                if (openBracketIndex != -1)
                {
                    int closeBracketIndex = correctAnswerText.indexOf(")", openBracketIndex);
                    if(closeBracketIndex == -1)
                        break;
                    correctAnswerText = correctAnswerText.substring(0, openBracketIndex) + correctAnswerText.substring(closeBracketIndex+1, correctAnswerText.length());
                } else
                {
                    break;
                }
            }

            String[] possibleAnswers = correctAnswerText.split(",");
            for(String possibleAnswer : possibleAnswers)
            {
                possibleAnswer = possibleAnswer.trim();
                if(possibleAnswer.startsWith("to ") && !answerText.startsWith("to "))
                    possibleAnswer = possibleAnswer.substring(3);
                else if(possibleAnswer.startsWith("to be ") && !answerText.startsWith("to be "))
                    possibleAnswer = possibleAnswer.substring(6);

                int dis = sDis(possibleAnswer, answerText, 0, 0);

//                int dis = levenshteinDistance(answerText, answerText.length(), possibleAnswer.trim(), possibleAnswer.trim().length());
//                if(answerText.equals(possibleAnswer.trim()))
//                    return true;
                if(dis <= 1)
                    return true;
            }
            return false;
        }

        //default case
        return correctAnswerText.equals(answerText);
    }

    private int sDis(String want, String have, int i, int cost)
    {
        if(i >= want.length())
        {
            cost += have.length() - want.length();
            return cost;
        }

        if(i >= have.length())
        {
            cost += want.length() - have.length();
            return cost;
        }

        char wChar = want.charAt(i);
        char hChar = have.charAt(i);

        if(wChar == hChar)
        {
            //do nothing
        }
        else if(have.length() > i+1 && have.charAt(i+1) == wChar)
        {
            //next char is correct, remove current char
            have = have.substring(0, i) + have.substring(i+1);
            cost++;
        }
        else if(want.length() > i+1 && want.charAt(i+1) == hChar)
        {
            //miss char, insert it
            have = have.substring(0, i) + wChar + have.substring(i);
            cost++;
        }
        else
        {
            //overwrite current char
            have = have.substring(0, i) + wChar + (have.length() > i ? have.substring(i+1) : "");
            cost++;
        }
        return sDis(want, have, i+1, cost);
    }

    private int levenshteinDistance(String s, int len_s, String t, int len_t)
    {
        int cost;

        /* base case: empty strings */
        if (len_s == 0) return len_t;
        if (len_t == 0) return len_s;

        /* test if last characters of the strings match */
        if (s.indexOf(len_s-1) == t.indexOf(len_t-1))
            cost = 0;
        else
            cost = 1;

        /* return minimum of delete char from s, delete char from t, and delete char from both */
        int min = levenshteinDistance(s, len_s - 1, t, len_t) + 1;
        int next = levenshteinDistance(s, len_s, t, len_t - 1) + 1;
        if(next < min)
            min = next;
        next = levenshteinDistance(s, len_s - 1, t, len_t - 1) + cost;
        if(next < min)
            min = next;
        return min;
//        return Math.min(levenshteinDistance(s, len_s - 1, t, len_t) + 1,
//                levenshteinDistance(s, len_s, t, len_t - 1) + 1,
//                levenshteinDistance(s, len_s - 1, t, len_t - 1) + cost);
    }

    @Override
    public boolean needsTesting()
    {
        List<IQuestion> qs = getQuestions();
        for(IQuestion q : qs)
        {
            if(q.needsTesting())
                return true;
        }
        return false;
    }

    @Override
    public String getKnowledgeSnippet()
    {
        return kanji + "\n" + kana + "\n" + eigo;
    }

    @Override
    public boolean filterBy(String key)
    {
        return kanji.contains(key) || kana.contains(key) || eigo.contains(key);
    }
}

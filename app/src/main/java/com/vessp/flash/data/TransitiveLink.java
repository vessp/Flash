package com.vessp.flash.data;

import java.util.List;

public class TransitiveLink extends MultipleChoiceQuestion
{
    public int transId;
    public int intransId;

    public transient Translation trans;
    public transient Translation intrans;

    public TransitiveLink()
    {

    }

    @Override
    public String getQuestionText(IQuestion q)
    {
        return "Which one is transitive?";
    }

    @Override
    public String getCorrectAnswer(IQuestion q)
    {
        return trans.kana;
    }

    @Override
    public String getOtherAnswer(IQuestion q)
    {
        return intrans.kana;
    }

//    public boolean hasBoth()
//    {
//        return trans != null && intrans != null;
//    }

    @Override
    public boolean isCorrectAnswer(IQuestion q, String answerText)
    {
        return getCorrectAnswer(q).equals(answerText);
    }

    @Override
    public boolean needsTesting()
    {
        return false;
    }

    @Override
    public String getKnowledgeSnippet()
    {
        return null;
    }

    @Override
    public List<IQuestion> getQuestions()
    {
        return null;
    }

    @Override
    public IQuestion getPriorityQuestion()
    {
        return null;
    }

    @Override
    public boolean filterBy(String key)
    {
        return trans.filterBy(key) || intrans.filterBy(key);
    }

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

//    public Translation getOnlyWord()
//    {
//        if(trans != null)
//            return trans;
//        return intrans;
//    }

//    @Override
//    public void readJson(JSONObject json)
//    {
//        super.readJson(json);
//
//        JSONObject jTrans = json.optJSONObject("trans");
//        if(jTrans != null)
//        {
//            trans = Translation.fromJson(jTrans);
//            trans.gType = Translation.GType.valueOf(json.optString("gType"));
//            trans.gSubType = Translation.GSubType.TRANS;
//        }
//
//        JSONObject jIntrans = json.optJSONObject("intrans");
//        if(jIntrans != null)
//        {
//            intrans = Translation.fromJson(jIntrans);
//            intrans.gType = Translation.GType.valueOf(json.optString("gType"));
//            intrans.gSubType = Translation.GSubType.INTRANS;
//        }
//    }
//
//    @Override
//    public JSONObject toJson()
//    {
//        JSONObject json = super.toJson();
//
//        try
//        {
//            json.put("gType", Translation.GType.VERB);
//
//            if(trans != null)
//                json.put("trans", trans.toJson());
//            if(intrans != null)
//                json.put("intrans", intrans.toJson());
//
//        } catch (JSONException e)
//        {
//            e.printStackTrace();
//        }
//        return json;
//    }
//
//    public String translationSnippet()
//    {
//        return (trans!=null?trans.translationSnippet():"") + "\n" + (intrans!=null?intrans.translationSnippet():"");
//    }
}

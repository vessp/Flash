package com.vessp.flash.data;

public abstract class MultipleChoiceQuestion implements IKnowledge
{
    public int _id = -1;

    public int id()
    {
        return _id;
    }

    public MultipleChoiceQuestion()
    {

    }

    public abstract String getOtherAnswer(IQuestion q);

}

package com.vessp.flash.data;

import java.util.List;

/*
 *  One piece of information (like a direct translation, or a transitive-intransitive link) that can be tested.
 */
public interface IKnowledge extends Comparable<IKnowledge>
{
    boolean needsTesting();
    String getKnowledgeSnippet();
    List<IQuestion> getQuestions();
    IQuestion getPriorityQuestion();

    String getQuestionText(IQuestion q);
    String getCorrectAnswer(IQuestion q);
    boolean isCorrectAnswer(IQuestion q, String answerText);
    boolean filterBy(String key);
}

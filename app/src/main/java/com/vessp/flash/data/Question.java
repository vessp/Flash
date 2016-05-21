package com.vessp.flash.data;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.concurrent.TimeUnit;

/*
 *  Contains all the marking information to be able to calculate how importantly and when retesting is required.
 */
public class Question extends FlashData implements IQuestion
{
    private int hitCount;
    private int maruCount;
    private int maruStreak;
    private long lastHitInstant;
    private long lastMaruInstant;

    @Override
    public int hitCount()
    {
        return hitCount;
    }

    @Override
    public int maruCount()
    {
        return maruCount;
    }

    @Override
    public long lastHitInstant()
    {
        return lastHitInstant;
    }

    @Override
    public long lastMaruInstant()
    {
        return lastMaruInstant;
    }

    @Override
    public int maruStreak()
    {
        return maruStreak;
    }

    @Override
    public void markHit(boolean maru)
    {
        long now = DateTime.now().getMillis();
        lastHitInstant = now;
        hitCount++;
        if(maru)
        {
            lastMaruInstant = now;
            maruCount++;
            maruStreak++;
        }
        else
        {
            maruStreak = 0;
        }

    }

    //bigger value means more chance to get this mondai
    @Override
    public int value()
    {
        int score = 0;

        long timeSinceLastHit = System.currentTimeMillis() - lastHitInstant();
        long timeSinceLastMaru = System.currentTimeMillis() - lastMaruInstant();

        if(timeSinceLastHit < TimeUnit.MINUTES.toMillis(1))
        {
            score -= 1000;
        }
        else if(maruStreak() == 0)
        {
            score += 1000;
        }

        float lastMaruRatio = scaledVal((int)TimeUnit.DAYS.toMillis(0), (int)TimeUnit.DAYS.toMillis(10), timeSinceLastMaru);
        score += lastMaruRatio * 100;

//        score += ((float)hitCount() / 10.0f) * 100;

        float mark = hitCount() == 0 ? 0 : (float)maruCount() / (float)hitCount();
        score += mark * 10;

//        int yasumiValue = scaledVal((int)TimeUnit.DAYS.toMillis(1), (int)TimeUnit.DAYS.toMillis(5), yasumiMillis);
//        int markValue = scaledVal(1, 0, mark) * markWeight;
        return score;
    }

    private static int scaledVal(double min, double max, double cur)
    {
        final int scaleMin = 0;
        final int scaleMax = 100;

        float progress = (float) ((cur - min) / (max - min));
        double scaledVal = scaleMin + (progress * (scaleMax - scaleMin));
        scaledVal = Math.max(Math.min(scaledVal, scaleMax), scaleMin);
        return (int) scaledVal;
    }

    @Override
    public Duration timeBetweenTakes()
    {
        double maruStreak = maruStreak();
        //http://www.wolframalpha.com/share/clip?f=d41d8cd98f00b204e9800998ecf8427ebtgakmk5ig
        double daysBetweenTests = 0.285714*Math.pow(maruStreak,2) + 0.0571429*(maruStreak) + 0.171429;
        return new Duration((long) (TimeUnit.DAYS.toMillis(1) * daysBetweenTests));
    }

    @Override
    public Duration timeTillTest()
    {
        return new Duration(timeBetweenTakes().getMillis() - (DateTime.now().getMillis() - lastMaruInstant()));
    }

    @Override
    public long nextTestDate()
    {
        return DateTime.now().getMillis() + timeTillTest().getMillis();
    }

    @Override
    public boolean needsTesting()
    {
        return DateTime.now().getMillis() > nextTestDate();
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
    public int compareTo(IQuestion another)
    {
        return another.value() - value();
    }

    //--JSON--------------------------------------------------------------------------------------

//    @Override
//    public void readJson(JSONObject json)
//    {
////        try
////        {
////            JSONArray jTakes = json.optJSONArray("takes");
////            if(jTakes != null)
////            {
////                for (int i = 0; i < jTakes.length(); i++)
////                {
////                    takes.add(Take.readJson(jTakes.getJSONObject(i)));
////                }
////            }
////        }catch (JSONException e)
////        {
////            e.printStackTrace();
////        }
//    }

//    @Override
//    public JSONObject toJson()
//    {
//        JSONObject json = new JSONObject();
//
////        try
////        {
//////            json.put("class", getClass().getSimpleName());
////
////            JSONArray jTakes = new JSONArray();
////            for(Take t : takes)
////                jTakes.put(t.toJson());
////            json.put("takes", jTakes);
////            return json;
////        }catch (JSONException e)
////        {
////            e.printStackTrace();
////        }
//
//        return json;
//    }

//    public static List<IQuestion> fromJsonArray(JSONArray jArr)
//    {
//        try
//        {
//            List<IQuestion> list = new ArrayList<>();
//            for (int i = 0; i < jArr.length(); i++)
//            {
//                JSONObject jObj = jArr.getJSONObject(i);
//                Translation.GType gType = Translation.GType.valueOf(jObj.getString("gType"));
//                IQuestion k = null;
//                switch(gType)
//                {
//                    case NOUN:
//                        k = new Translation();
//                        break;
//                    case VERB:
//                        k = new TransitiveLink();
//                        break;
//                    case PHRASE:
//                        k = new Translation();
//                        break;
//                }
//                k.readJson(jObj);
//                list.add(k);
//
//                //can't do it like this because they get committed to json later which i don't want
////                if(k instanceof VerbSet)
////                {
////                    VerbSet vs = (VerbSet)k;
////                    if(vs.trans != null)
////                        list.add(vs.trans);
////                    if(vs.intrans != null)
////                        list.add(vs.intrans);
////                }
//            }
//            return list;
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        return null;
//    }

    @Override
    public String getScoreSnippet()
    {
        return maruCount() + " / " + hitCount() + "  ( " + maruStreak() + " )   " + value() + " " + (needsTesting()?1:0);
    }
}

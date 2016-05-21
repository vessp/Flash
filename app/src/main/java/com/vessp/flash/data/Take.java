package com.vessp.flash.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Take
{
    public final long instant;
    public final boolean maru;

    public Take(long instant, boolean maru)
    {
        this.instant = instant;
        this.maru = maru;
    }

    public static Take readJson(JSONObject json)
    {
        try
        {
            return new Take(
                    json.getLong("instant"),
                    json.getBoolean("maru")
            );
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject toJson()
    {
        try
        {
            JSONObject json = new JSONObject();
            json.put("instant", instant);
            json.put("maru", maru);
            return json;
        }catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}


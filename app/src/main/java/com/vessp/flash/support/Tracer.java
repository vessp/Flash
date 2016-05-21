package com.vessp.flash.support;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/*
 *  Prints messages to console.
 */
public class Tracer
{
    private static boolean enabled = false; //will be set to true if a TracerLog is attached

    private static final int MIN_TAG_LENGTH = 11;//buffer tag names strings with filler characters just to line up console output

	// *HELPER OBJECTS********************************************************************************//
	public enum TT //TraceType
	{
		UNSPECIFIED(true), //should always be true
		APP_INIT(true),
		FLASH_DB(true);
		
		public boolean active;// if false all traces of this type are ignored

		TT(boolean active)
		{
			this.active = active;
		}
	}

	public enum TraceLevel
	{
		VERBOSE(2), DEBUG(3), INFO(4), WARN(5), ERROR(6);

		public int degree;// higher is more important

		TraceLevel(int level)
		{
			this.degree = level;
		}

		public static TraceLevel getHigher(TraceLevel a, TraceLevel b)
		{
			if (a == null)
				return b;
			if (b == null)
				return a;

			return a.degree >= b.degree ? a : b;
		}
	}

	public interface TracerLog
	{
		void v(String tag, String message);

		void d(String tag, String message);

		void i(String tag, String message);

		void w(String tag, String message);

		void e(String tag, String message);
	}

	// *SETUP********************************************************************************//

	private static Map<TT, Long> instantMap = new HashMap<>();

	private static TracerLog tracerLog = null;

	public static void attachLog(TracerLog tracerLog)
	{
		Tracer.tracerLog = tracerLog;
        if(tracerLog != null)
            enabled = true;
	}

	// *STATEMENTS********************************************************************************//

	public static void v(String message)
	{
		trace(TraceLevel.VERBOSE, message, TT.UNSPECIFIED);
	}
	
	public static void d(String message)
	{
		trace(TraceLevel.DEBUG, message, TT.UNSPECIFIED);
	}
	
	public static void i(String message)
	{
		trace(TraceLevel.INFO, message, TT.UNSPECIFIED);
	}
	
	public static void w(String message)
	{
		trace(TraceLevel.WARN, message, TT.UNSPECIFIED);
	}
	
	public static void e(String message)
	{
		trace(TraceLevel.ERROR, message, TT.UNSPECIFIED);
	}

	public static void v(String message, TT... types)
	{
		trace(TraceLevel.VERBOSE, message, types);
	}

	public static void d(String message, TT... types)
	{
		trace(TraceLevel.DEBUG, message, types);
	}

	public static void i(String message, TT... types)
	{
		trace(TraceLevel.INFO, message, types);
	}

	public static void w(String message, TT... types)
	{
		trace(TraceLevel.WARN, message, types);
	}

	public static void e(String message, TT... types)
	{
		trace(TraceLevel.ERROR, message, types);
	}

    public static void w(Exception e)
    {
        w(getStackTrace(e));
    }

    public static void e(Exception e)
    {
        e(getStackTrace(e));
    }

	public static void w(Exception e, TT... types)
	{
		w(getStackTrace(e), types);
	}
	
	public static void e(Exception e, TT... types)
	{
		e(getStackTrace(e), types);
	}

	private static String getTagString(TT... types)
	{
		String tags = "";
		for (TT type : types)
		{
			if (type.active)
			{
				if (tags.length() > 0)
					tags += ",";
				tags += type.toString();
			}
		}
		return tags;
	}
	
//	public static String enumString(int value, AbstractMap.SimpleEntry<Integer, String>... enums)
//	{
//		for(int i=0; i<enums.length; i++)
//		{
//			if(value == enums[i].getKey())
//				return enums[i].getValue();
//		}
//		return "";w
//	}

	private static void trace(TraceLevel level, String message, TT... types)
	{
        if(!enabled)
            return;

		String tagString = getTagString(types);
        if(tagString.length() == 0)
            return;

        while(tagString.length() < MIN_TAG_LENGTH)
            tagString = "_" + tagString;

        switch (level)
        {
        case VERBOSE:
            tracerLog.v(tagString, message);
            break;
        case DEBUG:
            tracerLog.d(tagString, message);
            break;
        case INFO:
            tracerLog.i(tagString, message);
            break;
        case WARN:
            tracerLog.w(tagString, message);
            break;
        case ERROR:
            tracerLog.e(tagString, message);
            break;
        }
	}

	// *STOPWATCH********************************************************************************//

	// mark begin instant
	public static void ib(TT type)
	{
		instantMap.put(type, System.currentTimeMillis());
	}

	// mark end instant, log result
	public static void ie(TraceLevel level, String timedOperationName, TT type)
	{
		long beginInstant = instantMap.get(type);
		if (beginInstant <= 0)
			return;

		long elapsedTime = System.currentTimeMillis() - beginInstant;

		trace(level, timedOperationName + " duration: " + elapsedTime + "ms", type);
	}

	public static void traceInPieces(String message)
	{
		final int cutoff = 4000;
		
		int index = 0;
		while(message.length() - index > cutoff)
		{
			Tracer.d(message.substring(index, Math.min(index+cutoff, message.length())));
			index += cutoff;
		}
		Tracer.d(message.substring(index, Math.min(index+cutoff, message.length())));
	}
	
	public static String getStackTrace()
	{
		return getStackTrace(new Throwable(""));
	}
	
	public static String getStackTrace(Throwable e)
	{
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

    public static void traceViewDimensions(View v)
    {
        i("View: " + v.getWidth() + "x" + v.getHeight());
    }

    public static void traceScreenDimensions(Context c)
    {
        DisplayMetrics dm = c.getResources().getDisplayMetrics();
        i("Screen: " + dm.widthPixels + "x" + dm.heightPixels);
    }
}

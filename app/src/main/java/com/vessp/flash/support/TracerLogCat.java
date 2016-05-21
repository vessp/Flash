package com.vessp.flash.support;

import android.util.Log;

import com.vessp.flash.support.Tracer.TracerLog;

public class TracerLogCat implements TracerLog
{

	@Override
	public void v(String tag, String message)
	{
		Log.v(tag, message);
	}

	@Override
	public void d(String tag, String message)
	{
		Log.d(tag, message);
	}

	@Override
	public void i(String tag, String message)
	{
		Log.i(tag, message);
	}

	@Override
	public void w(String tag, String message)
	{
		Log.w(tag, message);
	}

	@Override
	public void e(String tag, String message)
	{
		Log.e(tag, message);
	}

}

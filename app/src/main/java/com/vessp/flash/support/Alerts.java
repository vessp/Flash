package com.vessp.flash.support;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Alerts
{
	public static void alertError(Context context, int stringResId)
	{
		alertError(context, context.getString(stringResId));
	}

	public static void alertError(Context context, Throwable e)
	{
		alertError(context, e.getMessage());
	}
	
	public static void alertError(Context context, String message)
	{
		showInfoAlert(context, "Error", message);
	}

	public static void showInfoAlert(Context context, final String title, final String message,
									 DialogInterface.OnClickListener onPositiveResponse, DialogInterface.OnClickListener onNegativeResponse)
	{
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message);
		if(onPositiveResponse != null)
		{
			builder.setPositiveButton(android.R.string.yes, onPositiveResponse);
		}
		if(onNegativeResponse != null)
		{
			builder.setNegativeButton(android.R.string.no, onNegativeResponse);
		}
		builder.setIcon(android.R.drawable.ic_dialog_alert)
				.show();

//		if(context)
//			return;
		
//		DialogArgs dialogArgs = new DialogArgs(DialogType.ALERT, title, message);
//		dialogArgs.onComplete = onComplete;
//		app().posture().activeDialog.set(dialogArgs).commit();
	}

	public static void showInfoAlert(Context context, final String title, final String message)
	{

	}
	
//	public static void hideAlert(final boolean onlyProgressAlert)
//	{
//		if(noActivity())
//			return;
//
//		activity().runOnUiThread(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				if(noActivity())
//					return;
//
//				app().posture().activeDialog.clear().commit();
//			}
//		});
//	}
	
//	public static void alertNotImplemented()
//	{
//		toast("This feature has not yet been implemented.");
//	}
	
//	public static void toast(final String message)
//	{
//		if(noActivity())
//			return;
//
//		activity().runOnUiThread(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				if(activeToast == null)
//				{
//					activeToast = new Toast(activity());
//					activeToast.setView(activity().getLayoutInflater().inflate(R.layout.toast, null));
//					activeToast.setDuration(Toast.LENGTH_SHORT);
//
//					int actionBarHeight = activity().getSupportActionBar().getHeight();//activity().getResources().getDimensionPixelSize(R.dimen.actionBar_height);
//					activeToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, actionBarHeight);
//				}
//
//				((TextView)activeToast.getView().findViewById(R.id.toast_message)).setText(message);
//				activeToast.show();
//			}
//		});
//	}

//    private static Toast activeToast;
//
//	public static void toast(final Activity context, final String message, final int duration)
//	{
//		if(context.isFinishing() || context.isDestroyed())
//			return;
//
////		context.runOnUiThread(new Runnable()
////		{
////			@Override
////			public void run()
////			{
//				if (activeToast == null)
//				{
//					TextView tv = new TextView(context);
//					tv.setId(View.generateViewId());
//
//					activeToast = new Toast(context);
////					activeToast.setView(LayoutInflater.from(context).inflate(android.R, null));
//					activeToast.setDuration(duration);
//
////					int actionBarHeight = activity().getSupportActionBar().getHeight();//activity().getResources().getDimensionPixelSize(R.dimen.actionBar_height);
////					activeToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, actionBarHeight);
//				}
//
//				((TextView) activeToast.getView().findViewById(R.id.toast_message)).setText(message);
//				activeToast.show();
////			}
////		});
//	}
}

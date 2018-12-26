package com.anjoyo.wangyan.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;
/**
 * 自定义toast
 */
public class ToastShow {
	private static Toast toast = null;

	public static void toastShow(Context context, String text) {
		if (toast == null) {
			toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
		} else {
			toast.setText(text);
		}
		toast.show();
	}
}

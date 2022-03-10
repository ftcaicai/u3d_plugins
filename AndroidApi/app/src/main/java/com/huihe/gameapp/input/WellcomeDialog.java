package com.huihe.gameapp.input;

import android.app.Dialog;
import android.content.Context;
import android.widget.RelativeLayout;

public class WellcomeDialog extends Dialog {
	
	public WellcomeDialog(Context context) {
		super(context, context.getResources().getIdentifier("FswyWellcome", "style",context.getPackageName()));
	}
}

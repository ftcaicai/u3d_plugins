package com.huihe.gameapp.input;

import android.content.Context;

public class InputDialogManager {
	
	private static InputDialogManager instance = null;
	private InputDialog dialog = null;
	
	private InputDialogManager(){}
	
	public static InputDialogManager GetInstance() {
		if (instance == null) {
			instance = new InputDialogManager();
		}
		return instance;
	}
	
	public void Show(Context context, InputDialogSetting setting) {
		if (dialog == null) {
			dialog = new InputDialog(context, setting);
		}
		dialog.DoShow(setting);
	}
	
	public void Hide() {
		if (dialog != null) {
			dialog.hide();
		}
	}
}

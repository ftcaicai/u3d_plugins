package com.huihe.gameapp.input;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.unity3d.player.UnityPlayer;

public class InputDialog extends Dialog {
	
	private Context context = null;
	private InputDialogSetting setting = null;
	private EditText text = null;
	private RelativeLayout.LayoutParams lp = null;
	private RelativeLayout rLayout = null;
	
	public InputDialog(Context context, InputDialogSetting setting) {
		super(context, context.getResources().getIdentifier("HHTransparent", "style",context.getPackageName()));
		this.context = context;
		this.setting = setting;
		
		rLayout = new RelativeLayout(context);
		lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		text = new EditText(context);
		text.setText(setting.getText());
		lp.leftMargin = setting.getX();
		lp.topMargin = setting.getY();
	    text.setTextSize(setting.getFontSize());
	    text.setGravity(ConvertGravity(setting.getGravity()));
	    text.setWidth(setting.getWidth());
	    if (setting.getBgcolor() == 0) {
	    	text.setBackgroundColor(0);
	    	text.setPadding(0, 0, 0, 0);
	    } else {
	    	text.setBackgroundColor(Color.WHITE);
	    	text.setPadding(5, 5, 5, 5);
	    }
	    
	    text.setSingleLine();
		text.setFocusableInTouchMode(true);
		text.requestFocus();
		
		text.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEND | android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		
		text.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		    	switch (actionId) {  
			    	case  EditorInfo.IME_ACTION_SEND:
			    		if (v == text) {
			    			try {
								JSONObject json = new JSONObject();
								json.put("fromId", "" + InputDialog.this.setting.getFromId());
								json.put("actionType", "2");
								json.put("text", text.getText());
								UnityPlayer.UnitySendMessage("SdkGameObject", "OnInputDialogClose", json.toString());
							} catch (JSONException e) {
								e.printStackTrace();
							}
			    		}
			    		InputDialog.this.hide();
			            break;
			    	case  EditorInfo.IME_ACTION_DONE:
			    		if (v == text) {
			    			try {
								JSONObject json = new JSONObject();
								json.put("fromId", "" + InputDialog.this.setting.getFromId());
								json.put("actionType", "2");
								json.put("text", text.getText());
								UnityPlayer.UnitySendMessage("SdkGameObject", "OnInputDialogClose", json.toString());
							} catch (JSONException e) {
								e.printStackTrace();
							}
			    		}
			    		InputDialog.this.hide();
			    		break;
			    	default:  
			            break;
		    	}
		    	return true;
		    }
		});
		rLayout.addView(text, lp);
		setContentView(rLayout);
	}

	public void DoShow(InputDialogSetting setting) {
		this.setting = setting;
		text.setText(setting.getText());
		
		lp.leftMargin = setting.getX();
		lp.topMargin = setting.getY();
	    text.setTextSize(setting.getFontSize());
	    text.setGravity(ConvertGravity(setting.getGravity()));
	    text.setWidth(setting.getWidth());
	    if (setting.getBgcolor() == 0) {
	    	text.setBackgroundColor(0);
	    	text.setPadding(0, 0, 0, 0);
	    } else {
	    	text.setBackgroundColor(Color.WHITE);
	    	text.setPadding(5, 5, 5, 5);
	    }
	    if (setting.getKeyboardType() == 1) {
			text.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEND | android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		} else {
			text.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE | android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		}
	    text.setFocusableInTouchMode(true);
		text.requestFocus();
		if (setting.getText() != null && setting.getText().length() > 0) {
			text.setSelection(setting.getText().length());
		}
	    rLayout.removeView(text);
	    rLayout.addView(text, lp);
		this.show();
		Timer timer = new Timer();
	    timer.schedule(new TimerTask(){
	    	public void run() {
	    		InputMethodManager inputManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);  
			    inputManager.showSoftInput(text, 0);
	    	}
	    }, 100);
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		try {
			JSONObject json = new JSONObject();
			json.put("fromId", "" + setting.getFromId());
			json.put("actionType", "1");
			json.put("text", text.getText());
			UnityPlayer.UnitySendMessage("SdkGameObject", "OnInputDialogClose", json.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.hide();
		return true;
	}
	
	private int ConvertGravity(int setting) {
		switch(setting) {
			case 1:
				return Gravity.LEFT;
			case 2:
				return Gravity.CENTER;
			case 3:
				return Gravity.RIGHT;
			default:
				return Gravity.LEFT;
		}
	}
}

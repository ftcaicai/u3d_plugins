package com.huihe.gameapp.input;

public class InputDialogSetting {
//	int fromId, int keyboardType, int x, int y, int fontSize, int gravity, int width, String text
	private int fromId;
	private int keyboardType;
	private int x;
	private int y;
	private int fontSize;
	private int gravity;
	private int width;
	private String text;
	private int bgcolor;
	
	public int getFromId() {
		return fromId;
	}
	public void setFromId(int fromId) {
		this.fromId = fromId;
	}
	public int getKeyboardType() {
		return keyboardType;
	}
	public void setKeyboardType(int keyboardType) {
		this.keyboardType = keyboardType;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getFontSize() {
		return fontSize;
	}
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
	public int getGravity() {
		return gravity;
	}
	public void setGravity(int gravity) {
		this.gravity = gravity;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getBgcolor() {
		return bgcolor;
	}
	public void setBgcolor(int bgcolor) {
		this.bgcolor = bgcolor;
	}
}

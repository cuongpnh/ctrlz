package com.ceofox.mobile.ctrlz.model;

import java.util.ArrayList;

public class TextHolder {
	private String id;
	private ArrayList<String> text;
	private int cursor;

	public TextHolder(String id, ArrayList<String> text) {
		this.id = id;
		this.text = text;
	}

	public ArrayList<String> getText() {
		return text;
	}

	public int getCursor() {
		return cursor;
	}

	public void setCursor(int cursor) {
		this.cursor = cursor;
	}

	public void setText(ArrayList<String> text) {
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void updateCursorToEndIndex() {
		if (getText() == null) {
			cursor = -1;
			return;
		}
		cursor = getText().size() - 1;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(" [ Id: " + this.id);
		builder.append("; Text: " + text.toString());
		builder.append(" ]");
		return builder.toString();

	}

	public String childToString() {
		StringBuilder builder = new StringBuilder();
		builder.append("; Text: [");
		builder.append(text.toString());
		builder.append("]");
		return builder.toString();

	}
}

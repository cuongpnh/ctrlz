package com.ceofox.mobile.ctrlz.model;

import java.util.ArrayList;

public class ZHolder {
	private String packageName;
	private int pid;
	private ArrayList<TextHolder> textHolder;

	public ZHolder(String name, int pid, ArrayList<TextHolder> textHolder) {
		this.packageName = name;
		this.pid = pid;
		this.textHolder = textHolder;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String name) {
		this.packageName = name;
	}

	public ArrayList<TextHolder> getTextHolder() {
		return textHolder;
	}

	public void setTextHolder(ArrayList<TextHolder> textHolder) {
		this.textHolder = textHolder;
	}

	public int getPID() {
		return pid;
	}

	public void setPID(int pid) {
		this.pid = pid;
	}

	public static int isViewIdExists(ZHolder holder, String id) {
		for (int i = 0; i < holder.getTextHolder().size(); i++) {
			if (holder.getTextHolder().get(i).getId().equals(id)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(" [ Name: " + this.packageName);
		builder.append("; PID: " + getPID());
		StringBuilder childBuilder = new StringBuilder();
		for (int i = 0; i < getTextHolder().size(); i++) {
			builder.append("; Id: " + getTextHolder().get(i).getId());
			childBuilder.append(getTextHolder().get(i).childToString());
			builder.append(childBuilder);
		}
		builder.append(" ]");
		return builder.toString();
	}

	public static ZHolder getTextHolderByName(ArrayList<ZHolder> holderList,
			String name) {
		for (int i = 0; i < holderList.size(); i++) {
			if (holderList.get(i).getPackageName().equals(name)) {
				return holderList.get(i);
			}
		}
		return null;
	}

	public static ZHolder getTextHolderById(ArrayList<ZHolder> holderList,
			int pid) {
		for (int i = 0; i < holderList.size(); i++) {
			if (holderList.get(i).getPID() == pid) {
				return holderList.get(i);
			}
		}
		return null;
	}
}

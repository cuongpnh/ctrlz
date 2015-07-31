package com.ceofox.mobile.ctrlz.model;

public class MiniProcess {
	private String name;
	private int pid;
	public MiniProcess(int pid, String name)
	{
		this.name = name;
		this.pid = pid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPID() {
		return pid;
	}
	public void setPID(int pid) {
		this.pid = pid;
	}
	
}

package com.ceofox.mobile.ctrlz.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.ceofox.mobile.ctrlz.model.MiniProcess;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

public class Utils {
	private Context mContext = null;

	public Utils(Context context) {
		this.mContext = context;
	}

	public String getInputPackageName() {
		String packageName = null;
		InputMethodManager imm = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		List<InputMethodInfo> mInputMethodProperties = imm
				.getEnabledInputMethodList();

		final int n = mInputMethodProperties.size();

		for (int i = 0; i < n; i++) {

			InputMethodInfo imi = mInputMethodProperties.get(i);
			packageName = imi.getPackageName();
			if (imi.getId().equals(
					Settings.Secure.getString(mContext.getContentResolver(),
							Settings.Secure.DEFAULT_INPUT_METHOD))) {
				break;
			}
		}
		return packageName;
	}

	public int getPIDByPackageName(String packageName) {
		int pid = 0;
		// Checking if the packageName above is running or not
		ActivityManager manager = (ActivityManager) mContext
				.getSystemService(Activity.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningProcesses = manager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo process : runningProcesses) {
			if (packageName.equals(process.processName)) {
				pid = process.pid;
			}
		}
		return pid;
	}

	public ArrayList<MiniProcess> getRunningProcess() {
		ArrayList<MiniProcess> results = new ArrayList<MiniProcess>();
		ArrayList<Integer> checkingExits = new ArrayList<Integer>();
		ActivityManager manager = (ActivityManager) mContext
				.getSystemService(Activity.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningProcesses = manager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo process : runningProcesses) {
			if (!checkingExits.contains(process.processName)) {
				results.add(new MiniProcess(process.pid, process.processName));
				checkingExits.add(process.pid);
			}
		}
		return results;
	}

	public boolean readVisibleState(int uid) {
		if (uid != 0) {
			// Process is running
			String commandLine = "cat /proc/" + uid + "/oom_adj";

			Process process = null;
			try {
				process = Runtime.getRuntime().exec(commandLine);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (process != null) {
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				int read;
				char[] buffer = new char[4096];
				StringBuffer output = new StringBuffer();
				try {
					while ((read = bufferedReader.read(buffer)) > 0) {
						output.append(buffer, 0, read);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (!output.toString().trim().isEmpty()
						&& Integer.parseInt(output.toString().trim()) == 1) {
					return true;
				}
			}
			return false;
		}
		return false;
	}

	public static int dpToPixels(int dp, Resources res) {
		return (int) (res.getDisplayMetrics().density * dp + 0.5f);
	}
}

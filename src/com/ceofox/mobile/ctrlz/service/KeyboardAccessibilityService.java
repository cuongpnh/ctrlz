package com.ceofox.mobile.ctrlz.service;

import java.util.ArrayList;
import java.util.Arrays;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.ceofox.mobile.ctrlz.model.MiniProcess;
import com.ceofox.mobile.ctrlz.model.TextHolder;
import com.ceofox.mobile.ctrlz.model.ZHolder;
import com.ceofox.mobile.ctrlz.util.Utils;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class KeyboardAccessibilityService extends AccessibilityService {
	private static KeyboardAccessibilityService INSTANCE = null;
	public static final int NORMAL = 0;
	public static final int REDO = 1;
	public static final int UNDO = 2;
	private int TEXT_CHANGED_TYPE = NORMAL;
	public static final String TAG = "CtrlZ";
	public static boolean DEBUG = false;
	private boolean mIsKeyboardDisplayed = false;
	private boolean mIsInit = false;
	private AccessibilityServiceInfo mInfo = null;
	private Utils mUtil = null;
	private ArrayList<ZHolder> mZHolder = null;
	private ArrayList<Integer> mPackageNameHolder = null;
	private int mNotificationTimeout = 300;
	@SuppressWarnings("unused")
	private String mFocusId = "";
	@SuppressWarnings("unused")
	private int mCursor = 0;
	private Intent mZHeadIntent = null;
	private AccessibilityNodeInfo mCurrentNode = null;
	public static String DATA = "data";
	public static String EMPTY_STRING = "";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			Log.d(TAG, "Bundle is not null");
			String action = bundle.getString("action");
			if (action != null) {
				if (getInstance() != null) {
					Log.d(TAG, "undo");
					getInstance().undo(getApplicationContext());
				}
			}
			stopSelf();
			return START_NOT_STICKY;
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		super.onCreate();
		if (INSTANCE == null) {
			Log.d(TAG, "Singleton");
			INSTANCE = this;
		}

	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public void undo(Context context) {
		AccessibilityNodeInfo currentNode = getInstance().getCurrentNode();
		if (currentNode == null) {
			return;
		}
		/*
		 * Now we need to get the text holder of current node by package name
		 * and view's id
		 */
		String packageName = currentNode.getPackageName().toString();
		String viewId = Integer.toHexString(currentNode.hashCode());
		TextHolder textHolder = getTextHolderByPackageNameAndViewId(
				packageName, viewId);
		if (textHolder == null) {
			return;
		}
		if (textHolder.getCursor() == -1) {
			/*
			 * Empty text holder
			 */
			return;
		}
		String newData = null;
		if (textHolder.getCursor() == 0
				|| textHolder.getText() == null
				|| (textHolder.getText() != null && textHolder.getText().size() == 0)) {
			newData = EMPTY_STRING;
		} else {
			newData = textHolder.getText().get(textHolder.getCursor() - 1);
		}

		Log.d(TAG, "Current text holder: " + textHolder.toString());
		setTextChangeType(UNDO);
		/*
		 * Focus cursor to whole text of view
		 */
		Bundle arguments = new Bundle();
		arguments.putInt(
				AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
				AccessibilityNodeInfo.MOVEMENT_GRANULARITY_PAGE);
		arguments.putBoolean(
				AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
				true);
		getCurrentNode().performAction(
				AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
				arguments);

		/*
		 * Paste new text here!
		 */
		ClipboardManager clipboard = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText(DATA, newData);
		clipboard.setPrimaryClip(clip);
		if (getCurrentNode().performAction(AccessibilityNodeInfo.ACTION_PASTE)) {
			textHolder.setCursor(textHolder.getCursor() - 1);
		}
	}

	public static KeyboardAccessibilityService getInstance() {
		return INSTANCE;
	}

	public void setTextChangeType(int type) {
		getInstance().TEXT_CHANGED_TYPE = type;
	}

	public int getTextChangeType() {
		return getInstance().TEXT_CHANGED_TYPE;
	}

	public AccessibilityNodeInfo getCurrentNode() {
		return getInstance().mCurrentNode;
	}

	public void setCurrentNode(AccessibilityNodeInfo node) {
		getInstance().mCurrentNode = node;
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		Log.d(TAG, "onAccessibilityEvent");
		if (getTextChangeType() != NORMAL) {
			setTextChangeType(NORMAL);
			return;
		}
		if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
			String packageName = mUtil.getInputPackageName();
			int pid = mUtil.getPIDByPackageName(packageName);
			boolean isKeyboardDisplayed = mUtil.readVisibleState(pid);

			if (isKeyboardDisplayed != mIsKeyboardDisplayed) {
				String message = "Keyboard";
				mIsKeyboardDisplayed = isKeyboardDisplayed;
				if (mIsKeyboardDisplayed == true) {
					mZHeadIntent = new Intent(getApplicationContext(),
							ZHeadService.class);
					startService(mZHeadIntent);
				} else {
					if (mZHeadIntent != null) {
						stopService(mZHeadIntent);
					}

				}
				Toast.makeText(
						getApplicationContext(),
						(isKeyboardDisplayed) ? message + " Visible" : message
								+ " Invisible", Toast.LENGTH_SHORT).show();
			}

		} else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {

			/*
			 * Set focus note to current node
			 */
			setCurrentNode(event.getSource());
			/*
			 * Delete killed application here!
			 */
			ArrayList<MiniProcess> runningProcess = mUtil.getRunningProcess();
			deleteKilledApp(runningProcess);

			if (event.getSource() != null) {
				if (DEBUG) {
					StringBuilder stringBuilder = new StringBuilder();
					String id = Integer.toHexString(event.getSource()
							.hashCode());
					String currentText = event.getText().toString();
					String beforeText = event.getBeforeText().toString();
					stringBuilder.append(" [ Id: " + id);
					stringBuilder.append("; currentText: " + currentText);
					stringBuilder.append("; beforeText: " + beforeText);
					stringBuilder.append("; packageName: "
							+ event.getPackageName());
					stringBuilder
							.append("; className: " + event.getClassName());
					stringBuilder.append(" ]");
					Log.d(TAG, stringBuilder.toString());

				}
				if (!event.getSource().isPassword()) {
					String packageName = event.getPackageName().toString();
					int pid = 0;
					for (MiniProcess m : runningProcess) {
						if (m.getName().equals(packageName)) {
							pid = m.getPID();
							break;
						}
					}

					if (pid != 0) {
						String text = event
								.getText()
								.toString()
								.substring(1,
										event.getText().toString().length() - 1);
						addText(pid, packageName, Integer.toHexString(event
								.getSource().hashCode()), text, event
								.getBeforeText().toString());
						Log.d(TAG, event.getText().toString());
						Log.d(TAG, mZHolder.toString());
					}
				}
				if (DEBUG) {
					AccessibilityNodeInfo source = event.getSource()
							.getParent();
					for (int i = 0; i < source.getChildCount(); i++) {
						Log.d(TAG, source.getChild(i).toString());
						;
					}
				}
			}
		}
	}

	@Override
	public void onInterrupt() {
		System.out.print("onInterrupt");
		mIsInit = false;
	}

	@Override
	protected void onServiceConnected() {
		if (mIsInit) {
			return;
		}
		mInfo = new AccessibilityServiceInfo();
		mInfo.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
				| AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
		mInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
		mInfo.notificationTimeout = mNotificationTimeout;
		setServiceInfo(mInfo);
		init();
		mIsInit = true;
	}

	public void init() {
		mZHolder = new ArrayList<ZHolder>();
		mUtil = new Utils(getApplicationContext());
		mPackageNameHolder = new ArrayList<Integer>();

	}

	public ArrayList<ZHolder> getZHolder() {
		return getInstance().mZHolder;
	}

	private void deleteKilledApp(ArrayList<MiniProcess> miniPackage) {
		ArrayList<Integer> runningPackage = new ArrayList<Integer>();
		for (MiniProcess m : miniPackage) {
			runningPackage.add(m.getPID());
		}
		Log.d(TAG, "Running app:" + runningPackage.toString());
		if (mZHolder.size() > 0) {
			ArrayList<ZHolder> tmpHolder = new ArrayList<ZHolder>();
			ArrayList<Integer> tmpPackgeNameHolder = new ArrayList<Integer>();
			for (int i = 0; i < mZHolder.size(); i++) {
				if (!runningPackage.contains(mZHolder.get(i).getPID())) {
					Log.d(TAG, String.valueOf(mZHolder.get(i).getPID()));
					tmpHolder.add(mZHolder.get(i));
					tmpPackgeNameHolder.add(mPackageNameHolder.get(i));
				}
			}
			if (tmpHolder.size() > 0) {
				mZHolder.removeAll(tmpHolder);
				mPackageNameHolder.removeAll(tmpPackgeNameHolder);
			}
		}
	}

	private void addText(int pid, String name, String id, String currentText,
			String beforeText) {
		/*
		 * Checking package exists
		 */
		if (!mPackageNameHolder.contains(pid)) {
			/*
			 * Package name is not exists
			 */
			mPackageNameHolder.add(pid);
			ArrayList<String> childTextHolder = new ArrayList<String>(
					Arrays.asList(beforeText, currentText));
			TextHolder textHolder = new TextHolder(id, childTextHolder);
			/*
			 * Update cursor to the end of the list
			 */
			textHolder.updateCursorToEndIndex();
			ArrayList<TextHolder> textHolderList = new ArrayList<TextHolder>();
			textHolderList.add(textHolder);
			mZHolder.add(new ZHolder(name, pid, textHolderList));

		} else {
			/*
			 * Checking view's id exists
			 */
			ZHolder holder = ZHolder.getTextHolderById(mZHolder, pid);
			int idIndex = ZHolder.isViewIdExists(holder, id);
			if (idIndex > -1) {
				/*
				 * Exists
				 */
				TextHolder textHolder = holder.getTextHolder().get(idIndex);
				int currentCursor = textHolder.getCursor();
				if (currentCursor < 0) {
					/*
					 * That mean we should clear all old text
					 */
					currentCursor = -1;
				}
				Log.d(TAG, "currentCursor:" + currentCursor);
				int sizeTextHolder = textHolder.getText().size();
				Log.d(TAG, "sizeTextHolder:" + sizeTextHolder);
				if ((sizeTextHolder - currentCursor) >= 1) {
					/*
					 * Current cursor not point at the end of list. Then we
					 * should delete all items from current cursor to the end of
					 * list.
					 */

					for (int i = sizeTextHolder - currentCursor - 1; i > 0; i--) {
						Log.d(TAG,
								"remove index:"
										+ i
										+ ", text:"
										+ textHolder.getText()
												.get(textHolder.getText()
														.size() - 1));
						textHolder.getText().remove(
								textHolder.getText().size() - 1);
					}

				}
				textHolder.getText().add(currentText);
				/*
				 * Update cursor to the end of the list
				 */
				textHolder.updateCursorToEndIndex();
			} else {
				/*
				 * Not exists
				 */
				TextHolder textHolder = new TextHolder(id,
						new ArrayList<String>(Arrays.asList(beforeText,
								currentText)));
				textHolder.updateCursorToEndIndex();
				holder.getTextHolder().add(textHolder);
			}
		}
	}

	private TextHolder getTextHolderByPackageNameAndViewId(String packageName,
			String viewId) {
		ArrayList<ZHolder> zholderList = getInstance().getZHolder();
		if (zholderList == null) {
			return null;
		}
		for (int i = 0; i < zholderList.size(); i++) {
			if (zholderList.get(i).getPackageName().equals(packageName)) {
				ZHolder zholder = zholderList.get(i);
				if (zholder.getTextHolder() == null) {
					return null;
				}
				for (int j = 0; j < zholder.getTextHolder().size(); j++) {
					if (zholder.getTextHolder().get(j).getId().equals(viewId)) {
						return zholder.getTextHolder().get(j);
					}
				}

			}
		}
		return null;
	}

}

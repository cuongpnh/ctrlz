package com.ceofox.mobile.ctrlz.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ceofox.mobile.ctrlz.R;
import com.ceofox.mobile.ctrlz.util.Utils;

public class ZHeadService extends Service {

	public static String TAG = ZHeadService.class.getName();
	private WindowManager windowManager;
	private RelativeLayout mRootLayout;
	private WindowManager.LayoutParams mRootLayoutParams;
	private RelativeLayout mContentContainerLayout;
	private static final int TRAY_DIM_X_DP = 200; // Width of the tray in dp
	private static final int TRAY_DIM_Y_DP = 50; // Height of the tray in dp

	private ImageButton mIbUndo = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@SuppressLint("InflateParams")
	@Override
	public void onCreate() {
		super.onCreate();
		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mRootLayout = (RelativeLayout) LayoutInflater.from(this).inflate(
				R.layout.fragment_button_simple, null);
		mContentContainerLayout = (RelativeLayout) mRootLayout
				//.findViewById(R.id.content_container);
				.findViewById(R.id.root_layout);
		//mContentContainerLayout.setOnTouchListener(new TrayTouchListener());
		mRootLayout.setOnTouchListener(new TrayTouchListener());
		int contentWidth = Utils.dpToPixels(TRAY_DIM_X_DP, getResources());
		int contentHeight = Utils.dpToPixels(TRAY_DIM_Y_DP, getResources());
		mRootLayoutParams = new WindowManager.LayoutParams(
				contentWidth,
				contentHeight,
				// WindowManager.LayoutParams.WRAP_CONTENT,
				// WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				PixelFormat.TRANSLUCENT);

		mRootLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		mRootLayoutParams.x = 0;
		mRootLayoutParams.y = 100;

		// mContentContainerLayout.setOnTouchListener(new View.OnTouchListener()
		// {
		//
		// private int initialX;
		// private int initialY;
		// private float initialTouchX;
		// private float initialTouchY;
		//
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// Toast.makeText(getApplicationContext(),
		// "dinh menh" + event.getAction(), 5000).show();
		// switch (event.getAction()) {
		// case MotionEvent.ACTION_DOWN:
		// initialX = mRootLayoutParams.x;
		// initialY = mRootLayoutParams.y;
		// initialTouchX = event.getRawX();
		// initialTouchY = event.getRawY();
		// return true;
		// case MotionEvent.ACTION_UP:
		// return true;
		// case MotionEvent.ACTION_MOVE:
		// mRootLayoutParams.x = initialX
		// + (int) (event.getRawX() - initialTouchX);
		// mRootLayoutParams.y = initialY
		// + (int) (event.getRawY() - initialTouchY);
		// windowManager.updateViewLayout(mRootLayout,
		// mRootLayoutParams);
		// return true;
		// }
		// return false;
		// }
		// });
		mRootLayout.setVisibility(View.VISIBLE);
		windowManager.addView(mRootLayout, mRootLayoutParams);
		initView();
	}

	private void initView() {
		mIbUndo = (ImageButton) mRootLayout.findViewById(R.id.ibUndo);
		mIbUndo.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getApplicationContext(), KeyboardAccessibilityService.class);
				intent.putExtra("action", "undo");
				startService(intent);
			}
		});

	}

	class TrayTouchListener implements View.OnTouchListener {

		private int initialX;
		private int initialY;
		private float initialTouchX;
		private float initialTouchY;

		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			final int action = event.getAction();
			Toast.makeText(getApplicationContext(),
					"dinh menh" + event.getAction(), 5000).show();
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				initialX = mRootLayoutParams.x;
				initialY = mRootLayoutParams.y;
				initialTouchX = event.getRawX();
				initialTouchY = event.getRawY();
				return true;
			case MotionEvent.ACTION_UP:
				return true;
			case MotionEvent.ACTION_MOVE:
				mRootLayoutParams.x = initialX
						+ (int) (event.getRawX() - initialTouchX);
				mRootLayoutParams.y = initialY
						+ (int) (event.getRawY() - initialTouchY);
				windowManager.updateViewLayout(mRootLayout, mRootLayoutParams);
				return true;
			}
			return true;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mRootLayout != null)
			windowManager.removeView(mRootLayout);
	}
}

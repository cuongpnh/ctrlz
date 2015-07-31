package com.ceofox.mobile.ctrlz;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Toast;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		final View activityRootView = findViewById(android.R.id.content);
		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						Rect r = new Rect();
						// r will be populated with the coordinates of your view
						// that area still visible.
						activityRootView.getWindowVisibleDisplayFrame(r);
						int heightDiff = activityRootView.getRootView()
								.getHeight() - r.height();

						if (heightDiff > 100) { // if more than 100 pixels, its
												// probably a keyboard...
						// Toast.makeText(getApplicationContext(), "" +
						// activityRootView.getRootView().getHeight(),
						// Toast.LENGTH_LONG).show();
						// Toast.makeText(getApplicationContext(), "" +
						// r.height(), Toast.LENGTH_LONG).show();
							Toast.makeText(getApplicationContext(),
									"Soft Keyboard detected", Toast.LENGTH_LONG)
									.show();
						}
					}
				});
//		Intent i = new Intent(this, KeyboardAccessibilityService.class);
//		startService(i);
	}
}

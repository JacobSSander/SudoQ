package de.sudoq.activities;

import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.view.MotionEvent;

public class AppBarLayoutBehavior extends AppBarLayout.Behavior
{
	@Override
	public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev)
	{
		return !(parent != null && child != null && ev != null) || super.onInterceptTouchEvent(parent, child, ev);
	}
}

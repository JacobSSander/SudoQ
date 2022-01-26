/*
 * SudoQ is a Sudoku-App for Adroid Devices with Version 2.2 at least.
 * Copyright (C) 2012  Heiko Klare, Julian Geppert, Jan-Bernhard Kordaß, Jonathan Kieling, Tim Zeitz, Timo Abele
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.sudoq.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import androidx.annotation.NonNull;

/**
 * A ScrollView which allows vertical and horizontal scrolling.
 */
public class FullScrollLayout extends FrameLayout
{
	private static final String LOG_TAG = FullScrollLayout.class.getSimpleName();
	
	/**
	 * The current zoom factor. Responsible for scaling the child view.
	 */
	private float zoomFactor;
	
	/**
	 * The view in charge of horizontal scrolling.
	 *  It will be wrapped within the vertical scroll view.
	 */
	private HorizontalScroll horizontalScrollView;
	
	/**
	 * The view in charge of the vertical scrolling.
	 *  It will be the parent of the horizontal scroll view.
	 *  And the only child of this view.
	 */
	private VerticalScroll verticalScrollView;
	
	/**
	 * The zoom-gesture-detector. Will detect the pinch gesture.
	 *  It is not hooked into anything, and touch events will be delivered manually to this detector.
	 */
	private ScaleGestureDetector scaleGestureDetector;
	
	/**
	 * The child view, which is to be zoomed and scrolled within this FullScrollLayout view.
	 */
	private ZoomableView childView;
	
	/**
	 * Creates a new ScrollLayout with given parameters.
	 *
	 * @param context the context in charge of this view
	 * @param attributeSet the AttributeSet containing layout properties
	 */
	public FullScrollLayout(Context context, AttributeSet attributeSet)
	{
		super(context, attributeSet);
		initialize(context);
	}
	
	/**
	 * Creates a new ScrollLayout with given parameters.
	 *
	 * @param context the context in charge of this view
	 */
	public FullScrollLayout(Context context)
	{
		super(context);
		initialize(context);
	}
	
	/**
	 * Private instantiation code.
	 *
	 * @param context the context in charge of this view
	 */
	private void initialize(Context context)
	{
		this.scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
		
		//In case that there was any child view provided by an XML layout file, it will be removed here.
		this.removeAllViews();
		
		//TODO: Why is this not directly set in the field? There is no "good" way to change this value before this method anyway.
		if(this.zoomFactor == 0)
		{
			this.zoomFactor = 1.0f;
		}
		
		this.verticalScrollView = new VerticalScroll(context);
		this.horizontalScrollView = new HorizontalScroll(context);
		
		this.verticalScrollView.addView(this.horizontalScrollView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		this.addView(this.verticalScrollView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	
	/**
	 * Sets the child view of this layout. Only if that view is an instance of {@link ZoomableView}.
	 *  This method is not used by the app, but attempts to prevents misuse by the Android framework.
	 *
	 * @param view the View which should be the next child view, must implement {@link ZoomableView}
	 */
	@Override
	public void addView(View view)
	{
		if(view instanceof ZoomableView)
		{
			// The horizontal scroll view is the lowest view in this FullScrollLayout containing the child view.
			this.horizontalScrollView.removeAllViews();
			this.childView = (ZoomableView) view;
			this.horizontalScrollView.addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
	}
	
	/**
	 * The touch event cannot just be handed down to the child view.
	 * Before it has to be checked, if the child view would be scrolled,
	 *  or if multiple touch pointers are attempting to pinch.
	 *
	 * @param event the {@link MotionEvent} containing the touch information
	 * @return true if this view will intercept this event, then it will not be forwarded to the child view
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event)
	{
		//No need to call the super method, since we disabled visible scrollbars.
		
		//Make sure to call both scroll view intercept calls, to initialize/update their internal state.
		boolean intercept = verticalScrollView.checkIfInterceptsTouch(event);
		intercept |= horizontalScrollView.checkIfInterceptsTouch(event);
		return intercept || event.getPointerCount() > 1;
	}
	
	/**
	 * Processes the touch event on this view.
	 *  Called, if we intercepted this touch event. Or if no child was directly touched.
	 * This forwards to the gesture listener if there are more than one pointers.
	 * And else to the scroll views (both).
	 *
	 * @param event the {@link MotionEvent} containing the touch information
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		//Multiple pointers? Assume it is a pinch gesture.
		if(event.getPointerCount() > 1)
		{
			scaleGestureDetector.onTouchEvent(event);
			return true;
		}
		//If only one pointer, make sure, that a child view was set to prevent issues.
		//TODO: Is this if-check even required? How do scroll views act without a child component? -> Prevents wrong initialization.
		else if(this.childView != null)
		{
			//try
			//{
			
			//It is important that both scroll views receive all possible movement events.
			// Else only one triggers and consumes the whole event-chain.
			// Causing only vertical or horizontally to work. But not both (diagonally).
			verticalScrollView.triggerTouchEvent(event);
			horizontalScrollView.triggerTouchEvent(event);
			
			//}
			//catch(Exception e)
			//{
			//	//TODO: Is this still the case? Does it happen after Android 5+, which exception? Why no details? Can we solve it by copying the motion event?
			//
			//	// Old android versions sometimes throw an exception when
			//	// putting and Event of one view in the onTouch of
			//	// another view. We just catch that and do nothing
			//}
			return true;
		}
		return false;
	}
	
	/**
	 * Has to be overwritten, to let this class behave a bit more like a normal ScrollView,
	 *  any scroll command is forwarded to the two internal scroll views.
	 * Applies the raw scroll values. If the target should be centered half the width and height have to be subtracted.
	 *
	 * @param x the x position to scroll to
	 * @param y the y position to scroll to
	 */
	@Override
	public void scrollTo(int x, int y)
	{
		this.verticalScrollView.post(() -> {
			verticalScrollView.scrollTo(x, y);
		});
		this.horizontalScrollView.post(() -> {
			horizontalScrollView.scrollTo(x, y);
		});
	}
	
	/**
	 * Scrolls this view, to center provided x and y positions.
	 *  It cannot always center them (due to the border), but it tries to get them as close as possible to the center.
	 *
	 * @param x the x position to scroll to
	 * @param y the y position to scroll to
	 */
	public void centerTo(int x, int y)
	{
		//Subtract half the width and height, to move the scroll target to the center.
		scrollTo(x - getWidth() / 2, y - getHeight() / 2);
	}
	
	/**
	 * Resets the zoom.
	 */
	public void resetZoom()
	{
		this.childView.zoom(1.0f);
		setZoomFactor(1.0f);
		scrollTo(0, 0);
	}
	
	/**
	 * Returns the current zoom-factor of this layout.
	 *
	 * @return the current zoom factor
	 */
	public float getZoomFactor()
	{
		return this.zoomFactor;
	}
	
	/**
	 * Sets the current zoom-factor of this layout.
	 *
	 * @param zoomFactor the new zoom-factor
	 */
	public void setZoomFactor(float zoomFactor)
	{
		this.zoomFactor = zoomFactor;
	}
	
	/**
	 * Returns the currently scrolled x amount
	 *
	 * @return the currently scrolled x amount
	 */
	public float getScrollValueX()
	{
		return this.horizontalScrollView.getScrollX();
	}
	
	/**
	 * Returns the currently scrolled y amount
	 *
	 * @return the currently scrolled y amount
	 */
	public float getScrollValueY()
	{
		return this.verticalScrollView.getScrollY();
	}
	
	//It is a little bit unfortunate, that the scroll views do not have the same parent.
	// Thus there are two times the same classes below. Keep them the same.
	
	/**
	 * The scroll views that are used here should not listen to the normal touch events.
	 *  That is why 'onInterceptTouch' and 'onTouch' are overridden to have no function.
	 * Instead two methods have been added to be able to bypass this override and still call the two super methods.
	 * These should be called by the FullScrollLayout to manually distribute the touch events.
	 *
	 * Scrollbar visibility will be disabled.
	 */
	private static class VerticalScroll extends ScrollView
	{
		/**
		 * Initializes this scroll view and disables visibility of scroll bars.
		 *
		 * @param context the context which is in charge of the FullScrollLayout
		 */
		public VerticalScroll(Context context)
		{
			super(context);
			setVerticalScrollBarEnabled(false);
			setHorizontalScrollBarEnabled(false);
		}
		
		/**
		 * Overridden to disable the functionality by natural framework calls.
		 */
		@Override
		public boolean onInterceptTouchEvent(MotionEvent event)
		{
			return false;
		}
		
		/**
		 * Overridden to disable the functionality by natural framework calls.
		 */
		@Override
		public boolean onTouchEvent(MotionEvent event)
		{
			return false;
		}
		
		/**
		 * Should be called to check if this scroll view intends to accept the provided {@link MotionEvent}.
		 * In case of true, the onTouchEvent should be called manually.
		 *
		 * @param event the {@link MotionEvent} to check
		 * @return true, if event should be processed, else false
		 */
		public boolean checkIfInterceptsTouch(MotionEvent event)
		{
			return super.onInterceptTouchEvent(event);
		}
		
		/**
		 * Should be called to let the {@link MotionEvent} be processed by this scroll view.
		 *  If the onInterceptTouchEvent returned true.
		 *
		 * @param event the {@link MotionEvent} to process
		 */
		public void triggerTouchEvent(MotionEvent event)
		{
			super.onTouchEvent(event);
		}
	}
	
	/**
	 * The scroll views that are used here should not listen to the normal touch events.
	 *  That is why 'onInterceptTouch' and 'onTouch' are overridden to have no function.
	 * Instead two methods have been added to be able to bypass this override and still call the two super methods.
	 * These should be called by the FullScrollLayout to manually distribute the touch events.
	 *
	 * Scrollbar visibility will be disabled.
	 */
	private static class HorizontalScroll extends HorizontalScrollView
	{
		/**
		 * Initializes this scroll view and disables visibility of scroll bars.
		 *
		 * @param context the context which is in charge of the FullScrollLayout
		 */
		public HorizontalScroll(Context context)
		{
			super(context);
			setVerticalScrollBarEnabled(false);
			setHorizontalScrollBarEnabled(false);
		}
		
		/**
		 * Overridden to disable the functionality by natural framework calls.
		 */
		@Override
		public boolean onInterceptTouchEvent(MotionEvent event)
		{
			return false;
		}
		
		/**
		 * Overridden to disable the functionality by natural framework calls.
		 */
		@Override
		public boolean onTouchEvent(MotionEvent event)
		{
			return false;
		}
		
		/**
		 * Should be called to check if this scroll view intends to accept the provided {@link MotionEvent}.
		 * In case of true, the onTouchEvent should be called manually.
		 *
		 * @param event the {@link MotionEvent} to check
		 * @return true, if event should be processed, else false
		 */
		public boolean checkIfInterceptsTouch(MotionEvent event)
		{
			return super.onInterceptTouchEvent(event);
		}
		
		/**
		 * Should be called to let the {@link MotionEvent} be processed by this scroll view.
		 *  If the onInterceptTouchEvent returned true.
		 *
		 * @param event the {@link MotionEvent} to process
		 */
		public void triggerTouchEvent(MotionEvent event)
		{
			super.onTouchEvent(event);
		}
	}
	
	private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
	{
		private Point focus;
		
		@Override
		public boolean onScale(ScaleGestureDetector detector)
		{
			/* TODO still buggy, if sudokuLayout.maxzoom is unrestrained focuspoint appears ~1 cell next to where it is supposed to be. try out by painting focus point in hintpainter */
			if(detector.getScaleFactor() < 0.01)
			{
				return false;
			}
			
			/* compute the new absolute zoomFactor (∈ [1,2]) by multiplying the old one with the scale Factor*/
			float scaleFactor = detector.getScaleFactor();
			float newZoom = zoomFactor * scaleFactor;
			
			// Don't let the object get too large/small.
			float lowerLimit = childView.getMinZoomFactor();
			float upperLimit = childView.getMaxZoomFactor();
			newZoom = Math.max(Math.min(newZoom, upperLimit), lowerLimit);//ensure newZoom ∈ [lowerLim,upperLim]
			
			if(!childView.zoom(newZoom))
			{
				return false;
			}
			
			zoomFactor = newZoom;
			
			/*
			 * NB: we scale in comparison to the case zoom = 1.0, not in comparison to the current one
			 * if we just scale everything on the canvas, fp (focusPoint) and tl (i.e. topleft corner of window) are out of sync.
			 * especially tl is initially 0,0 so it doesn't scale anywhere...
			 *
			 * in the normalized case fp-tl is fp (bec tl==0)
			 * we want that distance to be kept so we subtract it from the scaled value for fp namely fp* zoom.
			 * hence lt = focus * zoom - focus
			 */
			float x = focus.x * zoomFactor - focus.x;
			float y = focus.y * zoomFactor - focus.y;
			scrollTo((int) x, (int) y);
			
			Log.d(LOG_TAG, "onScale() To: " + x + ", " + y + " Focus: " + focus + " Zoom: " + zoomFactor);
			return true;
		}
		
		@Override
		public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector)
		{
			Log.d(LOG_TAG, "onScaleBegin()");
			focus = new Point(scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
			return true;
		}
		
		@Override
		public void onScaleEnd(ScaleGestureDetector scaleGestureDetector)
		{
			Log.d(LOG_TAG, "onScaleEnd()");
		}
	}
	
	private static class Point
	{
		private final float x, y;
		
		public Point(float x, float y)
		{
			this.x = x;
			this.y = y;
		}
		
		@NonNull
		public String toString()
		{
			return (int) x + "," + (int) y;
		}
	}
}

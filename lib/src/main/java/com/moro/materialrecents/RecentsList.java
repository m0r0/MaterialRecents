package com.moro.materialrecents;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.OverScroller;

/**
 * Created by Marcin on 2015-04-13.
 */
public class RecentsList extends FrameLayout implements GestureDetector.OnGestureListener {
  OverScroller scroller;
  RecentsAdapter adapter;
  GestureDetector gestureDetector = new GestureDetector(this);
  int scroll = 0;
  OnItemClickListener onItemClickListener;
  Rect childTouchRect[];

  public interface OnItemClickListener {
    void onItemClick(View view, int position);
  }

  public RecentsList(Context context) {
    super(context);
    initRecentsList();
  }

  public RecentsList(Context context, AttributeSet attrs) {
    super(context, attrs);
    initRecentsList();
  }

  public RecentsList(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initRecentsList();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public RecentsList(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initRecentsList();
  }

  private void initRecentsList() {
    scroller = new OverScroller(getContext());
    setClipChildren(false);
    setClipToPadding(false);
  }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public RecentsAdapter getAdapter() {
    return adapter;
  }

  public void setAdapter(RecentsAdapter adapter) {
    this.adapter = adapter;
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    Log.d("moro", "onLayout");
    if (adapter == null) return;
    if (getChildCount() != adapter.getCount()) {
      initChildren();
    }
    Log.d("moro", "Handling onLayout");
    childTouchRect = new Rect[getChildCount()];
    for (int i = 0; i < getChildCount(); i++) {
      getChildAt(i).layout(0, 0, getWidth() - getPaddingLeft() - getPaddingRight(),
          getHeight() - getPaddingTop() - getPaddingBottom());
      childTouchRect[i] = new Rect();
    }
    scrollAllChildren();
  }

  private void initChildren() {
    removeAllViews();
    for (int i = 0; i < adapter.getCount(); i++) {
      final ViewGroup card = new FrameLayout(getContext());
      View itemContent = adapter.getView(this, i);
      if (!(itemContent instanceof CardView)) {
        throw new IllegalArgumentException("You can only use CardView with " + getClass().getSimpleName());
      }
      card.addView(itemContent);
      addView(card, i);
      final int finalI = i;
      card.setOnClickListener(new OnClickListener() {
        @Override public void onClick(View view) {
          if (onItemClickListener != null) {
            onItemClickListener.onItemClick(card, finalI);
          }
        }
      });
    }
  }

  private int getMaxScroll() {
    return (getChildCount() - 1) * (getWidth() - getPaddingLeft() - getPaddingRight());
  }

  @Override public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
    Log.d("moro", "dispatchTouchEvent");
    if (gestureDetector.onTouchEvent(event)) {
      for (int i = getChildCount() - 1; i >= 0; i--) {
        Log.d("moro", "dispatchTouchEvent, cancel for child " + i);
        MotionEvent e = MotionEvent.obtain(event);
        event.setAction(MotionEvent.ACTION_CANCEL);
        e.offsetLocation(-childTouchRect[i].left, -childTouchRect[i].top);
        getChildAt(i).dispatchTouchEvent(e);
      }
      return true;
    }

    if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
      forceFinished();
    }

    for (int i = getChildCount() - 1; i >= 0; i--) {
      if (childTouchRect[i].contains((int) event.getX(), (int) event.getY())) {
        Log.d("moro", "dispatchTouchEvent, coords contain child " + i);
        MotionEvent e = MotionEvent.obtain(event);
        e.offsetLocation(-childTouchRect[i].left, -childTouchRect[i].top);
        if (getChildAt(i).dispatchTouchEvent(e)) {
          Log.d("moro", "dispatchTouchEvent, child " + i + " consumed the event " + e);
          break;
        } else {
          Log.d("moro", "dispatchTouchEvent, child " + i + " ignored the event " + e);
        }
      }
    }

    return true;
  }

  @Override public boolean onDown(MotionEvent motionEvent) {
    Log.d("moro", "onDown");
    return false;
  }

  @Override public void onShowPress(MotionEvent motionEvent) {

  }

  @Override public boolean onSingleTapUp(MotionEvent event) {
    Log.d("moro", "onSingleTapUp");
    return false;
  }

  @Override
  public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float distanceX, float distanceY) {
    Log.d("moro", "onScroll");
    scroll = (int) Math.max(0, Math.min(scroll + distanceY, getMaxScroll()));
    scrollAllChildren();
    return true;
  }

  @Override public void onLongPress(MotionEvent motionEvent) {

  }

  @Override
  public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float velocityX, float velocityY) {
    Log.d("moro", "onFling");
    startScrolling(-velocityY);
    return true;
  }

  void startScrolling(float initialVelocity) {
    scroller.fling(0, scroll, 0, (int) initialVelocity, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    postInvalidateOnAnimation();
  }

  @Override public void computeScroll() {
    Log.d("moro", "computeScroll");
    if (scroller.computeScrollOffset()) {
      scroll = Math.max(0, Math.min(scroller.getCurrY(), getMaxScroll()));
      scrollAllChildren();
    }
  }

  private void scrollAllChildren() {
    int width = getWidth() - getPaddingLeft() - getPaddingRight();
    int height = getHeight() - getPaddingTop() - getPaddingBottom();
    float topSpace = height - width;
    for (int i = 0; i < getChildCount(); i++) {
      int y = (int) (topSpace * Math.pow(2, (i * width - scroll) / (float) width));
      childTouchRect[i].set( //
          getPaddingLeft(), //
          y + getPaddingTop(), //
          getPaddingLeft() + getWidth() - getPaddingLeft() - getPaddingRight(),
          y + getPaddingTop() + getHeight() - getPaddingTop() - getPaddingBottom());
      getChildAt(i).scrollTo(-getPaddingLeft(), -(y + getPaddingTop()));
    }
  }

  boolean isFlinging() {
    return !scroller.isFinished();
  }

  void forceFinished() {
    if (!scroller.isFinished()) {
      scroller.forceFinished(true);
    }
  }
}

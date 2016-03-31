package com.moro.materialrecents;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
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
  GestureDetector gestureDetector;
  int scroll = 0;
  OnItemClickListener onItemClickListener;

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
    gestureDetector = new GestureDetector(getContext(), this);
    gestureDetector.setIsLongpressEnabled(false);
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
    scrollAllChildren();
  }

  private void initChildren() {
    removeAllViews();
    for (int i = 0; i < adapter.getCount(); i++) {
      final ViewGroup card = new FrameLayout(getContext());
      final View itemContent = adapter.getView(this, i);
      if (!(itemContent instanceof CardView)) {
        throw new IllegalArgumentException("You can only use CardView with " + getClass().getSimpleName());
      }
      card.addView(itemContent);
      addView(card, i);
      final int finalI = i;
      itemContent.setOnClickListener(new OnClickListener() {
        @Override public void onClick(View view) {
          if (onItemClickListener != null) {
            onItemClickListener.onItemClick(itemContent, finalI);
          }
        }
      });
    }
  }

  private int getMaxScroll() {
    return (getChildCount() - 1) * (getWidth() - getPaddingLeft() - getPaddingRight());
  }

  @Override public boolean onInterceptTouchEvent(final MotionEvent event) {
    Log.d("moro", "onInterceptTouchEvent, action:" + event.getAction());
    return gestureDetector.onTouchEvent(event);
  }

  @Override public boolean onTouchEvent(@NonNull MotionEvent event) {
    Log.d("moro", "onTouchEvent, action:" + event.getAction());
    boolean result = event.getAction() == MotionEvent.ACTION_DOWN || gestureDetector.onTouchEvent(event);
    Log.d("moro", "onTouchEvent, returning:" + result);
    return result;
  }

  @Override public boolean onDown(MotionEvent motionEvent) {
    Log.d("moro", "onDown");
    forceFinished();
    ViewCompat.postInvalidateOnAnimation(this);
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
    forceFinished();
    startScrolling(-velocityY * 2);
    return true;
  }

  void startScrolling(float initialVelocity) {
    scroller.fling(0, scroll, 0, (int) initialVelocity, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    ViewCompat.postInvalidateOnAnimation(this);
  }

  @Override public void computeScroll() {
    Log.d("moro", "computeScroll");
    if (scroller.computeScrollOffset()) {
      scroll = Math.max(0, Math.min(scroller.getCurrY(), getMaxScroll()));
      scrollAllChildren();
    }
  }

  private void scrollAllChildren() {
    int width = getWidth();
    int height = getHeight();
    float topSpace = height - width;
    for (int i = 0; i < getChildCount(); i++) {
      int y = (int) (topSpace * Math.pow(2, (i * width - scroll) / (float) width));
      getChildAt(i).scrollTo(0, -(y + getPaddingTop()));
    }
  }

  boolean isFlinging() {
    return !scroller.isFinished();
  }

  void forceFinished() {
    Log.d("moro", "forceFinished");
    if (!scroller.isFinished()) {
      Log.d("moro", "forceFinished, Finishing scroller");
      scroller.forceFinished(true);
    }
  }
}

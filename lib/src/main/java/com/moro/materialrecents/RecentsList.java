package com.moro.materialrecents;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by Marcin on 2015-04-13.
 */
public class RecentsList extends FrameLayout implements GestureDetector.OnGestureListener {
  RecentsAdapter adapter;
  GestureDetector gestureDetector;
  int scroll = 0;
  OnItemClickListener onItemClickListener;
  ValueAnimator scrollBounceAnimator;
  private int actionBarSize;

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
    gestureDetector = new GestureDetector(getContext(), this);
    gestureDetector.setIsLongpressEnabled(false);
    scrollBounceAnimator = ValueAnimator.ofInt(scroll, 0);
    scrollBounceAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(final ValueAnimator animation) {
        scroll = (Integer) animation.getAnimatedValue();
        Log.d("moro", "onAnimationUpdate, scroll=" + scroll);
        scrollAllChildren();
      }
    });
    TypedValue value = new TypedValue();
    if (getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, value, true)) {
      actionBarSize = TypedValue.complexToDimensionPixelSize(value.data, getResources().getDisplayMetrics());
    }
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
      final int finalI = i;
      card.addView(itemContent);
      addView(card, i);
      itemContent.setOnClickListener(new OnClickListener() {
        @Override public void onClick(View view) {
          if (onItemClickListener != null) {
            onItemClickListener.onItemClick(itemContent, finalI);
          }
        }
      });
    }
  }

  @Override public boolean onInterceptTouchEvent(final MotionEvent event) {
    Log.d("moro", "onInterceptTouchEvent, action:" + event.getAction());
    if (adapter == null) return false;
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
      if (scrollBounceAnimator.isRunning()) {
        Log.d("moro", "onTouchEvent, cancelling anim");
        scrollBounceAnimator.cancel();
        return true;
      }
    }
    return gestureDetector.onTouchEvent(event);
  }

  @Override public boolean onTouchEvent(@NonNull MotionEvent event) {
    Log.d("moro", "onTouchEvent, action:" + event.getAction());
    if (adapter == null) return false;
    if (event.getAction() == MotionEvent.ACTION_UP) {
      scrollBounceAnimator.setIntValues(scroll, 0);
      scrollBounceAnimator.start();
    }
    boolean result = event.getAction() == MotionEvent.ACTION_DOWN || gestureDetector.onTouchEvent(event);
    Log.d("moro", "onTouchEvent, returning:" + result);
    return result;
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
    Log.d("moro", "onScroll, distanceY=" + distanceY);
    scroll = (int) Math.min(scroll + distanceY, getMaxScroll());
    Log.d("moro", "onScroll, scroll=" + scroll);
    scrollAllChildren();
    return true;
  }

  @Override public void onLongPress(MotionEvent motionEvent) {

  }

  @Override
  public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float velocityX, float velocityY) {
    Log.d("moro", "onFling");
    return false;
  }

  private void scrollAllChildren() {
    float min = scroll * 0.1f;
    float max = scroll * 0.4f;
    float step = 1 / ((float) adapter.getCount());
    for (int i = 0; i < getChildCount(); i++) {
      float y = min + i * step * (max - min) - (i * getHeight() / adapter.getCount());
      Log.d("moro", "scrollAllChildren, child " + i + ", y=" + (min + i * step * (max - min)) + ", fullY=" + y);
      getChildAt(i).scrollTo(0, (int) (y + getPaddingTop()));
    }
  }

  private int getMaxScroll() {
    return (int) (((getHeight() * adapter.getCount() - 1) / adapter.getCount()
        - actionBarSize
        - getPaddingTop()
        - getPaddingBottom()) / 0.4f);
  }
}

package com.moro.materialrecents;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Marcin on 2015-04-13.
 */
public interface RecentsAdapter {
  View getView(ViewGroup parent, int position);

  int getCount();
}

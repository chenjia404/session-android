package org.thoughtcrime.securesms.util.statelayout;

import android.view.animation.Animation;


public interface ViewAnimProvider {
    Animation showAnimation();

    Animation hideAnimation();
}

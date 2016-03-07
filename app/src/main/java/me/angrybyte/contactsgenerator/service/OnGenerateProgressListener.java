
package me.angrybyte.contactsgenerator.service;

import android.support.annotation.FloatRange;

public interface OnGenerateProgressListener {

    void onGenerateProgress(@FloatRange(from = 0.0f, to = 1.0f) float progress);

}


package me.angrybyte.contactsgenerator.service;

import android.support.annotation.IntRange;

public interface OnGenerateResultListener {

    void onGenerateResult(@IntRange(from = 0) int requested, @IntRange(from = 0) int generated);

}

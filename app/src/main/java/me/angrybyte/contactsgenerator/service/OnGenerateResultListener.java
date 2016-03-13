
package me.angrybyte.contactsgenerator.service;

import android.support.annotation.IntRange;

public interface OnGenerateResultListener {

    /**
     * Callback associated with the final step of generating sequence. It is called on the UI thread when generating completes.
     * 
     * @param requested How many contacts were requested
     * @param generated How many contacts were actually generated
     * @param forced Whether finishing was forced (by manually stopping) or not (finished generator sequence)
     */
    void onGenerateResult(@IntRange(from = 0) int requested, @IntRange(from = 0) int generated, boolean forced);

}

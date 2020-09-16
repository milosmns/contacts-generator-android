
package me.angrybyte.contactsgenerator.service;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;

public interface OnGenerateProgressListener {

    /**
     * Callback associated with the iteration step while generating contacts. Called on UI thread.
     * 
     * @param progress Percentage of job done (you can set this value on the {@link android.widget.ProgressBar})
     * @param iStep Sequential iteration step, this goes from 0 to '{@code howMany}' specified in
     *            {@link ServiceApi#generate(int, boolean, String)}
     * @param generated How many did the generator actually generate, in most cases the same as {@code iStep}
     */
    void onGenerateProgress(@FloatRange(from = 0.0f, to = 1.0f) float progress, @IntRange(from = 0) int iStep,
            @IntRange(from = 0) int generated);

}

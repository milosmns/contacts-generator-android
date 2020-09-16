
package me.angrybyte.contactsgenerator.service;

import androidx.annotation.NonNull;

import me.angrybyte.contactsgenerator.api.GeneratorStats;

public interface OnGenerateResultListener {

    /**
     * Callback associated with the final step of generating sequence. It is called on the UI thread when generating completes.
     * 
     * @param stats Stats object containing a lot of information about the generator operation
     * @param forced Whether finishing was forced (by manually stopping) or not (finished generator sequence)
     */
    void onGenerateResult(@NonNull GeneratorStats stats, boolean forced);

}

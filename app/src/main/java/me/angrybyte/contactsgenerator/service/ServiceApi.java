
package me.angrybyte.contactsgenerator.service;

import android.content.ComponentName;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

import me.angrybyte.contactsgenerator.api.Gender;
import me.angrybyte.contactsgenerator.api.GeneratorStats;
import me.angrybyte.contactsgenerator.api.Operations;
import me.angrybyte.contactsgenerator.parser.data.Person;

/**
 * Public API available to all clients that need to use the contact generating service. The service is not considered initialized until
 * {@link #generate(int, boolean, String)} is invoked for the first time. After that, the service will remain alive until the task
 * completes, which will trigger {@link android.content.ServiceConnection#onServiceDisconnected(ComponentName)}. Before the service gets
 * killed completely, it will invoke the {@link OnGenerateResultListener#onGenerateResult(GeneratorStats, boolean)} on its listener. It will
 * also invoke {@link OnGenerateProgressListener#onGenerateProgress(float, int, int)} along the way as each of the contacts is generated.
 */
public interface ServiceApi {

    String DELETE_CONTACTS_ACTION = "delete";

    /**
     * Checks whether the service is initialized.
     *
     * @return {@code True} if the service has started generating contacts, {@code false} if it has just been created or already finished
     */
    boolean isGenerating();

    /**
     * Checks whether the generator operation is forcefully stopped.
     * 
     * @return {@code True} if the service was forcefully interrupted by user action, {@code false} if still running or finished normally
     */
    boolean isForceStopped();

    /**
     * Starts generating contacts asynchronously. Events {@link OnGenerateProgressListener#onGenerateProgress(float, int, int)} and
     * {@link OnGenerateResultListener#onGenerateResult(GeneratorStats, boolean)} are posted to the main UI thread's message queue along the
     * way. Note that listeners need to be already attached in order to actually start generating contacts. Contacts are saved to the local
     * contact list and are not synchronized with any of device's accounts.
     *
     * @param howMany How many contacts do you need generated
     * @param withPhotos Whether to use contact photos when saving contacts
     * @param gender Which gender to force, one of {@link Operations#MALE}, {@link Operations#FEMALE} or {@link Operations#BOTH}
     * @return {@code True} if contact generating process has started, {@code false} if listeners are not attached or there is a problem
     */
    boolean generate(@IntRange(from = 0) int howMany, boolean withPhotos, @Gender String gender);

    /**
     * If available, returns the last generated contact's object.
     *
     * @return A {@link Person} representing the last generated contact, or {@code null} if none are yet generated or service has died
     */
    @Nullable
    Person getLastGeneratedPerson();

    /**
     * Sets the progress listener for the generating task. This needs to be set prior to calling {@link #generate(int, boolean, String)}.
     *
     * @param listener Listener instance to be able to generate contacts, or {@code null} to stop listening
     */
    void setOnGenerateProgressListener(@Nullable OnGenerateProgressListener listener);

    /**
     * Sets the result listener for the generating task. This needs to be set prior to calling {@link #generate(int, boolean, String)}.
     *
     * @param listener Listener instance to be able to generate contacts, or {@code null} to stop listening
     */
    void setOnGenerateResultListener(@Nullable OnGenerateResultListener listener);

    /**
     * Interrupts the current contact generating sequence. You will still get a
     * {@link OnGenerateResultListener#onGenerateResult(GeneratorStats, boolean)}.
     */
    void stopGenerating();

    /**
     * @return Statistics object containing various analysis data. Can be {@code null}
     */
    @Nullable
    GeneratorStats getStats();

    /**
     * @return {@code true} if the service is currently deleting contacts, {@code false} otherwise
     */
    boolean isDeleting();
}

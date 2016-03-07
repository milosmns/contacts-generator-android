
package me.angrybyte.contactsgenerator.api;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({
        Operations.MALE, Operations.FEMALE, Operations.BOTH
})
public @interface Gender {
}

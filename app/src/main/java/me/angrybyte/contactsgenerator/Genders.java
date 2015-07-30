
package me.angrybyte.contactsgenerator;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({
        RandomApi.MALE, RandomApi.FEMALE, RandomApi.BOTH
})
public @interface Genders {
}

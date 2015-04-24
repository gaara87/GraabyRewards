package graaby.app.wallet.modules;

/**
 * Created by gaara on 1/13/15.
 * Make some impeccable shyte
 */

import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Retention(RUNTIME)
public @interface ForApplication {
}
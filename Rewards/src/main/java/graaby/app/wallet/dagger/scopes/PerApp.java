package graaby.app.wallet.dagger.scopes;

import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Akash.
 */
@Scope
@Retention(RUNTIME)
public @interface PerApp {
}
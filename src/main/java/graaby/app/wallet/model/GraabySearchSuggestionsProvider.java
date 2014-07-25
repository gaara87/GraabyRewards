package graaby.app.wallet.model;

import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by gaara on 7/25/14.
 */
public class GraabySearchSuggestionsProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = GraabySearchSuggestionsProvider.class.getName();
    public final static int MODE = DATABASE_MODE_QUERIES;

    public GraabySearchSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (selectionArgs != null && selectionArgs.length > 0 && selectionArgs[0].length() > 0) {
            // the entered text can be found in selectionArgs[0]
            // return a cursor with appropriate data
        }
        else {
            // user hasn't entered anything
            // thus return a default cursor
            return super.query(uri, projection, selection, selectionArgs, sortOrder);
        }
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }
}

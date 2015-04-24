package graaby.app.wallet.models.retrofit;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by Akash on 4/8/15.
 */
@JsonObject
public class SearchRequest {
    @JsonField(name = "query")
    public String searchQuery;

    @JsonField(name = "collapse")
    public boolean collapseFlag;

    public SearchRequest(String query, boolean collapseFlag) {
        searchQuery = query;
        this.collapseFlag = collapseFlag;
    }

    public SearchRequest() {
    }
}

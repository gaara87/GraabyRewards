package graaby.app.wallet.ui.activities;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.view.MenuItem;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;

import de.greenrobot.event.Subscribe;
import graaby.app.wallet.R;
import graaby.app.wallet.events.ToolbarEvents;
import graaby.app.wallet.models.retrofit.OutletDetail;
import graaby.app.wallet.ui.fragments.BusinessDetailFragment;
import graaby.app.wallet.util.Helper;

public class BusinessDetailsActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_details);
        try {
            OutletDetail outlet = LoganSquare.parse(getIntent().getExtras().getString(
                    Helper.INTENT_CONTAINER_INFO), OutletDetail.class);
            BusinessDetailFragment businessFrag = (BusinessDetailFragment) getSupportFragmentManager().findFragmentByTag("b");
            if (businessFrag == null) {
                businessFrag = BusinessDetailFragment.newInstance(outlet);
                getSupportFragmentManager().beginTransaction().add(businessFrag, "b").commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);
    }

    @Override
    protected void setupInjections() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void handle(ToolbarEvents.SetTitle event) {
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setTitle(event.getName());
        }
    }
}

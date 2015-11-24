package graaby.app.taginitializer;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import graaby.app.taginitializer.fragments.GraabyTagDetailFragment;
import graaby.app.taginitializer.fragments.GraabyTagListFragment;


/**
 * An activity representing a list of GraabyTags. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link GraabyTagDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link graaby.app.taginitializer.fragments.GraabyTagListFragment} and the item details
 * (if present) is a {@link graaby.app.taginitializer.fragments.GraabyTagDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link graaby.app.taginitializer.fragments.GraabyTagListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class GraabyTagListActivity extends Activity
        implements GraabyTagListFragment.Callbacks, ActionBar.OnNavigationListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graabytag_list);

        if (findViewById(R.id.graabytag_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((GraabyTagListFragment) getFragmentManager()
                    .findFragmentById(R.id.graabytag_list))
                    .setActivateOnItemClick(true);
        }

        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.action_list, android.R.layout.simple_spinner_dropdown_item);
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(mSpinnerAdapter, this);

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link GraabyTagListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(Long id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(GraabyTagDetailFragment.ARG_ITEM_ID, id);
            GraabyTagDetailFragment fragment = new GraabyTagDetailFragment();
            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .replace(R.id.graabytag_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, GraabyTagDetailActivity.class);
            detailIntent.putExtra(GraabyTagDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_business:
                Intent intent = new Intent(this, TagWriterActivity.class);
                intent.putExtra("b", true);
                startActivity(intent);
                return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        ((GraabyTagListFragment) getFragmentManager()
                .findFragmentById(R.id.graabytag_list)).onTagListTypeSelected(itemPosition);
        return false;
    }
}

package graaby.app.wallet.ui.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import graaby.app.wallet.R;
import graaby.app.wallet.ui.fragments.BusinessesFragment;
import graaby.app.wallet.ui.fragments.ContactsFragment;
import graaby.app.wallet.ui.fragments.FeedFragment;
import graaby.app.wallet.ui.fragments.MarketFragment;

/**
 * Created by Akash.
 */
public class HomePagerAdapter extends FragmentPagerAdapter {


    private final Context mContext;

    public HomePagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MarketFragment.newInstance(false, -1, true);
            case 1:
                return BusinessesFragment.newInstance();
            case 2:
                return FeedFragment.newInstance();
            case 3:
                return ContactsFragment.newInstance();
            default:
                return MarketFragment.newInstance(false, -1, true);
        }
    }


    public CharSequence getTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.title_marketplace);
            case 1:
                return mContext.getString(R.string.title_businesses);
            case 2:
                return mContext.getString(R.string.title_feed);
            case 3:
                return mContext.getString(R.string.title_contacts);
            default:
                return mContext.getString(R.string.app_name);
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

}

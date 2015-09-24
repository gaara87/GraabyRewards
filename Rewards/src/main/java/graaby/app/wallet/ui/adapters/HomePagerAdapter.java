package graaby.app.wallet.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.ViewGroup;

import graaby.app.wallet.R;
import graaby.app.wallet.ui.fragments.BusinessesFragment;
import graaby.app.wallet.ui.fragments.ContactsFragment;
import graaby.app.wallet.ui.fragments.MarketFragment;
import graaby.app.wallet.ui.fragments.NearbyFragment;

/**
 * Created by Akash.
 */
public class HomePagerAdapter extends FragmentPagerAdapter {
    private final int[][] tabTitleResourceArray = {
            {R.string.title_marketplace, R.drawable.nav_market},
            {R.string.title_nearby, R.drawable.ic_nearby_grey},
            {R.string.title_contacts, R.drawable.nav_contacts},
            {R.string.title_businesses, R.drawable.nav_business}
    };
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
                return NearbyFragment.newInstance();
            case 2:
                return BusinessesFragment.newInstance();
            case 3:
                return ContactsFragment.newInstance();
            default:
                return MarketFragment.newInstance(false, -1, true);
        }
    }

    @StringRes
    public int getTitle(int position) {
        return tabTitleResourceArray[position][0];
    }

    @Override
    public int getCount() {
        return 4;
    }

    public void setupTabIcons(TabLayout tabLayout) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            Drawable drawable;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                drawable = mContext.getResources().getDrawable(tabTitleResourceArray[i][1], mContext.getTheme());
            } else {
                drawable = mContext.getResources().getDrawable(tabTitleResourceArray[i][1]);
            }
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), mContext.getResources().getColor(android.R.color.white));
            tabLayout.getTabAt(i).setIcon(drawable);
        }
    }
}

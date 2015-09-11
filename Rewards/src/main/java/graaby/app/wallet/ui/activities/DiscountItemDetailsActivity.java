package graaby.app.wallet.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.R;
import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.DiscountItemDetailsResponse;
import graaby.app.wallet.network.services.MarketService;
import graaby.app.wallet.ui.fragments.NearbyFragment;
import graaby.app.wallet.util.CacheSubscriber;
import graaby.app.wallet.util.Helper;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DiscountItemDetailsActivity extends BaseAppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.item_businessPicImageView)
    ImageView mItemBusinessPicImageView;
    @Bind(R.id.item_businessNameTextView)
    TextView mItemBusinessNameTextView;
    @Bind(R.id.item_valueTextView)
    TextView mItemValueTextView;
    @Bind(R.id.item_costTextView)
    TextView mItemCostTextView;
    @Bind(R.id.item_id_textView)
    TextView mItemIdTextView;
    @Bind(R.id.item_expiry_textView)
    TextView mItemExpiryTextView;
    @Bind(R.id.item_details_textView)
    TextView mItemDetailsTextView;
    @Bind(R.id.item_terms_textView)
    TextView mItemTermsTextView;
    @Bind(R.id.item_discount_for)
    TextView mItemFor;
    @Bind(R.id.grab_it_button)
    FloatingActionButton mGrabItButton;

    @Inject
    MarketService marketService;
    private Boolean isItemGraabed = Boolean.FALSE;
    private DiscountItemDetailsResponse mDiscountItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount_item);
        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbarLayout.setTitle(getString(R.string.title_activity_discount_item_details));
//        mSwiperefresh.setOnRefreshListener(this);
//        mSwiperefresh.setColorSchemeResources(R.color.sunflower, R.color.nephritis, R.color.peterriver, R.color.pumpkin);

//        mSwiperefresh.setSwipeableChildren(R.id.scrollview);
        try {
            mDiscountItem = LoganSquare.parse(getIntent().getExtras().getString(
                    Helper.INTENT_CONTAINER_INFO), DiscountItemDetailsResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
        if (savedInstanceState != null) {
            isItemGraabed = savedInstanceState.getBoolean("bought");
        } else {
            isItemGraabed = getIntent().getExtras().getBoolean(Helper.MY_DISCOUNT_ITEMS_FLAG);
        }


        getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);
        sendRequest();

        setDetails();

        setupNearbyListener();
    }

    private void setupNearbyListener() {
        NearbyFragment fragment = (NearbyFragment) getSupportFragmentManager().findFragmentByTag("nearby");
        if (fragment == null) {
            fragment = NearbyFragment.newInstance(true);
            getSupportFragmentManager().beginTransaction().add(fragment, "nearby").commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    @Override
    protected void setupInjections() {
        GraabyApplication.getApplication().getApiComponent().inject(this);
    }

    private void sendRequest() {
        Observable<DiscountItemDetailsResponse> observable = null;
        switch (mDiscountItem.typeOfDI) {
            case PUNCH:
            case COUPONS:
                observable = marketService.getCouponDetails(mDiscountItem);
                break;
            case VOUCHERS:
                observable = marketService.getVoucherDetails(mDiscountItem);
                break;
            default:
                break;
        }
        Subscription sub = observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CacheSubscriber<DiscountItemDetailsResponse>(this) {
                    @Override
                    public void onFail(Throwable e) {
                        resetButtonState();
                    }

                    @Override
                    public void onSuccess(DiscountItemDetailsResponse result) {
                        setDiscountItemDetailsFromResponse(result);
                    }
                });

        mCompositeSubscriptions.add(sub);
    }

    private void resetButtonState() {
        if (!isItemGraabed) {
            mGrabItButton.setOnClickListener(this);
            mGrabItButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_shopping_cart_white_24dp));
            mItemFor.setText(getString(R.string.discount_for));
        } else {
            mGrabItButton.setOnClickListener(null);
            mGrabItButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
            mItemFor.setText(getString(R.string.discount_purchased_for));

        }
    }

    private void setDetails() {

        String discountValue = mDiscountItem.discountValue;

        int defaultImageResource, leftDrawable = 0;
        switch (mDiscountItem.typeOfDI) {
            case VOUCHERS:
                defaultImageResource = R.drawable.v_def;
                discountValue = getString(R.string.Rs) + String.format(getString(R.string.discount_item_discount_vale), discountValue);
                mItemValueTextView.setTextAppearance(this, R.style.Base_TextAppearance_AppCompat_Display2);
                break;
            case COUPONS:
                defaultImageResource = R.drawable.c_def;
                mItemValueTextView.setTextAppearance(this, R.style.Base_TextAppearance_AppCompat_Display2);
                break;
            case PUNCH:
                mItemValueTextView.setTextAppearance(this, R.style.Base_TextAppearance_AppCompat_Headline);
                defaultImageResource = R.drawable.p_def;
                leftDrawable = R.drawable.ic_surprise;
                break;
            default:
                defaultImageResource = R.drawable.v_def;
        }
        mItemValueTextView.setText(discountValue);
        mItemValueTextView.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, 0, 0, 0);
        mItemBusinessNameTextView.setText(mDiscountItem.businessName);
        mItemIdTextView.setText(mDiscountItem.discountItemID);
        mItemCostTextView.setText(mDiscountItem.costOfDI);

        resetButtonState();

        Glide.with(this)
                .load(mDiscountItem.pictureURL)
                .placeholder(defaultImageResource)
                .crossFade()
                .into(mItemBusinessPicImageView);

    }

    @Override
    public void onClick(View v) {
        marketService.buyDiscountItem(mDiscountItem)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CacheSubscriber<BaseResponse>(this) {
                               @Override
                               public void onFail(Throwable e) {
                                   Toast.makeText(DiscountItemDetailsActivity.this, "Unable to acquire the item", Toast.LENGTH_SHORT).show();
                                   resetButtonState();
                               }

                               @Override
                               public void onSuccess(BaseResponse result) {
                                   isItemGraabed = ((result.responseSuccessCode == GraabyApplication.getContainerHolder().getContainer().getLong(getString(R.string.gtm_response_success))) ? Boolean.TRUE : Boolean.FALSE);
                                   String msg = "";
                                   if (isItemGraabed) {
                                       msg = GraabyApplication.getContainerHolder().getContainer().getString(getString(R.string.gtm_purchase_success));
                                   } else if (!TextUtils.isEmpty(result.message)) {
                                       msg = result.message;
                                   }
                                   Toast.makeText(DiscountItemDetailsActivity.this, msg,
                                           Toast.LENGTH_LONG).show();

                                   resetButtonState();
                               }
                           }
                );
    }

    @OnClick(R.id.view_outlets)
    public void viewOutletsClick() {
        try {
            Intent intent = new Intent(this, BrandDetailsActivity.class);
            intent.putExtra(Helper.INTENT_CONTAINER_INFO, LoganSquare.serialize(mDiscountItem));
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setDiscountItemDetailsFromResponse(DiscountItemDetailsResponse response) {
        if (!TextUtils.isEmpty(response.expiryDate)) {
            try {
                String unformattedString = response.expiryDate;
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date date = format.parse(unformattedString);
                DateFormat targetFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());

                String text = String.format(
                        getString(R.string.discount_item_expires_on), targetFormat.format(date));
                mItemExpiryTextView.setText(text);
                mItemExpiryTextView.setVisibility(View.VISIBLE);
            } catch (ParseException ignored) {
            }
        }
        if (!TextUtils.isEmpty(response.discountDetails)) {
            mItemDetailsTextView.setText(response.discountDetails);
            mItemDetailsTextView.setVisibility(View.VISIBLE);
        }


        if (!TextUtils.isEmpty(response.discountTermsAndConditions)) {
            mItemTermsTextView.setText(response.discountTermsAndConditions);
            mItemTermsTextView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("bought", isItemGraabed);
    }

    @Override
    public void onRefresh() {
        sendRequest();
    }
}

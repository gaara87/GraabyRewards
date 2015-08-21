package graaby.app.wallet.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import graaby.app.wallet.GraabyApplication;
import graaby.app.wallet.R;
import graaby.app.wallet.gcm.GraabyGCMListenerService;
import graaby.app.wallet.models.retrofit.BaseResponse;
import graaby.app.wallet.models.retrofit.ExtraInfoRequest;
import graaby.app.wallet.network.services.SettingsService;
import graaby.app.wallet.util.CacheSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by gaara on 4/24/15.
 */
public class ExtraInfoActivity extends BaseAppCompatActivity implements CalendarView.OnDateChangeListener, DatePicker.OnDateChangedListener {

    String selectedDate = "";

    @Inject
    SettingsService mService;

    @Bind(R.id.radio_gender_male)
    RadioButton radioGenderMale;
    @Bind(R.id.radio_gender_female)
    RadioButton radioGenderFemale;
    @Bind(R.id.birthday)
    DatePicker birthday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getIntent().getAction().equals(GraabyGCMListenerService.NOTIFICATION_ACTION_INFO)) {
            finish();
        } else {
            setContentView(R.layout.activity_extra_info);
            ButterKnife.bind(this);
            birthday.init(2000, 1, 1, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_item_save:
                onSubmitInfoClicked();
                break;
        }
        return true;
    }

    private void onSubmitInfoClicked() {
        if (!radioGenderMale.isChecked() && !radioGenderFemale.isChecked()) {
            radioGenderMale.setError("Please select your gender");
            radioGenderFemale.setError("Please select your gender");
        } else if (TextUtils.isEmpty(selectedDate)) {
            Toast.makeText(this, "Select your birthday", Toast.LENGTH_SHORT).show();
        } else {
            String gender = radioGenderMale.isChecked() ? "male" : "female";
            mCompositeSubscriptions.add(mService.updateUserInfo(new ExtraInfoRequest(selectedDate, gender))
                    .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.newThread())
                    .subscribe(new CacheSubscriber<BaseResponse>(this) {

                        @Override
                        public void onFail(Throwable e) {
                            Toast.makeText(ExtraInfoActivity.this, "Nevermind, try later!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onSuccess(BaseResponse result) {
                            if (result.responseSuccessCode == GraabyApplication.getContainerHolder().getContainer().getLong(getString(R.string.gtm_response_success)))
                                finishActivityWithSuccess();
                            else
                                Toast.makeText(ExtraInfoActivity.this, result.message, Toast.LENGTH_SHORT).show();
                        }
                    }));
            Toast.makeText(this, "Saving", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
        selectedDate = String.valueOf(dayOfMonth) + "/" + String.valueOf(month + 1) + "/" + String.valueOf(year);
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        selectedDate = String.valueOf(dayOfMonth) + "/" + String.valueOf(monthOfYear + 1) + "/" + String.valueOf(year);
    }

    private void finishActivityWithSuccess() {
        Toast.makeText(this, "Thank you", Toast.LENGTH_SHORT).show();
        finish();
    }
}
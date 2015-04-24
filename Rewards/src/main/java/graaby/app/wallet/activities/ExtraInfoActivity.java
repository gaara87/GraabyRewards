package graaby.app.wallet.activities;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import graaby.app.wallet.R;
import graaby.app.wallet.services.GcmIntentService;
import graaby.app.wallet.util.Helper;

/**
 * Created by gaara on 4/24/15.
 */
public class ExtraInfoActivity extends BaseAppCompatActivity implements CalendarView.OnDateChangeListener, DatePicker.OnDateChangedListener {

    @InjectView(R.id.radio)
    RadioGroup radioGroup;
    @InjectView(R.id.radio_gender_male)
    RadioButton radioGenderMale;
    @InjectView(R.id.radio_gender_female)
    RadioButton radioGenderFemale;
    @InjectView(R.id.birthday)
    DatePicker birthday;

    String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getIntent().getAction().equals(GcmIntentService.NOTIFICATION_ACTION_INFO)) {
            finish();
        } else {
            setContentView(R.layout.activity_extra_info);
            String information = getIntent().getStringExtra(Helper.INTENT_CONTAINER_INFO);
            ButterKnife.inject(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                birthday.getCalendarView().setOnDateChangeListener(this);
            } else {
                birthday.init(2000, 1, 1, this);
            }
        }
    }

    @OnClick(R.id.submit_info_button)
    public void onSubmitInfoClicked() {
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            radioGenderMale.setError("Please select your gender");
            radioGenderFemale.setError("Please select your gender");
        } else if (TextUtils.isEmpty(selectedDate)) {
            Toast.makeText(this, "Select your birthday", Toast.LENGTH_SHORT).show();
        } else {
            String gender = radioGenderMale.isChecked() ? "m" : "f";
            String dob = String.valueOf(birthday.getDayOfMonth()) + "/" + String.valueOf(birthday.getMonth()) + "/" + birthday.getYear();
            //Send request to server
        }
    }

    @Override
    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
        selectedDate = String.valueOf(dayOfMonth) + "/" + String.valueOf(month) + "/" + String.valueOf(year);
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        selectedDate = String.valueOf(dayOfMonth) + "/" + String.valueOf(monthOfYear) + "/" + String.valueOf(year);
    }

    private void finishActivityWithSuccess() {
        Toast.makeText(this, "Thank you", Toast.LENGTH_SHORT).show();
        finish();
    }
}
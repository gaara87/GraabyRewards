package graaby.app.lib.nfc.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import graaby.app.lib.nfc.R;
import graaby.app.lib.nfc.core.GraabyTag;

/**
 * Created by gaara on 9/11/13.
 */
public class GraabyBusinessConfirmationActivity extends Activity {

    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private CountDownTimer timer;
    private TextView triesLeft = null;
    private int triesCount = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType(getString(R.string.nfc_mime_type_business));
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[]{ndef,};

        setContentView(R.layout.activity_confirmation);

        final ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);
        final TextView timerTextView = (TextView) findViewById(R.id.tv_timer);
        triesLeft = (TextView) findViewById(R.id.tv_tries_left);
        triesLeft.setText(String.format(getString(R.string.tries_left), triesCount));
        if (getIntent().getExtras().isEmpty()) {
            findViewById(R.id.summary_layout).setVisibility(View.GONE);
        } else {
            Bundle b = getIntent().getExtras();
            float billed = b.getFloat("b");
            float netTotal = b.getFloat("n");
            float discount = b.getFloat("d");
            ArrayList<String> selectedCouponIDs = (ArrayList<String>) b.get("c");
            String rs = getString(R.string.rupee);
            String tempString = "Used Coupons: \n";
            if (selectedCouponIDs.size() != 0) {
                for (String item : selectedCouponIDs) {
                    tempString += item.replace("^", " - " + rs) + "\n";
                }
            }
            ((TextView) findViewById(R.id.tv_billed_coupons)).setText(tempString);

            tempString = "Used Vouchers: \n";

            ArrayList<String> selectedVoucherIDs = (ArrayList<String>) b.get("v");
            if (selectedVoucherIDs.size() != 0) {
                for (String item : selectedVoucherIDs) {
                    tempString += item.replace("^", " - " + rs) + "\n";
                }
            }
            ((TextView) findViewById(R.id.tv_billed_vouchers)).setText(tempString);

            ((TextView) findViewById(R.id.tv_billed)).setText(String.valueOf(billed));
            ((TextView) findViewById(R.id.tv_total_discount)).setText(String.valueOf(discount));
            ((TextView) findViewById(R.id.tv_net_total)).setText(String.valueOf(netTotal));

        }

        timer = new CountDownTimer(getResources().getInteger(R.integer.authentication_countdown_timer), 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                bar.setProgress((int) millisUntilFinished);
                timerTextView.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                setResult(getResources().getInteger(R.integer.response_code_fail));
                Toast.makeText(GraabyBusinessConfirmationActivity.this, R.string.business_authentication_message_fail, Toast.LENGTH_SHORT).show();
                this.cancel();
                finish();
            }
        }.start();

    }

    @Override
    public void onBackPressed() {
        timer.cancel();
        setResult(getResources().getInteger(R.integer.response_code_fail));
        finish();
        super.onBackPressed();
    }

    public void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    public void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
    }

    public void onNewIntent(Intent intent) {
        int messageResourceStringId;
        if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            if (GraabyTag.authenticateBusinessTag(this, intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES))) {
                Intent data = new Intent();

                try {
                    String billNo = ((EditText) findViewById(R.id.bill_number)).getText().toString();
                    data.putExtra("bill_number", billNo);
                } catch (Exception e) {
                    data.putExtra("bill_number", "");
                }
                setResult(getResources().getInteger(R.integer.response_code_success), data);
                messageResourceStringId = R.string.business_authentication_message_success;
                Toast.makeText(this, messageResourceStringId, Toast.LENGTH_SHORT).show();
                finish();
                timer.cancel();
            } else {
                triesCount--;
            }
        } else {
            triesCount--;
        }

        triesLeft.setText(String.format(getString(R.string.tries_left), triesCount));
        if (triesCount == 0) {
            setResult(getResources().getInteger(R.integer.response_code_fail));
            messageResourceStringId = R.string.business_authentication_message_fail;
            Toast.makeText(this, messageResourceStringId, Toast.LENGTH_SHORT).show();
            finish();
        }

    }


}

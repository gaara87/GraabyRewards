package graaby.app.vendor.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import graaby.app.vendor.R;
import graaby.app.vendor.helpers.CustomNumpadView;

/**
 * Created by gaara on 8/26/13.
 */
public class BillFragment extends Fragment implements TextWatcher, View.OnTouchListener {

    private Button clearBtn = null;
    private EditText billAmountEditText = null;
    private Callbacks callbacks = null;
    private Boolean isItAGiftVoucher = Boolean.FALSE;
    private EditText phoneNumberEditText;
    private Activity mActivity;

    public BillFragment() {

    }

    public static BillFragment newInstance(Boolean isItAGiftVoucher) {
        BillFragment fragment = new BillFragment();
        Bundle b = new Bundle();
        b.putBoolean("gift_voucher_flag", isItAGiftVoucher);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        callbacks = (Callbacks) activity;
        isItAGiftVoucher = getArguments().getBoolean("gift_voucher_flag");
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            Float tempPrice = Float.parseFloat(s.toString());
            callbacks.onBillAmountFixed(tempPrice, Boolean.FALSE);
        } catch (NumberFormatException e) {
            callbacks.onBillAmountFixed(0.0f, Boolean.FALSE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_bill, container, false);
        CustomNumpadView cnv = (CustomNumpadView) rootView
                .findViewById(R.id.numpadViewTx);
        billAmountEditText = (EditText) rootView.findViewById(R.id.et_billAmount);
        cnv.setActionListenerActivity(mActivity);
        billAmountEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9),
                new DigitsKeyListener(Boolean.FALSE, Boolean.TRUE) {
                    int beforeDecimal = 7;
                    int afterDecimal = 2;


                    public CharSequence filter(CharSequence source, int start,
                                               int end, @NonNull Spanned dest, int dstart, int dend) {
                        String temp = billAmountEditText.getText() + source.toString();

                        if (!temp.equals("")) {
                            clearBtn.setVisibility(View.VISIBLE);
                        } else {
                            clearBtn.setVisibility(View.INVISIBLE);
                        }

                        if (temp.equals(".")) {
                            return "0.";
                        } else if (!temp.contains(".")) {
                            // no decimal point placed yet
                            if (temp.length() > beforeDecimal) {
                                return "";
                            }
                        } else {
                            temp = temp.substring(temp.indexOf(".") + 1);
                            if (temp.length() > afterDecimal) {
                                return "";
                            }
                        }

                        return super.filter(source, start, end, dest, dstart,
                                dend);
                    }
                }});


        billAmountEditText.setOnTouchListener(this);

        billAmountEditText.addTextChangedListener(this);

        clearBtn = (Button) rootView.findViewById(R.id.btn_clear_bill_amount);

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btn_clear_bill_amount) {
                    billAmountEditText.setText("");
                    v.setVisibility(View.INVISIBLE);
                }
            }
        });


        Button nextBtn = (Button) rootView.findViewById(R.id.confirm);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isItAGiftVoucher) {
                    if (phoneNumberEditText.getText().length() != 10) {
                        phoneNumberEditText.setError("Please enter all digits");
                        return;
                    } else {
                        callbacks.onRedeemerPhoneNumberFixed(phoneNumberEditText.getText().toString());
                        phoneNumberEditText.setError(null);
                    }

                }
                try {
                    Float tempPrice = Float.parseFloat(billAmountEditText.getText().toString());
                    if (tempPrice == 0.0f) {
                        billAmountEditText.setError("Please enter an amount");
                    } else {
                        billAmountEditText.setError(null);
                        callbacks.onBillAmountFixed(tempPrice, Boolean.TRUE);
                    }
                    isItAGiftVoucher = Boolean.FALSE;
                } catch (NumberFormatException e) {
                    Toast.makeText(mActivity, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (isItAGiftVoucher) {
            phoneNumberEditText = (EditText) rootView.findViewById(R.id.et_phoneNumber);
            phoneNumberEditText.setVisibility(View.VISIBLE);
            phoneNumberEditText.requestFocus();
            phoneNumberEditText.setOnTouchListener(this);
            rootView.findViewById(R.id.btn_checkin).setVisibility(View.INVISIBLE);
        } else {
            rootView.findViewById(R.id.btn_checkin).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callbacks.onCheckInRequest();
                }
            });
        }


        return rootView;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isItAGiftVoucher && v.getId() == R.id.et_billAmount) {
            if (phoneNumberEditText.getText().length() != 10) {
                phoneNumberEditText.setError("Please enter all digits");
            }
        }
        v.requestFocus();
        InputMethodManager imm = (InputMethodManager) mActivity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        return true;
    }

    public interface Callbacks {
        void onBillAmountFixed(Float amount, Boolean changeToNextScreen);

        void onCheckInRequest();

        void onRedeemerPhoneNumberFixed(String phoneNumber);
    }
}

package graaby.app.taginitializer.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import graaby.app.lib.nfc.core.GraabyTag;
import graaby.app.taginitializer.R;

/**
 * Created by gaara on 10/1/13.
 */
public class BusinessTagInitializationFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_business_init, container, Boolean.FALSE);
        return rootView;
    }

    public void onNewIntent(Intent intent) {
        if (intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED) || intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (GraabyTag.writeBusinessTag(getActivity(), tag)) {
                Toast.makeText(getActivity(), "Business Tag Written Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "There was some problem while writing", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

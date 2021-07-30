package com.goboomtown.supportsdk.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.fragment.app.*;

import com.goboomtown.forms.fragment.FormFragment;
import com.goboomtown.supportsdk.BuildConfig;
import com.goboomtown.supportsdk.R;
import com.goboomtown.supportsdk.api.SupportSDK;
import com.goboomtown.supportsdk.view.SupportButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    private static final int    TAPS_TO_ENTER           = 10;
    private static final int    SCAN_REQUEST_CODE       = 312;

    private int     tapsSoFar;
    private boolean isDeveloperMode;

    private FragmentActivity    mActivity;
    private View                mView;
    private TextView            mVersionTextView;
    private LinearLayout        mDevModePanel;
    private TextView            mScanQRCodeTextView;
    private TextView            mPasteJSONConfiguration;
    private EditText            mPastedJSON;
    private TextView            mEnterParameters;
    private TextView            mReturnToDefault;
    private LinearLayout        mPasteJSONConfigurationPanel;
    private LinearLayout        mEnterParametersPanel;
    private Spinner             mServerSpinner;
    private EditText            mIntegrationId;
    private EditText            mApiKey;
    private Button              mPasteCancel;
    private Button              mPasteOk;
    private Button              mEnterCancel;
    private Button              mEnterOk;

    public Context mContext;
    public SupportButton supportButton;
    public SupportSDK supportSDK;

    private JSONObject updatedJSONConfig = null;

    public FormFragment.OnFragmentInteractionListener mListener;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance(int columnCount) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        mActivity = getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_settings, container, false);
        isDeveloperMode = supportSDK.isDeveloperMode();
        mDevModePanel = mView.findViewById(R.id.devModePanel);

        mDevModePanel.setVisibility(supportSDK.isDeveloperMode() ? View.VISIBLE : View.GONE);

        mVersionTextView = mView.findViewById(R.id.versionTextView);
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        mVersionTextView.setText("Support SDK Version " + versionName);
        mVersionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( ++tapsSoFar == TAPS_TO_ENTER ) {
                    changeDeveloperMode();
                }
            }
        });

        mScanQRCodeTextView = mView.findViewById(R.id.scanQRCodeTextView);
        final Fragment frag = this;
        final Activity activity = getActivity();
        mScanQRCodeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePanels();
//                IntentIntegrator integrator = new IntentIntegrator(activity);
                IntentIntegrator integrator = new IntentIntegrator(getActivity()) {
                    @Override
                    protected void startActivityForResult(Intent intent, int code) {
                        SettingsFragment.this.startActivityForResult(intent, SCAN_REQUEST_CODE); // REQUEST_CODE override
                    }
                };
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan a barcode");
//                integrator.setCameraId(0);  // Use a specific camera of the device
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(true);
                integrator.initiateScan();

            }
        });

        mPasteJSONConfigurationPanel = mView.findViewById(R.id.pasteJSONPanel);
        mPasteJSONConfiguration = mView.findViewById(R.id.pasteJSONConfiguration);
        mPasteJSONConfiguration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasteJSON();
            }
        });
        mPastedJSON = mView.findViewById(R.id.pastedJSON);
        mPasteCancel = mView.findViewById(R.id.pasteCancel);
        mPasteCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePanels();
            }
        });
        mPasteOk = mView.findViewById(R.id.pasteOk);
        mPasteOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePanels();
                String jsonString = mPastedJSON.getText().toString();
                try {
                    JSONObject json = new JSONObject(jsonString);
                    updatedJSONConfig = json;
//                    supportSDK.saveJSONConfig(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                    warn(getString(R.string.warn_invalid_json));
                }
            }
        });

        mEnterParametersPanel = mView.findViewById(R.id.enterParametersPanel);
        mEnterParameters = mView.findViewById(R.id.enterParameters);
        mEnterParameters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEnterParameters();
            }
        });
        mServerSpinner = mView.findViewById(R.id.serverSpinner);
        setupSpinner();
        mIntegrationId = mView.findViewById(R.id.integrationId);
        mApiKey = mView.findViewById(R.id.apiKey);
        mEnterCancel = mView.findViewById(R.id.enterCancel);
        mEnterCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePanels();
            }
        });
        mEnterOk = mView.findViewById(R.id.enterOk);
        mEnterOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePanels();
                JSONObject json = new JSONObject();
                try {
                    String key = mServerSpinner.getSelectedItem().toString();
                    String apiHost = SupportSDK.servers.get(key);
                    json.put(SupportSDK.JSON_API_HOST, apiHost);
                    json.put(SupportSDK.JSON_INTEGRATION_ID, mIntegrationId.getText().toString());
                    json.put(SupportSDK.JSON_API_KEY, mApiKey.getText().toString());
                    updatedJSONConfig = json;
//                    supportSDK.saveJSONConfig(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mReturnToDefault = mView.findViewById(R.id.returnToDefault);
        mReturnToDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToDefault();
            }
        });
        return mView;
    }

    private void returnToDefault() {
        supportSDK.resetToDefault();
    }

    private void setupSpinner() {
        Iterator<Map.Entry<String, String>> iterator = SupportSDK.servers.entrySet().iterator();
        ArrayList<String> entries = new ArrayList<>();
//        entries.add(getResources().getString(R.string.text_select_server));
        while ( iterator.hasNext() ) {
            entries.add(iterator.next().getKey());
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
                android.R.layout.simple_spinner_item, entries);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mServerSpinner.setAdapter(dataAdapter);
        int editPickerSelectedItemPos = AdapterView.INVALID_POSITION;
        mServerSpinner.setSelection(editPickerSelectedItemPos == AdapterView.INVALID_POSITION ? 0 : editPickerSelectedItemPos + 1);
    }

    private void showPasteJSON() {
        mPastedJSON.setText("");
        mPasteJSONConfigurationPanel.setVisibility(View.VISIBLE);
        mEnterParametersPanel.setVisibility(View.GONE);
    }

    private void showEnterParameters() {
        mPasteJSONConfigurationPanel.setVisibility(View.GONE);
        mEnterParametersPanel.setVisibility(View.VISIBLE);
    }

    private void hidePanels() {
        dismissKeyboard();
        mPasteJSONConfigurationPanel.setVisibility(View.GONE);
        mEnterParametersPanel.setVisibility(View.GONE);
    }


    private boolean isValidJSON(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void dismissKeyboard() {
        if ( mActivity==null || mActivity.getCurrentFocus()==null ) {
            return;
        }
        InputMethodManager inputManager = (InputMethodManager)
                mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if ( inputManager== null ) {
            return;
        }
        inputManager.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == SCAN_REQUEST_CODE ) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            String barcode = data.getStringExtra("SCAN_RESULT");

            if ( barcode != null ) {
                processBarcode(barcode);
            } else {
                warn(getString(R.string.warn_bad_qr_code));
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processBarcode(String barcode) {
        if ( barcode.startsWith("http") ) {
            try {
                new GetURLContents().execute(this, supportSDK);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            JSONObject json = null;
            try {
                json = new JSONObject(barcode);
                updatedJSONConfig = json;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }
        warn(getString(R.string.warn_bad_qr_code));
    }

    private void changeDeveloperMode() {
        tapsSoFar = 0;
        isDeveloperMode = !isDeveloperMode;
        supportSDK.enableDeveloperMode(isDeveloperMode);
        mDevModePanel.setVisibility(isDeveloperMode ? View.VISIBLE : View.GONE);
    }

    private void warn(String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if ( updatedJSONConfig != null ) {
//            supportSDK.saveJSONConfig(updatedJSONConfig);
            supportSDK.reloadConfiguration(updatedJSONConfig);
        }
    }


    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Uri var1);
    }


    private static class GetURLContents extends AsyncTask<Object, Void, String> {

        SettingsFragment    settingsFragment;
        SupportSDK          supportSDK;

        @Override
        protected String doInBackground(Object... params) {
            settingsFragment    = (SettingsFragment) params[0];
            supportSDK          = (SupportSDK) params[1];
            try {
                StringBuilder sb = new StringBuilder();
                URL url = new URL("http://www.google.com/");

                BufferedReader in;
                in = new BufferedReader(new InputStreamReader(url.openStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    sb.append(inputLine);

                in.close();

                return sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String string) {
            try {
                JSONObject json = new JSONObject(string);
                settingsFragment.updatedJSONConfig = json;
//                supportSDK.saveJSONConfig(json);
            } catch (JSONException e) {
                e.printStackTrace();
                settingsFragment.warn(settingsFragment.getString(R.string.warn_bad_qr_code));
            }
        }

    }


}


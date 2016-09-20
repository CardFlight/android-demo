package com.example.cardflight.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cardflight.R;
import com.example.cardflight.Settings;
import com.example.cardflight.handlers.MyCFAutoConfigHandler;
import com.example.cardflight.handlers.MyCFDeviceHandler;
import com.example.cardflight.handlers.MyUIHandler;
import com.getcardflight.interfaces.CardFlightApiKeyAccountTokenHandler;
import com.getcardflight.interfaces.CardFlightAuthHandler;
import com.getcardflight.interfaces.CardFlightCaptureHandler;
import com.getcardflight.interfaces.CardFlightDecryptHandler;
import com.getcardflight.interfaces.CardFlightPaymentHandler;
import com.getcardflight.interfaces.CardFlightTokenizationHandler;
import com.getcardflight.interfaces.OnCardKeyedListener;
import com.getcardflight.interfaces.OnFieldResetListener;
import com.getcardflight.models.Card;
import com.getcardflight.models.CardFlight;
import com.getcardflight.models.CardFlightError;
import com.getcardflight.models.Charge;
import com.getcardflight.models.Reader;
import com.getcardflight.util.Constants;
import com.getcardflight.util.PermissionUtils;
import com.getcardflight.views.PaymentView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright (c) 2015 CardFlight Inc. All rights reserved.
 */
public class ReaderDemoFragment extends Fragment implements MyUIHandler, View.OnClickListener, OnCardKeyedListener, OnFieldResetListener, CompoundButton.OnCheckedChangeListener {

    private final String TAG = this.getClass().getSimpleName();

    private Context mContext;

    private Button mButtonConnectAudiojack;
    private Button mButtonConnectBluetooth;
    private Button mButtonSwipeCard;
    private Button mButtonProcessPayment;
    private Button mButtonResetFields;
    private Button mButtonTokenize;
    private Button mButtonAuthorize;
    private Button mButtonCapture;
    private Button mButtonAutoConfig;
    private Button mButtonZipCode;
    private Button mButtonVoid;
    private Button mButtonRefund;
    private Button mButtonDecrypt;
    private Button mButtonSerialize;
    private TextView mTextReaderStatus;
    private TextView mTextCardType;
    private TextView mTextFirstSix;
    private TextView mTextLastFour;
    private TextView mTextChargeToken;
    private TextView mTextAmount;
    private TextView mTextCaptured;
    private TextView mTextVoided;
    private TextView mTextRefunded;
    private TextView mTextZipCode;
    private TextView mTextName;
    private CheckBox mCheckboxZipCode;
    private PaymentView mPaymentView;

    private Snackbar mSnackbar;
    private Card mCard = null;
    private Charge mCharge = null;
    private boolean mReaderIsConnected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        setRetainInstance(true);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        PermissionUtils.requestPermission(
                mContext,
                this,
                getView(),
                new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                "Need microphone and storage access for demo",
                1
        );

        // Instantiate CardFlight Instance
        CardFlight.getInstance()
                .setApiTokenAndAccountToken(mContext, Settings.API_TOKEN, Settings.ACCOUNT_TOKEN, new CardFlightApiKeyAccountTokenHandler() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "API Key and Account Token set!");
                    }

                    @Override
                    public void onFailed(CardFlightError cardFlightError) {
                        Log.e(TAG, "API Key and Account Token failed to set!");
                    }
                });

        CardFlight.getInstance()
                .setLogging(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.reader_demo_fragment, container, false);

        mButtonConnectAudiojack = (Button) rootView.findViewById(R.id.connectAudiojackReaderButton);
        mButtonConnectBluetooth = (Button) rootView.findViewById(R.id.connectBluetoothReaderButton);
        mButtonSwipeCard = (Button) rootView.findViewById(R.id.swipeCardButton);
        mButtonProcessPayment = (Button) rootView.findViewById(R.id.processPaymentButton);
        mButtonTokenize = (Button) rootView.findViewById(R.id.tokenizeCard);
        mButtonResetFields = (Button) rootView.findViewById(R.id.resetFieldsButton);
        mButtonAuthorize = (Button) rootView.findViewById(R.id.authorizeCard);
        mButtonCapture = (Button) rootView.findViewById(R.id.processCapture);
        mButtonDecrypt = (Button) rootView.findViewById(R.id.decryptButton);
        mButtonAutoConfig = (Button) rootView.findViewById(R.id.autoConfigButton);
        mButtonZipCode = (Button) rootView.findViewById(R.id.fetchZipCodeButton);
        mButtonVoid = (Button) rootView.findViewById(R.id.voidCard);
        mButtonRefund = (Button) rootView.findViewById(R.id.refundCard);
        mButtonSerialize = (Button) rootView.findViewById(R.id.displaySerialized);

        mTextReaderStatus = (TextView) rootView.findViewById(R.id.reader_status);
        mTextName = (TextView) rootView.findViewById(R.id.cardHolderName);
        mTextFirstSix = (TextView) rootView.findViewById(R.id.card_first_six);
        mTextCardType = (TextView) rootView.findViewById(R.id.card_type);
        mTextLastFour = (TextView) rootView.findViewById(R.id.card_last_four);
        mTextChargeToken = (TextView) rootView.findViewById(R.id.charge_token);
        mTextAmount = (TextView) rootView.findViewById(R.id.charge_amount);
        mTextCaptured = (TextView) rootView.findViewById(R.id.charge_captured);
        mTextVoided = (TextView) rootView.findViewById(R.id.charge_voided);
        mTextRefunded = (TextView) rootView.findViewById(R.id.charge_refund);
        mTextZipCode = (TextView) rootView.findViewById(R.id.zip_code_field);

        mPaymentView = (PaymentView) rootView.findViewById(R.id.cardEditText);

        mCheckboxZipCode = (CheckBox) rootView.findViewById(R.id.zip_code_switch);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mButtonConnectAudiojack.setOnClickListener(this);
        mButtonConnectBluetooth.setOnClickListener(this);
        mButtonSwipeCard.setOnClickListener(this);
        mButtonProcessPayment.setOnClickListener(this);
        mButtonTokenize.setOnClickListener(this);
        mButtonResetFields.setOnClickListener(this);
        mButtonCapture.setOnClickListener(this);
        mButtonAuthorize.setOnClickListener(this);
        mButtonAutoConfig.setOnClickListener(this);
        mButtonZipCode.setOnClickListener(this);
        mButtonVoid.setOnClickListener(this);
        mButtonRefund.setOnClickListener(this);
        mButtonDecrypt.setOnClickListener(this);
        mButtonSerialize.setOnClickListener(this);

        mPaymentView.setOnCardKeyedListener(this);
        mPaymentView.setOnFieldResetListener(this);
        mCheckboxZipCode.setOnCheckedChangeListener(this);

        enableZipCode(true);

        if (mReaderIsConnected) {
            updateReaderStatus("Reader connected", true);
        } else {
            updateReaderStatus("Reader disconnected", false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Reader.tearDown();
    }


    /***********************************************************************************************
     * MyUIHandler implementation
     **********************************************************************************************/

    @Override
    public void updateReaderStatus(MyCFDeviceHandler.ReaderStatus status) {
        switch (status) {
            case CONNECTED:
                updateReaderStatus("Reader connected", true);
                break;

            case CONNECTING:
                updateReaderStatus("Reader connecting", false);
                break;

            case UPDATING:
                updateReaderStatus("Reader updating", true);
                break;

            case DISCONNECTED:
                updateReaderStatus("Reader disconnected", false);
                break;

            case UNKNOWN:
                updateReaderStatus("Unknown error", false);

                mButtonAutoConfig.setEnabled(true);
                break;

            case NOT_COMPATIBLE:
                updateReaderStatus("Device not compatible", false);

                mButtonAutoConfig.setEnabled(true);
                break;
        }
    }

    @Override
    public void showAlert(String message) {
        if (mSnackbar != null) {
            mSnackbar.dismiss();
            mSnackbar = null;
        }

        if (getView() != null) {
            mSnackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_INDEFINITE);

            View view = mSnackbar.getView();
            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(Color.WHITE);

            mSnackbar.setAction("Dismiss", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSnackbar.dismiss();
                }
            });

            mSnackbar.setActionTextColor(ContextCompat.getColor(mContext, R.color.cardflight_blue));

            mSnackbar.show();
        }
    }

    @Override
    public void showConfirmCharge(String prompt) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);

        dialogBuilder.setTitle("Confirm Transaction");
        dialogBuilder.setMessage(prompt);

        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Reader.getDefault(mContext).emvProcessTransaction(true);

                dialog.dismiss();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Reader.getDefault(mContext).emvProcessTransaction(false);

                dialog.dismiss();
            }
        });

        dialogBuilder.create().show();
    }

    @Override
    public void setCard(Card card) {
        mCard = card;

        setCardPresent();

        mButtonSwipeCard.setEnabled(true);
    }

    @Override
    public void showBluetoothDevices(ArrayList<BluetoothDevice> devices) {
        CharSequence[] items = new CharSequence[devices.size()];

        int index = 0;
        for (BluetoothDevice device : devices) {
            items[index++] = device.getName();
        }

        new AlertDialog.Builder(mContext)
                .setCancelable(false)
                .setTitle("Select Bluetooth Device")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Reader.getDefault(mContext)
                                .selectBluetoothDevice(which);
                    }
                }).create()
                .show();
    }


    /***********************************************************************************************
     * View.OnClickListener implementation
     **********************************************************************************************/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connectAudiojackReaderButton:
                Reader.getDefault(mContext)
                        .setDeviceHandler(new MyCFDeviceHandler(this))
                        .setAutoConfigHandler(new MyCFAutoConfigHandler(this));
                break;

            case R.id.connectBluetoothReaderButton:
                Reader.getDefault(mContext)
                        .setDeviceHandler(new MyCFDeviceHandler(this))
                        .setAutoConfigHandler(new MyCFAutoConfigHandler(this))
                        .setIsBluetoothReader(true);
                break;

            case R.id.swipeCardButton:
                launchSwipeEvent();
                break;

            case R.id.processPaymentButton:
                makeChargeDialog().show();
                break;

            case R.id.tokenizeCard:
                tokenizeCardMethod();
                break;

            case R.id.resetFieldsButton:
                // Call this to reset the fields.
                // Attach a #OnFieldResetListener to capture when fields have reset.
                mPaymentView.resetFields();
                break;

            case R.id.authorizeCard:
                makeAuthorizeDialog().show();
                break;

            case R.id.processCapture:
                captureCharge();
                break;

            case R.id.autoConfigButton:
                Reader.getDefault(mContext)
                        .startAutoConfigProcess();
                break;

            case R.id.refundCard:
                makeRefundDialog().show();
                break;

            case R.id.voidCard:
                voidCharge();
                break;

            case R.id.fetchZipCodeButton:
                if (mCard != null) {
                    showAlert(String.format("Zip Code: %s", mCard.getZipCode()));
                } else {
                    showAlert("No card is present");
                }
                break;

            case R.id.decryptButton:
                decryptCardMethod();
                break;

            case R.id.displaySerialized:
                if (mCard != null) {
                    serializeCard(mCard);
                    Log.d(TAG, "file contents = " + readFile("card.bin"));
                    Card newcard = unserializeCard("card.bin");

                    if (newcard != null) {
                        showAlert("Check debug logs for details...");
                        Log.d(TAG, "newcard = " + newcard.getCardType() + " " + newcard.getLast4());
                    }
                } else {
                    showAlert("Card not entered!");
                }
                break;
        }
    }


    /***********************************************************************************************
     * OnCardKeyedListener implementation
     **********************************************************************************************/

    @Override
    public void onCardKeyed(Card card) {
        if (card != null) {
            mCard = card;
            fillFieldsWithData(mCard);
        }
    }


    /***********************************************************************************************
     * OnFieldResetListener implementation
     **********************************************************************************************/

    @Override
    public void onFieldReset() {
        fieldsReset();
    }


    /***********************************************************************************************
     * CompoundButton.OnCheckedChangeListener implementation
     **********************************************************************************************/

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        enableZipCode(isChecked);
    }


    /***********************************************************************************************
     * Private methods
     **********************************************************************************************/

    private void serializeCard(Card card) {
        File file = new File(mContext.getFilesDir().getPath() + "/card.bin");

        FileOutputStream fos;
        ObjectOutputStream oos;

        try {
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(card);

            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Card unserializeCard(String filename) {
        Card card = null;

        FileInputStream fis;
        ObjectInputStream ois;

        try {
            fis = new FileInputStream(mContext.getFilesDir().getPath() + "/" + filename);
            ois = new ObjectInputStream(fis);

            card = (Card) ois.readObject();
            ois.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        return card;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String readFile(String filename) {
        String contents = "";

        try (BufferedReader br = new BufferedReader(new FileReader(mContext.getFilesDir().getPath()
                + "/" + filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                contents += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contents;
    }

    private void enableZipCode(boolean enable) {
        mPaymentView.enableZipCode(enable);
        mCheckboxZipCode.setChecked(enable);
        if (enable) {
            mButtonZipCode.setVisibility(View.VISIBLE);
        } else {
            mButtonZipCode.setVisibility(View.GONE);
        }
    }

    private void launchSwipeEvent() {
        Reader.getDefault(mContext)
                .beginSwipe();
        mPaymentView.resetFields();
    }

    private void tokenizeCardMethod() {
        showAlert("Tokenize card...");
        if (mCard != null) {
            mCard.tokenize(
                    mContext,
                    new CardFlightTokenizationHandler() {
                        @Override
                        public void tokenizationSuccessful(String s) {
                            Log.d(TAG, "Tokenization Successful");
                            showAlert(s);
                        }

                        @Override
                        public void tokenizationFailed(CardFlightError error) {
                            Log.e(TAG, "error: " + error.toString());
                            showAlert(error.getMessage());
                        }
                    }
            );
        } else {
            showAlert("Unable to tokenize - no card present");
        }
    }

    private void decryptCardMethod() {
        if (mCard != null) {

            mCard.decryptPrivateLabel(mContext, new CardFlightDecryptHandler() {

                @Override
                public void decryptSuccess(HashMap decryptData) {
                    Toast.makeText(mContext,
                            "Decrypt completed: " + decryptData.toString(), Toast.LENGTH_LONG)
                            .show();
                }

                @Override
                public void decryptFailed(CardFlightError error) {
                    showAlert("Decrypt failed: " + error.getMessage());
                }
            });
        } else {
            showAlert("No card is present");
        }
    }

    private void authorizeCard(int price) {
        showAlert("Authorizing card...");
        HashMap<String, Integer> chargeDetailsHash = new HashMap<>();
        chargeDetailsHash.put(Constants.REQUEST_KEY_AMOUNT, price);

        if (mCard != null) {
            mCard.authorize(
                    mContext,
                    chargeDetailsHash,
                    new CardFlightAuthHandler() {
                        @Override
                        public void authValid(Charge charge) {
                            Log.d(TAG, "Card authorize valid");
                            showAlert("Card authorized");
                            mCharge = charge;
                            chargePresent();
                            chargeUpdated();

                            HashMap newMap = charge.getMetadata();
                            showAlert("metadata: " + newMap.get("Test"));
                        }

                        @Override
                        public void authFailed(CardFlightError error) {
                            Log.e(TAG, "error: " + error.toString());
                            showAlert(error.getMessage());
                        }
                    }
            );
        } else {
            showAlert("Unable to tokenize- no card present");
        }
    }

    private void captureCharge() {
        showAlert("Capturing charge...");
        if (mCharge != null) {
            Charge.processCapture(mCharge.getToken(), mCharge.getAmount().doubleValue(), new CardFlightCaptureHandler() {

                @Override
                public void captureSuccessful(Charge charge) {
                    showAlert(String.format("Capture of $%s successful", charge.getAmount()));
                    mCharge = charge;
                    chargeUpdated();
                }

                @Override
                public void captureFailed(CardFlightError error) {
                    Log.e(TAG, "error: " + error.toString());
                    showAlert(error.getMessage());
                }
            });
        } else {
            showAlert("Unable to capture charge");
        }
    }

    private void chargeCard(int price) {
        Log.d(TAG, "Processing payment of: " + price);

        JSONObject metadata = new JSONObject();

        try {
            metadata.put("example_data", "123456789");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HashMap<String, Object> chargeDetailsHash = new HashMap<>();

        chargeDetailsHash.put(Constants.REQUEST_KEY_AMOUNT, price);
        chargeDetailsHash.put(Constants.REQUEST_KEY_META_DATA, metadata);

        if (mCard != null) {
            mCard.chargeCard(
                    mContext,
                    chargeDetailsHash,
                    new CardFlightPaymentHandler() {
                        @Override
                        public void transactionSuccessful(Charge charge) {
                            showAlert(String.format("Charge of $%s successful", charge.getAmount()));

                            // Save charge object
                            mCharge = charge;
                            chargePresent();
                            chargeUpdated();
                        }

                        @Override
                        public void transactionFailed(CardFlightError error) {
                            Log.e(TAG, "error: " + error.toString());
                            showAlert(error.toString());
                        }
                    }
            );
        } else {
            showAlert("Unable to process payment - no card present");
        }
    }

    private void voidCharge() {
        showAlert("Voiding charge...");
        if (mCharge != null) {
            Charge.processVoid(
                    mCharge.getToken(),
                    new CardFlightPaymentHandler() {
                        @Override
                        public void transactionSuccessful(Charge charge) {
                            showAlert("Charge voided");
                            mCharge = charge;
                            chargeUpdated();
                        }

                        @Override
                        public void transactionFailed(CardFlightError error) {
                            Log.e(TAG, "error: " + error.toString());
                            showAlert(error.toString());
                        }
                    }
            );
        } else {
            showAlert("Unable to void charge");
        }
    }

    private void refundCharge(double refund) {
        showAlert("Refunding charge...");
        if (mCharge != null) {
            Charge.processRefund(
                    mCharge.getToken(),
                    refund,
                    new CardFlightPaymentHandler() {
                        @Override
                        public void transactionSuccessful(Charge charge) {
                            showAlert(String.format("%s refunded to charge", mCharge.getAmountRefunded()));
                            mCharge = charge;
                            chargeUpdated();
                        }

                        @Override
                        public void transactionFailed(CardFlightError error) {
                            showAlert(error.toString());
                        }
                    }
            );
        } else {
            showAlert("Unable to refund charge");
        }
    }

    private void updateReaderStatus(String message, boolean isConnected) {
        mReaderIsConnected = isConnected;
        mTextReaderStatus.setText(message);
        mButtonSwipeCard.setEnabled(isConnected);
        mButtonAutoConfig.setEnabled(isConnected);

        if (!isConnected) {
            fieldsReset();
        }
    }

    private void chargePresent() {
        mButtonCapture.setEnabled(true);
        mTextChargeToken.setText(mCharge.getToken());
        mTextAmount.setText(String.format("$%s", mCharge.getAmount().toString()));
    }

    private void chargeUpdated() {
        mTextCaptured.setText(String.valueOf(mCharge.isCaptured()));
        mTextVoided.setText(String.valueOf(mCharge.isVoided()));
        mTextRefunded.setText(String.format("%s | $%s", mCharge.isRefunded(),
                mCharge.isRefunded() ? mCharge.getAmountRefunded().toString() : "-.--"));

        if (mCharge.isCaptured() && !mCharge.isVoided() && !mCharge.isRefunded()) {
            mButtonProcessPayment.setEnabled(false);
            mButtonCapture.setEnabled(false);
            mButtonAuthorize.setEnabled(false);
            mButtonVoid.setEnabled(true);
            mButtonRefund.setEnabled(true);
        } else if (mCharge.isRefunded() || mCharge.isVoided()) {
            mButtonProcessPayment.setEnabled(false);
            mButtonCapture.setEnabled(false);
            mButtonAuthorize.setEnabled(false);
            mButtonVoid.setEnabled(false);
            mButtonRefund.setEnabled(false);
        } else {
            mButtonVoid.setEnabled(false);
            mButtonRefund.setEnabled(false);
            mButtonProcessPayment.setEnabled(true);
            mButtonCapture.setEnabled(true);
            mButtonAuthorize.setEnabled(true);
        }
    }

    private void chargeCleared() {
        mButtonCapture.setEnabled(false);
        mButtonVoid.setEnabled(false);
        mButtonRefund.setEnabled(false);

        mTextChargeToken.setText("---");
        mTextAmount.setText("$-.--");

        mTextCaptured.setText("---");
        mTextVoided.setText("---");
        mTextRefunded.setText("---");
    }

    private void setCardPresent() {
        if (mCard == null) {
            mTextName.setText("");
            mTextCardType.setText("");
            mTextFirstSix.setText("");
            mTextLastFour.setText("");
            mTextZipCode.setText("");
        } else {
            mTextName.setText(mCard.getName());
            mTextCardType.setText(mCard.getType());
            mTextFirstSix.setText(mCard.getFirst6());
            mTextLastFour.setText(mCard.getLast4());
            mTextZipCode.setText(mCard.getZipCode());
        }

        mButtonProcessPayment.setEnabled(true);
        mButtonTokenize.setEnabled(true);
        mButtonDecrypt.setEnabled(true);
        mButtonAuthorize.setEnabled(true);
    }

    private void fieldsReset() {
        mCard = null;
        mTextFirstSix.setText("----");
        mTextLastFour.setText("----");
        mTextCardType.setText("----");
        mTextName.setText("----");

        mButtonProcessPayment.setEnabled(false);
        mButtonTokenize.setEnabled(false);
        mButtonDecrypt.setEnabled(false);
        mButtonAuthorize.setEnabled(false);
        chargeCleared();
    }

    private void fillFieldsWithData(Card cardData) {
        mCard = cardData;
        setCardPresent();
    }

    private Dialog makeChargeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View editView = View.inflate(mContext, R.layout.payment_dialog, null);
        final EditText priceInput = (EditText) editView.findViewById(R.id.price_field);
        priceInput.setRawInputType(InputType.TYPE_CLASS_NUMBER);

        builder.setTitle("Process Charge");
        builder.setMessage("Enter a test price to charge.").setCancelable(false);

        String dialogNegativeText = "Cancel";
        String dialogPositiveText = "Charge";

        builder.setNegativeButton(dialogNegativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton(dialogPositiveText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                String price = priceInput.getText().toString();

                if (TextUtils.isEmpty(price)) {
                    showAlert("Price cannot be empty");
                } else {
                    chargeCard((int) (Double.valueOf(price) * 100));
                }
            }
        });

        builder.setView(editView);

        return builder.create();
    }

    private Dialog makeAuthorizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View editView = View.inflate(mContext, R.layout.payment_dialog, null);
        final EditText priceInput = (EditText) editView.findViewById(R.id.price_field);
        priceInput.setRawInputType(InputType.TYPE_CLASS_NUMBER);

        builder.setTitle("Authorize Card");
        builder.setMessage("Enter a test price to authorize.").setCancelable(false);

        String dialogNegativeText = "Cancel";
        String dialogPositiveText = "Authorize";


        builder.setNegativeButton(dialogNegativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton(dialogPositiveText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                String price = priceInput.getText().toString();
                int amount = Integer.valueOf(price);

                authorizeCard(amount);
            }
        });

        builder.setView(editView);

        return builder.create();
    }

    private Dialog makeRefundDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View editView = View.inflate(mContext, R.layout.payment_dialog, null);
        final EditText priceInput = (EditText) editView.findViewById(R.id.price_field);
        priceInput.setRawInputType(InputType.TYPE_CLASS_NUMBER);

        builder.setTitle("Refund charge");
        builder.setMessage("Enter a test price to refund.").setCancelable(false);

        String dialogNegativeText = "Cancel";
        String dialogPositiveText = "Refund";


        builder.setNegativeButton(dialogNegativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton(dialogPositiveText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                String price = priceInput.getText().toString();
                double amount = Double.valueOf(price);

                refundCharge(amount);
            }
        });

        builder.setView(editView);

        return builder.create();
    }
}

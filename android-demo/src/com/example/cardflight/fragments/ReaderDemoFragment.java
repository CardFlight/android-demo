package com.example.cardflight.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
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
import java.util.HashMap;

/**
 * Copyright (c) 2015 CardFlight Inc. All rights reserved.
 */
public class ReaderDemoFragment extends Fragment implements MyUIHandler {

    private final String TAG = this.getClass().getSimpleName();

    private Context mContext;

    private Button swipeCardButton;
    private Button processPaymentButton;
    private Button resetFieldsButton;
    private Button displaySerialButton;
    private Button tokenizeCardButton;
    private Button authorizeCardButton;
    private Button captureChargeButton;
    private Button autoConfigButton;
    private Button zipCodeButton;
    private Button voidButton;
    private Button refundButton;
    private Button decryptButton;
    private Button serializeButton;
    private TextView readerStatus;
    private TextView cardNumber;
    private TextView cardType;
    private TextView cardLastFour;
    private TextView chargeToken;
    private TextView chargeAmount;
    private TextView chargeCaptured;
    private TextView chargeVoided;
    private TextView chargeRefunded;
    private TextView zipCode;
    private TextView cardHolderName;
    private CheckBox zipCodeEnabled;
    private Card mCard = null;
    private Charge mCharge = null;
    private OnCardKeyedListener onCardKeyedListener;
    private OnFieldResetListener onFieldResetListener;
    private PaymentView mFieldHolder;

    private Snackbar alert;
    private boolean readerIsConnected;

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
                .setApiTokenAndAccountToken(Settings.API_TOKEN, Settings.ACCOUNT_TOKEN);
        CardFlight.getInstance()
                .setLogging(true);

        // Instantiate Reader Instance
        Reader.getDefault(mContext)
                .setDeviceHandler(new MyCFDeviceHandler(this))
                .setAutoConfigHandler(new MyCFAutoConfigHandler(this));

        // Create the listener that listens to when the PaymentView has been filled out manually
        onCardKeyedListener = new OnCardKeyedListener() {

            @Override
            public void onCardKeyed(Card card) {
                if (card != null) {
                    mCard = card;
                    fillFieldsWithData(mCard);
                }
            }
        };

        // Create the listener that listens to when the PaymentView has been cleared and reset.
        // NOTE: This is not necessary and should be used to simply clear out any variables set.
        onFieldResetListener = new OnFieldResetListener() {
            @Override
            public void onFieldReset() {
                fieldsReset();
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.reader_demo_fragment, container, false);

        mFieldHolder = (PaymentView) rootView.findViewById(R.id.cardEditText);
        // Set the CardKeyedListener and FieldResetListener here
        mFieldHolder.setOnCardKeyedListener(onCardKeyedListener);
        mFieldHolder.setOnFieldResetListener(onFieldResetListener);

        swipeCardButton = (Button) rootView.findViewById(R.id.swipeCardButton);
        processPaymentButton = (Button) rootView.findViewById(R.id.processPaymentButton);
        displaySerialButton = (Button) rootView.findViewById(R.id.serialNumber);
        tokenizeCardButton = (Button) rootView.findViewById(R.id.tokenizeCard);
        resetFieldsButton = (Button) rootView.findViewById(R.id.resetFieldsButton);
        authorizeCardButton = (Button) rootView.findViewById(R.id.authorizeCard);
        captureChargeButton = (Button) rootView.findViewById(R.id.processCapture);
        decryptButton = (Button) rootView.findViewById(R.id.decryptButton);
        autoConfigButton = (Button) rootView.findViewById(R.id.autoConfigButton);
        zipCodeButton = (Button) rootView.findViewById(R.id.fetchZipCodeButton);
        voidButton = (Button) rootView.findViewById(R.id.voidCard);
        refundButton = (Button) rootView.findViewById(R.id.refundCard);
        serializeButton = (Button) rootView.findViewById(R.id.displaySerialized);
        readerStatus = (TextView) rootView.findViewById(R.id.reader_status);
        cardHolderName = (TextView) rootView.findViewById(R.id.cardHolderName);

        cardNumber = (TextView) rootView.findViewById(R.id.card_number);
        cardType = (TextView) rootView.findViewById(R.id.card_type);
        cardLastFour = (TextView) rootView.findViewById(R.id.card_last_four);
        chargeToken = (TextView) rootView.findViewById(R.id.charge_token);
        chargeAmount = (TextView) rootView.findViewById(R.id.charge_amount);
        chargeCaptured = (TextView) rootView.findViewById(R.id.charge_captured);
        chargeVoided = (TextView) rootView.findViewById(R.id.charge_voided);
        chargeRefunded = (TextView) rootView.findViewById(R.id.charge_refund);
        zipCode = (TextView) rootView.findViewById(R.id.zip_code_field);

        zipCodeEnabled = (CheckBox) rootView.findViewById(R.id.zip_code_switch);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        zipCodeEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableZipCode(isChecked);
            }
        });
        enableZipCode(true);

        swipeCardButton.setOnClickListener(buttonClickListener);
        processPaymentButton.setOnClickListener(buttonClickListener);
        displaySerialButton.setOnClickListener(buttonClickListener);
        tokenizeCardButton.setOnClickListener(buttonClickListener);
        resetFieldsButton.setOnClickListener(buttonClickListener);
        captureChargeButton.setOnClickListener(buttonClickListener);
        authorizeCardButton.setOnClickListener(buttonClickListener);
        autoConfigButton.setOnClickListener(buttonClickListener);
        zipCodeButton.setOnClickListener(buttonClickListener);
        voidButton.setOnClickListener(buttonClickListener);
        refundButton.setOnClickListener(buttonClickListener);
        decryptButton.setOnClickListener(buttonClickListener);
        serializeButton.setOnClickListener(buttonClickListener);

        if (readerIsConnected) {
            updateReaderStatus("Reader connected", true);
        } else {
            updateReaderStatus("Reader disconnected", false);
        }
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DialogFragment dialogFragment;

            switch (v.getId()) {
                case R.id.swipeCardButton:
                    launchSwipeEvent();
                    break;
                case R.id.processPaymentButton:
                    dialogFragment = new DialogFragment() {
                        @Override
                        public void onCreate(Bundle savedInstanceState) {
                            super.onCreate(savedInstanceState);

                            setRetainInstance(true);
                        }

                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {
                            return makeChargeDialog();
                        }
                    };
                    dialogFragment.setRetainInstance(true);
                    dialogFragment.setCancelable(false);
                    dialogFragment.show(getFragmentManager(), "dialogFragment");
                    break;
                case R.id.serialNumber:
                    displaySerialNumber();
                    break;
                case R.id.tokenizeCard:
                    tokenizeCardMethod();
                    break;
                case R.id.resetFieldsButton:
                    // Call this to reset the fields.
                    // Attach a #OnFieldResetListener to capture when fields have reset.
                    mFieldHolder.resetFields();
                    break;
                case R.id.authorizeCard:
                    dialogFragment = new DialogFragment() {
                        @Override
                        public void onCreate(Bundle savedInstanceState) {
                            super.onCreate(savedInstanceState);

                            setRetainInstance(true);
                        }

                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {
                            return makeAuthorizeDialog();
                        }
                    };
                    dialogFragment.setRetainInstance(true);
                    dialogFragment.setCancelable(false);
                    dialogFragment.show(getFragmentManager(), "dialogFragment");
                    break;
                case R.id.processCapture:
                    captureCharge();
                    break;
                case R.id.autoConfigButton:
                    Reader.getDefault(mContext)
                            .startAutoConfigProcess();
                    break;
                case R.id.refundCard:
                    dialogFragment = new DialogFragment() {
                        @Override
                        public void onCreate(Bundle savedInstanceState) {
                            super.onCreate(savedInstanceState);

                            setRetainInstance(true);
                        }

                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {
                            return makeRefundDialog();
                        }
                    };
                    dialogFragment.setRetainInstance(true);
                    dialogFragment.setCancelable(false);
                    dialogFragment.show(getFragmentManager(), "dialogFragment");
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
    };

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
        mFieldHolder.enableZipCode(enable);
        zipCodeEnabled.setChecked(enable);
        if (enable) {
            zipCodeButton.setVisibility(View.VISIBLE);
        } else {
            zipCodeButton.setVisibility(View.GONE);
        }
    }

    private void launchSwipeEvent() {
        Reader.getDefault(mContext)
                .beginSwipe();
        mFieldHolder.resetFields();
    }

    private void displaySerialNumber() {
        String s = Reader.serialNumber;
        showAlert(s);
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
                    Toast.makeText(getApplicationContext(),
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
        chargeDetailsHash.put(Charge.REQUEST_KEY_AMOUNT, price);

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

        HashMap<String, Object> chargeDetailsHash = new HashMap<>();
        chargeDetailsHash.put(Card.REQUEST_KEY_ACCOUNT_TOKEN, Settings.ACCOUNT_TOKEN);
        chargeDetailsHash.put(Card.REQUEST_KEY_AMOUNT, price);

        JSONObject metadata = new JSONObject();

        try {
            metadata.put("example_data", "123456789");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        chargeDetailsHash.put(Card.REQUEST_KEY_META_DATA, metadata);

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
        readerIsConnected = isConnected;
        readerStatus.setText(message);
        swipeCardButton.setEnabled(isConnected);
        displaySerialButton.setEnabled(isConnected);
        autoConfigButton.setEnabled(isConnected);

        if (!isConnected) {
            fieldsReset();
        }
    }

    private void chargePresent() {
        captureChargeButton.setEnabled(true);
        chargeToken.setText(mCharge.getToken());
        chargeAmount.setText(String.format("$%s", mCharge.getAmount().toString()));
    }

    private void chargeUpdated() {
        chargeCaptured.setText(String.valueOf(mCharge.isCaputred()));
        chargeVoided.setText(String.valueOf(mCharge.isVoided()));
        chargeRefunded.setText(String.format("%s | $%s", mCharge.isRefunded(),
                mCharge.isRefunded() ? mCharge.getAmountRefunded().toString() : "-.--"));

        if (mCharge.isCaputred() && !mCharge.isVoided() && !mCharge.isRefunded()) {
            processPaymentButton.setEnabled(false);
            captureChargeButton.setEnabled(false);
            authorizeCardButton.setEnabled(false);
            voidButton.setEnabled(true);
            refundButton.setEnabled(true);
        } else if (mCharge.isRefunded() || mCharge.isVoided()) {
            processPaymentButton.setEnabled(false);
            captureChargeButton.setEnabled(false);
            authorizeCardButton.setEnabled(false);
            voidButton.setEnabled(false);
            refundButton.setEnabled(false);
        } else {
            voidButton.setEnabled(false);
            refundButton.setEnabled(false);
            processPaymentButton.setEnabled(true);
            captureChargeButton.setEnabled(true);
            authorizeCardButton.setEnabled(true);
        }
    }

    private void chargeCleared() {
        captureChargeButton.setEnabled(false);
        voidButton.setEnabled(false);
        refundButton.setEnabled(false);

        chargeToken.setText("---");
        chargeAmount.setText("$-.--");

        chargeCaptured.setText("---");
        chargeVoided.setText("---");
        chargeRefunded.setText("---");
    }

    private void setCardPresent() {
        if (mCard == null) {
            cardHolderName.setText("");
            cardType.setText("");
            cardNumber.setText("");
            cardLastFour.setText("");
            zipCode.setText("");
        } else {
            cardHolderName.setText(mCard.getName());
            cardType.setText(mCard.getType());
            cardNumber.setText(mCard.getCardNumber());
            cardLastFour.setText(mCard.getLast4());
            zipCode.setText(mCard.getZipCode());
        }

        processPaymentButton.setEnabled(true);
        tokenizeCardButton.setEnabled(true);
        decryptButton.setEnabled(true);
        authorizeCardButton.setEnabled(true);
    }

    private void fieldsReset() {
        mCard = null;
        cardNumber.setText("----");
        cardLastFour.setText("----");
        cardType.setText("----");
        cardHolderName.setText("----");

        processPaymentButton.setEnabled(false);
        tokenizeCardButton.setEnabled(false);
        decryptButton.setEnabled(false);
        authorizeCardButton.setEnabled(false);
        chargeCleared();
    }

    private void fillFieldsWithData(Card cardData) {
        mCard = cardData;
        setCardPresent();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Reader.tearDown();
    }

    private Context getApplicationContext() {
        return mContext.getApplicationContext();
    }

    /**
     * Dialog creators
     */

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

    @Override
    public void updateReaderStatus(MyCFDeviceHandler.ReaderStatus status) {
        switch (status) {
            case CONNECTED:
                updateReaderStatus("Reader connected", true);
                break;
            case ATTACHED:
                updateReaderStatus("Reader connecting", false);
                break;
            case DISCONNECTED:
                updateReaderStatus("Reader disconnected", false);
                break;
            case UNKNOWN:
                updateReaderStatus("Unknown error", false);

                autoConfigButton.setEnabled(true);
                break;
            case NOT_COMPATIBLE:
                updateReaderStatus("Device not compatible", false);

                autoConfigButton.setEnabled(true);
                break;
        }
    }

    @Override
    public void showAlert(String message) {
        if (alert != null) {
            alert.dismiss();
            alert = null;
        }

        if (getView() != null) {
            alert = Snackbar.make(getView(), message, Snackbar.LENGTH_INDEFINITE);

            View view = alert.getView();
            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(Color.WHITE);

            alert.setAction("Dismiss", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.dismiss();
                }
            });

            alert.setActionTextColor(ContextCompat.getColor(mContext, R.color.cardflight_blue));

            alert.show();
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

        swipeCardButton.setEnabled(true);
    }
}

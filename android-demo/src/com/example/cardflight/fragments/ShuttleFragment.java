package com.example.cardflight.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.example.cardflight.R;
import com.example.cardflight.Settings;
import com.getcardflight.interfaces.*;
import com.getcardflight.models.Card;
import com.getcardflight.models.CardFlight;
import com.getcardflight.models.Charge;
import com.getcardflight.models.Reader;
import com.getcardflight.views.PaymentView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

/**
 * Created by pcedrowski on 8/25/14.
 */
public class ShuttleFragment extends Fragment {
    private static final String TAG = ShuttleFragment.class.getSimpleName();
    private Context mContext;

    private boolean readerIsConnected;
    private boolean readerFailed;
    private boolean swipedCard;

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
    private CheckBox zipCodeEnabled;

    private Reader reader = null;
    private Card mCard = null;
    private Charge mCharge = null;
    private OnCardKeyedListener onCardKeyedListener;
    private OnFieldResetListener onFieldResetListener;
    private PaymentView mFieldHolder;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        setRetainInstance(true);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Instantiate CardFlight Instance
        CardFlight.getInstance().setApiTokenAndAccountToken(Settings.API_TOKEN, Settings.ACCOUNT_TOKEN);
        CardFlight.getInstance().setLogging(true);

        // Create a new Reader object with AutoConfig handler
        reader = new Reader(getApplicationContext(), new CardFlightDeviceHandler() {

            @Override
            public void readerIsConnecting() {
                Toast.makeText(getApplicationContext(),
                        "Device connecting", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void readerIsAttached() {
                readerFailed = false;
                Toast.makeText(getApplicationContext(),
                        "Device connected", Toast.LENGTH_SHORT).show();

                readerConnected();
            }

            @Override
            public void readerIsDisconnected() {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(),
                        "Device disconnected", Toast.LENGTH_SHORT)
                        .show();

                readerDisconnected();
            }

            @Override
            public void deviceBeginSwipe() {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(),
                        "Device begin swipe", Toast.LENGTH_SHORT)
                        .show();

            }

            @Override
            public void readerCardResponse(Card card) {
                // TODO Auto-generated method stub

                Toast.makeText(getApplicationContext(),
                        "Device swipe completed", Toast.LENGTH_SHORT)
                        .show();

                mCard = card;

                fillFieldsWithData(card);
            }

            @Override
            public void deviceSwipeFailed() {
                readerFailed = true;
                Toast.makeText(getApplicationContext(),
                        "Device swipe failed", Toast.LENGTH_SHORT)
                        .show();

            }

            @Override
            public void deviceSwipeTimeout() {
                readerFailed = true;
                Toast.makeText(getApplicationContext(),
                        "Device swipe time out", Toast.LENGTH_SHORT)
                        .show();

            }

            @Override
            public void deviceNotSupported() {
                readerFailed = true;
                Toast.makeText(getApplicationContext(),
                        "Device not supported", Toast.LENGTH_SHORT)
                        .show();

                enableAutoconfigButton();
            }

            @Override
            public void readerTimeout() {
                readerFailed = true;
                Toast.makeText(getApplicationContext(),
                        "Reader has timed out", Toast.LENGTH_SHORT)
                        .show();

                enableAutoconfigButton();
            }

        }, new CardFlightAutoConfigHandler() {
            @Override
            public void autoConfigProgressUpdate(int i) {
                Log.i(TAG, "AutoConfig progress %" + i);
                readerStatus.setText("AutoConfig Progress %" + i);
            }

            @Override
            public void autoConfigFinished() {
                Log.i(TAG, "AutoConfig finished");
            }

            @Override
            public void autoConfigFailed() {
                Log.i(TAG, "AutoConfig failed");
                readerStatus.setText("AutoConfig failed -- device is not supported");
            }
        });

        // Create the listener that listens to when the PaymentView has been filled out manually
        onCardKeyedListener = new OnCardKeyedListener() {

            @Override
            public void onCardKeyed(Card card) {
                mCard = card;
                fillFieldsWithData(mCard);
            }
        };

        // Create the listener that listens to when the PaymentView has been cleared and reset.
        // NOTE: This is not necessary and should be used to simply clear out any variables set.
        onFieldResetListener = new OnFieldResetListener(){
            @Override
            public void onFieldReset() {
                fieldsReset();

                // TODO
                // Example of how to listen for swipe after reset:
                // 2 flags need to be maintained to follow the state of the reader
//                if (readerIsConnected && !readerFailed)
//                    reader.beginSwipe();
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView  = inflater.inflate(R.layout.shuttle_fragment, container, false);

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
        autoConfigButton = (Button) rootView.findViewById(R.id.autoConfigButton);
        zipCodeButton = (Button) rootView.findViewById(R.id.fetchZipCodeButton);
        voidButton = (Button) rootView.findViewById(R.id.voidCard);
        refundButton = (Button) rootView.findViewById(R.id.refundCard);
        readerStatus = (TextView) rootView.findViewById(R.id.reader_status);

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

        if (readerIsConnected){
            readerConnected();
        } else {
            readerDisconnected();
        }

        return rootView;
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DialogFragment dialogFragment;

            switch (v.getId()){
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
                            return makeAuthroizeDialog();
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
                    reader.startAutoConfigProcess();
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
                        showToast(String.format("Zip Code: %s", mCard.getZipCode()));
                    } else {
                        showToast("No card is present");
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private void enableZipCode(boolean enable){
        mFieldHolder.enableZipCode(enable);
        zipCodeEnabled.setChecked(enable);
        if (enable){
            zipCodeButton.setVisibility(View.VISIBLE);
        } else {
            zipCodeButton.setVisibility(View.GONE);
        }
    }

    private void launchSwipeEvent() {
        reader.beginSwipe();
        mFieldHolder.resetFields();
    }

    private void displaySerialNumber() {
        String s = reader.serialNumber;
        Toast.makeText(getApplicationContext(),s, Toast.LENGTH_SHORT).show();
    }

    private void tokenizeCardMethod(){
        showToast("Tokenizing card...");
        if (mCard != null) {
            mCard.tokenize(
                    new CardFlightTokenizationHandler() {
                        @Override
                        public void tokenizationSuccessful(String s) {
                            Log.d(TAG, "tokenizationSuccessful");
                            showToast(s);
                        }

                        @Override
                        public void tokenizationFailed(String s) {
                            Log.d(TAG, "tokenizationFailed");
                            showToast(s);
                        }
                    },
                    getApplicationContext()
            );
        } else {
            showToast("Unable to tokenize- no card present");
        }
    }

    private void authorizeCard(double price){
        showToast("Authorizing card...");
        HashMap chargeDetailsHash = new HashMap();
        chargeDetailsHash.put(Charge.REQUEST_KEY_AMOUNT, price);

        if (mCard != null) {
            mCard.authorize(
                    chargeDetailsHash,
                    new CardFlightAuthHandler() {
                        @Override
                        public void authValid(Charge charge) {
                            Log.d(TAG, "Card authorize valid");
                            showToast("Card authorized");
                            mCharge = charge;
                            chargePresent();
                            chargeUpdated();

                            HashMap<String, String> newMap = charge.getMetadata();
                            showToast("metadata: " + newMap.get("Test"));
                        }

                        @Override
                        public void authFailed(String s) {
                            Log.d(TAG, "Card authorize failed");
                            showToast(s);
                        }
                    },
                    getApplicationContext()
            );
        } else {
            showToast("Unable to tokenize- no card present");
        }
    }

    private void captureCharge(){
        showToast("Capturing charge...");
        if (mCharge != null) {
            Charge.processCapture(mCharge.getToken(), mCharge.getAmount().doubleValue(), new CardFlightCaptureHandler() {

                @Override
                public void captureSuccessful(Charge charge) {
                    showToast(String.format("Capture of $%s successful", charge.getAmount()));
                    mCharge = charge;
                    chargeUpdated();
                }

                @Override
                public void captureFailed(String error) {
                    showToast(error);
                }
            });
        } else {
            showToast("Unable to capture charge");
        }
    }

    private void chargeCard(String price) {
        if (TextUtils.isEmpty(price)){
            showToast("Price cannot be empty");
            return;
        }
        Log.d(TAG, "Processing payment of: " + price);

        HashMap chargeDetailsHash = new HashMap();
        chargeDetailsHash.put(mCard.REQUEST_KEY_AMOUNT, Double.valueOf(price));

        if (mCard != null) {
            mCard.chargeCard(chargeDetailsHash, new CardFlightPaymentHandler() {

                @Override
                public void transactionSuccessful(Charge charge) {
                    showToast(String.format("Charge of $%s successful", charge.getAmount()));

                    // Save charge object
                    mCharge = charge;
                    chargePresent();
                    chargeUpdated();
                }

                @Override
                public void transactionFailed(String error) {
                    Toast.makeText(getApplicationContext(), error,
                            Toast.LENGTH_SHORT).show();
                }
            }, getApplicationContext());
        } else {
            showToast("Unable to process payment- no card present");
        }
    }

    private void voidCharge(){
        showToast("Voiding charge...");
        if (mCharge != null) {
            Charge.processVoid(mCharge.getToken(), new CardFlightPaymentHandler() {

                @Override
                public void transactionSuccessful(Charge charge) {
                    showToast("Charge voided");
                    mCharge = charge;
                    chargeUpdated();
                }

                @Override
                public void transactionFailed(String error) {
                    showToast(error);

                }
            });
        } else {
            showToast("Unable to void charge");
        }
    }

    private void refundCharge(double refund){
        showToast("Refunding charge...");
        if (mCharge != null) {
            Charge.processRefund(mCharge.getToken(), refund, new CardFlightPaymentHandler() {

                @Override
                public void transactionSuccessful(Charge charge) {
                    showToast(String.format("%s refunded to charge", mCharge.getAmountRefunded()));
                    mCharge = charge;
                    chargeUpdated();
                }

                @Override
                public void transactionFailed(String error) {
                    showToast(error);

                }
            });
        } else {
            showToast("Unable to refund charge");
        }
    }


    private void readerConnected(){
        readerIsConnected = true;
        readerStatus.setText("Reader connected");
        swipeCardButton.setEnabled(true);
        displaySerialButton.setEnabled(true);
        autoConfigButton.setEnabled(false);
    }

    private void readerDisconnected(){
        readerIsConnected = false;
        readerStatus.setText("Reader not connected");
        swipeCardButton.setEnabled(false);
        displaySerialButton.setEnabled(false);
        autoConfigButton.setEnabled(false);
        fieldsReset();
    }

    private void chargePresent(){
        captureChargeButton.setEnabled(true);
        chargeToken.setText(mCharge.getToken());
        chargeAmount.setText("$" + mCharge.getAmount());
    }

    private void chargeUpdated(){
        chargeCaptured.setText(String.valueOf(mCharge.isCaputred()));
        chargeVoided.setText(String.valueOf(mCharge.isVoided()));
        chargeRefunded.setText(String.format("%s | $%s", mCharge.isRefunded(),
                mCharge.isRefunded() ? mCharge.getAmountRefunded().toString() : "-.--"));

        if (mCharge.isCaputred() && !mCharge.isVoided() && !mCharge.isRefunded()){
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

    private void chargeCleared(){
        captureChargeButton.setEnabled(false);
        voidButton.setEnabled(false);
        refundButton.setEnabled(false);

        chargeToken.setText("---");
        chargeAmount.setText("$-.--");

        chargeCaptured.setText("---");
        chargeVoided.setText("---");
        chargeRefunded.setText("---");
    }

    private void setCardPresent(){
        cardNumber.setText(mCard.getCardNumber());
        cardLastFour.setText(mCard.getLast4());
        cardType.setText(mCard.getType());
        zipCode.setText(mCard.getZipCode());

        processPaymentButton.setEnabled(true);
        tokenizeCardButton.setEnabled(true);
        authorizeCardButton.setEnabled(true);
    }

    private void fieldsReset() {
        mCard = null;
        cardNumber.setText("----");
        cardLastFour.setText("----");
        cardType.setText("----");

        processPaymentButton.setEnabled(false);
        tokenizeCardButton.setEnabled(false);
        authorizeCardButton.setEnabled(false);
        chargeCleared();
    }

    private void fillFieldsWithData(Card cardData) {
        mCard = cardData;
        setCardPresent();
    }

    private void enableAutoconfigButton(){
        autoConfigButton.setEnabled(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // destroy cardflight instance
        if (reader != null)
            reader.destroy();
    }

    private Context getApplicationContext(){
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
                double amount = Double.valueOf(price);

                chargeCard(String.valueOf(amount));
            }
        });

        builder.setView(editView);

        return builder.create();
    }

    private Dialog makeAuthroizeDialog() {
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
                double amount = Double.valueOf(price);

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

    private void showToast(String text){
        Toast t = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }
}

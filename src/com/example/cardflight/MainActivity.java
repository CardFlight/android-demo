package com.example.cardflight;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.getcardflight.models.Card;
import com.getcardflight.models.CardFlight;
import com.getcardflight.models.Charge;
import com.getcardflight.models.Reader;
import com.getcardflight.interfaces.CardFlightDeviceHandler;
import com.getcardflight.interfaces.CardFlightPaymentHandler;
import com.getcardflight.views.CustomView;

import java.util.HashMap;

public class MainActivity extends Activity {

	private Charge charge;
    private Reader reader;
	private Card mCard;

	private static final String API_TOKEN = "4fb831302debeb03128c5c23633a5b42";
	private static final String ACCOUNT_TOKEN = "c10aa9a847b55d87";

	private EditText mPriceEditText, mCurrencyEditText, mDescriptionEditText, mPersonNameEditText, mCardEditText,
			mCVVEditText, mExpireDateEditText;
	
	private static final String CARD_DATA_KEY = "cardData";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        CardFlight.getInstance().setApiToken(API_TOKEN);
        CardFlight.getInstance().setAccountToken(ACCOUNT_TOKEN);

		mPriceEditText = (EditText) findViewById(R.id.priceEditText);
		mPersonNameEditText = (EditText) findViewById(R.id.nameEditText);
		mCardEditText = (EditText) findViewById(R.id.cardEditText);
		mCVVEditText = (EditText) findViewById(R.id.cvvCodeEditText);
		mExpireDateEditText = (EditText) findViewById(R.id.expireDateEditText);
        mCurrencyEditText = (EditText) findViewById(R.id.currencyEditText);
        mDescriptionEditText = (EditText) findViewById(R.id.descriptionEditText);

		// init cardFlight
        // create new Card object
		reader = new Reader(getApplicationContext(), new CardFlightDeviceHandler() {

					@Override
					public void readerIsConnecting() {
						Toast.makeText(getApplicationContext(),
								"Device connecting", Toast.LENGTH_SHORT).show();

					}

					@Override
					public void readerIsAttached() {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(),
								"Device connected", Toast.LENGTH_SHORT).show();

					}

					@Override
					public void readerIsDisconnected() {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(),
								"Device disconnected", Toast.LENGTH_SHORT)
								.show();

					}

					@Override
					public void deviceBeginSwipe() {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(),
								"Device begin swipe", Toast.LENGTH_SHORT)
								.show();

					}

					@Override
					public void deviceSwipeCompleted(Card card) {
						// TODO Auto-generated method stub

						Toast.makeText(getApplicationContext(),
								"Device swipe completed", Toast.LENGTH_SHORT)
								.show();

						mCard = card;

						fillFieldsWithData(card);

					}

					@Override
					public void deviceSwipeFailed() {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(),
								"Device swipe failed", Toast.LENGTH_SHORT)
								.show();

					}

					@Override
					public void deviceSwipeTimeout() {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(),
								"Device swipe time out", Toast.LENGTH_SHORT)
								.show();

					}

					@Override
					public void deviceNotSupported() {
						// TODO Auto-generated method stub

						Toast.makeText(getApplicationContext(),
								"Device not supported", Toast.LENGTH_SHORT)
								.show();

					}

				});
		
		
		if (savedInstanceState != null && savedInstanceState.containsKey(CARD_DATA_KEY))
		{
			mCard = savedInstanceState.getParcelable(CARD_DATA_KEY);
			fillFieldsWithData(mCard);
		}

	}
	
	
	@Override 
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mCard != null)
			outState.putParcelable(CARD_DATA_KEY, mCard);
	}

	public void launchSwipeEvent(View view) {

		reader.beginSwipe();

		resetFields();
	}

    public void displaySerialNumber(View view) {

        String s = reader.serialNumber;
        Toast.makeText(getApplicationContext(),s, 10).show();
    }

	public void processPayment(View view) {

		String price = mPriceEditText.getText().toString();
		if (TextUtils.isEmpty(price))
        {
			return;
        }

        // generate card object if custom manual entry
        if (mCard == null)
        {
            mCard = CustomView.getInstance().generateCard();
        }

		// process payment

        String description = mDescriptionEditText.getText().toString();
        String currency = mCurrencyEditText.getText().toString();

        HashMap chargeDetailsHash = new HashMap();
        chargeDetailsHash.put(Charge.REQUEST_KEY_CURRENCY, currency);
        chargeDetailsHash.put(Charge.REQUEST_KEY_DESCRIPTION, description);
        chargeDetailsHash.put(Charge.REQUEST_KEY_AMOUNT, Double.valueOf(price));
        chargeDetailsHash.put(Charge.REQUEST_KEY_CARD_DETAILS, mCard);

		mCard.chargeCard(chargeDetailsHash, new CardFlightPaymentHandler() {

                    @Override
                    public void transactionSuccessful(Charge charge) {

                        Toast.makeText(getApplicationContext(), "Transaction successful",
                                Toast.LENGTH_SHORT).show();

                        resetFields();
                    }

                    @Override
                    public void transactionFailed(String error) {
                        Toast.makeText(getApplicationContext(), error,
                                Toast.LENGTH_SHORT).show();

                        resetFields();

                    }
                });
	}

	private void resetFields() {

		mCard = null;
		mPersonNameEditText.setText("");
		mCardEditText.setText("");
		mCVVEditText.setText("");
		mExpireDateEditText.setText("");
	}

	private void fillFieldsWithData(Card cardData) {
		mPersonNameEditText.setText(cardData.getName());
		mCardEditText.setText(cardData.getCardNumber());
		mCVVEditText.setText(cardData.getCVVCode());
		mExpireDateEditText.setText(cardData.getExpirationMonth() + "/"
				+ cardData.getExpirationYear());

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// destroy cardflight instance
		reader.destroy();
	}

}

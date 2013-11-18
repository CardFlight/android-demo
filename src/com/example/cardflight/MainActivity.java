package com.example.cardflight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.getcardflight.models.CardDetailsData;
import com.getcardflight.models.Charge;
import com.getcardflight.interfaces.CardFlightDeviceHandler;
import com.getcardflight.interfaces.CardFlightPaymentHandler;
import com.getcardflight.activities.ManualEntryActivity;

import java.util.HashMap;

public class MainActivity extends Activity {

	private Charge charge;
	private CardDetailsData mCardData;

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

		mPriceEditText = (EditText) findViewById(R.id.priceEditText);
		mPersonNameEditText = (EditText) findViewById(R.id.nameEditText);
		mCardEditText = (EditText) findViewById(R.id.cardEditText);
		mCVVEditText = (EditText) findViewById(R.id.cvvCodeEditText);
		mExpireDateEditText = (EditText) findViewById(R.id.expireDateEditText);
        mCurrencyEditText = (EditText) findViewById(R.id.currencyEditText);
        mDescriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
		

		// init cardFlight
		charge = new Charge(API_TOKEN, ACCOUNT_TOKEN,
				getApplicationContext(), new CardFlightDeviceHandler() {

					@Override
					public void deviceConnecting() {
						Toast.makeText(getApplicationContext(),
								"Device connecting", Toast.LENGTH_SHORT).show();

					}

					@Override
					public void deviceConnected() {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(),
								"Device connected", Toast.LENGTH_SHORT).show();

					}

					@Override
					public void deviceDisconnected() {
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
					public void deviceSwipeCompleted(CardDetailsData cardData) {
						// TODO Auto-generated method stub

						Toast.makeText(getApplicationContext(),
								"Device swipe completed", Toast.LENGTH_SHORT)
								.show();

						mCardData = cardData;

						fillFieldsWithData(cardData);

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
			mCardData = savedInstanceState.getParcelable(CARD_DATA_KEY);
			fillFieldsWithData(mCardData);
		}

	}
	
	
	@Override 
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mCardData != null)
			outState.putParcelable(CARD_DATA_KEY, mCardData);
	}

	public void launchManualEntry(View view) {
		startActivityForResult(new Intent(this, ManualEntryActivity.class),
				ManualEntryActivity.MANUAL_ENTRY_REQUEST_CODE);
	}

	public void launchSwipeEvent(View view) {

		charge.beginSwipe();

		resetFields();
	}

    public void displaySerialNumber(View view) {
        String s = Charge.getSerialNumber();
        Toast.makeText(getApplicationContext(),s, 10).show();
    }

	public void processPayment(View view) {

		String price = mPriceEditText.getText().toString();
		if (TextUtils.isEmpty(price))
			return;

		// process payment

        String description = mDescriptionEditText.getText().toString();
        String currency = mCurrencyEditText.getText().toString();

        HashMap chargeDetailsHash = new HashMap();
        chargeDetailsHash.put(Charge.REQUEST_KEY_CURRENCY, currency);
        chargeDetailsHash.put(Charge.REQUEST_KEY_DESCRIPTION, description);
        chargeDetailsHash.put(Charge.REQUEST_KEY_AMOUNT, Double.valueOf(price));
        chargeDetailsHash.put(Charge.REQUEST_KEY_CARD_DETAILS, mCardData);

		charge.create(chargeDetailsHash, new CardFlightPaymentHandler() {

                    @Override
                    public void transactionSuccessful(String result) {

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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK
				&& requestCode == ManualEntryActivity.MANUAL_ENTRY_REQUEST_CODE) {
			mCardData = data
					.getParcelableExtra(ManualEntryActivity.EXTRA_CARD_DETAILS);

			if (mCardData != null)
				fillFieldsWithData(mCardData);

		}

	}

	private void resetFields() {

		mCardData = null;
		mPersonNameEditText.setText("");
		mCardEditText.setText("");
		mCVVEditText.setText("");
		mExpireDateEditText.setText("");
	}

	private void fillFieldsWithData(CardDetailsData cardData) {
		mPersonNameEditText.setText(cardData.getPersonName());
		mCardEditText.setText(cardData.getCardNumber());
		mCVVEditText.setText(cardData.getCVVCode());
		mExpireDateEditText.setText(cardData.getExpireMonth() + "/"
				+ cardData.getExpireYear());

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// destroy cardflight instance
		charge.destroy();
	}

}

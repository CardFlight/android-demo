package com.example.cardflight.handlers;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AlertDialog;

import com.getcardflight.interfaces.CardFlightDeviceHandler;
import com.getcardflight.models.CFEMVMessage;
import com.getcardflight.models.Card;
import com.getcardflight.models.CardFlightError;
import com.getcardflight.models.Charge;
import com.getcardflight.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MyCFDeviceHandler implements CardFlightDeviceHandler {

    public enum ReaderStatus {
        CONNECTING,
        UPDATING,
        CONNECTED,
        DISCONNECTED,
        UNKNOWN,
        NOT_COMPATIBLE
    }

    private MyUIHandler uiHandler;

    public MyCFDeviceHandler(MyUIHandler myUIHandler) {
        this.uiHandler = myUIHandler;
    }

    /***********************************************************************************************
     * CardFlightDeviceHandler
     **********************************************************************************************/

    @Override
    public void emvTransactionResult(Charge charge, boolean requiresSignature, CFEMVMessage message) {
        if (charge == null) {
            this.uiHandler.showAlert(message.getMessage());
        } else if (requiresSignature) {
            this.uiHandler.showAlert(message.getMessage() + " needs signature");
        } else {
            this.uiHandler.showAlert(message.getMessage() + " no CVM");
        }
    }

    @Override
    public void emvRequestApplicationSelection(ArrayList appList) {
        this.uiHandler.showAlert("Multiple AIDs available");

        // Implement method to select which AID,
        // then call - Reader.getDefault(context).emvSelectApplication(aidIndex);
    }

    @Override
    public void emvMessage(CFEMVMessage message) {
        this.uiHandler.showAlert(message.getMessage());
    }

    @Override
    public void emvCardResponse(HashMap<String, Object> hashMap) {
        String cardType = (String) hashMap.get(Constants.CARD_TYPE);
        String firstSix = (String) hashMap.get(Constants.FIRST_SIX);
        String lastFour = (String) hashMap.get(Constants.LAST_FOUR);

        this.uiHandler.showConfirmCharge(String.format(Locale.US, "Charge %s %s****%s?", cardType, firstSix, lastFour));
    }

    @Override
    public void emvErrorResponse(CardFlightError error) {
        this.uiHandler.showAlert("Error: " + error.getMessage());
    }

    @Override
    public void emvAmountRequested() {
        // prompt for amount - doesn't happen if amount properly set
    }

    @Override
    public void readerBatteryLow() {
        this.uiHandler.showAlert("Reader battery low");
    }

    @Override
    public void emvCardDipped() {
        this.uiHandler.showAlert("Card dipped");
    }

    @Override
    public void emvCardRemoved() {
        this.uiHandler.showAlert("Card removed");
    }

    @Override
    public void emvTransactionVaultID(String s) {
        this.uiHandler.showAlert("Vault ID: " + s);
    }

    @Override
    public void readerCardResponse(Card card, CardFlightError error) {
        if (error == null) {
            this.uiHandler.showAlert("Swipe completed");
            this.uiHandler.setCard(card);
        } else {
            this.uiHandler.showAlert("Error: " + error.getMessage());
            this.uiHandler.setCard(null);
        }
    }

    @Override
    public void readerIsAttached() {
        // Deprecated - This method is a duplicate of readerIsConnecting as of version 3.1 and will be
        // removed in a future release
        this.uiHandler.updateReaderStatus(ReaderStatus.CONNECTING);
    }

    @Deprecated
    @Override
    public void readerIsConnecting() {
        this.uiHandler.updateReaderStatus(ReaderStatus.CONNECTING);
    }

    @Override
    public void readerIsUpdating() {
        this.uiHandler.updateReaderStatus(ReaderStatus.UPDATING);
    }

    @Override
    public void readerIsConnected(boolean isConnected, CardFlightError error) {
        if (error == null) {
            this.uiHandler.updateReaderStatus(ReaderStatus.CONNECTED);
        } else {
            this.uiHandler.updateReaderStatus(ReaderStatus.UNKNOWN);
        }
    }

    @Override
    public void readerSwipeDetected() {
        this.uiHandler.showAlert("Swipe detected");
    }

    @Override
    public void readerIsDisconnected() {
        this.uiHandler.updateReaderStatus(ReaderStatus.DISCONNECTED);

    }

    @Override
    public void readerSwipeDidCancel() {
        this.uiHandler.showAlert("Swipe cancelled");
    }

    @Override
    public void readerNotDetected() {
        this.uiHandler.updateReaderStatus(ReaderStatus.DISCONNECTED);
    }

    @Override
    public void selectBluetoothDevice(ArrayList<BluetoothDevice> arrayList) {
        this.uiHandler.showBluetoothDevices(arrayList);
    }
}

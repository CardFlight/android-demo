package com.example.cardflight.handlers;

import com.getcardflight.interfaces.CardFlightDeviceHandler;
import com.getcardflight.models.CFEMVMessage;
import com.getcardflight.models.Card;
import com.getcardflight.models.CardFlightError;
import com.getcardflight.models.Charge;

import java.util.ArrayList;

public class MyCFDeviceHandler implements CardFlightDeviceHandler {

    public enum ReaderStatus {
        ATTACHED,
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
    public void emvCardResponse(String last4, String cardType) {
        this.uiHandler.showConfirmCharge("Charge " + cardType + "-" + last4);
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
        this.uiHandler.updateReaderStatus(ReaderStatus.ATTACHED);
    }

    @Deprecated
    @Override
    public void readerIsConnecting() {
        // Deprecated - This method is a duplicate of readerIsAttached as of version 3.1 and will be
        // removed in a future release
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
    public void deviceBeginSwipe() {
        // Deprecated in SDK v3.0.4
    }

    @Override
    public void readerFail(String errorMessage, int errorCode) {
        // Deprecated in SDK v3.0.4
    }
}

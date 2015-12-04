package com.example.cardflight.handlers;

import com.getcardflight.models.Card;

/**
 * Created by aodell on 12/4/15.
 */
public interface MyUIHandler {
    void updateReaderStatus(MyCFDeviceHandler.ReaderStatus status);

    void showAlert(String message);

    void showConfirmCharge(String prompt);

    void setCard(Card card);
}

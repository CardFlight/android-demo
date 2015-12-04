package com.example.cardflight.handlers;

import com.getcardflight.interfaces.CardFlightAutoConfigHandler;

public class MyCFAutoConfigHandler implements CardFlightAutoConfigHandler {

    private MyUIHandler uiHandler;

    public MyCFAutoConfigHandler(MyUIHandler myUIHandler) {
        this.uiHandler = myUIHandler;
    }

    @Override
    public void autoConfigProgressUpdate(int progress) {
        this.uiHandler.showAlert("AutoConfig Progress = " + progress + "%");
    }

    @Override
    public void autoConfigFinished() {
        this.uiHandler.showAlert("AutoConfig Finish");
    }

    @Override
    public void autoConfigFailed() {
        this.uiHandler.updateReaderStatus(MyCFDeviceHandler.ReaderStatus.NOT_COMPATIBLE);
    }
}

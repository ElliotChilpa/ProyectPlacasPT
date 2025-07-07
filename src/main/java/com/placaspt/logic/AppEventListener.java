// com/placaspt/logic/AppEventListener.java
package com.placaspt.logic;

import com.placaspt.model.EventoPlacaDTO;

public interface AppEventListener {
    /** Se dispara cuando llega un tag RFID. */
    void onRfidTag(String tag);

    /** Se dispara cuando llega un evento de placa (JSON). */
    void onPlacaEvent(EventoPlacaDTO ev);
}
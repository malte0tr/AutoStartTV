package de.autostarttv;

import android.widget.TextView;

/** Small UI helpers shared by the screens. */
final class Ui {

    private Ui() {
    }

    /** Shows the text in the accent colour (or the normal colour). */
    @SuppressWarnings("deprecation")
    static void setAccent(TextView view, boolean accent) {
        view.setTextColor(view.getResources().getColorStateList(
                accent ? R.color.text_accent : R.color.text_primary));
    }
}

package com.starling.assignment.model;

import java.text.DecimalFormat;
import java.util.Locale;

public class Amount {

    private static final DecimalFormat currencyFormat = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.UK);

    static {
        currencyFormat.setMinimumFractionDigits(2);
    }

    private final String currency;
    private final long minorUnits;

    public Amount(String currency, long minorUnits) {
        this.currency = currency;
        this.minorUnits = minorUnits;
    }

    public String getCurrency() {
        return currency;
    }

    public long getMinorUnits() {
        return minorUnits;
    }

    public String getDisplayValue() {

        return currencyFormat.format(minorUnits * 0.01d);
    }
}

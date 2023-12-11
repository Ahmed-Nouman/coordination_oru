package se.oru.coordination.coordination_oru.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Round {

    /**
     * Rounds a double value to a specified number of decimal places.
     *
     * @param value the value to round
     * @param places the number of decimal places to round to
     * @return the rounded value
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException("Decimal places must be non-negative.");

        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        bigDecimal = bigDecimal.setScale(places, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }
}
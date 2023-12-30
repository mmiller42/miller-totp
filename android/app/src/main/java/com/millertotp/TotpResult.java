package com.millertotp;

import javax.annotation.Nonnull;

public class TotpResult {
    private final String code;
    private final long period;
    private final TotpGenerator generator;

    public TotpResult(final TotpGenerator generator) {
        this.generator = generator;
        period = generator.getPeriod();
        code = formatCode(generator.generate());
    }

    public TotpResult(final TotpGenerator generator, final long time) {
        this.generator = generator;
        period = generator.getPeriod(time);
        code = formatCode(generator.generate(time));
    }

    private String formatCode(final String code) {
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < code.length(); i++) {
            formatted.append(code.charAt(i));
            if ((i + 1) % 3 == 0 && i < code.length() - 1) {
                formatted.append(" ");
            }
        }

        return formatted.toString();
    }

    public String getCode() {
        return code;
    }

    public long getPeriod() {
        return period;
    }

    public long getSecondsLeft() {
        return generator.getSecondsLeft();
    }

    @Nonnull
    public String toString() {
        return "TotpResult { code: \"" + code + "\", period: " + period + ", secondsLeft: " + getSecondsLeft() + " }";
    }
}

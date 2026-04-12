package org.example;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

public class LogLevelColorConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {
    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        final String CUSTOM_GRAY_FG = "90m";
        return switch (event.getLevel().toInt()) {
            case Level.ERROR_INT -> ANSIConstants.BOLD + ANSIConstants.RED_FG;
            case Level.WARN_INT  -> ANSIConstants.BOLD + ANSIConstants.YELLOW_FG;
            case Level.INFO_INT  -> ANSIConstants.BOLD + ANSIConstants.GREEN_FG;
            case Level.DEBUG_INT -> ANSIConstants.BOLD + ANSIConstants.CYAN_FG;
            case Level.TRACE_INT -> ANSIConstants.BOLD + CUSTOM_GRAY_FG;
            default              -> ANSIConstants.DEFAULT_FG;
        };
    }
}

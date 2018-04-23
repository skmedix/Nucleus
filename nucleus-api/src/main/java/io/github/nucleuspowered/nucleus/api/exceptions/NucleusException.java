/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.exceptions;

import com.google.common.base.Preconditions;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TextMessageException;

public class NucleusException extends TextMessageException {

    private final ExceptionType exceptionType;

    public NucleusException(Text message, ExceptionType exceptionType) {
        super(message);
        Preconditions.checkNotNull(exceptionType);
        this.exceptionType = exceptionType;
    }

    public NucleusException(Text message, Throwable inner, ExceptionType exceptionType) {
        super(message, inner);
        Preconditions.checkNotNull(exceptionType);
        this.exceptionType = exceptionType;
    }

    /**
     * Gets the basic reason for the issue.
     *
     * @return The exception.
     */
    public ExceptionType getExceptionType() {
        return this.exceptionType;
    }

    public enum ExceptionType {

        /**
         * The requested name is not allowed.
         */
        DISALLOWED_NAME,

        /**
         * The requested object does not exist.
         */
        DOES_NOT_EXIST,

        /**
         * An event was cancelled.
         */
        EVENT_CANCELLED,

        /**
         * A limit was reached, such as a home count limit.
         */
        LIMIT_REACHED,

        /**
         * Unknown error.
         */
        UNKNOWN_ERROR
    }
}

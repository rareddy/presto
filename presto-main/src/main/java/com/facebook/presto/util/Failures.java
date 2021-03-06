/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.util;

import com.facebook.presto.client.ErrorLocation;
import com.facebook.presto.execution.ExecutionFailureInfo;
import com.facebook.presto.execution.Failure;
import com.facebook.presto.spi.ErrorCode;
import com.facebook.presto.spi.ErrorCodeSupplier;
import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.sql.parser.ParsingException;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;

import java.util.List;

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.collect.Iterables.transform;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public final class Failures
{
    private Failures() {}

    public static ExecutionFailureInfo toFailure(Throwable failure)
    {
        if (failure == null) {
            return null;
        }
        // todo prevent looping with suppressed cause loops and such
        String type;
        if (failure instanceof Failure) {
            type = ((Failure) failure).getType();
        }
        else {
            type = failure.getClass().getCanonicalName();
        }

        ErrorCode errorCode = null;
        if (failure instanceof PrestoException) {
            errorCode = ((PrestoException) failure).getErrorCode();
        }
        else if (failure instanceof Failure) {
            errorCode = ((Failure) failure).getErrorCode();
        }

        return new ExecutionFailureInfo(type,
                failure.getMessage(),
                toFailure(failure.getCause()),
                toFailures(asList(failure.getSuppressed())),
                Lists.transform(asList(failure.getStackTrace()), toStringFunction()),
                getErrorLocation(failure),
                errorCode);
    }

    public static void checkCondition(boolean condition, ErrorCodeSupplier errorCode, String formatString, Object... args)
    {
        if (!condition) {
            throw new PrestoException(errorCode, format(formatString, args));
        }
    }

    public static List<ExecutionFailureInfo> toFailures(Iterable<? extends Throwable> failures)
    {
        return ImmutableList.copyOf(transform(failures, toFailureFunction()));
    }

    private static Function<Throwable, ExecutionFailureInfo> toFailureFunction()
    {
        return new Function<Throwable, ExecutionFailureInfo>()
        {
            @Override
            public ExecutionFailureInfo apply(Throwable throwable)
            {
                return toFailure(throwable);
            }
        };
    }

    @Nullable
    private static ErrorLocation getErrorLocation(Throwable throwable)
    {
        // TODO: this is a big hack
        if (throwable instanceof ParsingException) {
            ParsingException e = (ParsingException) throwable;
            return new ErrorLocation(e.getLineNumber(), e.getColumnNumber());
        }
        return null;
    }
}

/*
 * Copyright 2019, 2025 Michael Büchner <m.buechner@dnb.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ddb.labs.europack.source.ddbapi;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Response;

final class MetricsInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        final long t0 = System.nanoTime();
        try {
            final Response r = chain.proceed(chain.request());
            final long durMs = Math.max(0L, (System.nanoTime() - t0) / 1_000_000L);
            HttpMetrics.recordStatus(r.code(), durMs);
            return r;
        } catch (IOException ex) {
            final long durMs = Math.max(0L, (System.nanoTime() - t0) / 1_000_000L);
            HttpMetrics.recordException(ex, durMs);
            throw ex;
        } catch (RuntimeException ex) {
            final long durMs = Math.max(0L, (System.nanoTime() - t0) / 1_000_000L);
            // Treat unexpected runtime exceptions as "other" for metrics
            HttpMetrics.recordException(new IOException(ex.getMessage(), ex), durMs);
            throw ex;
        }
    }
}

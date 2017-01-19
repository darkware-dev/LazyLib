/*----------------------------------------------------------------------------------------------
 -
 - Copyright 2017: darkware.org
 -
 -    Licensed under the Apache License, Version 2.0 (the "License");
 -    you may not use this file except in compliance with the License.
 -    You may obtain a copy of the License at
 -
 -        http://www.apache.org/licenses/LICENSE-2.0
 -
 -    Unless required by applicable law or agreed to in writing, software
 -    distributed under the License is distributed on an "AS IS" BASIS,
 -    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 -    See the License for the specific language governing permissions and
 -    limitations under the License.
 -
 ---------------------------------------------------------------------------------------------*/

package org.darkware.lazylib;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * This is a base implementation of a lazy loading helper class. This class is mostly useless by itself,
 * but it simplifies and normalizes the creation of child classes for lazy loading particular types of
 * data.
 *
 * @author jeff
 * @since 2016-05-16
 */
public abstract class LazyLoader<T>
{
    /** The internal data storage for the item */
    protected T data;
    private Supplier<T> loader;
    private final Duration ttl;
    private LocalDateTime expiration;

    public LazyLoader(final Supplier<T> loader, final Duration ttl)
    {
        super();

        this.ttl = ttl;
        this.expiration = null;
        this.loader = loader;

        this.data = this.prepopulate();
    }

    /**
     * Prepopulate the data upon creation and data release. This is often used to initialized stored objects that will
     * have specialized {@link #applyData(Object)} methods.
     *
     * @return The data to pre-populate.
     */
    protected T prepopulate()
    {
        return null;
    }

    /**
     * Unload the currently stored data and mark it as expired. This will force it to be re-fetched the next time the
     * value is accessed. This is similar to {@link #expire()}, but actually removes the reference to stored data,
     * possibly allowing it to be garbage-collected. <em>Note:</em> Using this instead of {@link #expire()} can decrease
     * the efficiency composed items like {@link LazyLoadedSet}s and {@link LazyLoadedMap}s
     */
    public final void unload()
    {
        synchronized (this)
        {
            this.data = prepopulate();
            this.expire();
        }
    }

    /**
     * Load the data for this item, if needed. This will only load data if the data is not currently loaded
     * or if it has expired.
     */
    public final void load()
    {
        this.load(false);
    }

    /**
     * Load the data for this item.
     *
     * @param forceful Force the data to reload, ignoring and resetting the expiration time, if used.
     */
    public final void load(final boolean forceful)
    {
        synchronized (this)
        {
            if (forceful || this.isExpired())
            {
                try
                {
                    this.applyData(loader.get());
                    this.renew();
                }
                catch (Throwable t)
                {
                    this.reportLoadError(t);
                    throw new LazyGenerationException("There was an error loading the value.", t);
                }
            }
        }
    }

    /**
     * Set the internal data to match the data loaded. In very simple implementations, this just sets the data to the
     * object supplied. Other implementations, particularly those with {@link Set}s or {@link Map}s may choose to do
     * more complex merge operations to maintain smoother concurrency behavior.
     *
     * @param newData The data to store in this object.
     */
    protected void applyData(final T newData)
    {
        this.data = newData;
    }

    /**
     * Fetch the value. If the value has not been fetched or if the value has expired, a new copy will be
     * retrieved.
     *
     * @return A value of the declared type. The value may be {@code null} or any assignable subtype.
     */
    public final T value()
    {
        synchronized (this)
        {
            this.load(false);
            return this.data;
        }
    }

    /**
     * Force the expiration of the value. Following this call, the next call to retrieve the data will
     * trigger a fresh fetch of the data.
     */
    public final void expire()
    {
        synchronized (this)
        {
            this.expiration = null;
            this.onExpiration();
        }
    }

    /**
     * Check if the data has been loaded for this item.
     * <p>
     * <em>Note:</em> Data that is loaded may still be expired. This method simply checks to see if some data has been
     * loaded.
     *
     * @return {@code true} if the data is loaded, {@code false} if it is not.
     */
    public final boolean isLoaded()
    {
        return this.expiration != null;
    }

    /**
     * Checks if the lazy loaded data is expired or not. Data that has never been loaded is considered to
     * be expired.
     *
     * @return {@code true} if the data is expired or never loaded, otherwise {@code false}.
     */
    public final boolean isExpired()
    {
        return this.expiration == null || this.expiration.isBefore(LocalDateTime.now());
    }

    /**
     * Extend the expiration another generation.
     */
    protected void renew()
    {
        if (ttl == null) this.expiration = LocalDateTime.MAX;
        else this.expiration = LocalDateTime.now().plus(this.ttl);
    }

    /**
     * Report any errors encountered while trying to fetch the backend value.
     *
     * @param t The {@link Throwable} which was caught during value loading.
     */
    protected void reportLoadError(final Throwable t)
    {
        // Do nothing by default.
    }

    /**
     * This method is called whenever a {@link LazyLoader}
     */
    protected void onExpiration()
    {
        // Do nothing by default
    }
}

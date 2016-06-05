/*******************************************************************************
 * Copyright (c) 2016. darkware.org and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.darkware.lazylib;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * This is a simple wrapper object which acts as deferral mechanism for loading an object value. Upon
 * creation, only the wrapper is allocated. No underlying object is loaded. Only once the value is requested
 * will it be loaded. Once loaded, the value will be retained until it expires, at which point a new value
 * will be fetched. The method of fetching or deriving the value is left for child classes or instances to
 * implement.
 * <p>
 * There are two slightly different modes this wrapper can operate. Ultimately, both abide by the
 * same rules.
 *
 * @author jeff
 * @since 2016-05-15
 */
public class LazyLoaded<T> extends LazyLoader<T>
{
    /**
     * Create a new lazy loaded value handler. It will store an object of the parameterized type. The value
     * will not be fetched until needed, and won't be fetched again until it expires. This particular value
     * does not automatically expire, but it can be made to manually expire.
     */
    public LazyLoaded(final Supplier<T> loader)
    {
        this(loader, null);
    }

    /**
     * Create a new lazy loaded value handler. It will store an object of the parameterized type. The value
     * will not be fetched until needed, and won't be fetched again until it expires.
     *
     * @param ttl The amount of time the value should be allowed to be stored before the value automatically
     * expires.
     */
    public LazyLoaded(final Supplier<T> loader, final Duration ttl)
    {
        super(loader, ttl);
    }
}

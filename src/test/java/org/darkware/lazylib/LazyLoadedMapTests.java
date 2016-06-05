package org.darkware.lazylib;

import org.junit.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author jeff
 * @since 2016-06-05
 */
public class LazyLoadedMapTests
{
    private Map<Integer, String> loadedMap;

    private Map<String, Map<Integer, String>> stringMaps;

    public LazyLoadedMapTests()
    {
        super();

        this.stringMaps = new HashMap<>();

        this.createMap("A", "A", "B", "C");
        this.createMap("D", "D", "E");
        this.createMap("F", "F", "G", "H", "I");
    }

    private void createMap(final String name, String ... items)
    {
        Map<Integer, String> map = new HashMap<>();
        int id = 0;
        for (String item : items) map.put(id++, item);

        this.stringMaps.put(name, map);
    }

    private void useMap(final String name)
    {
        this.loadedMap = this.stringMaps.get(name);
    }

    @Test
    public void checkCreation()
    {
        this.useMap("A");
        LazyLoadedMap<Integer, String> a = new LazyLoadedMap<>(this::loadMap);

        // Check that the loading hasn't happened automatically
        assertFalse(a.isLoaded());
    }

    @Test
    public void checkCreationWithTTL() throws InterruptedException
    {
        this.useMap("A");
        LazyLoadedMap<Integer, String> a = new LazyLoadedMap<>(this::loadMap, Duration.ofMillis(300));

        // Check that the loading hasn't happened automatically
        assertFalse(a.isLoaded());

        // Load the value and implicitly set expiration
        a.value();
        assertFalse(a.isExpired());

        // Wait longer than the expiration
        Thread.sleep(350);

        // Check that the value is expired now
        assertTrue(a.isExpired());
    }

    @Test
    public void checkLoading()
    {
        this.useMap("A");
        LazyLoadedMap<Integer, String> a = new LazyLoadedMap<>(this::loadMap);

        // Check that the loading hasn't happened automatically
        assertFalse(a.isLoaded());

        // Check that the loading does happen
        assertEquals(this.stringMaps.get("A"), a.value());
        assertTrue(a.isLoaded());
    }

    @Test
    public void checkLoadingWithChangedBackend()
    {
        this.useMap("A");
        LazyLoadedMap<Integer, String> a = new LazyLoadedMap<>(this::loadMap);

        // Check that the loading grabbed the initial value
        assertEquals(this.stringMaps.get("A"), a.value());
        assertTrue(a.isLoaded());

        // Change the backend
        this.useMap("D");

        // Check that this doesn't change the loaded value
        assertNotEquals(this.stringMaps.get("D"), a.value());

        // Expire the current value and check that the new value is loaded
        a.expire();
        assertEquals(this.stringMaps.get("D"), a.value());
    }

    @Test
    public void checkExpiration()
    {
        this.useMap("A");
        LazyLoadedMap<Integer, String> a = new LazyLoadedMap<>(this::loadMap);

        // Load the value
        Map<Integer, String> originalValue = a.value();

        // Check expiration
        assertFalse(a.isExpired());
        a.expire();
        assertTrue(a.isExpired());

        // Change the backend value
        this.useMap("D");

        // Renew the value
        a.renew();

        // Check if its not expired any longer
        assertFalse(a.isExpired());

        // Check that the value hasn't been reloaded
        assertEquals(originalValue, a.value());
    }

    public Map<Integer, String> loadMap()
    {
        return this.loadedMap;
    }
}

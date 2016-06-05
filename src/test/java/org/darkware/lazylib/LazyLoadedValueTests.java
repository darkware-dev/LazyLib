package org.darkware.lazylib;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.*;

/**
 * @author jeff
 * @since 2016-06-05
 */
public class LazyLoadedValueTests
{
    private String loadedString;

    @Test
    public void checkCreation()
    {
        this.loadedString = "Test1";
        LazyLoaded<String> a = new LazyLoaded<>(this::loadString);

        // Check that the loading hasn't happened automatically
        assertFalse(a.isLoaded());
    }

    @Test
    public void checkCreationWithTTL() throws InterruptedException
    {
        this.loadedString = "Test1";
        LazyLoaded<String> a = new LazyLoaded<>(this::loadString, Duration.ofMillis(300));

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
        this.loadedString = "Test1";
        LazyLoaded<String> a = new LazyLoaded<>(this::loadString);

        // Check that the loading hasn't happened automatically
        assertFalse(a.isLoaded());

        // Check that the loading does happen
        assertEquals(this.loadedString, a.value());
        assertTrue(a.isLoaded());
    }

    @Test
    public void checkLoadingWithChangedBackend()
    {
        this.loadedString = "Test1";
        LazyLoaded<String> a = new LazyLoaded<>(this::loadString);

        // Check that the loading grabbed the initial value
        assertEquals(this.loadedString, a.value());
        assertTrue(a.isLoaded());

        // Change the backend
        this.loadedString = "TestB";

        // Check that this doesn't change the loaded value
        assertNotEquals(this.loadedString, a.value());

        // Expire the current value and check that the new value is loaded
        a.expire();
        assertEquals(this.loadedString, a.value());
    }

    @Test
    public void checkExpiration()
    {
        this.loadedString = "Test1";
        LazyLoaded<String> a = new LazyLoaded<>(this::loadString);

        // Load the value
        String originalValue = a.value();

        // Check expiration
        assertFalse(a.isExpired());
        a.expire();
        assertTrue(a.isExpired());

        // Change the backend value
        this.loadedString = "BackendChange";

        // Renew the value
        a.renew();

        // Check if its not expired any longer
        assertFalse(a.isExpired());

        // Check that the value hasn't been reloaded
        assertEquals(originalValue, a.value());
    }

    public String loadString()
    {
        return this.loadedString;
    }
}

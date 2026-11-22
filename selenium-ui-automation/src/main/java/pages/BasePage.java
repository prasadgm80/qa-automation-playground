package pages;

import utilities.Utils;

import java.util.Objects;

/**
 * Base class for all page objects.
 *
 * <p>Holds the single shared {@link Utils} instance for the current test thread,
 * so every page reuses the same driver and explicit wait instead of creating its own.
 */
public abstract class BasePage {
    protected final Utils utils;

    protected BasePage(Utils utils) {
        this.utils = Objects.requireNonNull(utils, "utils must not be null");
    }
}

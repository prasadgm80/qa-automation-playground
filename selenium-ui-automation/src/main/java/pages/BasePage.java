package pages;

import utilities.Utils;

import java.util.Objects;

/** Base for page objects: holds the shared {@link Utils} for the current test thread. */
public abstract class BasePage {
    protected final Utils utils;

    protected BasePage(Utils utils) {
        this.utils = Objects.requireNonNull(utils, "utils must not be null");
    }
}

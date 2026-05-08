package com.github.rspereiratech.plugin.insomnia.deprecated;

import com.github.rspereiratech.plugin.core.deprecated.DeprecationMarker;

/**
 * {@link DeprecationMarker} implementation that uses Insomnia-friendly formatting
 * with a warning emoji prefix and descriptive suffix for deprecated operations.
 */
public class InsomniaDeprecationMarker implements DeprecationMarker {

    @Override
    public String markName(String n, boolean d) {
        return d ? "⚠ " + n + " (deprecated)" : n;
    }

    @Override
    public String markDescription(String desc, boolean d) {
        if (!d) {
            return desc;
        }

        String w = "DEPRECATED: This operation may be removed in a future version.";
        return desc.isBlank() ? w : w + "\n\n" + desc;
    }
}

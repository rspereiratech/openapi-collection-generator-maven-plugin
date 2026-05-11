package io.github.rspereiratech.plugin.core.loader;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Filesystem-based implementation of {@link SpecLoader} that checks whether the spec file
 * exists on disk and is a regular file.
 */
public class FileSpecLoader implements SpecLoader {

    @Override
    public void validate(File f) throws MojoExecutionException {
        if (f == null || !f.exists()) {
            throw new MojoExecutionException("Spec not found: " + f);
        }

        if (!f.isFile()) {
            throw new MojoExecutionException("Spec is not a file: " + f);
        }
    }
}

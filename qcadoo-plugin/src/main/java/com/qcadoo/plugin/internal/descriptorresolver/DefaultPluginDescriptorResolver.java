/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 0.4.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.plugin.internal.descriptorresolver;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.qcadoo.plugin.internal.api.PluginDescriptorResolver;

@Service
public class DefaultPluginDescriptorResolver implements PluginDescriptorResolver {

    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Value("#{plugin.descriptors}")
    private String descriptor;

    @Override
    public Resource[] getDescriptors() {
        try {
            return resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + descriptor);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to find classpath resources for "
                    + ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + descriptor, e);
        }
    }

    @Override
    public Resource getDescriptor(final File file) {
        try {
            JarFile jar = new JarFile(file);

            JarEntry entry = jar.getJarEntry(descriptor);

            if (entry == null) {
                entry = jar.getJarEntry(descriptor.split("/")[descriptor.split("/").length - 1]);

                if (entry == null) {
                    throw new IllegalStateException("Plugin descriptor " + descriptor + " not found in " + file.getAbsolutePath());
                }
            }

            return new InputStreamResource(jar.getInputStream(entry));
        } catch (IOException e) {
            throw new IllegalStateException("Plugin descriptor " + descriptor + " not found in " + file.getAbsolutePath(), e);
        }
    }

    public void setDescriptor(final String descriptor) {
        this.descriptor = descriptor;
    }

}
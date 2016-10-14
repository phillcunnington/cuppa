/*
 * Copyright 2015-2016 ForgeRock AS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.forgerock.cuppa;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * STUFF.
 */
public final class ServiceLoaderConfigurationProvider {

    private static final ServiceLoader<ConfigurationProvider> CONFIGURATION_PROVIDER_LOADER
            = ServiceLoader.load(ConfigurationProvider.class);

    private ServiceLoaderConfigurationProvider() {
    }

    /**
     * STUFF.
     *
     * @return STUFF.
     */
    public static Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        Iterator<ConfigurationProvider> iterator = CONFIGURATION_PROVIDER_LOADER.iterator();
        if (iterator.hasNext()) {
            ConfigurationProvider configurationProvider = iterator.next();
            if (iterator.hasNext()) {
                throw new CuppaException("There must only be a single configuration provider available on the "
                        + "classpath");
            }
            configurationProvider.configure(configuration);
        }
        return configuration;
    }
}

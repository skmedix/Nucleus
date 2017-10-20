/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.qsml;

import com.google.inject.Injector;
import io.github.nucleuspowered.nucleus.internal.qsml.module.StandardModule;
import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;
import uk.co.drnaylor.quickstart.loaders.ModuleConstructor;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class QuickStartModuleConstructor implements ModuleConstructor {

    private final Injector injector;
    private final Map<String, Map<String, List<String>>> mm;

    public QuickStartModuleConstructor(Injector injector,
            @Nullable Map<String, Map<String, List<String>>> m) {
        this.injector = injector;
        this.mm = m;
    }

    @Override
    public Module constructModule(Class<? extends Module> moduleClass) throws QuickStartModuleLoaderException.Construction {
        Module m = injector.getInstance(moduleClass);
        if (m instanceof StandardModule) {
            ((StandardModule) m).init(this.mm.get(moduleClass.getName()));
        }

        return m;
    }
}

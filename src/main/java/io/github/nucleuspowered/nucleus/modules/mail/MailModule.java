/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mail;

import io.github.nucleuspowered.nucleus.api.service.NucleusMailService;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.StandardModule;
import io.github.nucleuspowered.nucleus.modules.mail.handlers.MailHandler;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@RegisterService(value = MailHandler.class, apiService = NucleusMailService.class)
@ModuleData(id = "mail", name = "Mail")
public class MailModule extends StandardModule {
}

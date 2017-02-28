/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.forge.generator.git;

import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

/**
 * Presents a user with a list of git account kinds then lets the user configure it
 */
public class ConfigureGitAccount implements UIWizard {
    private static final transient Logger LOG = LoggerFactory.getLogger(ConfigureGitAccount.class);

    private List<GitProvider> gitProviders;

    @Inject
    @WithAttributes(label = "git provider", required = true, description = "Select which git provider you wish to configure")
    private UISelectOne<GitProvider> gitProvider;

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(getClass()).name("fabric8: Configure Git Account")
                .description("Configures a git account")
                .category(Categories.create("Fabric8"));
    }

    @Override
    public boolean isEnabled(UIContext context) {
        return true;
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        gitProviders = GitProvider.loadGitProviders();
        LOG.debug("Git providers: " + gitProviders);

        builder.add(gitProvider);
        gitProvider.setItemLabelConverter(new Converter<GitProvider, String>() {
            @Override
            public String convert(GitProvider gitProvider) {
                return gitProvider.getName();
            }
        });

        if (!gitProviders.isEmpty()) {
            gitProvider.setDefaultValue(gitProviders.get(0));
        }
    }

    @Override
    public void validate(UIValidationContext context) {
    }

    @Override
    public NavigationResult next(UINavigationContext context) throws Exception {
        NavigationResultBuilder builder = NavigationResultBuilder.create();
        GitProvider provider = gitProvider.getValue();
        if (provider != null) {
            provider.addConfigureStep(builder);
        }
        return builder.build();
    }

    @Override
    public Result execute(UIExecutionContext uiExecutionContext) throws Exception {
        return Results.success();
    }
}

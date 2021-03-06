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
package io.fabric8.forge.generator.pipeline;

import io.fabric8.forge.addon.utils.StopWatch;
import io.fabric8.forge.generator.AttributeMapKeys;
import io.fabric8.utils.Files;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.util.ResourceUtil;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UISelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.Map;

import static io.fabric8.forge.generator.AttributeMapKeys.PROJECT_DIRECTORY_FILE;

/**
 */
public abstract class AbstractDevToolsCommand extends AbstractProjectCommand {
    public static final String CATEGORY = "Obsidian";

    final transient Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    private ProjectFactory projectFactory;

    @Override
    protected boolean isProjectRequired() {
        return false;
    }

    @Override
    protected ProjectFactory getProjectFactory() {
        return projectFactory;
    }

    protected Project getSelectedProjectOrNull(UIContext context) {
        return Projects.getSelectedProject(this.getProjectFactory(), context);
    }

    protected String getProjectName(UIContext uiContext) {
        Object name = uiContext.getAttributeMap().get(AttributeMapKeys.NAME);
        if (name != null) {
            return name.toString();
        }
        Project project = getCurrentSelectedProject(uiContext);
        if (project != null) {
            MetadataFacet metadataFacet = project.getFacet(MetadataFacet.class);
            if (metadataFacet != null) {
                return metadataFacet.getProjectName();
            }
        }
        return "";
    }

    public Project getCurrentSelectedProject(UIContext context) {
        Project project;
        Map<Object, Object> attributeMap = context.getAttributeMap();
        if (attributeMap != null) {
            Object object = attributeMap.get(Project.class);
            if (object instanceof Project) {
                project = (Project) object;
                return project;
            }
        }
        UISelection<Object> selection = context.getSelection();
        Object selectedObject = selection.get();
        StopWatch watch = new StopWatch();
        try {
            log.debug("START getCurrentSelectedProject: on " + getProjectFactory() + " selection: " + selectedObject
                    + ". This may result in mvn artifacts being downloaded to ~/.m2/repository");
            project = Projects.getSelectedProject(getProjectFactory(), context);
            if (project != null && attributeMap != null) {
                attributeMap.put(Project.class, project);
            }
            return project;
        } finally {
            log.debug("END   getCurrentSelectedProject: on " + getProjectFactory() + " selection: " + selectedObject);
            log.debug("getCurrentSelectedProject took " + watch.taken());
        }
    }

    protected File getSelectionFolder(UIContext context) {
        Map<Object, Object> attributeMap = context.getAttributeMap();
        File file = (File) attributeMap.get(PROJECT_DIRECTORY_FILE);
        if (file != null) {
            return file;
        }
        UISelection<Object> selection = context.getSelection();
        if (selection != null) {
            Object object = selection.get();
            if (object instanceof File) {
                File folder = (File) object;
                if (Files.isDirectory(folder)) {
                    return folder;
                }
            } else if (object instanceof Resource) {
                File folder = ResourceUtil.getContextFile((Resource<?>) object);
                if (folder != null && Files.isDirectory(folder)) {
                    return folder;
                }
            }
        }
        Project project = (Project) attributeMap.get(Project.class);
        if (project != null) {
            DirectoryResource root = project.getRoot().reify(DirectoryResource.class);
            File folder = ResourceUtil.getContextFile(root);
            if (folder != null && Files.isDirectory(folder)) {
                return folder;
            }
        }
        return null;
    }
}

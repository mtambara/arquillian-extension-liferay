/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.maven.arquillain.generator;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.impl.client.deployment.AnnotationDeploymentScenarioGenerator;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:kamesh.sampath@liferay.com">Kamesh Sampath</a>
 */
public class MavenDeploymentScenarioGenerator
    extends AnnotationDeploymentScenarioGenerator {

    private static final Logger log =
        LoggerFactory.getLogger(MavenDeploymentScenarioGenerator.class);
    @Inject
    private Instance<ArquillianDescriptor> descriptor;

    /*
     * (non-Javadoc)
     * @see org.jboss.arquillian.container.test.impl.client.deployment.
     * AnnotationDeploymentScenarioGenerator
     * #generate(org.jboss.arquillian.test.spi.TestClass)
     */
    @Override
    public List<DeploymentDescription> generate(TestClass testClass) {

        List<DeploymentDescription> descriptions = super.generate(testClass);

        if (descriptions == null) {
            descriptions = new ArrayList<DeploymentDescription>();
        }

        // this will hold all server deployable decriptions
        List<DeploymentDescription> deployablesDescriptions =
            new ArrayList<DeploymentDescription>();

        log.info("Generating Deployment for Liferay Plugin ");

        if (descriptions.isEmpty()) {

            DeploymentDescription deploymentDecription =
                createLiferayPluginDeployment();
            if (deploymentDecription != null) {
                deployablesDescriptions.add(deploymentDecription);
            }
        }
        else {

            ListIterator<DeploymentDescription> listIterator =
                descriptions.listIterator();

            while (listIterator.hasNext()) {

                DeploymentDescription deploymentDescription =
                    listIterator.next();
                /*
                 * Proably Liferay Deployables
                 */
                if (deploymentDescription.managed() &&
                    deploymentDescription.testable()) {
                    deployablesDescriptions.add(deploymentDescription);
                    descriptions.remove(deploymentDescription);
                }
            }

        }

        if (deployablesDescriptions != null &&
            !deployablesDescriptions.isEmpty()) {

            log.debug("Liferay pluginize archive:" +
                deployablesDescriptions.size());

            ListIterator<DeploymentDescription> listIterator =
                deployablesDescriptions.listIterator();

            while (listIterator.hasNext()) {

                DeploymentDescription deploymentDescription =
                    listIterator.next();
            }
        }

        return descriptions;
    }

    private DeploymentDescription createLiferayPluginDeployment() {
        File pomFile = new File("pom.xml");

        if (pomFile != null && pomFile.exists()) {

            log.debug("Loading project from pom file:" +
                pomFile.getAbsolutePath());

            WebArchive archive =
                ShrinkWrap.create(MavenImporter.class).loadPomFromFile(
                    pomFile).importBuildOutput().as(WebArchive.class);

            DeploymentDescription deploymentDescription =
                new DeploymentDescription("_DEFAULT", archive);

            return deploymentDescription;

        }
        return null;

    }

    private ContainerDef containerDef() {

        List<ContainerDef> containers = descriptor.get().getContainers();

        ContainerDef defaultContainer = null;

        for (int i = 0; i < containers.size(); i++) {
            defaultContainer = containers.get(i);
            if (defaultContainer.isDefault()) {
                return defaultContainer;
            }
        }

        return null;
    }

    private Map<String, String> configuration() {

        ContainerDef defaultContainer = containerDef();

        if (defaultContainer != null) {

            Map<String, String> configuration = new HashMap<String, String>();

            String containerName = defaultContainer.getContainerName();

            if (containerName != null && containerName.contains("tomcat")) {
                configuration.put("appServerType", "tomcat");
            }
            else if (containerName != null && containerName.contains("jboss")) {
                configuration.put("appServerType", "jboss");
            }

            return configuration;

        }
        else {

            throw new RuntimeException("No default Remote container defined");

        }
    }

}

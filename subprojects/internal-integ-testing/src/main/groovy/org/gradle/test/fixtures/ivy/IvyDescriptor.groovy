/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.test.fixtures.ivy

import groovy.xml.QName

import java.util.regex.Pattern

class IvyDescriptor {
    final Map<String, IvyDescriptorDependencyConfiguration> dependencies = [:]
    Map<String, IvyDescriptorConfiguration> configurations = [:]
    List<IvyDescriptorArtifact> artifacts = []
    String organisation
    String module
    String revision
    String status
    String description

    IvyDescriptor(File ivyFile) {
        def ivy = new XmlParser().parse(ivyFile)
        organisation = ivy.info[0].@organisation
        module = ivy.info[0].@module
        revision = ivy.info[0].@revision
        status = ivy.info[0].@status
        description = ivy.info[0].description[0]?.text()

        ivy.configurations.conf.each {
            configurations[it.@name] = new IvyDescriptorConfiguration(
                    name: it.@name, visibility: it.@visibility, description: it.@description,
                    extend: it.@extends == null ? null : it.@extends.split(",")*.trim()
            )
        }

        ivy.dependencies.dependency.each { dep ->
            def configName = dep.@conf ?: "default"
            def matcher = Pattern.compile("(\\w+)->\\w+").matcher(configName)
            if (matcher.matches()) {
                configName = matcher.group(1)
            }
            def config = dependencies[configName]
            if (!config) {
                config = new IvyDescriptorDependencyConfiguration()
                dependencies[configName] = config
            }
            config.addDependency(dep.@org, dep.@name, dep.@rev)
        }

        ivy.publications.artifact.each { artifact ->
            def ivyArtifact = new IvyDescriptorArtifact(
                    name: artifact.@name, type: artifact.@type,
                    ext: artifact.@ext,
                    conf: artifact.@conf == null ? null : artifact.@conf.split(",") as List,
                    mavenAttributes: artifact.attributes().findAll { it.key instanceof QName && it.key.namespaceURI == "http://ant.apache.org/ivy/maven" }.collectEntries { [it.key.localPart, it.value] }
            )

            artifacts.add(ivyArtifact)
        }

    }

    IvyDescriptorArtifact expectArtifact(String name, String ext, String classifier = null) {
        return oneResult(artifacts.findAll({
            it.name == name && it.ext == ext && it.classifier == classifier
        }), [name, ext, classifier])
    }

    IvyDescriptorArtifact expectArtifact(String name) {
        return oneResult(artifacts.findAll({
            it.name == name
        }), [name])
    }

    private IvyDescriptorArtifact oneResult(List<IvyDescriptorArtifact> artifacts, def description) {
        assert artifacts.size() > 0 : "Expected artifact not found: $description"
        assert artifacts.size() == 1 : "Multiple artifacts found: $description"
        return artifacts.get(0)
    }
}

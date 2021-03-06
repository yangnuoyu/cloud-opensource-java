/*
 * Copyright 2019 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.opensource.classpath;

import com.google.cloud.tools.opensource.dependencies.DependencyGraph;
import com.google.cloud.tools.opensource.dependencies.DependencyGraphBuilder;
import com.google.cloud.tools.opensource.dependencies.DependencyPath;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import java.util.List;
import org.eclipse.aether.artifact.Artifact;

/**
 * Utility to build {@link ClassPathResult} that holds class path (a list of {@link ClassPathEntry})
 * through a dependency tree of Maven artifacts.
 *
 * @see <a
 *     href="https://docs.oracle.com/javase/8/docs/technotes/tools/unix/classpath.html#sthref15">
 *     Setting the Class Path: Specification Order</a>
 * @see <a
 *     href="https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Transitive_Dependencies">
 *     Maven: Introduction to the Dependency Mechanism</a>
 */
public final class ClassPathBuilder {

  private final DependencyGraphBuilder dependencyGraphBuilder;

  public ClassPathBuilder() {
    this(new DependencyGraphBuilder());
  }

  public ClassPathBuilder(DependencyGraphBuilder dependencyGraphBuilder) {
    this.dependencyGraphBuilder = dependencyGraphBuilder;
  }

  /**
   * Builds a classpath from the transitive dependency graph of a list of artifacts.
   * When there are multiple versions of an artifact in
   * the dependency tree, the closest to the root in breadth-first order is picked up. This "pick
   * closest" strategy follows Maven's dependency mediation.
   *
   * @param artifacts the first artifacts that appear in the classpath, in order
   * @param full if true all optional dependencies and their transitive dependencies are
   *     included. If false, optional dependencies are not included.
   */
  public ClassPathResult resolve(List<Artifact> artifacts, boolean full) {
    LinkedListMultimap<ClassPathEntry, DependencyPath> multimap = LinkedListMultimap.create();
    if (artifacts.isEmpty()) {
      return new ClassPathResult(multimap, ImmutableList.of());
    }
    // dependencyGraph holds multiple versions for one artifact key (groupId:artifactId)
    DependencyGraph result;
    if (full) {
      result = dependencyGraphBuilder.buildFullDependencyGraph(artifacts);
    } else {
      result = dependencyGraphBuilder.buildVerboseDependencyGraph(artifacts);
    }
    List<DependencyPath> dependencyPaths = result.list();

    // TODO should DependencyGraphResult have a mediate() method that returns a ClassPathResult?
    
    // To remove duplicates on (groupId:artifactId) for dependency mediation
    MavenDependencyMediation mediation = new MavenDependencyMediation();

    for (DependencyPath dependencyPath : dependencyPaths) {
      Artifact artifact = dependencyPath.getLeaf();
      mediation.put(dependencyPath);
      if (mediation.selects(artifact)) {
        // We include multiple dependency paths to the first version of an artifact we see,
        // but not paths to other versions of that artifact.
        multimap.put(new ClassPathEntry(artifact), dependencyPath);
      }
    }
    return new ClassPathResult(multimap, result.getUnresolvedArtifacts());
  }
}

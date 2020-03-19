/*
 * Copyright 2020 Google LLC.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.google.common.testing.EqualsTester;
import java.nio.file.Paths;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.Test;

public class AnnotatedJarTest {

  @Test
  public void testGetters() {
    AnnotatedJar jar = new AnnotatedJar(Paths.get("foo"), new DefaultArtifact("com.foo:bar:1.0.0"));
    assertEquals(Paths.get("foo"), jar.getJar());
    assertEquals(new DefaultArtifact("com.foo:bar:1.0.0"), jar.getArtifact());
  }

  @Test
  public void testNullArtifact() {
    AnnotatedJar jar = new AnnotatedJar(Paths.get("foo"));
    assertNull(jar.getArtifact());
  }

  @Test
  public void testNullJar() {
    try {
      new AnnotatedJar(null, new DefaultArtifact("com.foo:bar:1.0.0"));
      fail("AnnotatedJar should invalidate null jar file");
    } catch (NullPointerException expected) {
      // pass
    }
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new AnnotatedJar(Paths.get("foo"), new DefaultArtifact("com.foo:bar:1.0.0")),
            new AnnotatedJar(Paths.get("foo"), new DefaultArtifact("com.foo:bar:1.0.0")))
        .addEqualityGroup(
            new AnnotatedJar(Paths.get("foo"), null), new AnnotatedJar(Paths.get("foo"), null))
        .addEqualityGroup(
            new AnnotatedJar(Paths.get("foo", "bar"), null),
            new AnnotatedJar(Paths.get("foo", "bar"), null))
        .testEquals();
  }
}

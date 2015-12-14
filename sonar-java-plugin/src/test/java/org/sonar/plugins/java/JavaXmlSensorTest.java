/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.java;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputPath;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.RuleAnnotationUtils;
import org.sonar.java.SonarComponents;
import org.sonar.java.checks.maven.PomElementOrderCheck;
import org.sonar.plugins.java.api.JavaCheck;
import org.sonar.squidbridge.api.CodeVisitor;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JavaXmlSensorTest {

  private final DefaultFileSystem fileSystem = new DefaultFileSystem(null);
  private JavaXmlSensor sensor;

  @Before
  public void setUp() {
    sensor = new JavaXmlSensor(mock(SonarComponents.class), fileSystem);
  }

  @Test
  public void should_execute_on_project_having_xml() {
    Project project = mock(Project.class);
    fileSystem.add(new DefaultInputFile("fake.java").setLanguage(Java.KEY));
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();

    fileSystem.add(new DefaultInputFile("fake.xml").setLanguage(Java.KEY));
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();

    fileSystem.add(new DefaultInputFile("pom.xml").setLanguage(Java.KEY));
    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
  }

  @Test
  public void test_issues_creation() throws Exception {
    DefaultFileSystem fs = new DefaultFileSystem(new File(""));
    File file = new File("pom.xml");
    fs.add(new DefaultInputFile(file.getPath()).setFile(file).setLanguage(Java.KEY));
    Project project = mock(Project.class);

    SonarComponents sonarComponents = createSonarComponentsMock(fs);
    JavaXmlSensor mps = new JavaXmlSensor(sonarComponents, fs);

    SensorContext context = mock(SensorContext.class);
    when(context.getResource(any(InputPath.class))).thenReturn(org.sonar.api.resources.File.create("pom.xml"));

    mps.analyse(project, context);
  }

  private static SonarComponents createSonarComponentsMock(DefaultFileSystem fs) {
    SonarComponents sonarComponents = mock(SonarComponents.class);
    PomElementOrderCheck check = new PomElementOrderCheck();
    when(sonarComponents.checkClasses()).thenReturn(new CodeVisitor[] {check});

    when(sonarComponents.fileLinesContextFor(any(File.class))).thenReturn(mock(FileLinesContext.class));

    when(sonarComponents.getFileSystem()).thenReturn(fs);

    Checks<JavaCheck> checks = mock(Checks.class);
    when(checks.ruleKey(any(JavaCheck.class))).thenReturn(RuleKey.of("java-xml", RuleAnnotationUtils.getRuleKey(PomElementOrderCheck.class)));
    when(sonarComponents.checks()).thenReturn(Lists.<Checks<JavaCheck>>newArrayList(checks));

    return sonarComponents;
  }
}

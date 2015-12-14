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

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.resources.Project;
import org.sonar.java.SonarComponents;
import org.sonar.java.checks.CheckList;
import org.sonar.maven.MavenFileSearchEngine;

import java.io.File;

public class JavaXmlSensor implements Sensor {

  private final FileSystem fs;
  private final FilePredicates pred;
  private final SonarComponents sonarComponents;

  public JavaXmlSensor(SonarComponents sonarComponents, FileSystem fs) {
    this.fs = fs;
    this.pred = fs.predicates();
    this.sonarComponents = sonarComponents;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fs.hasFiles(pomFilePredicate());
  }

  @Override
  public void analyse(Project module, SensorContext context) {
    sonarComponents.registerCheckClasses(CheckList.REPOSITORY_KEY, CheckList.getXmlChecks());
    scanPomFiles();
  }

  private void scanPomFiles() {
    new MavenFileSearchEngine(sonarComponents, sonarComponents.checkClasses()).scan(getPomFiles());
  }

  private Iterable<File> getPomFiles() {
    return fs.files(pomFilePredicate());
  }

  private FilePredicate pomFilePredicate() {
    return pred.matchesPathPattern("**/pom.xml");
  }

}

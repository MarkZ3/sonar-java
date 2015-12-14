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
package org.sonar.maven;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.sonar.java.SonarComponents;
import org.sonar.java.ast.visitors.VisitorContext;
import org.sonar.maven.model.maven2.MavenProject;
import org.sonar.squidbridge.ProgressReport;
import org.sonar.squidbridge.api.CodeVisitor;
import org.sonar.squidbridge.api.Query;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceCodeSearchEngine;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceProject;
import org.sonar.squidbridge.indexer.SquidIndex;

import javax.annotation.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MavenFileSearchEngine implements SourceCodeSearchEngine {

  private final SonarComponents sonarComponents;
  private final List<MavenFileScanner> scanners;
  private final SquidIndex index;

  public MavenFileSearchEngine(@Nullable SonarComponents sonarComponents, CodeVisitor... visitors) {
    ImmutableList.Builder<MavenFileScanner> scannersBuilder = ImmutableList.builder();
    for (CodeVisitor visitor : visitors) {
      if (visitor instanceof MavenFileScanner) {
        scannersBuilder.add((MavenFileScanner) visitor);
      }
    }
    this.scanners = scannersBuilder.build();
    this.sonarComponents = sonarComponents;
    this.index = new SquidIndex();
  }

  public void scan(Iterable<File> files) {
    SourceProject project = new SourceProject("Java Project");
    index.index(project);
    project.setSourceCodeIndexer(index);
    VisitorContext context = new VisitorContext(project);

    ProgressReport progressReport = new ProgressReport("Report about progress of Maven Pom analyzer", TimeUnit.SECONDS.toMillis(10));
    progressReport.start(Lists.newArrayList(files));

    boolean successfulyCompleted = false;
    try {
      for (File file : files) {
        simpleScan(file, context);
        progressReport.nextFile();
      }
      successfulyCompleted = true;
    } finally {
      if (successfulyCompleted) {
        progressReport.stop();
      } else {
        progressReport.cancel();
      }
    }

  }

  private void simpleScan(File file, VisitorContext context) {
    context.setFile(file);
    MavenProject project = MavenParser.parseXML(file);
    if (project != null) {
      MavenFileScannerContext scannerContext = new MavenFileScannerContextImpl(project, (SourceFile) context.peekSourceCode(), context.getFile(), sonarComponents);
      for (MavenFileScanner mavenFileScanner : scanners) {
        mavenFileScanner.scanFile(scannerContext);
      }
    }
  }

  @Override
  public SourceCode search(String key) {
    return index.search(key);
  }

  @Override
  public Collection<SourceCode> search(Query... query) {
    return index.search(query);
  }

}

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

import com.google.common.annotations.Beta;
import org.sonar.maven.model.LocatedTree;
import org.sonar.maven.model.maven2.MavenProject;

import java.util.List;

@Beta
public interface MavenFileScannerContext {

  MavenProject getMavenProject();

  void reportIssueOnFile(MavenCheck check, String message);

  void reportIssue(MavenCheck check, LocatedTree tree, String message);

  void reportIssue(MavenCheck check, int line, String message);

  void reportIssue(MavenCheck check, int line, String message, List<Location> secondary);

  class Location {
    public final String msg;
    public final LocatedTree tree;

    public Location(String msg, LocatedTree tree) {
      this.msg = msg;
      this.tree = tree;
    }
  }

}
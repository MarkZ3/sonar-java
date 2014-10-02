/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
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
package org.sonar.java.checks;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.sonar.java.JavaAstScanner;
import org.sonar.java.model.VisitorsBridge;

import java.io.File;
import java.util.Set;

public class PackageInfoCheckTest {

  @Test
  public void test() throws Exception {
    PackageInfoCheck check = new PackageInfoCheck();
    JavaAstScanner.scanSingleFile(new File("src/test/files/checks/packageInfo/package-info.java"), new VisitorsBridge(check));
    Set<File> set = check.getDirectoriesWithPackageFile();
    Assertions.assertThat(set).hasSize(1);
    Assertions.assertThat(set.iterator().next().getName()).isEqualTo("packageInfo");

  }
}
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
package org.sonar.java.checks.verifier;

import com.google.common.annotations.Beta;
import org.fest.assertions.Fail;
import org.sonar.java.AnalyzerMessage;
import org.sonar.maven.MavenCheck;
import org.sonar.maven.MavenFileScanner;
import org.sonar.maven.MavenFileScannerContext;
import org.sonar.maven.MavenParser;
import org.sonar.maven.model.LocatedTree;
import org.sonar.maven.model.maven2.MavenProject;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;

@Beta
public class MavenCheckVerifier extends CheckVerifier {

  private MavenCheckVerifier() {
  }

  @Override
  public String getExpectedIssueTrigger() {
    return ISSUE_MARKER;
  }

  public static void verify(String filename, MavenFileScanner check) {
    MavenCheckVerifier mavenCheckVerifier = new MavenCheckVerifier();
    scanFile(filename, check, mavenCheckVerifier);
  }

  public static void verifyNoIssue(String filename, MavenFileScanner check) {
    MavenCheckVerifier mavenCheckVerifier = new MavenCheckVerifier();
    mavenCheckVerifier.expectNoIssues();
    scanFile(filename, check, mavenCheckVerifier);
  }

  public static void verifyIssueOnFile(String filename, String message, MavenFileScanner check) {
    MavenCheckVerifier mavenCheckVerifier = new MavenCheckVerifier();
    mavenCheckVerifier.setExpectedFileIssue(message);
    scanFile(filename, check, mavenCheckVerifier);
  }

  private static void scanFile(String filename, MavenFileScanner check, MavenCheckVerifier mavenCheckVerifier) {
    File pom = new File(filename);
    MavenProject project = MavenParser.parseXML(pom);
    if (project != null) {
      FakeMavenFileScannerContext context = new FakeMavenFileScannerContext(pom, project);
      check.scanFile(context);
      retrieveExpectedIssuesFromFile(pom, mavenCheckVerifier);
      mavenCheckVerifier.checkIssues(context.messages, false);
    } else {
      Fail.fail("The test file can not be parsed");
    }
  }

  private static void retrieveExpectedIssuesFromFile(File pom, MavenCheckVerifier mavenCheckVerifier) {
    try (FileInputStream is = new FileInputStream(pom)) {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      XMLStreamReader reader = factory.createXMLStreamReader(is);

      while (reader.hasNext()) {
        int line = reader.getLocation().getLineNumber();
        reader.next();
        if (reader.getEventType() == XMLStreamReader.COMMENT) {
          String text = reader.getText().trim();
          mavenCheckVerifier.collectExpectedIssues(text, line);
        }
      }
    } catch (XMLStreamException | IOException e) {
      Fail.fail("The test file can not be parsed to retrieve comments", e);
    }
  }

  private static class FakeMavenFileScannerContext implements MavenFileScannerContext {

    private final File file;
    private final MavenProject project;
    private final HashSet<AnalyzerMessage> messages = new HashSet<>();

    public FakeMavenFileScannerContext(File file, MavenProject project) {
      this.file = file;
      this.project = project;
    }

    @Override
    public MavenProject getMavenProject() {
      return project;
    }

    @Override
    public void reportIssueOnFile(MavenCheck check, String message) {
      messages.add(new AnalyzerMessage(check, file, -1, message, 0));
    }

    @Override
    public void reportIssue(MavenCheck check, LocatedTree tree, String message) {
      messages.add(new AnalyzerMessage(check, file, tree.startLocation().line(), message, 0));
    }

    @Override
    public void reportIssue(MavenCheck check, int line, String message) {
      messages.add(new AnalyzerMessage(check, file, line, message, 0));
    }
  }
}

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
package org.sonar.java.checks;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.java.tag.Tag;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = "S1194",
  name = "\"java.lang.Error\" should not be extended",
  priority = Priority.CRITICAL,
  tags = {Tag.ERROR_HANDLING, Tag.SECURITY})
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.EXCEPTION_HANDLING)
@SqaleConstantRemediation("10min")
public class ErrorClassExtendedCheck extends BaseTreeVisitor implements JavaFileScanner {

  private JavaFileScannerContext context;

  @Override
  public void scanFile(JavaFileScannerContext context) {
    this.context = context;
    scan(context.getTree());
  }

  @Override
  public void visitClass(ClassTree tree) {
    if (tree.is(Tree.Kind.CLASS) && tree.superClass() != null) {
      if (tree.superClass().is(Tree.Kind.IDENTIFIER)) {
        IdentifierTree idt = (IdentifierTree) tree.superClass();
        if ("Error".equals(idt.name())) {
          context.addIssue(tree, this, "Extend \"java.lang.Exception\" or one of its subclasses.");
        }
      } else if (tree.superClass().is(Tree.Kind.MEMBER_SELECT)) {
        MemberSelectExpressionTree mse = (MemberSelectExpressionTree) tree.superClass();
        if ("Error".equals(mse.identifier().name()) && isJavaLang(mse.expression())) {
          context.addIssue(tree, this, "Extend \"java.lang.Exception\" or one of its subclasses.");
        }
      }
    }
    super.visitClass(tree);
  }

  private static boolean isJavaLang(ExpressionTree tree) {
    if (tree.is(Tree.Kind.MEMBER_SELECT)) {
      MemberSelectExpressionTree mse = (MemberSelectExpressionTree) tree;
      if (!"lang".equals(mse.identifier().name())) {
        return false;
      }
      if (mse.expression().is(Tree.Kind.IDENTIFIER)) {
        IdentifierTree idt = (IdentifierTree) mse.expression();
        return "java".equals(idt.name());
      }
    }
    return false;
  }

}

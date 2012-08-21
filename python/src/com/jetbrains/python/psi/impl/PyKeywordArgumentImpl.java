package com.jetbrains.python.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.psi.LanguageLevel;
import com.jetbrains.python.psi.PyElementGenerator;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyKeywordArgument;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class PyKeywordArgumentImpl extends PyElementImpl implements PyKeywordArgument {
  public PyKeywordArgumentImpl(ASTNode astNode) {
    super(astNode);
  }

  @Nullable
  public String getKeyword() {
    ASTNode node = getKeywordNode();
    return node != null ? node.getText() : null;
  }

  @Override
  public ASTNode getKeywordNode() {
    return getNode().findChildByType(PyTokenTypes.IDENTIFIER);
  }

  @Override
  public PyExpression getValueExpression() {
    return PsiTreeUtil.getChildOfType(this, PyExpression.class);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + ": " + getKeyword();
  }

  public PyType getType(@NotNull TypeEvalContext context) {
    final PyExpression e = getValueExpression();
    return e != null ? e.getType(context) : null;
  }

  @Override
  public PsiReference getReference() {
    final ASTNode keywordNode = getKeywordNode();
    if (keywordNode != null) {
      return new PyKeywordArgumentReference(this, keywordNode.getTextRange().shiftRight(-getTextRange().getStartOffset()));
    }
    return null;
  }

  @Override
  public String getName() {
    return getKeyword();
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    PyElementGenerator generator = PyElementGenerator.getInstance(getProject());
    PyExpression expression = getValueExpression();
    PyKeywordArgument keywordArgument = generator.createKeywordArgument(LanguageLevel.forElement(this), name,
                                                                        expression != null ? expression.getText() : name);
    getNode().replaceChild(getKeywordNode(), keywordArgument.getKeywordNode());
    return this;
  }
}

/*
 * Copyright (C) 2018-2024 University of Waterloo.
 *
 * This file is part of Perses.
 *
 * Perses is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3, or (at your option) any later version.
 *
 * Perses is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Perses; see the file LICENSE.  If not see <http://www.gnu.org/licenses/>.
 */
package org.perses.reduction.reducer

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import org.apache.commons.text.StringEscapeUtils
import org.perses.antlr.GrammarHierarchy
import org.perses.antlr.ast.AbstractPersesRuleElement
import org.perses.antlr.ast.AstTag
import org.perses.antlr.ast.PersesAlternativeBlockAst
import org.perses.antlr.ast.PersesPlusAst
import org.perses.antlr.ast.PersesRuleReferenceAst
import org.perses.antlr.ast.PersesSequenceAst
import org.perses.antlr.ast.PersesTerminalAst
import org.perses.antlr.ast.RuleNameRegistry.RuleNameHandle
import org.perses.grammar.AbstractParserFacade
import org.perses.spartree.AbstractNodePayload
import org.perses.spartree.AbstractSparTreeNode
import org.perses.spartree.AbstractTreeNode.NodeIdCopyStrategy.ReuseNodeIdStrategy
import org.perses.spartree.SparTreeNodeFactory
import org.perses.spartree.SparTreeSimplifier
import org.perses.util.SimpleStack
import org.perses.util.Util.lazyAssert
import org.perses.util.toImmutableList
import org.perses.util.transformToImmutableList

class MinimalSparTreeGenerator(
  private val facade: AbstractParserFacade,
  private val sparTreeNodeFactory: SparTreeNodeFactory,
  private val maxRuleDepth: Int = 2,
) {

  internal val grammarHierarchy: GrammarHierarchy
    get() = sparTreeNodeFactory.grammarHierarchy

  fun generateForRule(ruleName: String): GenerationResult {
    val stack = SimpleStack<RuleNameHandle>()
    val initialResult = generateForBoundedRule(ruleName, stack)
    val result = initialResult
      .onEach {
        SparTreeSimplifier.removeEmptyAndDeletedRuleNodes(it)
        it.buildTokenIntervalInfoRecursive()
        it.updateLeafTokenCount()
      }.filter {
        it.leafTokenCount != 0
      }.toImmutableList()
    return GenerationResult(
      nonEmptyNodes = result,
      acceptEpsilon = (result.size != initialResult.size),
    )
  }

  private fun generateForBoundedRule(
    ruleName: String,
    ruleStack: SimpleStack<RuleNameHandle>,
  ): ImmutableList<AbstractSparTreeNode> {
    val rule = grammarHierarchy.getRuleHierarchyEntryWithNameOrThrow(ruleName)
    val ruleNameHandle = rule.ruleDef.ruleNameHandle
    ruleStack.add(ruleNameHandle)
    try {
      if (ruleStack.count(ruleNameHandle) > maxRuleDepth) {
        return ImmutableList.of()
      }
      val body = rule.ruleDef.body
      if (rule.ruleDef.isLexerRule) {
        if (body !is PersesTerminalAst) {
          return ImmutableList.of()
        }
        return generateForLexerTerminalAst(body)
          .transformToImmutableList { it.value }
          .also { result ->
            lazyAssert {
              result.isNotEmpty() &&
                result.all { node -> node.ruleName == ruleName }
            }
          }
      }
      check(rule.ruleDef.isParserRule)
      val candidates = dispatchGeneration(
        rule.ruleDef.body,
        ruleStack,
      )
      return candidates.transformToImmutableList { candidate ->
        val currentNode = sparTreeNodeFactory.createParserRuleSparTreeNode(ruleName)
        candidate.forEach { child ->
          currentNode.addChild(child, AbstractNodePayload.SinglePayload(rule))
        }
        currentNode
      }
    } finally {
      check(ruleStack.peek() == ruleNameHandle)
      ruleStack.remove()
    }
  }

  internal fun dispatchGeneration(
    ast: AbstractPersesRuleElement,
    ruleStack: SimpleStack<RuleNameHandle>,
  ): ImmutableList<out AbstractNodeSequence> {
    println("dispatching $ast")
    return when (ast.tag) {
      AstTag.STAR -> ImmutableList.of(EpsilonNodeSequence)
      AstTag.OPTIONAL -> ImmutableList.of(EpsilonNodeSequence)
      AstTag.PLUS -> {
        dispatchGeneration((ast as PersesPlusAst).body, ruleStack)
      }

      AstTag.TERMINAL -> generateForLexerTerminalAst(ast as PersesTerminalAst)
      AstTag.SEQUENCE -> generateForSequenceAst(ast as PersesSequenceAst, ruleStack)
      AstTag.RULE_REF -> generateForRuleRefAst(ast as PersesRuleReferenceAst, ruleStack)
        .transformToImmutableList {
          SingleNodeSequence(it)
        }

      AstTag.ALTERNATIVE_BLOCK -> generateForAltBlockAst(
        ast as PersesAlternativeBlockAst,
        ruleStack,
      )

      else -> error("unhandled $ast")
    }
  }

  private fun generateForAltBlockAst(
    ast: PersesAlternativeBlockAst,
    ruleStack: SimpleStack<RuleNameHandle>,
  ): ImmutableList<AbstractNodeSequence> {
    return ast.alternatives
      .asSequence()
      .flatMap { dispatchGeneration(it, ruleStack) }
      .toImmutableList()
  }

  internal fun generateForRuleRefAst(
    rule: PersesRuleReferenceAst,
    ruleStack: SimpleStack<RuleNameHandle>,
  ): ImmutableList<AbstractSparTreeNode> {
    return generateForBoundedRule(rule.ruleNameHandle.ruleName, ruleStack)
  }

  private fun generateForSequenceAst(
    rule: PersesSequenceAst,
    ruleStack: SimpleStack<RuleNameHandle>,
  ): ImmutableList<AbstractNodeSequence> {
    val children = Lists.cartesianProduct(
      rule.children.transformToImmutableList { child ->
        dispatchGeneration(child, ruleStack)
      },
    ).transformToImmutableList { list ->
      AbstractNodeSequence.create(list.flatMap { it.asSequence() }.toImmutableList())
    }
    return convertToListOfNodeSequences(children)
  }

  private fun convertToListOfNodeSequences(
    listOfSequences: List<AbstractNodeSequence>,
  ): ImmutableList<AbstractNodeSequence> {
    val used = mutableSetOf<AbstractSparTreeNode>()
    val result = ImmutableList.builder<AbstractNodeSequence>()
    for (sequence in listOfSequences) {
      val nodeSequence = AbstractNodeSequence.create(
        sequence.asSequence()
          .map { node ->
            if (used.add(node)) {
              node
            } else {
              node.recursiveDeepCopy(ReuseNodeIdStrategy)
            }
          }
          .toImmutableList(),
      )
      result.add(nodeSequence)
    }
    return result.build()
  }

  internal fun generateForLexerTerminalAst(
    rule: PersesTerminalAst,
  ): ImmutableList<SingleNodeSequence> {
    return when {
      rule.isStringLiteral() -> {
        val stringLiteral = StringEscapeUtils.unescapeJava(rule.getStringLiteralOrThrow())
        val antlrToken = facade.transformLiteralIntoSingleToken(
          stringLiteral,
        )
        return ImmutableList.of(
          SingleNodeSequence(
            sparTreeNodeFactory.createLexerRuleSparTreeNode(
              antlrToken,
            ),
          ),
        )
      }

      rule.isEOF() -> ImmutableList.of()
      else -> {
        val body = grammarHierarchy.getRuleHierarchyEntryWithNameOrThrow(rule.text)
          .ruleDef.body
        if (body !is PersesTerminalAst) {
          // Such as identifiers.
          ImmutableList.of()
        } else {
          generateForLexerTerminalAst(body)
        }
      }
    }
  }

  sealed class AbstractNodeSequence {
    abstract fun forEach(action: (AbstractSparTreeNode) -> Unit)

    abstract fun asSequence(): Sequence<AbstractSparTreeNode>

    companion object {
      fun create(value: ImmutableList<AbstractSparTreeNode>): AbstractNodeSequence {
        return when (value.size) {
          0 -> error("empty")
          1 -> SingleNodeSequence(value.single())
          else -> NodeSequence(value)
        }
      }
    }
  }

  data class SingleNodeSequence(val value: AbstractSparTreeNode) : AbstractNodeSequence() {
    override fun forEach(action: (AbstractSparTreeNode) -> Unit) {
      action(value)
    }

    override fun asSequence(): Sequence<AbstractSparTreeNode> {
      return sequenceOf(value)
    }
  }

  object EpsilonNodeSequence : AbstractNodeSequence() {
    override fun forEach(action: (AbstractSparTreeNode) -> Unit) {
      // Do nothing.
    }

    override fun asSequence(): Sequence<AbstractSparTreeNode> {
      return sequenceOf()
    }
  }

  data class NodeSequence(val value: ImmutableList<AbstractSparTreeNode>) : AbstractNodeSequence() {
    init {
      require(value.size > 1)
    }

    override fun forEach(action: (AbstractSparTreeNode) -> Unit) {
      value.forEach(action)
    }

    override fun asSequence(): Sequence<AbstractSparTreeNode> {
      return value.asSequence()
    }
  }

  data class GenerationResult(
    val nonEmptyNodes: ImmutableList<AbstractSparTreeNode>,
    val acceptEpsilon: Boolean,
  ) {
    init {
      lazyAssert {
        nonEmptyNodes.none {
          it.updateLeafTokenCount()
          it.leafTokenCount == 0
        }
      }
    }
  }
}

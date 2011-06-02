package jgo.compiler
package parser
package exprs

import interm._
import types._
import symbol._
import expr._
import expr.Combinators

trait Operands extends CompositeLiterals with ExprUtils /*with FunctionLiterals*/ {
  self: Expressions =>
  
  //in general, "E = E ~ t2 | t1" MUST be used instead of "E = t1 | E ~ t2"
  lazy val operand: Rule[Expr] =                       "operand" $
    ( "(" ~> expression <~ ")"
//  | methodAsFunc
    | literal
    | InPos ~ symbol  ^^ procSymbOperand //yes, this *must* be last, to prevent preemptive prefix-matching
    | failure("not an operand")
    )
  
  lazy val literal: Rule[Expr] =                 "literal value" $
    ( intLit       ^^ { i => Result(UntypedIntegralConst(i)) }
    | floatLit     ^^ { f => Result(UntypedFloatingConst(f)) }
//  | imaginaryLit
    | charLit      ^^ { c => Result(UntypedIntegralConst(c)) }
    | stringLit    ^^ { s => Result(UntypedStringConst(s)) }
//  | compositeLit //nonterminal
//  | functionLit  //nonterminal
    )
  
  protected def procSymbOperand(pos: Pos, symbM: M[Symbol]): M[Expr] =
    symbM flatMap {
      case ConstSymbol(c) => Result(c)
      case v: Variable    => Result(Combinators.fromVariable(v))
      case f: Function    => Result(Combinators.fromFunction(f))
      case s => Problem("invalid operand: not a variable, constant, or function: %s", s)(pos)
    }
}

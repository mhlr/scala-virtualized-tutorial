package org.scala_lang.virtualized.patmat

import org.scala_lang.virtualized.prob.ProbLang
import org.scala_lang.virtualized.prob.ProbCore
import org.scala_lang.virtualized.prob.ProbCoreLazy
import org.scala_lang.virtualized.prob.ProbPrettyPrint


trait ProbRuleLang extends ProbLang {
  object __match {
    def one[T](x: T): Rand[T] = always(x)
    def zero = never
    def guard[T](cond: Boolean, result: => T): Rand[T] =
      if (cond) one(result) else zero
    def runOrElse[T, U](in: T)(matcher: T => Rand[U]): Rand[U] =
      matcher(in)
  }

  implicit class Rule[A,B](f: A => Rand[B]) {
    def unapply(x: A): Rand[B] = f(x)
  }

  implicit class SRule(f: String => Rand[String]) {
    def unapply(x: String): Rand[String] = f(x)
  }


  def rule[A,B](f: A => Rand[B]) = new Rule[A,B](f)

  def infix_rule[A, B](f: A => Rand[B]): Rule[A,B] = new Rule(f)

  val && = ((x: Any) => x match {
    case x => (x,x)
  }) rule

  val Likes: SRule = { x: String => x match {
    case "A" => "Coffee"
    case "B" => "Coffee"
    case "D" => "Coffee"
    case "D" => "Coffee" // likes coffee very much!
    case "E" => "Coffee"
  }}

  val Friend: SRule = { x: String => x match {
    case "A" => "C"
    case "A" => "C" // are really good friends!
    case "C" => "D"
    case "B" => "D"
    case "A" => "E"
  }}

  val Knows: SRule = { x: String => x match {
    case Friend(Knows(y)) => y
    case x => x
  }}

  val ShouldGrabCoffee: SRule = { x: String => x match {
    case Likes("Coffee") && Knows(y @ Likes("Coffee")) if x != y =>
      x + " and " + y + " should grab coffee"
  }}


  val coffeeModel1: Rand[String] = uniform("A","B","C","D","E").flatMap({ case ShouldGrabCoffee(y) => always(y) }).flatMap(x=>x)

}

trait RunTests extends ProbRuleLang with ProbPrettyPrint {
  show(coffeeModel1, "coffeeModel1")
}

object LogicTest extends App {
  new RunTests with ProbCore {}
  new RunTests with ProbCoreLazy {}
}

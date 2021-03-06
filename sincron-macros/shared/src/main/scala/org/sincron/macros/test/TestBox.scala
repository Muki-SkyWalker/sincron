/*
 * Copyright (c) 2016 by its authors. Some rights reserved.
 * See the project homepage at: https://sincron.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sincron.macros.test

import org.sincron.macros.{HygieneUtilMacros, InlineMacros}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/** Represents a boxed value, to be used in the testing
  * of [[InlineMacros]].
  */
private[sincron] final case class TestBox[A](value: A) {
  def map[B](f: A => B): TestBox[B] = macro TestBox.Macros.mapMacroImpl[A,B]
}

/** Represents a boxed value, to be used in the testing
  * of [[InlineMacros]].
  */
private[sincron] object TestBox {
  @macrocompat.bundle
  class Macros(override val c: whitebox.Context)
    extends InlineMacros with HygieneUtilMacros {
    import c.universe._

    def mapMacroImpl[A : c.WeakTypeTag, B : c.WeakTypeTag]
      (f: c.Expr[A => B]): c.Expr[TestBox[B]] = {

      val selfExpr = c.Expr[TestBox[A]](c.prefix.tree)

      val tree =
        if (util.isClean(f)) {
          q"""
          TestBox($f($selfExpr.value))
          """
        }
        else {
          val fn = util.name("fn")
          q"""
          val $fn = $f
          TestBox($fn($selfExpr.value))
          """
        }

      inlineAndReset[TestBox[B]](tree)
    }
  }
}

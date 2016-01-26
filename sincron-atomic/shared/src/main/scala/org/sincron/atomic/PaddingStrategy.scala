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

package org.sincron.atomic


/** For applying padding to atomic references, in order to reduce
  * cache contention. JEP 142 should reduce the need for this along
  * with the `@Contended` annotation, however that might have
  * security restrictions, the runtime might not act on it since it's
  * just a recommendation, plus it's nice to provide backwards
  * compatibility.
  *
  * See: http://mail.openjdk.java.net/pipermail/hotspot-dev/2012-November/007309.html
  *
  * The default strategy is [[PaddingStrategy.NoPadding NoPadding]].
  * In order to apply padding, you can import an implicit into scope:
  * {{{
  *   import import org.sincron.atomic.Atomic
  *   import org.sincron.atomic.PaddingStrategy.Right64
  *
  *   val paddedAtomic = Atomic(10)
  * }}}
  */
sealed trait PaddingStrategy

object PaddingStrategy {
  /** A [[PaddingStrategy]] that specifies no padding should be applied.
    * This is the default.
    */
  case object NoPadding extends PaddingStrategy

  /** A [[PaddingStrategy]] that applies padding to the left of our
    * atomic value for a total cache line of 64 bytes (8 longs).
    *
    * Note the actual padding applied will be less, like 48 or 52 bytes,
    * because we take into account the object's header and the
    * the stored value.
    */
  case object Left64 extends PaddingStrategy

  /** A [[PaddingStrategy]] that applies padding to the right of our
    * atomic value for a total cache line of 64 bytes (8 longs).
    *
    * Note the actual padding applied will be less, like 48 or 52 bytes,
    * because we take into account the object's header and the
    * the stored value.
    */
  case object Right64 extends PaddingStrategy

  /** A [[PaddingStrategy]] that applies padding both to the left
    * and to the right of our atomic value for a total cache
    * line of 128 bytes (16 longs).
    *
    * Note the actual padding applied will be less, like 112 or 116 bytes,
    * because we take into account the object's header and the stored value.
    */
  case object LeftRight128 extends PaddingStrategy

  /** Implicits for overriding the default strategy in scope. */
  object Implicits {
    implicit def NoPadding = PaddingStrategy.NoPadding
    implicit def Left64 = PaddingStrategy.Left64
    implicit def Right64 = PaddingStrategy.Right64
    implicit def LeftRight128 = PaddingStrategy.LeftRight128
  }

  /** Implicit [[PaddingStrategy]] used by default if no strategy is in scope. */
  implicit def default: PaddingStrategy =
    NoPadding
}
/*
 * Copyright (c) 2016 by its authors. Some rights reserved.
 * See the project's homepage at: https://github.com/monifu/scalax
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

package scalax.concurrent.cancelables

import scalax.concurrent.Cancelable
import scalax.concurrent.atomic.Atomic
import scala.annotation.tailrec

/** Represents a [[scalax.concurrent.Cancelable]] whose underlying cancelable
  * reference can be swapped for another.
  *
  * Example:
  * {{{
  *   val s = MultiAssignmentCancelable()
  *   s := c1 // sets the underlying cancelable to c1
  *   s := c2 // swaps the underlying cancelable to c2
  *
  *   s.cancel() // also cancels c2
  *
  *   s := c3 // also cancels c3, because s is already canceled
  * }}}
  *
  * Also see [[SerialCancelable]], which is similar, except that it cancels the
  * old cancelable upon assigning a new cancelable.
  */
final class MultiAssignmentCancelable private ()
  extends AssignableCancelable {

  import MultiAssignmentCancelable.State
  import MultiAssignmentCancelable.State._

  private[this] val state = Atomic(Active(Cancelable()) : State)

  def isCanceled: Boolean = state.get match {
    case Cancelled => true
    case _ => false
  }

  @tailrec
  def cancel(): Boolean = state.get match {
    case Cancelled => false
    case current @ Active(s) =>
      if (state.compareAndSet(current, Cancelled)) {
        s.cancel()
        true
      }
      else
        cancel()
  }

  /** Swaps the underlying cancelable reference with `s`.
    *
    * In case this `MultiAssignmentCancelable` is already canceled,
    * then the reference `value` will also be canceled on assignment.
    *
    * @return `this`
    */
  @tailrec def `:=`(value: Cancelable): this.type = {
    state.get match {
      case Cancelled =>
        value.cancel()
        this

      case current @ Active(_) =>
        if (!state.compareAndSet(current, Active(value)))
          :=(value)
        else
          this
    }
  }
}

object MultiAssignmentCancelable {
  def apply(): MultiAssignmentCancelable =
    new MultiAssignmentCancelable()

  def apply(s: Cancelable): MultiAssignmentCancelable = {
    val ms = new MultiAssignmentCancelable()
    ms := s
  }

  private[scalax] sealed trait State
  private[scalax] object State {
    case class Active(s: Cancelable) extends State
    case object Cancelled extends State
  }
}
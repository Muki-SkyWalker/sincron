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

/** Optimized implementation of a simple [[BooleanCancelable]],
  * a cancelable that doesn't trigger any actions on cancel, it just
  * mutates its internal `isCanceled` field, which might be visible
  * at some point.
  *
  * Its `cancel()` always returns false, always.
  */
private[cancelables] class WeakBooleanCancelable private ()
  extends BooleanCancelable {

  private[this] var _isCanceled = false
  def isCanceled = _isCanceled

  def cancel(): Boolean = {
    _isCanceled = true
    false
  }
}

private[cancelables] object WeakBooleanCancelable {
  def apply(): BooleanCancelable =
    new WeakBooleanCancelable
}
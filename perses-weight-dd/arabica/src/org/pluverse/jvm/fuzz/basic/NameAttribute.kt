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
package org.pluverse.jvm.fuzz.basic

enum class NameAttribute(val value: Int) {
  LOCAL(1), // name is localized to given context
  ARGUMENT(2), // name of method argument
  NULL(4), // name of var/arr assigned zero/null value for spec cases
  STATIC(8), // name of static field
  AUXILIARY(16), // name of auxiliary variable
  CLASS(32), // class member
  PUBLIC(64), // public member
  FINAL(128), // final member
  SUB(256), // local field of a local object
  NOTNULL(512), // variable must not be null
  BLOCK(1024), // block local variable
  VOLATILE(2048), // volatile variable
  CHECKSUM(4096), // checksum variable
  INDUCTION(8192), // Induction variable
  HIDDEN(16384), // Override member from parent
}

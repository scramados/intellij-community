/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.formatting;

import com.intellij.openapi.diagnostic.Logger;

/**
 * The wrap setting for a formatting model block. Indicates the conditions under which a line break
 * is inserted before the block when formatting, if the block extends beyond the
 * right margin.
 *
 * @see com.intellij.formatting.Block#getWrap()
 */

public abstract class Wrap {
  /**
   * Converts a low-priority wrap setting to a regular wrap setting.
   * @see #createChildWrap(Wrap, WrapType, boolean)
   */
  public abstract void ignoreParentWraps();

  private static final Logger LOG = Logger.getInstance("#com.intellij.formatting.Wrap");

  private static WrapFactory myFactory;

  public static WrapType ALWAYS = WrapType.ALWAYS;
  public static WrapType NORMAL = WrapType.NORMAL;
  public static WrapType NONE = WrapType.NONE;
  public static WrapType CHOP_DOWN_IF_LONG = WrapType.CHOP_DOWN_IF_LONG;

  static void setFactory(WrapFactory factory) {
    myFactory = factory;
  }

  /**
   * Creates a block wrap setting of the specified type.
   *
   * @param type             the type of the wrap setting.
   * @param wrapFirstElement if true, the first element in a sequence of elements of the same type
   *                         is also wrapped.
   * @return the wrap setting instance.
   */
  public static Wrap createWrap(final WrapType type, final boolean wrapFirstElement) {
    return myFactory.createWrap(type, wrapFirstElement);
  }

  /**
   * Creates a low priority wrap setting of the specified type. If the formatter detects that
   * the line should be wrapped at a location of the child wrap, the line is wrapped at the
   * position of its parent wrap instead.
   *
   * @param parentWrap       the parent for this wrap setting.
   * @param wrapType         the type of the wrap setting.
   * @param wrapFirstElement if true, the first element in a sequence of elements of the same type
   *                         is also wrapped.
   * @return the wrap setting instance.
   * @see #ignoreParentWraps() 
   */
  public static Wrap createChildWrap(final Wrap parentWrap, final WrapType wrapType, final boolean wrapFirstElement) {
    return myFactory.createChildWrap(parentWrap, wrapType, wrapFirstElement);    
  }

}

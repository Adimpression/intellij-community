/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.intellij.psi.impl.source.resolve.graphInference;

import com.intellij.psi.*;

import java.util.*;

/**
 * User: anna
 */
public class InferenceVariable {
  private boolean myCaptured;

  public PsiTypeParameter getParameter() {
    return myParameter;
  }

  private Map<InferenceBound, List<PsiType>> myBounds = new HashMap<InferenceBound, List<PsiType>>();
  private PsiTypeParameter myParameter;

  private PsiType myInstantiation;
  public InferenceVariable(PsiTypeParameter parameter) {
    myParameter = parameter;
  }
  public PsiType getInstantiation() {
    return myInstantiation;
  }

  public void setInstantiation(PsiType instantiation) {
    myInstantiation = instantiation;
  }

  public boolean isCaptured() {
    return myCaptured;
  }

  public void setCaptured(boolean captured) {
    myCaptured = captured;
  }

  public void addBound(PsiType classType, InferenceBound inferenceBound) {
    List<PsiType> list = myBounds.get(inferenceBound);
    if (list == null) {
      list = new ArrayList<PsiType>();
      myBounds.put(inferenceBound, list);
    }
    if (!list.contains(classType)) list.add(classType);
  }

  public List<PsiType> getBounds(InferenceBound inferenceBound) {
    final List<PsiType> bounds = myBounds.get(inferenceBound);
    return bounds != null ? new ArrayList<PsiType>(bounds) : Collections.<PsiType>emptyList();
  }
}

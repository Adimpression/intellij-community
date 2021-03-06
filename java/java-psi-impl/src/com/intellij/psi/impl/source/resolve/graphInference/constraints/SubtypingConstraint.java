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
package com.intellij.psi.impl.source.resolve.graphInference.constraints;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.graphInference.InferenceBound;
import com.intellij.psi.impl.source.resolve.graphInference.InferenceSession;
import com.intellij.psi.impl.source.resolve.graphInference.InferenceVariable;
import com.intellij.psi.util.TypeConversionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: anna
 */
public class SubtypingConstraint implements ConstraintFormula {
  private PsiType myS;
  private PsiType myT;
  private boolean myIsRefTypes;

  public SubtypingConstraint(@NotNull PsiType t, @NotNull PsiType s, boolean isRefTypes) {
    myT = t;
    myS = s;
    myIsRefTypes = isRefTypes;
  }

  @Override
  public boolean reduce(InferenceSession session, List<ConstraintFormula> constraints) {
    if (myIsRefTypes) {
      if (session.isProperType(myS) && session.isProperType(myT)) {
        return TypeConversionUtil.isAssignable(myT, myS);
      }
      InferenceVariable inferenceVariable = session.getInferenceVariable(myS);
      if (inferenceVariable != null) {
        inferenceVariable.addBound(myT, InferenceBound.UPPER);
        return true;
      }
      if (myS.equals(PsiType.NULL)) return true;
      inferenceVariable = session.getInferenceVariable(myT);
      if (inferenceVariable != null) {
        inferenceVariable.addBound(myS, InferenceBound.LOWER);
        return true;
      }
      if (myT instanceof PsiArrayType) {
        if (!(myS instanceof PsiArrayType)) return false; //todo most specific array supertype
        final PsiType tComponentType = ((PsiArrayType)myT).getComponentType();
        final PsiType sComponentType = ((PsiArrayType)myS).getComponentType();
        if (!(tComponentType instanceof PsiPrimitiveType) && !(sComponentType instanceof PsiPrimitiveType)) {
          constraints.add(new SubtypingConstraint(tComponentType, sComponentType, true));
          return true;
        }
        return sComponentType instanceof PsiPrimitiveType && sComponentType.equals(tComponentType);
      }
      if (myT instanceof PsiClassType) {
        final PsiClassType.ClassResolveResult TResult = ((PsiClassType)myT).resolveGenerics();
        final PsiClass CClass = TResult.getElement();
        if (CClass != null) {
          if (CClass instanceof PsiTypeParameter) {
            if (myS instanceof PsiIntersectionType) {
              for (PsiType conjunct : ((PsiIntersectionType)myS).getConjuncts()) {
                if (myT.equals(conjunct)) return true;
              }
            }
            //todo ((PsiTypeParameter)C).getLowerBound()
            return false;
          }
  
          if (!(myS instanceof PsiClassType)) return false;
          final PsiClassType.ClassResolveResult SResult = ((PsiClassType)myS).resolveGenerics();
          final PsiClass SClass = SResult.getElement();
          if (!((PsiClassType)myT).hasParameters()) {
            return SClass == CClass;//todo 
          }
          final PsiSubstitutor tSubstitutor = SClass != null ? TypeConversionUtil.getClassSubstitutor(SClass,CClass,  TResult.getSubstitutor()) : null;
          final PsiSubstitutor sSubstitutor = SResult.getSubstitutor();
          if (tSubstitutor != null) {
            for (PsiTypeParameter parameter : SClass.getTypeParameters()) {
              final PsiType tSubstituted = tSubstitutor.substitute(parameter);
              final PsiType sSubstituted = sSubstitutor.substitute(parameter);
              if (tSubstituted != null && sSubstituted != null) {
                constraints.add(new SubtypingConstraint(tSubstituted, sSubstituted, false));
              }
            }
            return true;
          }
        }
        return false;
      }

      if (myT instanceof PsiIntersectionType) {
        for (PsiType conjunct : ((PsiIntersectionType)myT).getConjuncts()) {
          constraints.add(new SubtypingConstraint(conjunct, myS, true));
        }
        return true;
      }

      if (myT.equals(PsiType.NULL)) return false;
    } else {
      if (myT instanceof PsiWildcardType) {
        final PsiType tBound = ((PsiWildcardType)myT).getBound();
        if (tBound == null) {
          return true;
        }
        if (((PsiWildcardType)myT).isExtends()) {
          if (tBound.equalsToText(CommonClassNames.JAVA_LANG_OBJECT)) {
            return true;
          }
          if (myS instanceof PsiWildcardType) {
            final PsiType sBound = ((PsiWildcardType)myS).getBound();
            if (sBound != null && ((PsiWildcardType)myS).isExtends()) {
              constraints.add(new SubtypingConstraint(tBound, sBound, true));
              return true;
            }
          } else {
            constraints.add(new SubtypingConstraint(tBound, myS, true));
            return true;
          }
          return false;
        } else {
          if (myS instanceof PsiWildcardType) {
            final PsiType sBound = ((PsiWildcardType)myS).getBound();
            if (sBound != null && ((PsiWildcardType)myS).isSuper()) {
              constraints.add(new SubtypingConstraint(sBound, tBound, true));
              return true;
            }
          } else {
            constraints.add(new SubtypingConstraint(myS, tBound, true));
            return true;
          }
        }
        return false;
      } else {
        if (myS instanceof PsiWildcardType) {
          return false;
        } else {
          constraints.add(new SubtypingConstraint(myT, myS, true));
          return true;
        }
      }
    }
    return true;
  }
}

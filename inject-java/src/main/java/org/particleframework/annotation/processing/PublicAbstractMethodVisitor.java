/*
 * Copyright 2018 original authors
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
package org.particleframework.annotation.processing;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.*;

/**
 * Utility visitor that only visits public abstract methods that have not been implemented by the given type
 *
 * @author graemerocher
 * @since 1.0
 */
public abstract class PublicAbstractMethodVisitor<R,P> extends PublicMethodVisitor<R,P> {

    private final TypeElement classElement;
    private final ModelUtils modelUtils;
    private final Elements elementUtils;

    private Map<String, List<ExecutableElement>> declaredMethods = new HashMap<>();

    PublicAbstractMethodVisitor(TypeElement classElement, ModelUtils modelUtils, Elements elementUtils) {
        this.classElement = classElement;
        this.modelUtils = modelUtils;
        this.elementUtils = elementUtils;
    }

    @Override
    protected boolean isAcceptable(ExecutableElement executableElement) {
        Set<Modifier> modifiers = executableElement.getModifiers();
        String methodName = executableElement.getSimpleName().toString();
        boolean acceptable = modelUtils.isAbstract(executableElement) && !modifiers.contains(Modifier.FINAL) && !modifiers.contains(Modifier.STATIC);
        boolean isDeclared = executableElement.getEnclosingElement().equals(classElement);
        if(acceptable && !isDeclared && declaredMethods.containsKey(methodName)) {
            // check method is not overridden already
            for (ExecutableElement element : declaredMethods.get(methodName)) {
                if(elementUtils.overrides(element, executableElement, classElement)) {
                    return false;
                }
            }
        }
        else if(!acceptable && !modelUtils.isStatic(executableElement)) {
            List<ExecutableElement> declaredMethodList = declaredMethods.computeIfAbsent(methodName, s -> new ArrayList<>());
            declaredMethodList.add(executableElement);
        }
        return acceptable;
    }
}

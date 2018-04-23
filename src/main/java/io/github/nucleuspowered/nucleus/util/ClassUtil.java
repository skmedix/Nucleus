/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.util;

/*
 * This file was used via this blog post:
 * http://blog.xebia.com/a-general-purpose-utility-to-retrieve-java-generic-type-values/
 *
 * The file is available under the Apache License, which is compatible with the MIT licence.
 * This licence supersedes the MIT licence for this file.
 *
 * Licence reproduced below
 *
 * @(#)ClassUtils.java     9 Feb 2009
 *
 * Copyright © 2009 Andrew Phillips.
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Utility methods for dealing with {@link Class Classes}.
 *
 * @author anph
 * @since 9 Feb 2009
 *
 */
public class ClassUtil {

    /**
     * Returns <u>one</u> of the possible chains of superclasses and/or interfaces joining the
     * specified class to the given superclass, as returned by {@link #getSuperclassChains(Class, Class)}.
     * <i>Which</i> of the possible chains will be returned is not defined.
     * <p>
     * If <code>superclass</code> is <i>not</i> a superclass or -interface of <code>class</code>,
     * the method returns <code>null</code>. This may happen (in spite of the signature) if the 
     * method is called with non-generic arguments.
     *
     * @param <S>       the type of the superclass at the &quot;end&quot; of the chain
     * @param clazz     the class at the &quot;start&quot; of the superclass chain
     * @param superclass        the class at the &quot;end&quot; of the superclass chain
     * @return <u>one</u> superclass chain linking <code>class</code> to <code>superclass</code>,
     *         where successive elements of the list are immediate superclasses or -interfaces. If
     *         <code>class</code> is not a subclass of <code>superclass</code>, returns <code>null</code>.
     * @throws IllegalArgumentException if either argument is null    
     * @see #getSuperclassChains(Class, Class)
     */
    public static <S> List<Class<? extends S>> getSuperclassChain(Class<? extends S> clazz,
            Class<S> superclass) {
        Set<List<Class<? extends S>>> superclassChains = getSuperclassChainsInternal(clazz, superclass, true);
        return (superclassChains.isEmpty() ? null : superclassChains.iterator().next());
    }

    /**
     * Returns the chain of superclass and/or interfaces from the specified class to the given
     * superclass. Either parameter may be an interface.
     * <p>
     * Each list in the resulting set contains immediate superclass elements in order, i.e. for
     * classes
     *
     * <pre>
     * class Foo {}
     * class Bar extends Foo {}
     * class Baz extends Bar {}
     * </pre>
     *
     * <code>getSuperclassChains(Baz.class, Foo.class)</code> will return one list, <code>[Baz.class,
     * Bar.class, Foo.class]</code>.
     * <p>
     * If both parameters are classes, there can only be one possible chain. However, if the superclass
     * is an interface, there may be multiple possible inheritance chains. For instance, for
     *
     * <pre>
     * interface Foo {}
     * interface Bar1 extends Foo {}
     * interface Bar2 extends Foo {}
     * interface Baz extends Bar1, Bar2 {}
     * </pre>
     *
     * both <code>[Baz.class, Bar1.class, Foo.class]</code> and <code>[Baz.class, Bar2.class, Foo.class]</code>
     * are valid inheritance chains, and the method returns both.
     * <p>
     * If <code>superclass</code> is <i>not</i> a superclass or -interface of <code>class</code>,
     * the method returns an empty set. This may happen (in spite of the signature) if the 
     * method is called with non-generic arguments.
     *
     * @param <S>       the type of the superclass at the &quot;end&quot; of the chain 
     * @param clazz     the class at the &quot;start&quot; of the superclass chain
     * @param superclass        the class at the &quot;end&quot; of the superclass chain
     * @return all possible superclass chains linking <code>class</code> to <code>superclass</code>,
     *         where successive elements of the list are immediate superclasses or -interfaces. If
     *         <code>class</code> is not a subclass of <code>superclass</code>, returns an empty set.
     * @throws IllegalArgumentException if either argument is null  
     * @see #getSuperclassChain(Class, Class)
     */
    public static <S> Set<List<Class<? extends S>>> getSuperclassChains(Class<? extends S> clazz, Class<S> superclass) {
        return getSuperclassChainsInternal(clazz, superclass, false);
    }

    private static <S> Set<List<Class<? extends S>>> getSuperclassChainsInternal(Class<? extends S> clazz,
            Class<S> superclass, boolean oneChainSufficient) {
        checkNotNull(clazz, "'clazz' and 'superclass' may not be non-null");
        checkNotNull(superclass, "'clazz' and 'superclass' may not be non-null");

        if (!superclass.isAssignableFrom(clazz)) {
            return Collections.emptySet();
        }

        // interfaces only need to be considered if the superclass is an interface
        return getSuperclassSubchains(clazz, superclass, oneChainSufficient,
                superclass.isInterface());
    }

    // recursive method: gets the subchains from the given class to the target class
    @SuppressWarnings("unchecked")
    private static <S> Set<List<Class<? extends S>>> getSuperclassSubchains(
            Class<? extends S> subclass, Class<S> superclass, boolean oneChainSufficient,
            boolean considerInterfaces) {

        // base case: the subclass *is* the target class
        if (subclass.equals(superclass)) {

            // since the list will be built from the *head*, a linked list is a good choice
            List<Class<? extends S>> subchain = new LinkedList<>();
            subchain.add(subclass);
            return singleton(subchain);
        }

        // recursive case: get all superclasses and, if required, interfaces and recurse
        Set<Class<? extends S>> supertypes = new HashSet<>();

        Class<? extends S> immediateSuperclass = (Class<? extends S>) subclass.getSuperclass();

        // interfaces and Object don't have a superclass
        if (immediateSuperclass != null) {
            supertypes.add(immediateSuperclass);
        }

        if (considerInterfaces) {
            supertypes.addAll(asList((Class<? extends S>[]) subclass.getInterfaces()));
        }

        Set<List<Class<? extends S>>> subchains = new HashSet<>();

        for (Class<? extends S> supertype : supertypes) {
            Set<List<Class<? extends S>>> subchainsFromSupertype =
                    getSuperclassSubchains(supertype, superclass, oneChainSufficient,
                            considerInterfaces);

            // each chain from the supertype results in a chain [current, subchain-from-super]
            if (!subchainsFromSupertype.isEmpty()) {

                if (oneChainSufficient) {
                    ClassUtil.addSubchain(subchains, subclass,
                            subchainsFromSupertype.iterator().next());
                    return subchains;
                } else {

                    for (List<Class<? extends S>> subchainFromSupertype : subchainsFromSupertype) {
                        ClassUtil.addSubchain(subchains, subclass, subchainFromSupertype);
                    }

                }

            }

        }

        return subchains;
    }

    // adds the class to the beginning of the subchain and stores this extended subchain
    private static <T> void addSubchain(Set<List<Class<? extends T>>> subchains,
            Class<? extends T> clazz, List<Class<? extends T>> subchainFromSupertype) {
        subchainFromSupertype.add(0, clazz);
        subchains.add(subchainFromSupertype);
    }

    /**
     * Determines if <i>any</i> of the given superclasses is a superclass or -interface of the
     * specified class. See {@link Class#isAssignableFrom(Class)}.
     *
     * @param superclasses      the superclasses and -interfaces to be tested against
     * @param clazz     the class to be to be checked
     * @return  <code>true</code> iff <i>any</i> of the given classes is a superclass or -interface
     *          of the specified class
     * @throws IllegalArgumentException if either of the arguments is null
     */
    public static boolean isAnyAssignableFrom(Collection<Class<?>> superclasses,
            Class<?> clazz) {
        checkNotNull(superclasses, "All arguments must be non-null");
        checkNotNull(clazz, "All arguments must be non-null");

        for (Class<?> superclass : superclasses) {

            if (superclass.isAssignableFrom(clazz)) {
                return true;
            }

        }

        return false;
    }

    /**
     * Determines if the specified object is assignment-compatible
     * with <em>any</em> of the specified {@code Class Classes}. class. 
     * See {@link Class#isInstance(Object)}.
     *
     * @param superclasses      the superclasses and -interfaces to be tested against
     * @param instance     the object to be to be checked
     * @return  <code>true</code> iff the object is an instance of <i>any</i> of the 
     *          given classes interfaces
     * @throws IllegalArgumentException if either of the arguments is null
     */
    public static boolean isInstance(Collection<Class<?>> superclasses,
            Object instance) {
        checkNotNull(superclasses, "Superclasses non-null");

        for (Class<?> superclass : superclasses) {

            if (superclass.isInstance(instance)) {
                return true;
            }

        }

        return false;
    }

    /**
     * Collects all fields declared in the given class <u>and</u> inherited from 
     * parents.
     *
     * @param clazz the class to parse
     * @return  a list of all fields declared in the class or its  parents, in the 
     *          order determined by successive {@link Class#getDeclaredFields()}
     *          calls
     * @see #getAllDeclaredFields(Class, Class)
     */
    public static List<Field> getAllDeclaredFields(Class<?> clazz) {
        return getAllDeclaredFields(clazz, Object.class);
    }

    /**
     * Collects all fields declared in the given class <u>and</u> inherited from 
     * parents <strong>up to and including the given parent class</strong>.
     *
     * @param <T> the type of the class to parse
     * @param clazz the class to parse
     * @param superclass the superclass of the class to parse at which traversal should be
     *                   stopped
     * @return  a list of all fields declared in the class or its parents up to and including
     *          the given parent class, in the order determined by successive 
     *          {@link Class#getDeclaredFields()} calls
     * @see #getAllDeclaredFields(Class)
     */
    public static <T> List<Field> getAllDeclaredFields(Class<T> clazz,
            Class<? super T> superclass) {
        final List<Field> fields = new ArrayList<>();

        for (Class<?> immediateSuperclass : getSuperclassChain(clazz, superclass)) {
            fields.addAll(Arrays.asList(immediateSuperclass.getDeclaredFields()));
        }

        return fields;
    }

    /**
     * Collects all fields declared in the given class <u>and</u> inherited from 
     * parents that are annotated with an annotation of the given type.
     *
     * @param clazz the class to parse
     * @param annotationType the non-{@code null} type (class) of the annotation required
     * @return  a list of all fields declared in the class or its  parents, in the 
     *          order determined by successive {@link Class#getDeclaredFields()}
     *          calls
     * @throws IllegalArgumentException if {@code clazz} or {@code annotationType} is {@code null}          
     */
    public static List<Field> getAllAnnotatedDeclaredFields(Class<?> clazz,
            Class<? extends Annotation> annotationType) {
        checkNotNull(clazz, "All arguments must be non-null");
        checkNotNull(annotationType, "All arguments must be non-null");

        final List<Field> annotatedFields = new ArrayList<>();

        for (Field field : getAllDeclaredFields(clazz)) {

            if (field.isAnnotationPresent(annotationType)) {
                annotatedFields.add(field);
            }

        }

        return annotatedFields;
    }

    /**
     * Collects all methods of the given class (as returned by {@link Class#getMethods()} that 
     * are annotated with the given annotation.
     *
     * @param clazz     the class whose methods should be returned
     * @param annotationType        the annotation that the returned methods should be annotated with
     * @return  the methods of the given class annotated with the given annotation
     * @throws IllegalArgumentException if {@code clazz} is {@code null}
     */
    public static Set<Method> getAnnotatedMethods(Class<?> clazz,
            Class<? extends Annotation> annotationType) {
        checkNotNull(clazz, "'clazz' must be non-null");

        // perhaps this case should throw an exception, but an empty list also seems sensible 
        if (annotationType == null) {
            return new HashSet<>();
        }

        Set<Method> annotatedMethods = new HashSet<>();

        for (Method method : clazz.getMethods()) {

            if (method.isAnnotationPresent(annotationType)) {
                annotatedMethods.add(method);
            }

        }

        return annotatedMethods;
    }

    /**
     * Retrieves the type arguments of a class when regarded as an subclass of the
     * given typed superclass or interface. The order of the runtime type classes matches the order
     * of the type variables in the declaration of the typed superclass or interface.
     * <p>
     * For example, for the classes
     *
     * <pre>
     * class Foo&lt;U, V&gt; {}
     * class Bar&lt;W&gt; extends Foo&lt;String, W&gt; {}
     * class Baz extends Bar&lt;Long&gt;
     * </pre>
     *
     * and a <code>typedClass</code> argument of <code>Baz.class</code>, the method should return
     * <p>
     * <ul>
     * <li><code>[String, Long]</code> for a <code>typedSuperclass</code> argument of <code>Foo.class</code>,
     *     and
     * <li><code>[Long]</code> if <code>typedSuperclass</code> is <code>Bar.class</code>.
     * </ul>
     * For type parameters that cannot be determined, <code>null</code> is returned.
     * <p>
     * <b>Note:</b> It is <u>not</u> possible to retrieve type information that is not available
     * in the (super)class hierarchy at <u>compile</u>-time. Calling 
     * <code>getActualTypeArguments(new ArrayList&lt;String&gt;().getClass(), List.class)</code> will, 
     * for instance, return <code>[null]</code> because the specification of the actual type 
     * (<code>String</code>, in this example) did not take place either in the superclass {@link AbstractList}
     * or the interface {@link List}.
     * <p>
     * If {@code superclass} is <em>not</em> a superclass or -interface of {@code class},
     * the method returns {@code null}. This may happen (in spite of the signature) if the 
     * method is called with non-generic arguments.
     *
     * @param <S>       the type of the object
     * @param typedClass the class for which type information is required
     * @param typedSuperclass the typed class or interface of which the object is to be regarded a 
     *                        subclass
     * @return  the type arguments for the given class when regarded as a subclass of the
     *          given typed superclass, in the order defined in the superclass. If
     *          {@code class} is not a subclass of {@code superclass}, returns {@code null}.
     * @throws IllegalArgumentException if <code>typedSuperclass</code> or <code>typedClass</code> 
     *                                  is <code>null</code>         
     */
    @Nonnull
    public static <S> List<Class<?>> getActualTypeArguments(Class<? extends S> typedClass,
            Class<S> typedSuperclass) {
        checkNotNull(typedSuperclass, "All arguments must be non-null");
        checkNotNull(typedClass, "All arguments must be non-null");

        /*
         * The type signature should ensure that the class really *is* an subclass of
         * typedSuperclass, but this can be circumvented by using "generic-less" arguements.
         */
        if (!typedSuperclass.isAssignableFrom(typedClass)) {
            return null;
        }

        TypeVariable<?>[] typedClassTypeParams = typedSuperclass.getTypeParameters();

        // if the class has no parameters, return
        if (typedClassTypeParams.length == 0) {
            return new ArrayList<>(0);
        }

        /*
         * It would be nice if the parent class simply "aggregated" all the type variable
         * assignments that happen in subclasses. In other words, it would be nice if, in the
         * example in the JavaDoc, new Baz().getClass().getSuperclass().getGenericSuperclass()
         * would return [String, Long] as actual type arguments.
         * Unfortunately, though, it returns [String, W], because the assignment of Long to W
         * isn't accessible to Bar. W's value is available from new Baz().getClass().getGenericSuperclass(),
         * and must be "remembered" as we traverse the object hierarchy.
         * Note, though, that the "variable substitution" of W (the variable used in Bar) for V (the
         * equivalent variable in Foo) *is* propagated, but only to the immediate parent!
         */
        Map<TypeVariable<?>, Class<?>> typeAssignments =
                new HashMap<>(typedClassTypeParams.length);

        /*
         * Get one possible path from the typed class to the typed superclass. For classes, there
         * is only one (the superclass chain), but for interfaces there may be multiple. We only
         * need one, however (and it doesn't matter which one) since the compiler does not allow
         * inheritance chains with conflicting generic type information.
         */
        List<Class<? extends S>> superclassChain = getSuperclassChain(typedClass, typedSuperclass);

        assert (superclassChain != null) : Arrays.<Class<?>>asList(typedSuperclass, typedClass);

        /*
         * The list is ordered so that successive elements are immediate superclasses. The iteration
         * stops with the class whose *superclass* is the last element, because type information
         * is collected from the superclass.
         */
        for (int i = 0; i < superclassChain.size() - 1; i++) {
            collectAssignments(superclassChain.get(i), superclassChain.get(i + 1), typeAssignments);
        }

        // will contain null for entries for which no class could be resolved
        return getActualAssignments(typedClassTypeParams, typeAssignments);
    }

    private static void collectAssignments(Class<?> clazz, Class<?> supertype,
            Map<TypeVariable<?>, Class<?>> typeAssignments) {
        TypeVariable<?>[] typeParameters = supertype.getTypeParameters();

        // the superclass is not necessarily a generic class
        if (typeParameters.length == 0) {
            return;
        }

        Type[] actualTypeAttributes = getActualTypeAttributes(clazz, supertype);

        assert (typeParameters.length == actualTypeAttributes.length)
                : Arrays.asList(typeParameters, typeAssignments);

        // matches up type parameters with their actual assignments, assuming the order is the same!
        for (int i = 0; i < actualTypeAttributes.length; i++) {
            Type type = actualTypeAttributes[i];

            /*
             * type will be a Class or ParameterizedType if the actual type is known,
             * and a TypeVariable if not.
             */
            if (type instanceof Class) {
                typeAssignments.put(typeParameters[i], (Class<?>) type);
            } else if (type instanceof ParameterizedType) {
                assert (((ParameterizedType) type).getRawType() instanceof Class) : type;
                typeAssignments.put(typeParameters[i],
                        (Class<?>) ((ParameterizedType) type).getRawType());
            } else {
                assert (type instanceof TypeVariable<?>) : type;

                /*
                 * The actual type arguments consist of classes and type variables from the
                 * immediate child class. So if the type assignment mapping is updated to
                 * contain the mapping of all type variables of the *current* class to
                 * their classes, then these can be used in the *next* iteration to resolve
                 * any variable "left over" from this round.
                 *
                 * Any variables that cannot be resolved in this round are not resolvable, otherwise
                 * the would have been resolved in the previous round.
                 */
                if (typeAssignments.containsKey(type)) {
                    typeAssignments.put(typeParameters[i], typeAssignments.get(type));
                }

            }

        }

    }

    private static Type[] getActualTypeAttributes(Class<?> clazz, Class<?> supertype) {
        /*
         * The superclass is not necessarily a ParameterizedType even if it has type
         * parameters! This happens if a user fails to specify type parameters for a
         * class and ignores the warning, e.g.
         *
         * class MyList extends ArrayList
         *
         * In this case, the superclass ArrayList.class has one type parameter, but
         * MyList.class.getGenericSuperclass() returns a simple type object!
         *
         * In this case, no type assignments take place, so the actual arguments are
         * simply the type parameters.
         */
        Type genericSupertype = tryGetGenericSupertype(clazz, supertype);
        return ((genericSupertype instanceof ParameterizedType)
                ? ((ParameterizedType) genericSupertype).getActualTypeArguments()
                : supertype.getTypeParameters());
    }

    private static Type tryGetGenericSupertype(Class<?> clazz, Class<?> supertype) {

        if (!supertype.isInterface()) {
            return clazz.getGenericSuperclass();
        } else {
            Type[] genericInterfaces = clazz.getGenericInterfaces();

            for (Type interfaceType : genericInterfaces) {
                // there is no guarantee that *all* the interfaces are generic
                if ((interfaceType instanceof ParameterizedType)
                        && (((ParameterizedType) interfaceType).getRawType().equals(supertype))) {
                    return interfaceType;
                } else {
                    assert (interfaceType instanceof Class) : interfaceType;

                    if (interfaceType.equals(supertype)) {
                        return interfaceType;
                    }

                }

            }

        }

        throw new AssertionError("Unable to find generic superclass information for class '"
                + clazz + "' and superclass/-interface '" + supertype + "'");
    }

    private static List<Class<?>> getActualAssignments(
            TypeVariable<?>[] typedClassTypeParams,
            Map<TypeVariable<?>, Class<?>> typeAssignments) {
        int numTypedClassTypeParams = typedClassTypeParams.length;
        List<Class<?>> actualAssignments =
                new ArrayList<>(numTypedClassTypeParams);

        // for entries that could not be resolved, null should be returned
        for (TypeVariable<?> typedClassTypeParam : typedClassTypeParams) {
            actualAssignments.add(typeAssignments.get(typedClassTypeParam));
        }

        return actualAssignments;
    }

}
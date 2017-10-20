package io.github.nucleuspowered.nucleus.annotationprocessor;

import com.google.auto.service.AutoService;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.AbstractElementVisitor8;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("io.github.nucleuspowered.nucleus.annotationprocessor.Store")
public class StoreProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<Element, String> classes = new HashMap<>();

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Store.class);
        for (Element element : elements) {
            // Only storing classes.
            if (element.getKind().isClass()) {
                Store s = element.getAnnotation(Store.class);
                classes.put(element, s.isRoot() ? null : s.value());
            }
        }

        // Get the root elements
        ClassElementVisitor cev = new ClassElementVisitor();
        Map<String, String> conv = classes.entrySet().stream()
                .filter(x -> x.getValue() == null)
                .map(x -> cev.visit(x.getKey(), true))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(x -> x.pa, x -> x.cl));

        final Map<String, Map<String, List<String>>> result = conv.values().stream().collect(Collectors.toMap(x -> x, x -> new HashMap<>()));

        classes.entrySet().stream().filter(x -> x.getValue() != null)
                .map(x -> {
                    StringTuple st = cev.visit(x.getKey(), false);
                    if (st != null) {
                        return new StringTuple(x.getValue(), st.cl);
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(x -> {
            // Check the class vs package name
            conv.entrySet().stream().filter(y -> x.cl.startsWith(y.getKey())).distinct().findFirst().ifPresent(y ->
                    result.get(y.getValue()).computeIfAbsent(x.pa, z -> new ArrayList<>()).add(x.cl));
        });

        if (!classes.isEmpty()) {

            // Write this file
            try {
                FileObject fo = this.processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "assets.nucleus", "classes.json");
                try (Writer os = fo.openWriter()) {
                    os.write(new GsonBuilder().setPrettyPrinting().create().toJson(result));
                    os.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    static class ClassElementVisitor extends AbstractElementVisitor8<StringTuple, Boolean> {

        @Override public StringTuple visitPackage(PackageElement e, Boolean aBoolean) {
            return null;
        }

        @Override public StringTuple visitType(TypeElement e, Boolean aBoolean) {
            String name = e.getQualifiedName().toString();
            if (!e.getModifiers().contains(Modifier.ABSTRACT) && name.startsWith("io.github.nucleuspowered.nucleus.modules")) {

                if (aBoolean) {
                    // Only care about the package here.
                    return new StringTuple(name.replaceAll("\\.[^.]+?$", "\\."), name);
                } else if (e.getNestingKind().isNested()) {
                    return new StringTuple(null, name.replaceAll("\\.([^.]+?)$", "\\$$1"));
                }

                return new StringTuple(null, name);
            }

            return null;
        }

        @Override public StringTuple visitVariable(VariableElement e, Boolean aBoolean) {
            return null;
        }

        @Override public StringTuple visitExecutable(ExecutableElement e, Boolean aBoolean) {
            return null;
        }

        @Override public StringTuple visitTypeParameter(TypeParameterElement e, Boolean aBoolean) {
            return null;
        }
    }

    static class StringTuple {
        @Nullable final String pa;
        final String cl;

        StringTuple(@Nullable String pa, String cl) {
            this.pa = pa;
            this.cl = cl;
        }
    }
}

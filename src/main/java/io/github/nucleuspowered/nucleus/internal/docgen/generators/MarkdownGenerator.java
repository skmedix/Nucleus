/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.docgen.generators;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.internal.docgen.CommandDoc;
import io.github.nucleuspowered.nucleus.internal.docgen.PermissionDoc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class MarkdownGenerator<T> {

    private static final String EMPTY = "";

    abstract List<String> create(List<T> input);

    public final void create(Path file, List<T> input) throws Exception {
        Files.write(file, create(input), StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    public static class CommandMarkdownGenerator extends MarkdownGenerator<CommandDoc> {

        @Override List<String> create(List<CommandDoc> input) {
            List<String> output = Lists.newArrayList();
            output.add("# Commands");
            output.add("This is a summary of commands available in Nucleus. You can also visit a searchable version: "
                    + "see https://nucleuspowered.org/docs/commands2.html");

            Map<String, List<CommandDoc>> d = input.stream().collect(Collectors.groupingBy(CommandDoc::getModule, Collectors.toList()));
            List<String> i = Lists.newArrayList(d.keySet());
            i.sort(String::compareToIgnoreCase);

            LinkedHashMap<String, Function<CommandDoc, String>> lhm = Maps.newLinkedHashMap();
            lhm.put("Command", t -> "`/" + t.getCommandName() + "`");
            lhm.put("Permission", x -> {
                if (!x.getPermissionbase().isEmpty()) {
                    return x.getPermissionbase().concat(".base");
                }

                return "";
            });
            lhm.put("Description", CommandDoc::getOneLineDescription);

            // for each commanddoc
            for (String mod : i) {
                output.add("## Module: " + mod);

                List<CommandDoc> dd = d.get(mod);
                dd.sort(Comparator.comparing(CommandDoc::getCommandName));

                output.addAll(MarkdownCreator.createTable(dd, lhm));
            }

            return output;
        }

    }

    public static class PermissionMarkdownGenerator extends MarkdownGenerator<PermissionDoc> {

        @Override List<String> create(List<PermissionDoc> input) {
            // Condense the permission docs.
            input.stream().collect(Collectors.groupingBy(PermissionDoc::getPermission, Collectors.counting())).forEach((k, v) -> {
                if (v > 1) {
                    System.out.println(k + ": " + v);
                }
            });

            // Remove any that have "colour" - I'm British, but we're tending to American English here...
            Map<String, PermissionDoc> docs = input.stream()
                    .filter(x -> !x.getPermission().contains("colour"))
                    .collect(Collectors.toMap(PermissionDoc::getPermission, x -> x));

            List<PermissionDoc> newDocs = Lists.newArrayList();

            // Get the docs that end in ".base"
            List<PermissionDoc> endInBase = docs.entrySet().stream().filter(c -> c.getKey().endsWith(".base"))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
            endInBase.forEach(x -> x.setDescription(x.getDescription().replaceAll("Allows the user to run the command ", "")));

            endInBase.forEach(x -> {
                // Get all the exemption permissions.
                String perm = x.getPermission().replace(".base", "").concat(".exempt.");
                List<PermissionDoc> toProcess =
                        docs.entrySet().stream().filter(y -> y.getKey().startsWith(perm)).map(Map.Entry::getValue).collect(Collectors.toList());
                if (!toProcess.isEmpty()) {
                    PermissionDoc combine = MarkdownGenerator.combine(perm, toProcess);
                    toProcess.forEach(c -> docs.remove(c.getPermission()));
                    newDocs.add(combine);
                }
            });

            newDocs.addAll(docs.values());
            newDocs.sort(Comparator.comparing(PermissionDoc::getModule).thenComparing(PermissionDoc::getPermission));

            LinkedHashMap<String, Function<PermissionDoc, String>> lhm = Maps.newLinkedHashMap();
            lhm.put("Module", PermissionDoc::getModule);
            lhm.put("Permission", PermissionDoc::getPermission);
            lhm.put("Description", t -> t.getDescription().replace("\n", " "));

            return MarkdownCreator.createTable(newDocs, lhm);
        }
    }

    private static PermissionDoc combine(String base, List<PermissionDoc> combine) {
        return new PermissionDoc().setDefaultLevel("ADMIN").setDescription("Exemption permissions")
                .setModule(combine.get(0).getModule())
                .setPermission(base + "<" + combine.stream().map(x -> x.getPermission().replace(base, ""))
                        .collect(Collectors.joining("\\|")) + ">");
    }
}

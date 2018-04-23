/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Util;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nullable;

@NonnullByDefault
public class PaginationBuilderWrapper implements PaginationList.Builder {

    private final PaginationList.Builder builder;
    @Nullable private List<Text> texts = null;

    public PaginationBuilderWrapper(PaginationList.Builder builder) {
        this.builder = builder;
    }

    @Override public PaginationList.Builder contents(Iterable<Text> contents) {
        this.texts = Lists.newArrayList(contents);
        return this;
    }

    @Override public PaginationList.Builder contents(Text... contents) {
        this.texts = Lists.newArrayList(contents);
        return this;
    }

    @Override public PaginationList.Builder title(Text title) {
        this.builder.title(title);
        return this;
    }

    @Override public PaginationList.Builder header(@Nullable Text header) {
        this.builder.header(header);
        return this;
    }

    @Override public PaginationList.Builder footer(@Nullable Text footer) {
        this.builder.footer(footer);
        return this;
    }

    @Override public PaginationList.Builder padding(Text padding) {
        this.builder.padding(padding);
        return this;
    }

    @Override public PaginationList.Builder linesPerPage(int linesPerPage) {
        this.builder.linesPerPage(linesPerPage);
        return this;
    }

    @Override public PaginationList build() {
        Preconditions.checkNotNull(this.texts);
        ListIterator<Text> text = this.texts.listIterator();
        while (text.hasNext()) {
            Text t = text.next();
            if (t.toPlain().isEmpty()) {
                text.set(Util.SPACE);
            }
        }

        return this.builder.contents(this.texts).build();
    }

    @Override public PaginationList.Builder from(PaginationList value) {
        return new PaginationBuilderWrapper(this.builder.from(value));
    }

    @Override public PaginationList.Builder reset() {
        this.builder.reset();
        this.texts = null;
        return this;
    }
}

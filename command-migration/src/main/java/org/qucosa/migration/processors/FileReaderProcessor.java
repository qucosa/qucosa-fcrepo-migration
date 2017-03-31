/*
 * Copyright (C) 2015 Saxon State and University Library Dresden (SLUB)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.qucosa.migration.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileReaderProcessor implements Processor {

    private final String commentPrefix;

    public FileReaderProcessor(String commentPrefix) {
        this.commentPrefix = (commentPrefix == null) ? "" : commentPrefix;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();
        String filename = in.getBody(String.class);
        List<String> lines = Files.readAllLines(Paths.get(filename)).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty() && (commentPrefix.isEmpty() || !s.startsWith(commentPrefix)))
                .collect(Collectors.toList());
        in.setBody(lines);
    }

}

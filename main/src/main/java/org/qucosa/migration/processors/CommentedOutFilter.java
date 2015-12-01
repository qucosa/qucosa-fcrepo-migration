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

import java.util.LinkedList;
import java.util.List;

public class CommentedOutFilter implements org.apache.camel.Processor {

    final private String commentSymbol;

    public CommentedOutFilter(String commentSymbol) {
        this.commentSymbol = commentSymbol;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Message in = exchange.getIn();
        List inputLines = in.getBody(List.class);
        List<String> outputLines = new LinkedList<>();
        for (Object o : inputLines) {
            String sv = String.valueOf(o);
            if (!sv.startsWith(commentSymbol)) outputLines.add(sv);
        }
        in.setBody(outputLines);
    }
}

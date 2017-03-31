/*
 * Copyright (C) 2016 Saxon State and University Library Dresden (SLUB)
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
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

public class FileReaderProcessorTest extends CamelTestSupport {

    private Exchange exchange;
    private File file;
    private FileReaderProcessor subject;

    @Before
    public void setUp() throws Exception {
        subject = new FileReaderProcessor("#");
        file = File.createTempFile(FileReaderProcessorTest.class.getName(), UUID.randomUUID().toString());
        exchange = createExchangeWithBody(file.getAbsolutePath());
    }

    @After
    public void tearDown() throws Exception {
        file.delete();
    }

    @Test
    public void Reading_empty_file_returns_empty_list() throws Exception {
        subject.process(exchange);

        Object body = exchange.getIn().getBody();
        assertNotNull(body);
        assertTrue(body instanceof List);
        assertEquals(0, ((List) body).size());
    }

    @Test
    public void Reading_two_line_file_results_in_list_with_two_strings() throws Exception {
        String line1 = "Line 1";
        String line2 = "Line 2";

        PrintWriter pw = new PrintWriter(file);
        pw.println(line1);
        pw.println(line2);
        pw.flush();

        subject.process(exchange);

        List<String> body = (List<String>) exchange.getIn().getBody(List.class);
        assertTrue("List should contain " + line1, body.contains(line1));
        assertTrue("List should contain " + line2, body.contains(line2));
    }

    @Test
    public void Lines_get_trimmed() throws Exception {
        String line1 = "Line 1";
        String line2 = "Line 2";

        PrintWriter pw = new PrintWriter(file);
        pw.println(line1 + "       ");
        pw.println("       " + line2);
        pw.flush();

        subject.process(exchange);

        List<String> body = (List<String>) exchange.getIn().getBody(List.class);
        assertTrue("List should contain " + line1, body.contains(line1));
        assertTrue("List should contain " + line2, body.contains(line2));
    }

    @Test
    public void Empty_lines_are_filtered_out() throws Exception {
        String line1 = "Line 1";
        String line2 = "Line 2";

        PrintWriter pw = new PrintWriter(file);
        pw.println();
        pw.println(line1);
        pw.println();
        pw.println(line2);
        pw.println();
        pw.flush();

        subject.process(exchange);

        List<String> body = (List<String>) exchange.getIn().getBody(List.class);
        assertEquals(2, body.size());
        assertTrue("List should contain " + line1, body.contains(line1));
        assertTrue("List should contain " + line2, body.contains(line2));
    }

    @Test
    public void Commented_lines_are_filtered_out() throws Exception {
        String line1 = "Line 1";
        String line2 = "#Line 2";
        String line3 = "Line 2";

        PrintWriter pw = new PrintWriter(file);
        pw.println();
        pw.println(line1);
        pw.println();
        pw.println(line2);
        pw.println();
        pw.println(line3);
        pw.println();
        pw.flush();

        subject.process(exchange);

        List<String> body = (List<String>) exchange.getIn().getBody(List.class);
        assertEquals(2, body.size());
        assertTrue("List should contain " + line1, body.contains(line1));
        assertFalse("List should not contain " + line2, body.contains(line2));
        assertTrue("List should contain " + line3, body.contains(line3));
    }

}

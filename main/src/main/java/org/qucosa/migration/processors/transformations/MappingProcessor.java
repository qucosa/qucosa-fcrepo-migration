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

package org.qucosa.migration.processors.transformations;

import de.slubDresden.InfoDocument;
import gov.loc.mods.v3.ModsDocument;
import noNamespace.OpusDocument;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class MappingProcessor implements Processor {
    public static final String NS_MODS_V3 = "http://www.loc.gov/mods/v3";
    public static final String NS_SLUB = "http://slub-dresden.de/";
    public static final String NS_FOAF = "http://xmlns.com/foaf/0.1/";
    private static final XPath xPath;
    private static final XPathFactory xPathFactory;

    static {
        xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                switch (prefix) {
                    case "mods":
                        return NS_MODS_V3;
                    case "slub":
                        return NS_SLUB;
                    case "foaf":
                        return NS_FOAF;
                    default:
                        return XMLConstants.NULL_NS_URI;
                }
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return XMLConstants.DEFAULT_NS_PREFIX;
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return new ArrayList() {{
                    add(XMLConstants.XML_NS_PREFIX);
                }}.iterator();
            }
        });
    }

    private String label;
    private boolean modsChanges;
    private boolean slubChanges;

    public static XPath getXPath() {
        return xPath;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        Map m = (Map) exchange.getIn().getBody();

        modsChanges = (boolean) exchange.getProperty("MODS_CHANGES", false);
        slubChanges = (boolean) exchange.getProperty("SLUB-INFO_CHANGES", false);

        process((OpusDocument) m.get("QUCOSA-XML"),
                (ModsDocument) m.get("MODS"),
                (InfoDocument) m.get("SLUB-INFO"));

        exchange.getIn().setBody(m);
        exchange.setProperty("MODS_CHANGES", modsChanges);
        exchange.setProperty("SLUB-INFO_CHANGES", slubChanges);
    }


    public abstract void process(
            OpusDocument opusDocument,
            ModsDocument modsDocument,
            InfoDocument infoDocument) throws Exception;

    public String getLabel() {
        if (label == null) {
            String classname = this.getClass().getSimpleName();
            if (classname.endsWith("Processor")) {
                label = classname.substring(0, classname.length() - 9).toLowerCase();
            }
        }
        return label;
    }

    public void signalChanges(String dsid) {
        if (dsid.equals("MODS")) {
            this.modsChanges = true;
        } else if (dsid.equals("SLUB-INFO")) {
            this.slubChanges = true;
        }
    }

    public Boolean hasChanges() {
        return this.modsChanges;
    }

    protected XmlObject select(String query, XmlObject xmlObject) {
        XmlCursor cursor = xmlObject.newCursor();
        cursor.selectPath("declare namespace mods='" + NS_MODS_V3 + "'" + query);
        XmlObject result = cursor.toNextSelection() ? cursor.getObject() : null;
        cursor.dispose();
        return result;
    }

    protected List<XmlObject> selectAll(String query, XmlObject xmlObject) {
        List<XmlObject> results = new ArrayList<>();
        XmlCursor cursor = xmlObject.newCursor();
        cursor.selectPath("declare namespace mods='" + NS_MODS_V3 + "'" + query);
        while (cursor.toNextSelection()) {
            results.add(cursor.getObject());
        }
        cursor.dispose();
        return results;
    }


    public String languageEncoding(String code) {
        if (code != null) {
            if (code.length() != 3) {
                return Locale.forLanguageTag(code).getISO3Language();
            }
        }
        return code;
    }

    protected Boolean nodeExists(String expression, XmlObject object) throws XPathExpressionException {
        final XPath xPath = getXPath();
        xPath.reset();
        return (Boolean) xPath.evaluate(expression,
                object.getDomNode().cloneNode(true), XPathConstants.BOOLEAN);
    }

    protected String dateEncoding(BigInteger year) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, year.intValue());
        return dateFormat.format(cal.getTime());
    }

    protected String dateEncoding(noNamespace.Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        TimeZone tz;
        GregorianCalendar cal;
        if (date.getTimezone() != null) {
            tz = SimpleTimeZone.getTimeZone(date.getTimezone());
            cal = new GregorianCalendar(tz);
        } else {
            cal = new GregorianCalendar();
        }

        cal.set(Calendar.YEAR, date.getYear().intValue());
        cal.set(Calendar.MONTH, date.getMonth().intValue() - 1);
        cal.set(Calendar.DAY_OF_MONTH, date.getDay().intValue());

        return dateFormat.format(cal.getTime());
    }
}

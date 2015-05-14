/**
 *   Copyright 2015 Royce Remulla
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package oafext.util;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author $Author: $
 * @version $Date: $
 *
 */
public final class XmlUtil {

    /** */
    private XmlUtil() {}

    /**
     * This will prevent the DocumentBuilder from validating the DTD. Saves us
     * the trouble of dependence to online DTD resource.
     *
     * @param docBuilder DocumentBuilder instance.
     */
    public static void ignoreDtd(final DocumentBuilder docBuilder,
                                 final String dtdFilename)
    {
        docBuilder.setEntityResolver(new EntityResolver() {


            @Override
            public InputSource resolveEntity(final String publicId,
                                             final String systemId)
                    throws SAXException, IOException
            {
                InputSource retval = null; //NOPMD: null default, conditionally redefine.
                if (systemId.contains(dtdFilename)) {
                    retval = new InputSource(new StringReader(""));
                }
                return retval;
            }
        });
    }

}

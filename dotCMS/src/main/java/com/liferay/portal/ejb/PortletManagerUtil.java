/**
 * Copyright (c) 2000-2005 Liferay, LLC. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.liferay.portal.ejb;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.dotcms.business.CloseDBIfOpened;
import com.dotmarketing.business.portal.PortletFactory;
import com.liferay.portal.model.Portlet;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

/**
 * <a href="PortletManagerUtil.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 * @version $Revision: 1.78 $
 *
 */
public class PortletManagerUtil {

    public static Collection addPortlets(final java.lang.String[] xmls) throws com.liferay.portal.SystemException {
        try {
            final PortletFactory portletFactory = PortletManagerFactory.getManager();
            
            final Map<String,Portlet> portlets = portletFactory.xmlToPortlets(xmls);
            for(Portlet portlet : portlets.values()) {
              portletFactory.insertPortlet(portlet);
            }
            
            return portletFactory.getPortlets();
        } catch (final Exception e) {
            throw new com.liferay.portal.SystemException(e);
        }
    }

    public static Collection addPortletsDocument(final String xml) throws com.liferay.portal.SystemException {
        try {
            final PortletFactory portletFactory = PortletManagerFactory.getManager();

            InputStream stream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
            SAXBuilder builder = new SAXBuilder();
            Document doc = (Document) builder.build(stream);

            final Map<String,Portlet> portlets = portletFactory.xmlToPortlets(doc);
            List<Portlet> portletsInserted = new ArrayList<Portlet>();
            for(Portlet portletToInsert : portlets.values()) {
                final Portlet portlet = portletFactory.insertPortlet(portletToInsert);
                portletsInserted.add(portlet);
            }

            return portletsInserted;
        } catch (final Exception e) {
            throw new com.liferay.portal.SystemException(e);
        }
    }

    public static com.liferay.portal.model.Portlet getPortletById(final java.lang.String companyId, final java.lang.String portletId)
            throws com.liferay.portal.SystemException {
        try {
            final PortletFactory portletFactory = PortletManagerFactory.getManager();

            return portletFactory.findById(portletId);
        } catch (final Exception e) {
            throw new com.liferay.portal.SystemException(e);
        }
    }



    @CloseDBIfOpened
    public static java.util.Collection<Portlet> getPortlets(final java.lang.String companyId) throws com.liferay.portal.SystemException {
        try {
            final PortletFactory portletFactory = PortletManagerFactory.getManager();

            return portletFactory.getPortlets();
        } catch (final Exception e) {
            throw new com.liferay.portal.SystemException(e);
        }
    }


}

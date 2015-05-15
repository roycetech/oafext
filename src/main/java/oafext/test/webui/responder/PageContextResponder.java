package oafext.test.webui.responder;

import oafext.test.webui.MdsFixture;
import oafext.test.webui.PageContextMocker;

import org.mockito.Mockito;

/**
 * Responder for PageContext AND OAWebBeanFactory.
 *
 * @author $Author: $
 * @version $Date: $
 *
 */
public class PageContextResponder {


    public void mockMethods(final MdsFixture mdsFixture,
                            final PageContextMocker pcMocker)
    {
        Mockito
            .doReturn(mdsFixture.getRootWbMocker().getMock())
            .when(pcMocker.getMock())
            .getPageLayoutBean();

        Mockito
            .doReturn(pcMocker.getWbFocker().getMock())
            .when(pcMocker.getMock())
            .getWebBeanFactory();
    }
}

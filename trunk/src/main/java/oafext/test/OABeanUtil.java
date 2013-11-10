package oafext.test;

import java.util.Arrays;
import java.util.List;

import oracle.apps.fnd.framework.webui.beans.OAWebBean;

import com.sun.java.util.collections.UnsupportedOperationException;

public class OABeanUtil {

    @SuppressWarnings("unchecked")
    public static Class<? extends OAWebBean> getOABeanClass(final String string)
    {
        String className = string;

        if (list1.contains(string)) {
            className = "oracle.apps.fnd.framework.webui.beans." + string;
        } else if (messageList.contains(string)) {
            className = "oracle.apps.fnd.framework.webui.beans.message." + string;
        } else if (layoutList.contains(string)) {
            className = "oracle.apps.fnd.framework.webui.beans.layout." + string;
        } else if (listTable.contains(string)) {
            className = "oracle.apps.fnd.framework.webui.beans.table." + string;
        } else if (listTable.contains(string)) {
            className = "oracle.apps.fnd.framework.webui.beans.table." + string;
        } else if (LIST_NAV.contains(string)) {
            className = "oracle.apps.fnd.framework.webui.beans.nav." + string;

        }

        Class<? extends OAWebBean> retval = null;

        if (className.equals(string)) {
            throw new UnsupportedOperationException("Implement: " + string);
        } else {
            try {

                retval = (Class<? extends OAWebBean>) Class.forName(className);
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return retval;
    }

    static final List<String> LIST_NAV = Arrays.asList(new String[] {
            "OAApplicationSwitcherBean",
            "OABreadCrumbsBean",
            "OABrowseMenuBean",
            "OAButtonBean",
            "OADefaultTreeBean",
            "OAFooterBean",
            "OAGlobalButtonBarBean",
            "OAGlobalButtonBean",
            "OAGlobalHeaderBean",
            "OAHGridHierarchyBean",
            "OALinkBean",
            "OALovActionButtonBean",
            "OANavigationBarBean",
            "OAPageButtonBarBean",
            "OAQuickLinksBean",
            "OASideBarBean",
            "OASideNavBean",
            "OASubTabBarBean",
            "OATabBarBean",
            "OATrainBean",
            "OATrainStepBean",
            "OATreeBean",
            "OATreeChildBean",
            "OATreeDefinitionBean",
            "OATreeLevelBean",
            "OATreeRecursiveBean" });

    static final List<String> listTable = Arrays.asList(new String[] {
            "OAAddTableRowBean",
            "OAAdvancedTableBean",
            "OAColumnBean",
            "OAColumnGroupBean",
            "OAGanttBean",
            "OAHGridBean",
            "OAMultipleSelectionBean",
            "OASingleSelectionBean",
            "OASortableHeaderBean",
            "OATableBean",
            "OATableFooterBean",
            "OATableUtils",
            "OATotalRowBean" });

    static final List<String> list1 = Arrays.asList(new String[] {
            "OAAttachmentImageBean",
            "OABodyBean",
            "OADataScopeBean",
            "OADescriptiveFlexBean",
            "OADocumentBean",
            "OADownloadBean",
            "OAFlexBean",
            "OAFlexibleContentBean",
            "OAFlexibleContentListBean",
            "OAFormattedTextBean",
            "OAHeadBean",
            "OAHTMLWebBean",
            "OAIconBean",
            "OAImageBean",
            "OAImportScriptBean",
            "OAKeyFlexBean",
            "OALabelBean",
            "OALovSelectColumnBean",
            "OAPortletStyleSheetBean$1",
            "OAPortletStyleSheetBean$PSSRenderer",
            "OAPortletStyleSheetBean",
            "OAProcessingBean",
            "OARawTextBean",
            "OARepeaterBean",
            "OAScriptBean",
            "OAStaticStyledTextBean",
            "OAStyledTextBean",
            "OAStyleSheetBean",
            "OASwitcherBean",
            "OATipBean",
            "OATryBean",
            "OAWebBean",
            "OAWebBeanAttachment",
            "OAWebBeanBaseTable",
            "OAWebBeanCheckBox",
            "OAWebBeanChoice",
            "OAWebBeanClientAction",
            "OAWebBeanContainer",
            "OAWebBeanData",
            "OAWebBeanDataAttribute",
            "OAWebBeanDateField",
            "OAWebBeanDownload",
            "OAWebBeanFileUpload",
            "OAWebBeanFlex",
            "OAWebBeanFormElement",
            "OAWebBeanHGrid",
            "OAWebBeanHideShow",
            "OAWebBeanLov",
            "OAWebBeanMessage",
            "OAWebBeanPickList",
            "OAWebBeanRadioButton",
            "OAWebBeanRadioGroup",
            "OAWebBeanStatic",
            "OAWebBeanStyledText",
            "OAWebBeanTable",
            "OAWebBeanTextInput" });

    static final List<String> messageList = Arrays.asList(new String[] {
            "OAInlineMessageBean",
            "OAMessageAttachmentLinkBean",
            "OAMessageBoxBean",
            "OAMessageCheckBoxBean",
            "OAMessageChoiceBean",
            "OAMessageColorFieldBean",
            "OAMessageDateFieldBean",
            "OAMessageDownloadBean",
            "OAMessageFileUploadBean",
            "OAMessageInlineAttachmentBean",
            "OAMessageLayoutBean",
            "OAMessageListBean",
            "OAMessageLovChoiceBean",
            "OAMessageLovInputBean",
            "OAMessageLovTextInputBean",
            "OAMessagePromptBean",
            "OAMessageRadioButtonBean",
            "OAMessageRadioGroupBean",
            "OAMessageRichTextEditorBean",
            "OAMessageStyledTextBean",
            "OAMessageTextInputBean",
            "OARichTextEditorBean",
            "RichTextEditorBean$1",
            "RichTextEditorBean$RichTextUIExtension",
            "RichTextEditorBean",
            "RichTextEditorBeanConstants",
            "RichTextEditorValidator" });

    static final List<String> layoutList = Arrays.asList(new String[] {
            "OAAdvancedSearchBean",
            "OAAttachmentTableBean",
            "OABorderLayoutBean",
            "OABulletedListBean",
            "OAButtonSpacerBean",
            "OAButtonSpacerRowBean",
            "OACellFormatBean",
            "OAContentContainerBean",
            "OAContentFooterBean",
            "OADefaultDoubleColumnBean",
            "OADefaultFormStackLayoutBean",
            "OADefaultHideShowBean",
            "OADefaultSingleColumnBean",
            "OADefaultStackLayoutBean",
            "OADefaultTableLayoutBean",
            "OAFieldTableLayoutBean",
            "OAFlexibleCellLayoutBean",
            "OAFlexibleLayoutBean",
            "OAFlexibleRowLayoutBean",
            "OAFlowLayoutBean",
            "OAFrameBean",
            "OAFrameBorderLayoutBean",
            "OAGraphTableBean",
            "OAHeaderBean",
            "OAHideShowBean",
            "OAHideShowHeaderBean",
            "OAKFFLovBean",
            "OALabeledFieldLayoutBean",
            "OAListOfValuesBean",
            "OALovBean",
            "OAMessageComponentLayoutBean",
            "OAPageHeaderLayoutBean",
            "OAPageLayoutBean",
            "OAQueryBean",
            "OARowLayoutBean",
            "OASeparatorBean",
            "OASpacerBean",
            "OASpacerCellBean",
            "OASpacerRowBean",
            "OAStackLayoutBean",
            "OAStyledItemBean",
            "OAStyledListBean",
            "OASubTabLayoutBean",
            "OATableLayoutBean" });
}

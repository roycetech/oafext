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
package oafext.test.webui;

import java.util.Arrays;
import java.util.List;

import oafext.lang.Return;
import oafext.logging.OafLogger;
import oracle.apps.fnd.framework.webui.beans.OAWebBean;

public final class OABeanUtil {


    /** */
    private static final OafLogger LOGGER = OafLogger.getInstance();

    static final List<String> LIST_BEANS = Arrays.asList(new String[] {
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

    static final List<String> LIST_FORM = Arrays.asList(new String[] {
            "OACheckBoxBean",
            "OAChoiceBean",
            "OADateFieldBean",
            "OADefaultListBean",
            "OADefaultShuttleBean",
            "OAExportBean",
            "OAFileUploadBean",
            "OAFormBean",
            "OAFormParameterBean",
            "OAFormValueBean",
            "OAInlineDatePickerBean",
            "OAInternalFileUploadBean",
            "OAListBean",
            "OALovTextInputBean",
            "OAOptionBean",
            "OARadioButtonBean",
            "OARadioGroupBean",
            "OAResetButtonBean",
            "OASelectionButtonBean",
            "OAShuttleBean",
            "OASubmitButtonBean",
            "OATextInputBean",
            "RichTextEditorBean" });

    static final List<String> LIST_LAYOUT = Arrays.asList(new String[] {
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

    static final List<String> LIST_MSG = Arrays.asList(new String[] {
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


    static final List<String> LIST_TAB = Arrays.asList(new String[] {
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


    /** */
    private OABeanUtil() {}

    /**
     * Will construct OAF web bean simple name from element name.
     *
     * e.g. oa:messageStyledText will result into OAMessageStyledTextBean.
     *
     * @param elemName XML Element Name.
     * @return Web Bean class simple name.
     */
    static String buildOaWebBeanType(final String elemName)
    {
        final String[] arr = elemName.split(":");
        return "OA" + arr[1].substring(0, 1).toUpperCase()
                + arr[1].substring(1) + "Bean";
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends OAWebBean> getOABeanClass(final String string)
    {
        String className;
        if (LIST_BEANS.contains(string)) {
            className = "oracle.apps.fnd.framework.webui.beans." + string;
        } else if (LIST_MSG.contains(string)) {
            className =
                    "oracle.apps.fnd.framework.webui.beans.message." + string;
        } else if (LIST_LAYOUT.contains(string)) {
            className =
                    "oracle.apps.fnd.framework.webui.beans.layout." + string;
        } else if (LIST_TAB.contains(string)) {
            className = "oracle.apps.fnd.framework.webui.beans.table." + string;
        } else if (LIST_FORM.contains(string)) {
            className = "oracle.apps.fnd.framework.webui.beans.form." + string;
        } else if (LIST_NAV.contains(string)) {
            className = "oracle.apps.fnd.framework.webui.beans.nav." + string;
        } else {
            throw new UnsupportedOperationException("Implement: " + string);
        }

        final Return<Class<? extends OAWebBean>> retval = new Return<>();
        try {

            retval.set((Class<? extends OAWebBean>) Class.forName(className));
        } catch (final ClassNotFoundException e) {
            LOGGER.error(e);
        }
        return retval.get();
    }
}

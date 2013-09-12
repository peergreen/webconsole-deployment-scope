package com.peergreen.webconsole.scope.deployment.internal.components;

import com.peergreen.deployment.report.ArtifactErrorDetail;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Mohammed Boukada
 */
public class ExceptionView  extends VerticalLayout {

    private ArtifactErrorDetail artifactErrorDetail;
    private HorizontalLayout messageView;
    private VerticalLayout stackTraceView;

    public ExceptionView(ArtifactErrorDetail artifactErrorDetail) {
        this.artifactErrorDetail = artifactErrorDetail;
        setStyleName("repository-entry");
        showDefaultView();
    }

    private void showDefaultView() {
        if (messageView == null) {
            messageView = new HorizontalLayout();
            messageView.setWidth("100%");
            StringBuilder sb = new StringBuilder();
            sb.append("<span style=\"color:red\">");
            sb.append(artifactErrorDetail.getMessage());
            sb.append("</span>");
            Label message = new Label(sb.toString(), ContentMode.HTML);
            messageView.addComponent(message);
            messageView.setComponentAlignment(message, Alignment.TOP_LEFT);
            Button details = new Button("Details");
            details.addStyleName("link");
            details.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    showDetailsView();
                }
            });
            messageView.addComponent(details);
            messageView.setComponentAlignment(details, Alignment.TOP_RIGHT);
        }

        removeAllComponents();
        addComponent(messageView);
    }

    private void showDetailsView() {
        if (stackTraceView == null) {
            stackTraceView = new VerticalLayout();
            stackTraceView.setWidth("100%");

            StringBuilder sb = new StringBuilder();
            sb.append("<span style=\"color:red\"> Message : ");
            sb.append(artifactErrorDetail.getMessage());
            sb.append("<br />");
            for (StackTraceElement element : artifactErrorDetail.getStackTrace()) {
                sb.append("&nbsp;&nbsp;&nbsp;");
                sb.append(" |- ");
                sb.append(element.getClassName());
                sb.append('.');
                sb.append(element.getMethodName());
                sb.append('(');
                sb.append(element.getFileName());
                sb.append(':');
                sb.append(element.getLineNumber());
                sb.append(')');
                sb.append("<br />");
            }
            sb.append("</span>");
            Label stackTrace = new Label(sb.toString(), ContentMode.HTML);
            stackTraceView.addComponent(stackTrace);
            stackTraceView.setComponentAlignment(stackTrace, Alignment.TOP_LEFT);

            Button hideDetails = new Button("Hide");
            hideDetails.addStyleName("link");
            hideDetails.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    showDefaultView();
                }
            });
            stackTraceView.addComponent(hideDetails);
            stackTraceView.setComponentAlignment(hideDetails, Alignment.TOP_RIGHT);
        }

        removeAllComponents();
        addComponent(stackTraceView);
    }


}

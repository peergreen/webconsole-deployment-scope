package com.peergreen.webconsole.scope.deployment.internal.components;

import java.net.MalformedURLException;

import com.peergreen.deployment.facet.endpoint.Endpoint;
import com.peergreen.deployment.report.ArtifactStatusReport;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableEntry;
import com.peergreen.webconsole.vaadin.DefaultWindow;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Mohammed Boukada
 */
public class DeployableWindow {

    private DeployableEntry deployableEntry;
    private ArtifactStatusReport report;

    public DeployableWindow(DeployableEntry deployableEntry) {
        this.deployableEntry = deployableEntry;
    }

    public DeployableWindow(DeployableEntry deployableEntry, ArtifactStatusReport report) {
        this.deployableEntry = deployableEntry;
        this.report = report;
    }

    public Component getContent() {
        FormLayout content = new FormLayout();
        content.setSpacing(true);
        content.setMargin(true);

        Label name = new Label(deployableEntry.getName());
        name.setCaption("Name");
        content.addComponent(name);
        Label uri = new Label(deployableEntry.getUri().toString());
        uri.setCaption("URI");
        content.addComponent(uri);
        Label status = new Label();
        status.setCaption("Status");
        content.addComponent(status);
        if (report == null) {
            // is not deployed yet
            status.setValue("Ready to be deployed");
        } else {
            status.setValue("Deployed");
            VerticalLayout endPointsLayout = new VerticalLayout();
            for (Endpoint endpoint : report.getEndpoints()) {
                try {
                    Link link = new Link(endpoint.getURI().toString(), new ExternalResource(endpoint.getURI().toURL()));
                    endPointsLayout.addComponent(link);
                } catch (MalformedURLException e) {
                    endPointsLayout.addComponent(new Label(endpoint.getURI().toString()));
                }
            }

            if (endPointsLayout.getComponentCount() > 0) {
                content.addComponent(endPointsLayout);
                endPointsLayout.setCaption("End points");
            }
        }
        return content;
    }

    public DefaultWindow getWindow() {
        Button close = new Button("Close");
        close.addStyleName("wide");
        close.addStyleName("default");
        final DefaultWindow w = new DefaultWindow(deployableEntry.getName(), getContent(), close);
        close.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                w.close();
            }
        });
        w.center();
        return w;
    }
}

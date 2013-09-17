/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.components;

import java.net.MalformedURLException;

import com.peergreen.deployment.facet.endpoint.Endpoint;
import com.peergreen.deployment.report.ArtifactError;
import com.peergreen.deployment.report.ArtifactErrorDetail;
import com.peergreen.deployment.report.ArtifactStatusReport;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableEntry;
import com.peergreen.webconsole.vaadin.DefaultWindow;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
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
        VerticalLayout status = new VerticalLayout();
        status.setCaption("Status");
        content.addComponent(status);
        if (report == null) {
            // is not deployed yet
            status.addComponent(new Label("Ready to be deployed"));
        } else {
            if (report.getExceptions().size() == 0) {
                status.addComponent(new Label("<p style=\"color:#329932\">Deployed</p>", ContentMode.HTML));
            } else {
                for (ArtifactError artifactError : report.getExceptions()) {
                    for (ArtifactErrorDetail detail : artifactError.getDetails()) {
                        ExceptionView exceptionView = new ExceptionView(detail);
                        status.addComponent(exceptionView);
                    }
                }
            }
            VerticalLayout endPointsLayout = new VerticalLayout();
            for (Endpoint endpoint : report.getEndpoints()) {
                try {
                    Link link = new Link(endpoint.getURI().toString(), new ExternalResource(endpoint.getURI().toURL()));
                    link.setTargetName("_blank");
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

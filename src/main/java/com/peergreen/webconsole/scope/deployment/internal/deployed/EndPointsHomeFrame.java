/**
 * Peergreen S.A.S. All rights reserved.
 * Proprietary and confidential.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.deployed;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URI;

import com.peergreen.deployment.facet.endpoint.Endpoint;
import com.peergreen.deployment.model.ArtifactModelManager;
import com.peergreen.deployment.report.ArtifactStatusReportException;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.scope.deployment.internal.service.facade.DeploymentManager;
import com.peergreen.webconsole.scope.home.Frame;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.home.HomeScope.top.right")
@Frame("End points")
public class EndPointsHomeFrame extends Table {

    @Inject
    private ArtifactModelManager artifactModelManager;
    @Inject
    private DeploymentManager deploymentManager;

    @PostConstruct
    public void init() {
        addContainerProperty("Artifact", String.class, null);
        addContainerProperty("End points", VerticalLayout.class, null);
        setWidth("100%");
        setPageLength(10);
        setImmediate(true);
        addStyleName("plain");
        addStyleName("borderless");
        setSortEnabled(false);
        setImmediate(true);
    }

    private void updateEndPoints() {
        removeAllItems();
        int i = 0;
        for (URI uri : artifactModelManager.getDeployedRootURIs()) {
            VerticalLayout endPointsLayout = new VerticalLayout();
            try {
                for (Endpoint endpoint : deploymentManager.getReport(uri.toString()).getEndpoints()) {
                    Link link = new Link(endpoint.getURI().toString(), new ExternalResource(endpoint.getURI().toURL()));
                    link.setTargetName("_blank");
                    endPointsLayout.addComponent(link);
                }
            } catch (ArtifactStatusReportException | MalformedURLException e) {
                e.printStackTrace();
            }

            if (endPointsLayout.getComponentCount() > 0) {
                addItem(new Object[]{artifactModelManager.getArtifactModel(uri).getArtifact().name(), endPointsLayout}, i);
                i++;
            }
        }
    }

    @Override
    public void attach() {
        super.attach();
        updateEndPoints();
    }
}

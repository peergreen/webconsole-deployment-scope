/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.container.entry;

import com.peergreen.deployment.report.ArtifactStatusReportException;
import com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainerType;
import com.peergreen.webconsole.scope.deployment.internal.service.facade.DeploymentManager;
import com.vaadin.ui.Table;

/**
 * @author Mohammed Boukada
 */
public class ItemStyle implements Table.CellStyleGenerator {

    private DeployableContainerType containerType;
    private DeploymentManager deploymentManager;

    public ItemStyle(DeployableContainerType containerType) {
        this(containerType, null);
    }

    public ItemStyle(DeployableContainerType containerType, DeploymentManager deploymentManager) {
        this.containerType = containerType;
        this.deploymentManager = deploymentManager;
    }

    @Override
    public String getStyle(Table source, Object itemId, Object propertyId) {
        switch (containerType) {
            case DEPLOYABLE:
                return "";
            case DEPLOYED:
//                return "deployed-entry";
                DeployableEntry deployableEntry = (DeployableEntry) itemId;
                try {
                    if (deploymentManager != null &&
                        deploymentManager.getReport(deployableEntry.getUri().toString()).getExceptions().size() > 0) {
                        return "deployed-entry-error";
                    }
                } catch (ArtifactStatusReportException e) {
                    // do nothing
                }
                return "";
            case DEPLOYMENT_PLAN:
                return "";
        }
        return "";
    }
}

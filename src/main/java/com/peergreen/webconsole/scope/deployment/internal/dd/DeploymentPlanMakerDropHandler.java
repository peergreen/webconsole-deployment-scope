/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.dd;

import com.peergreen.webconsole.notifier.INotifierService;
import com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;

/**
 * @author Mohammed Boukada
 */
public class DeploymentPlanMakerDropHandler implements DropHandler {

    private DropHandler delegate;
    private DeploymentViewManager deploymentViewManager;

    public DeploymentPlanMakerDropHandler(DeploymentViewManager deploymentViewManager, DeployableContainer deployableContainer, INotifierService notifierService) {
        delegate = new DeploymentDropHandler(deploymentViewManager, deployableContainer, notifierService);
        this.deploymentViewManager = deploymentViewManager;
    }

    @Override
    public void drop(DragAndDropEvent event) {
        deploymentViewManager.showDeploymentPlanView();
        delegate.drop(event);
    }

    @Override
    public AcceptCriterion getAcceptCriterion() {
        return AcceptAll.get();
    }
}

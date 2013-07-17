/*
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.dd;

import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.scope.deployment.IDeploymentView;
import com.peergreen.webconsole.scope.deployment.components.DeployableEntry;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Html5File;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @author Mohammed Boukada
 */
public class DeploymentViewDropHandler implements DropHandler {

    private IDeploymentView deploymentView;
    private boolean deploy;
    private INotifierService notifierService;

    public DeploymentViewDropHandler(IDeploymentView deploymentView, INotifierService notifierService, boolean deploy) {
        this.deploymentView = deploymentView;
        this.notifierService = notifierService;
        this.deploy = deploy;
    }

    @Override
    public void drop(DragAndDropEvent event) {
        final Component deployableEntry = event.getTransferable().getSourceComponent();
        if (deployableEntry != null && deployableEntry instanceof DeployableEntry) {
            if (deploy) {
                deploymentView.deploy((DeployableEntry) deployableEntry);
            } else {
                deploymentView.undeploy((DeployableEntry) deployableEntry);
            }
        } else {
            try {
                Html5File[] files = ((DragAndDropWrapper.WrapperTransferable) event.getTransferable()).getFiles();
                for (Html5File file : files) {
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(Constants.STORAGE_DIRECTORY + File.separator + file.getFileName(), true);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    file.setStreamVariable(
                            new DesktopDragAndDropStream(fos, file.getFileName(), notifierService, deploymentView, deploy));
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    @Override
    public AcceptCriterion getAcceptCriterion() {
        return AcceptAll.get();
    }
}

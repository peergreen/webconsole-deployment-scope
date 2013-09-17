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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Collections;

import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.notifier.INotifierService;
import com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.peergreen.webconsole.vaadin.ConfirmDialog;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Html5File;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

/**
 * @author Mohammed Boukada
 */
public class DeploymentDropHandler implements DropHandler {

    private DeploymentViewManager deploymentViewManager;
    private DeployableContainer deployableContainer;
    private INotifierService notifierService;

    public DeploymentDropHandler(DeploymentViewManager deploymentViewManager, DeployableContainer deployableContainer, INotifierService notifierService) {
        this.deploymentViewManager = deploymentViewManager;
        this.deployableContainer = deployableContainer;
        this.notifierService = notifierService;
    }

    @Override
    public void drop(DragAndDropEvent event) {
        final Component component = event.getTransferable().getSourceComponent();
        Collection<DeployableEntry> draggedDeployableEntries;
        if (component instanceof Table) {
            Table table = (Table) component;
            draggedDeployableEntries = (Collection<DeployableEntry>) table.getValue();

            DataBoundTransferable t = (DataBoundTransferable) event.getTransferable();
            DeployableEntry deployableEntry = (DeployableEntry) t.getItemId();

            if (draggedDeployableEntries.size() <= 1 ||
                    (draggedDeployableEntries.size() > 1 && !draggedDeployableEntries.contains(deployableEntry))) {
                draggedDeployableEntries = Collections.singleton(deployableEntry);
            }

            for (final DeployableEntry entry : draggedDeployableEntries) {
                if (!deployableContainer.equals(entry.getContainer())) {
                    if (entry.isDeployable()) {
                        // single artifact
                        deployableContainer.receive(entry);
                    } else {
                        // dragging a directory
                        String message = String.format("Would you like to create a deployment plan <br />with the content of <b>%s</b> ?", entry.getName());
                        Label label = new Label(message, ContentMode.HTML);
                        ConfirmDialog.show(deployableContainer.getView().getUI(), label, new ConfirmDialog.Listener() {
                            @Override
                            public void onClose(boolean isConfirmed) {
                                if (isConfirmed) {
                                    for (DeployableEntry child : entry.getChildren()) {
                                        deploymentViewManager.addToDeploymentPlan(child.getUri());
                                    }
                                    deploymentViewManager.showDeploymentPlanView();
                                }
                            }
                        });
                    }
                }
            }
        } else {
            try {
                Html5File[] files = ((DragAndDropWrapper.WrapperTransferable) event.getTransferable()).getFiles();
                for (Html5File file : files) {
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(Constants.STORAGE_DIRECTORY + File.separator + file.getFileName(), true);
                    } catch (FileNotFoundException e) {
                        // do nothing
                    }
                    file.setStreamVariable(
                            new DesktopDragAndDropStream(fos, file.getFileName(), notifierService, deployableContainer));
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

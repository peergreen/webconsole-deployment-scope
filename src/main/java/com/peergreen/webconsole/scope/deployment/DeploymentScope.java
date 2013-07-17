/*
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment;

import com.peergreen.deployment.Artifact;
import com.peergreen.deployment.ArtifactBuilder;
import com.peergreen.deployment.ArtifactProcessRequest;
import com.peergreen.deployment.DeploymentMode;
import com.peergreen.deployment.model.ArtifactModelManager;
import com.peergreen.deployment.report.DeploymentStatusReport;
import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Ready;
import com.peergreen.webconsole.Scope;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.scope.deployment.components.AbstractFrame;
import com.peergreen.webconsole.scope.deployment.dd.DeploymentViewDropHandler;
import com.peergreen.webconsole.scope.deployment.components.FileUploader;
import com.peergreen.webconsole.scope.deployment.service.DeploymentManager;
import com.peergreen.webconsole.scope.deployment.components.DeployableEntry;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope")
@Scope("deployment")
public class DeploymentScope extends VerticalLayout implements IDeploymentView {

    private final static String DEPLOYED_STATE = "deployed";
    private final static String UNDEPLOYED_STATE = "undeployed";
    private final static String UPDATE_STATE = "updated";
    private final static String FAILED_STATE = "failed";

    @Inject
    private INotifierService notifierService;
    @Inject
    private UIContext uiContext;
    @Inject
    private DeploymentManager deploymentManager;
    @Inject
    private ArtifactModelManager artifactModelManager;
    @Inject
    private ArtifactBuilder artifactBuilder;
    private HorizontalLayout uploadRow;
    private AbstractFrame deployableFrame;
    private AbstractFrame deployedFrame;

    public DeploymentScope() {
        setSizeFull();
        addStyleName("deployment-view");
        setMargin(true);
    }

    @Ready
    public void init() {
        addStyle();
        uploadRow = new HorizontalLayout();
        uploadRow.setWidth("100%");
        uploadRow.setSpacing(true);
        uploadRow.addComponent(createUpload(notifierService));

        addComponent(uploadRow);

        final HorizontalLayout row2 = new HorizontalLayout();
        row2.setSizeFull();
        row2.setMargin(true);
        row2.setSpacing(true);
        row2.addStyleName("row");

        deployableFrame = new AbstractFrame("Deployable Artifacts", new String[]{DeploymentActions.DEPLOY, DeploymentActions.DELETE, DeploymentActions.DOWNLOAD}, this);
        deployedFrame = new AbstractFrame("Deployed Artifacts", new String[]{DeploymentActions.UNDEPLOY, DeploymentActions.UPDATE, DeploymentActions.DELETE, DeploymentActions.DOWNLOAD}, this);

        DragAndDropWrapper deployablePanelWrapper = createPanel(deployableFrame);

        deployablePanelWrapper.setDropHandler(new DeploymentViewDropHandler(this, notifierService, false));
        row2.addComponent(deployablePanelWrapper);
        row2.setExpandRatio(deployablePanelWrapper, 1.5f);

        DragAndDropWrapper deployedPanelWrapper = createPanel(deployedFrame);
        deployedPanelWrapper.setDropHandler(new DeploymentViewDropHandler(this, notifierService, true));
        row2.addComponent(deployedPanelWrapper);
        row2.setExpandRatio(deployedPanelWrapper, 1.5f);

        addComponent(row2);
        setExpandRatio(row2, 1.5f);

        refresh();
    }

    @Override
    public void addDeployable(final String filename) {
        URI uri = new File(Constants.STORAGE_DIRECTORY + File.separator + filename).toURI();
        deployableFrame.addDeployableEntry(uri);
    }

    @Override
    public void addToDeployed(String filename) {
        URI uri = new File(Constants.STORAGE_DIRECTORY + File.separator + filename).toURI();
        deployedFrame.addDeployableEntry(uri);
    }

    @Override
    public void deploy(final List<DeployableEntry> deployableEntries) {
        ConfirmDialog.show(uiContext.getUI(), "Please confirm", formatDeployableEntriesList(deployableEntries), "Deploy", "Cancel", new ConfirmDialog.Listener() {
            @Override
            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    for (DeployableEntry deployableEntry : deployableEntries) {
                        deployableFrame.removeDeployableEntry(deployableEntry);
                        deployedFrame.addDeployableEntry(deployableEntry);
                        DeployerThread deployerThread = new DeployerThread(deployableEntry, DeploymentMode.DEPLOY);
                        deployerThread.start();
                    }
                }
            }
        });
    }

    @Override
    public void deploy(final DeployableEntry deployableEntry) {
        if (!deployedFrame.containsDeployableEntry(deployableEntry)) {
            deploy(Collections.singletonList(deployableEntry));
        }
    }

    @Override
    public void undeploy(final List<DeployableEntry> deployableEntries) {
        ConfirmDialog.show(uiContext.getUI(), "Please confirm", formatDeployableEntriesList(deployableEntries), "Undeploy", "Cancel", new ConfirmDialog.Listener() {
            @Override
            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    for (DeployableEntry deployableEntry : deployableEntries) {
                        deployedFrame.removeDeployableEntry(deployableEntry);
                        deployableFrame.addDeployableEntry(deployableEntry);
                        DeployerThread deployerThread = new DeployerThread(deployableEntry, DeploymentMode.UNDEPLOY);
                        deployerThread.start();
                    }
                }
            }
        });
    }

    @Override
    public void undeploy(final DeployableEntry deployableEntry) {
        if (!deployableFrame.containsDeployableEntry(deployableEntry)) {
            undeploy(Collections.singletonList(deployableEntry));
        }
    }

    @Override
    public void update(final List<DeployableEntry> deployableEntries) {
        ConfirmDialog.show(uiContext.getUI(), "Please confirm", formatDeployableEntriesList(deployableEntries), "Update", "Cancel", new ConfirmDialog.Listener() {
            @Override
            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    for (DeployableEntry deployableEntry : deployableEntries) {
                        deployedFrame.removeDeployableEntry(deployableEntry);
                        deployableFrame.addDeployableEntry(deployableEntry);
                        DeployerThread deployerThread = new DeployerThread(deployableEntry, DeploymentMode.UPDATE);
                        deployerThread.start();
                    }
                }
            }
        });
    }

    @Override
    public void update(final DeployableEntry deployableEntry) {
        update(Collections.singletonList(deployableEntry));
    }

    @Override
    public void delete(final List<DeployableEntry> deployableEntries) {
        ConfirmDialog.show(uiContext.getUI(), "Please confirm", formatDeployableEntriesList(deployableEntries), "Delete", "Cancel", new ConfirmDialog.Listener() {
            @Override
            public void onClose(ConfirmDialog dialog) {
                if (dialog.isConfirmed()) {
                    for (DeployableEntry deployableEntry : deployableEntries) {
                        delete(deployableEntry);
                    }
                }
            }
        });
    }

    @Override
    public void delete(final DeployableEntry deployableEntry) {
        deployableFrame.removeDeployableEntry(deployableEntry);
        deployedFrame.removeDeployableEntry(deployableEntry);
        File file = new File(deployableEntry.getUri());
        if (file.exists() && file.canWrite() && !file.isDirectory()) {
            file.delete();
        }
        notifierService.addNotification("'" + deployableEntry.getFileName() + "' was deleted.");
    }

    @Override
    public void download(List<DeployableEntry> deployableEntries, Button download) {
        File fileToDownload = null;
        if (deployableEntries.size() == 1) {
            fileToDownload = new File(deployableEntries.get(0).getUri());
        } else {
            //make a zip file
        }
        Resource resource = new FileResource(fileToDownload);
        FileDownloader fileDownloader = new FileDownloader(resource);
        fileDownloader.extend(download);
        download.click();
    }

    private Upload createUpload(INotifierService notifierService) {
        Upload uploader = new Upload("Upload a deployable here", null);
        uploader.setButtonCaption("Upload");
        final FileUploader fileUploader = new FileUploader(this, notifierService);
        uploader.setReceiver(fileUploader);
        uploader.addSucceededListener(fileUploader);
        uploader.addProgressListener(fileUploader);
        uploader.addStartedListener(fileUploader);
        return uploader;
    }

    private DragAndDropWrapper createPanel(AbstractFrame frame) {
        Panel panel = new Panel();
        panel.setSizeFull();
        panel.setStyleName("frame-panel");
        panel.setContent(frame);
        DragAndDropWrapper dragAndDropWrapper = new DragAndDropWrapper(panel);
        dragAndDropWrapper.setSizeFull();
        return dragAndDropWrapper;
    }

    private void addStyle() {
        Page.Styles styles = Page.getCurrent().getStyles();
        styles.add(".deployable-entry {" +
                "background : -webkit-linear-gradient(top, #ffffff 0%, #fafafa 5%, #eaebec 94%, #e2e3e4 100%);" +
                "padding : 10px !important;" +
                "width : 100%;" +
                "border: 0.1em solid #b3b4b5;" +
                "border-top-color: #d4d4d6;" +
                "border-bottom-color: #989b9c;" +
                "-webkit-box-shadow: 0 2px 5px rgba(0, 0, 0, 0.5);" +
                "-moz-box-shadow: 0 2px 5px rgba(0, 0, 0, 0.5);" +
                "box-shadow: 0 2px 5px rgba(0, 0, 0, 0.5);" +
                "-webkit-border-radius: 2px;" +
                "-moz-border-radius: 2px;" +
                "border-radius: 2px;" +
                "overflow: hidden;" +
                "}" +
                ".deployable-entry input {" +
                "padding-right : 10px;" +
                "}" +
                ".deployment-view {" +
                "background: #dfe0e1;" +
                "background: -moz-linear-gradient(top, #d8d9da 0%, #e6e7e8 10%, #e0e1e2 100%);" +
                "background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #d8d9da), color-stop(10%, #e6e7e8), color-stop(100%, #e0e1e2));" +
                "background: -webkit-linear-gradient(top, #d8d9da 0%, #e6e7e8 10%, #e0e1e2 100%);" +
                "background: -o-linear-gradient(top, #d8d9da 0%, #e6e7e8 10%, #e0e1e2 100%);" +
                "background: -ms-linear-gradient(top, #d8d9da 0%, #e6e7e8 10%, #e0e1e2 100%);" +
                "}" +
                ".deployment-view .v-caption {" +
                "font-weight: bold;" +
                "text-transform: uppercase;" +
                "color: #349aff;" +
                "font-size: 14px;" +
                "}");
    }

    private String formatDeployableEntriesList(List<DeployableEntry> deployableEntries) {
        StringBuilder sb = new StringBuilder();
        for (DeployableEntry deployableEntry : deployableEntries) {
            sb.append(deployableEntry.getFileName()).append("\n");
        }
        return sb.toString();
    }

    private void refresh() {
        Collection<URI> deployedURIs = artifactModelManager.getDeployedRootURIs();

        // Get deployables from deploy/ and peergreen/storage/webconsole/
        File storageDir = new File(Constants.STORAGE_DIRECTORY);
        File[] storageFiles = new File[0];
        if (storageDir.exists() && storageDir.isDirectory()) {
            storageFiles = storageDir.listFiles();
        }

        File deployDir = new File("deploy");
        File[] deployFiles = new File[0];
        if (deployDir.exists() && deployDir.isDirectory()) {
            deployFiles = deployDir.listFiles();
        }

        File[] files = new File[storageFiles.length + deployFiles.length];
        System.arraycopy(storageFiles, 0, files, 0, storageFiles.length);
        System.arraycopy(deployFiles, 0, files, storageFiles.length, deployFiles.length);
        for (File file : files) {
            if (!file.isDirectory()) {
                if (!deployableFrame.containsDeployableEntry(file.toURI())
                        && !deployedURIs.contains(file.toURI())) {
                    setDeployableState(deployableFrame.addDeployableEntry(file.toURI()));
                    if (deployedFrame.containsDeployableEntry(file.toURI())) {
                        deployedFrame.removeDeployableEntry(file.toURI());
                    }
                }
            }
        }

        // Get deployed
        for (URI uri : artifactModelManager.getDeployedRootURIs()) {
            if (!deployedFrame.containsDeployableEntry(uri)) {
                setDeployableState(deployedFrame.addDeployableEntry(uri));
                if (deployableFrame.containsDeployableEntry(uri)) {
                    deployableFrame.removeDeployableEntry(uri);
                }
            }
        }

    }

    @Override
    public void attach() {
        super.attach();
        refresh();
    }

    protected void setDeployableState(DeployableEntry deployableEntry) {
//        ArtifactModel artifactModel = artifactModelManager.getArtifactModel(deployableEntry.getUri());
//        switch (artifactModel.as(ArtifactModelDeploymentView.class).getDeploymentState()) {
//            case DEPLOYED:
//                deployableEntry.setState(DEPLOYED_STATE);
//                break;
//            case UNDEPLOYED:
//                deployableEntry.setState(UNDEPLOYED_STATE);
//                break;
//            case UNKNOWN:
//                deployableEntry.setState(FAILED_STATE);
//                break;
//        }
    }

    private class DeployerThread extends Thread {

        private DeployableEntry deployableEntry;
        private DeploymentMode deploymentMode;

        private DeployerThread(DeployableEntry deployableEntry, DeploymentMode deploymentMode) {
            this.deployableEntry = deployableEntry;
            this.deploymentMode = deploymentMode;
        }

        @Override
        public void run() {
            // disable drag and drop until the artifact is deployed
            //deployableEntry.setDragStartMode(DragAndDropWrapper.DragStartMode.NONE);
            Artifact artifact = artifactBuilder.build(deployableEntry.getFileName(), deployableEntry.getUri());
            ArtifactProcessRequest artifactProcessRequest = new ArtifactProcessRequest(artifact);
            artifactProcessRequest.setDeploymentMode(deploymentMode);
            DeploymentStatusReport report = deploymentManager.process(Collections.singleton(artifactProcessRequest));
            String state = "";
            if (report.hasFailed()) {
                state = FAILED_STATE;
            } else {
                switch (deploymentMode) {
                    case DEPLOY:
                        state = DEPLOYED_STATE;
                        break;
                    case UNDEPLOY:
                        state = UNDEPLOYED_STATE;
                        break;
                    case UPDATE:
                        state = UPDATE_STATE;
                        break;
                }
            }
            notifierService.addNotification("'" + deployableEntry.getFileName() + "' was " + state + ".");
            setDeployableState(deployableEntry);
            // Enable drag and drop
            //deployableEntry.setDragStartMode(DragAndDropWrapper.DragStartMode.COMPONENT);
        }
    }
}

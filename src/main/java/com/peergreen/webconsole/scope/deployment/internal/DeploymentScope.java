/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Requires;

import com.peergreen.deployment.ArtifactBuilder;
import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.HelpOverlay;
import com.peergreen.webconsole.notifier.INotifierService;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Link;
import com.peergreen.webconsole.Scope;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.Unlink;
import com.peergreen.webconsole.scope.deployment.internal.components.FileUploader;
import com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.container.DeployableContainerType;
import com.peergreen.webconsole.scope.deployment.internal.dd.DeploymentPlanMakerDropHandler;
import com.peergreen.webconsole.scope.deployment.internal.manager.BaseDeploymentViewManager;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope")
@Scope("deployment")
public class DeploymentScope extends VerticalLayout {

    @Inject
    private ArtifactBuilder artifactBuilder;
    @Inject
    private INotifierService notifierService;
    @Inject
    private UIContext uiContext;
    @Requires(from = "com.peergreen.webconsole.scope.deployment.internal.manager.BaseDeploymentViewManager")
    private Factory deploymentViewManagerFactory;

    private DragAndDropWrapper deploymentPlanMakerWrapper;
    private HorizontalLayout framesContainer;
    private BaseDeploymentViewManager deploymentViewManager;
    private ComponentInstance deploymentViewManagerComponentInstance;

    private HelpOverlay helpWindow;

    public DeploymentScope() {
        setSizeFull();
        addStyleName("deployment-view");
        setMargin(true);
        framesContainer = new HorizontalLayout();
        framesContainer.setSizeFull();
        framesContainer.setMargin(true);
        framesContainer.setSpacing(true);
        framesContainer.addStyleName("row");
    }

    @PostConstruct
    public void init() {
        deploymentViewManager = createDeploymentViewManager();

        OptionGroup option = new OptionGroup();
        HorizontalLayout toolBar = new HorizontalLayout();
        toolBar.setWidth("100%");
        toolBar.setSpacing(true);
        toolBar.setMargin(true);

        VerticalLayout uploadLayout = new VerticalLayout();

        Upload uploader = new Upload("Upload a file here", null);
        uploader.setButtonCaption("Upload");
        final FileUploader fileUploader = new FileUploader(deploymentViewManager, notifierService, artifactBuilder, option);
        uploader.setReceiver(fileUploader);
        uploader.addSucceededListener(fileUploader);
        uploader.addStartedListener(fileUploader);
        uploadLayout.addComponent(uploader);

        HorizontalLayout target = new HorizontalLayout();
        option.addContainerProperty("id", String.class, null);
        option.setItemCaptionPropertyId("id");
        option.addItem(DeployableContainerType.DEPLOYABLE.attribute()).getItemProperty("id").setValue("Add to deployables");
        option.addItem(DeployableContainerType.DEPLOYED.attribute()).getItemProperty("id").setValue("Deploy");
        option.addItem(DeployableContainerType.DEPLOYMENT_PLAN.attribute()).getItemProperty("id").setValue("Init a deployment plan");
        option.addStyleName("horizontal");
        option.select(DeployableContainerType.DEPLOYABLE.attribute());

        target.addComponent(option);
        uploadLayout.addComponent(target);
        toolBar.addComponent(uploadLayout);

        Label infoLabel = new Label("Drop files here to create a deployment plan");
        infoLabel.setSizeUndefined();
        final VerticalLayout deploymentPlanMaker = new VerticalLayout(infoLabel);
        deploymentPlanMaker.setComponentAlignment(infoLabel, Alignment.MIDDLE_CENTER);
        Button draft = new Button("A draft is under construction");
        draft.addStyleName("link");
        draft.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                deploymentViewManager.showDeploymentPlanView();
            }
        });
        draft.setVisible(false);
        deploymentViewManager.setDeploymentPlanDraftViewer(draft);
        deploymentPlanMaker.addComponent(draft);
        deploymentPlanMaker.setComponentAlignment(draft, Alignment.TOP_CENTER);
        deploymentPlanMaker.setSizeFull();
        deploymentPlanMaker.addStyleName("drop-area");
        deploymentPlanMakerWrapper = new DragAndDropWrapper(deploymentPlanMaker);
        deploymentPlanMakerWrapper.setSizeFull();
        toolBar.addComponent(deploymentPlanMakerWrapper);
        addComponent(toolBar);

        addComponent(framesContainer);
        setExpandRatio(framesContainer, 1.5f);

        helpWindow = notifierService.createHelpOverlay(
                "Deployment module",
                "<p>To deploy, or undeploy, artifacts, you can drag and drop elements from deployables panel " +
                        "to deployed panel and vice versa.</p>" +
                        "<p>You can also drag files from desktop and drop them where you want to add them.");
    }

    @PreDestroy
    public void stop() {
        if (deploymentViewManagerComponentInstance != null) {
            deploymentViewManagerComponentInstance.stop();
            deploymentViewManagerComponentInstance.dispose();
        }
    }

    private BaseDeploymentViewManager createDeploymentViewManager() {
        BaseDeploymentViewManager baseDeploymentViewManager = new BaseDeploymentViewManager(framesContainer);
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(Constants.UI_ID, uiContext.getUIId());
        properties.put("instance.object", baseDeploymentViewManager);
        try {
            deploymentViewManagerComponentInstance = deploymentViewManagerFactory.createComponentInstance(properties);
        } catch (UnacceptableConfiguration | MissingHandlerException | ConfigurationException unacceptableConfiguration) {
            return null;
        }
        return baseDeploymentViewManager;
    }

    @Link("deployable")
    public void addDeployablePanel(DeployableContainer deployablePanel) {
        framesContainer.addComponentAsFirst(deployablePanel.getView());
        deploymentViewManager.setDeployableContainer(deployablePanel);
    }

    @Unlink("deployable")
    public void removeDeployablePanel(DeployableContainer deployablePanel) {
        framesContainer.removeComponent(deployablePanel.getView());
        deploymentViewManager.setDeployableContainer(null);
    }

    @Link("deployed")
    public void addDeployedPanel(DeployableContainer deployedPanel) {
        framesContainer.addComponent(deployedPanel.getView());
        deploymentViewManager.setDeployedContainer(deployedPanel);
    }

    @Unlink("deployed")
    public void removeDeployedPanel(DeployableContainer deployedPanel) {
        framesContainer.removeComponent(deployedPanel.getView());
        deploymentViewManager.setDeployedContainer(null);
    }

    @Link("deployment.plan")
    public void addDeploymentPlanPanel(DeployableContainer deploymentPlanPanel) {
        deploymentViewManager.setDeploymentPlanContainer(deploymentPlanPanel);
        deploymentPlanMakerWrapper.setDropHandler(new DeploymentPlanMakerDropHandler(deploymentViewManager, deploymentPlanPanel, notifierService));
    }

    @Unlink("deployment.plan")
    public void removeDeploymentPlanPanel(DeployableContainer deploymentPlanPanel) {
        framesContainer.removeComponent(deploymentPlanPanel.getView());
        deploymentViewManager.setDeploymentPlanContainer(null);
        deploymentPlanMakerWrapper.setDropHandler(null);
    }

    @Override
    public void attach() {
        super.attach();
        if (helpWindow != null && !helpWindow.isSeen() && !helpWindow.isAttached()) {
            getUI().addWindow(helpWindow);
        }
    }

    @Override
    public void detach() {
        super.detach();
        if (helpWindow != null && helpWindow.isAttached()) {
            getUI().removeWindow(helpWindow);
        }
    }
}

package com.peergreen.webconsole.scope.deployment.internal;

import com.peergreen.deployment.ArtifactBuilder;
import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.HelpOverlay;
import com.peergreen.webconsole.INotifierService;
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
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.MissingHandlerException;
import org.apache.felix.ipojo.UnacceptableConfiguration;
import org.apache.felix.ipojo.annotations.Requires;

import javax.annotation.PostConstruct;
import java.util.Dictionary;
import java.util.Hashtable;

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
    private BaseDeploymentViewManager deploymentViewManager;
    private HorizontalLayout framesContainer;

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
        uploader.addProgressListener(fileUploader);
        uploader.addStartedListener(fileUploader);
        uploadLayout.addComponent(uploader);
        HorizontalLayout target = new HorizontalLayout();
        Label addTo = new Label("Add to ");
        addTo.addStyleName("v-caption");
        option.addItem(DeployableContainerType.DEPLOYABLE.attribute());
        option.addItem(DeployableContainerType.DEPLOYED.attribute());
        option.addItem(DeployableContainerType.DEPLOYMENT_PLAN.attribute());
        option.addStyleName("horizontal");
        option.select(DeployableContainerType.DEPLOYABLE.attribute());
        target.addComponent(addTo);
        target.addComponent(option);
        uploadLayout.addComponent(target);
        toolBar.addComponent(uploadLayout);

        Label infoLabel = new Label("Drop files here to create a deployment plan");
        infoLabel.setSizeUndefined();
        final VerticalLayout deploymentPlanMaker = new VerticalLayout(infoLabel);
        deploymentPlanMaker.setComponentAlignment(infoLabel, Alignment.MIDDLE_CENTER);
        deploymentPlanMaker.setSizeFull();
        deploymentPlanMaker.addStyleName("drop-area");
        deploymentPlanMakerWrapper = new DragAndDropWrapper(deploymentPlanMaker);
        deploymentPlanMakerWrapper.setSizeFull();
        toolBar.addComponent(deploymentPlanMakerWrapper);
        addComponent(toolBar);

        addComponent(framesContainer);
        setExpandRatio(framesContainer, 1.5f);
    }

    private BaseDeploymentViewManager createDeploymentViewManager() {
        BaseDeploymentViewManager baseDeploymentViewManager = new BaseDeploymentViewManager(framesContainer);
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(Constants.UI_ID, uiContext.getUIId());
        properties.put("instance.object", baseDeploymentViewManager);
        try {
            deploymentViewManagerFactory.createComponentInstance(properties);
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
}

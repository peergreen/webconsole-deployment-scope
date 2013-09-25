/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.deployable.directory;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Requires;

import com.peergreen.deployment.ArtifactBuilder;
import com.peergreen.deployment.repository.DirectoryRepositoryService;
import com.peergreen.deployment.repository.RepositoryManager;
import com.peergreen.deployment.repository.RepositoryType;
import com.peergreen.deployment.repository.view.Repository;
import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.notifier.INotifierService;
import com.peergreen.webconsole.scope.deployment.internal.DeploymentActions;
import com.peergreen.webconsole.scope.deployment.internal.actions.DeleteFileShortcutListener;
import com.peergreen.webconsole.scope.deployment.internal.actions.DoClickListener;
import com.peergreen.webconsole.scope.deployment.internal.actions.FilterFiles;
import com.peergreen.webconsole.scope.deployment.internal.container.AbstractDeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableSource;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.TreeItemExpandListener;
import com.peergreen.webconsole.scope.deployment.internal.deployable.Deployable;
import com.peergreen.webconsole.scope.deployment.internal.deployable.repository.RepositoryManagerPanel;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.deployment.internal.deployable.DeployablePanel.directory")
@Deployable("directory")
public class DirectoryView extends AbstractDeployableContainer {

    @Requires(filter = "(repository.type=" + RepositoryType.FACADE + ")")
    private DirectoryRepositoryService directoryRepositoryService;
    @Inject
    private RepositoryManager repositoryManager;
    @Inject
    private ArtifactBuilder artifactBuilder;
    @Inject
    private INotifierService notifierService;
    @Inject
    private DeploymentViewManager deploymentViewManager;

    protected DirectoryView() {
        super(DeployableSource.FILE);
    }

    @PostConstruct
    public void init() {
        File deploy = new File(System.getProperty("user.dir") + File.separator + "deploy");
        repositoryManager.addRepository(formatUrl(deploy), "Deploy", RepositoryType.DIRECTORY);

        File tmp = new File(Constants.STORAGE_DIRECTORY);
        if (!tmp.exists()) {
            tmp.mkdirs();
        }
        repositoryManager.addRepository(formatUrl(tmp), "Temporary directory", RepositoryType.DIRECTORY);

        File m2 = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
        if (m2.exists()) {
            repositoryManager.addRepository(formatUrl(m2), "Local M2 repository", RepositoryType.DIRECTORY);
        }

        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("100%");
        header.setSpacing(true);
        header.setMargin(true);

        final TextField filter = new TextField();
        filter.setInputPrompt("Filter deployable files");
        filter.setWidth("100%");
        filter.addTextChangeListener(new FilterFiles(DEPLOYABLE_NAME, getContainer()));
        header.addComponent(filter);
        header.setComponentAlignment(filter, Alignment.TOP_LEFT);
        header.setExpandRatio(filter, 3);

        HorizontalLayout actionArea = new HorizontalLayout();
        final NativeSelect actionSelection = new NativeSelect();
        actionSelection.addItem(DeploymentActions.DEPLOY);
        actionSelection.addItem(DeploymentActions.DELETE);
        actionSelection.addItem(DeploymentActions.DEPLOYMENT_PLAN);
        actionSelection.setWidth("100px");
        actionSelection.setNullSelectionAllowed(false);

        Button doButton = new Button("Do");
        doButton.addStyleName("default");
        doButton.addClickListener(new DoClickListener(artifactBuilder, getTree(), actionSelection, deploymentViewManager));

        actionArea.addComponent(actionSelection);
        actionArea.addComponent(doButton);
        header.addComponent(actionArea);
        header.setExpandRatio(actionArea, 2);
        header.setComponentAlignment(actionArea, Alignment.TOP_RIGHT);
        addComponent(header);

        HorizontalLayout repositoryInfo = new HorizontalLayout();
        repositoryInfo.setWidth("100%");
        repositoryInfo.addComponent(getFetching());
        repositoryInfo.setComponentAlignment(getFetching(), Alignment.MIDDLE_LEFT);
        addComponent(repositoryInfo);

        getTree().addShortcutListener(new DeleteFileShortcutListener(deploymentViewManager, getTree(), "Delete", ShortcutAction.KeyCode.DELETE, null));
        getTree().addExpandListener(new TreeItemExpandListener(this, directoryRepositoryService, repositoryManager));

        addComponent(getTree());
        setExpandRatio(getTree(), 1.5f);
    }

    protected void updateTree() {
        getUiContext().getUI().access(new Runnable() {
            @Override
            public void run() {
                updateTree(directoryRepositoryService);
            }
        });
    }

    private String formatUrl(File file) {
        String url = file.toURI().toString();
        if (url.charAt(url.length() - 1) != '/') {
            url += '/';
        }
        return url;
    }

    @Bind(optional = true, aggregate = true, filter = "(!(|(repository.type=" + RepositoryType.SUPER + ")" +
            "(repository.type=" + RepositoryType.FACADE + ")))")
    public void bindRepository(DirectoryRepositoryService repositoryService) throws URISyntaxException {
        final Repository repository = repositoryService.getAttributes().as(Repository.class);
        addRootItemToContainer(repository.getName(), new URI(repository.getUrl()));
        if (isAttached()) {
            notifierService.addNotification(String.format("Directory repository '%s' was added.", repository.getName()));
        }
    }

    @Bind(optional = true)
    public void bindRepositoryManagerPanel(RepositoryManagerPanel repositoryManagerPanel) {
        repositoryManagerPanel.setDirectoryContainer(this);
    }
}

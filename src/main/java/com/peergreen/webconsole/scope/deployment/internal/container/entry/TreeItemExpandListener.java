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

import com.peergreen.deployment.repository.DirectoryRepositoryService;
import com.peergreen.deployment.repository.MavenRepositoryService;
import com.peergreen.deployment.repository.RepositoryManager;
import com.peergreen.deployment.repository.RepositoryType;
import com.peergreen.deployment.repository.maven.MavenArtifactInfo;
import com.peergreen.deployment.repository.view.Repository;
import com.peergreen.webconsole.scope.deployment.internal.container.AbstractDeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.deployable.fetcher.DirectoryDeployableFetcher;
import com.peergreen.webconsole.scope.deployment.internal.deployable.fetcher.MavenDeployableFetcher;
import com.peergreen.webconsole.vaadin.ConfirmDialog;
import com.vaadin.ui.Label;
import com.vaadin.ui.Tree;
import com.vaadin.ui.TreeTable;

/**
 * @author Mohammed Boukada
 */
public class TreeItemExpandListener implements Tree.ExpandListener {

    private AbstractDeployableContainer deployableContainer;
    private MavenRepositoryService mavenRepositoryService;
    private DirectoryRepositoryService directoryRepositoryService;
    private RepositoryManager repositoryManager;

    public TreeItemExpandListener(AbstractDeployableContainer deployableContainer,
                                  MavenRepositoryService mavenRepositoryService,
                                  RepositoryManager repositoryManager) {
        this.deployableContainer = deployableContainer;
        this.mavenRepositoryService = mavenRepositoryService;
        this.repositoryManager = repositoryManager;
    }

    public TreeItemExpandListener(AbstractDeployableContainer deployableContainer,
                                  DirectoryRepositoryService directoryRepositoryService,
                                  RepositoryManager repositoryManager) {
        this.deployableContainer = deployableContainer;
        this.directoryRepositoryService = directoryRepositoryService;
        this.repositoryManager = repositoryManager;
    }

    @Override
    public void nodeExpand(Tree.ExpandEvent event) {
        final DeployableEntry deployableEntry = (DeployableEntry) event.getItemId();
        if (deployableEntry.getParent() == null) {
            fetchRepository(deployableEntry, event);
        }

        if (DeployableSource.MAVEN.equals(deployableEntry.getSource())) {
            MavenDeployableEntry mavenDeployableEntry = (MavenDeployableEntry) deployableEntry;
            MavenArtifactInfo mavenArtifactInfo = mavenDeployableEntry.getArtifactInfo();
            MavenDeployableFetcher fetcher = new MavenDeployableFetcher(deployableContainer, mavenRepositoryService);
            fetcher.setUri(deployableEntry.getUri());
            fetcher.setType(mavenArtifactInfo.type);
            fetcher.setParent(deployableEntry);
            fetcher.start();
        } else if (DeployableSource.FILE.equals(deployableEntry.getSource())) {
            DirectoryDeployableFetcher fetcher = new DirectoryDeployableFetcher(deployableContainer, directoryRepositoryService);
            fetcher.setUri(deployableEntry.getUri());
            fetcher.setParent(deployableEntry);
            fetcher.start();
        }
    }

    private void fetchRepository(final DeployableEntry deployableEntry, final Tree.ExpandEvent event) {
        // it is root element --> repository entry
        if (!containsRepository(deployableEntry.getUri().toString())) {
            ConfirmDialog.show(event.getConnector().getUI(),
                    String.format("Download %s index file(s)", deployableEntry.getName()),
                    new Label(String.format("Would you like to download %s index files ? This operation may takes a few minutes", deployableEntry.getName())),
                    "Download",
                    "Cancel",
                    new ConfirmDialog.Listener() {
                        @Override
                        public void onClose(boolean isConfirmed) {
                            if (isConfirmed) {
                                String repoType = null;
                                if (DeployableSource.FILE.equals(deployableEntry.getSource())) {
                                    repoType = RepositoryType.DIRECTORY;
                                } else if (DeployableSource.MAVEN.equals(deployableEntry.getSource())) {
                                    repoType = RepositoryType.MAVEN;
                                }
                                if (repoType != null) {
                                    String oldName = deployableEntry.getName();
                                    String name = deployableEntry.getName() + " [Updating index ...]";
                                    deployableContainer.addRootItemToContainer(name, deployableEntry.getUri());
                                    repositoryManager.addRepository(deployableEntry.getUri().toString(), oldName, repoType);
                                    TreeTable tree = (TreeTable) event.getSource();
                                    tree.setCollapsed(event.getItemId(), true);
                                }
                            }
                        }
                    });
        }
    }

    private boolean containsRepository(String url) {
        for (Repository repository : repositoryManager.getRepositories()) {
            if (repository.getUrl().equals(url)) {
                return true;
            }
        }
        return false;
    }
}

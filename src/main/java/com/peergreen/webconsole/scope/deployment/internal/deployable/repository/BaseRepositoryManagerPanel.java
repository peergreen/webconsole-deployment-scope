/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.deployment.internal.deployable.repository;

import static com.peergreen.deployment.repository.maven.MavenArtifactInfo.Type.REPOSITORY;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.felix.ipojo.annotations.Bind;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import com.peergreen.deployment.repository.RepositoryManager;
import com.peergreen.deployment.repository.RepositoryService;
import com.peergreen.deployment.repository.RepositoryType;
import com.peergreen.deployment.repository.maven.MavenArtifactInfo;
import com.peergreen.deployment.repository.view.Repository;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.notifier.INotifierService;
import com.peergreen.webconsole.notifier.Task;
import com.peergreen.webconsole.scope.deployment.internal.container.AbstractDeployableContainer;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableSource;
import com.peergreen.webconsole.scope.deployment.internal.container.entry.MavenDeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.deployable.Deployable;
import com.peergreen.webconsole.vaadin.DefaultWindow;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.deployment.internal.deployable.DeployablePanel.manager")
@Deployable("manager")
public class BaseRepositoryManagerPanel extends Panel implements RepositoryManagerPanel {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(BaseRepositoryManagerPanel.class);

    private VerticalLayout contentLayout = new VerticalLayout();
    private List<RepositoryEntry> repositories = new CopyOnWriteArrayList<>();
    private Map<String, Task> tasks = new ConcurrentHashMap<>();

    private AbstractDeployableContainer mavenContainer;
    private AbstractDeployableContainer directoryContainer;

    @Inject
    private RepositoryManager repositoryManager;
    @Inject
    private UIContext uiContext;
    @Inject
    private INotifierService notifierService;

    @PostConstruct
    public void init() {
        setSizeFull();
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        setContent(mainLayout);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("100%");
        header.setSpacing(true);
        header.setMargin(true);

        TextField filter = new TextField();
        filter.setInputPrompt("Filter repositories");
        filter.setWidth("100%");
        filter.addTextChangeListener(new RepositoryFilter());
        header.addComponent(filter);
        header.setComponentAlignment(filter, Alignment.TOP_LEFT);

        Button createRepository = new Button("Add repository");
        createRepository.addStyleName("wide");
        createRepository.addStyleName("default");
        createRepository.addClickListener(new CreateNewRepositoryListener());
        header.addComponent(createRepository);
        header.setComponentAlignment(createRepository, Alignment.TOP_RIGHT);
        mainLayout.addComponent(header);
        mainLayout.addComponent(contentLayout);
        mainLayout.setExpandRatio(contentLayout, 1.5f);
        updateRepositories();
    }

    private void updateRepositories() {
        for (Repository repository : repositoryManager.getRepositories()) {
            if (!containsRepository(repository)) {
                RepositoryEntry entry = new RepositoryEntry(repository);
                contentLayout.addComponent(entry);
                repositories.add(entry);
            }
        }
    }

    private boolean containsRepository(Repository repository) {
        for (RepositoryEntry entry : repositories) {
            if (repository.equals(entry.getRepository())) {
                return true;
            }
        }
        return false;
    }

    @Bind(optional = true, aggregate = true, filter = "(!(|(repository.type=" + RepositoryType.SUPER + ")" +
            "(repository.type=" + RepositoryType.FACADE + ")))")
    public void bindRepository(RepositoryService repositoryService) {
        Repository repository = repositoryService.getAttributes().as(Repository.class);
        if (tasks.containsKey(repository.getUrl())) {
            tasks.get(repository.getUrl()).stop();
            tasks.remove(repository.getUrl());
        }
        uiContext.getUI().access(new Runnable() {
            @Override
            public void run() {
                updateRepositories();
            }
        });
    }

    @Override
    public void setDirectoryContainer(AbstractDeployableContainer directoryContainer) {
        this.directoryContainer = directoryContainer;
    }

    @Override
    public void setMavenContainer(AbstractDeployableContainer mavenContainer) {
        this.mavenContainer = mavenContainer;
    }

    private class RepositoryFilter implements FieldEvents.TextChangeListener {
        @Override
        public void textChange(FieldEvents.TextChangeEvent event) {
            String filter = event.getText();
            for (RepositoryEntry entry : repositories) {
                Repository repository = entry.getRepository();
                if (repository.getName().contains(filter) ||
                        repository.getUrl().contains(filter) ||
                        repository.getType().contains(filter)) {
                    entry.setVisible(true);
                } else {
                    entry.setVisible(false);
                }
            }
        }
    }

    private class CreateNewRepositoryListener implements Button.ClickListener {

        private Label error;

        @Override
        public void buttonClick(Button.ClickEvent event) {
            GridLayout layout = new GridLayout(3, 1);
            layout.setMargin(true);
            layout.setSpacing(true);

            Label repositoryType = new Label("<b>Type</b>", ContentMode.HTML);
            final NativeSelect types = new NativeSelect();
            types.addItem(RepositoryType.DIRECTORY);
            types.addItem(RepositoryType.MAVEN);
            types.select(RepositoryType.DIRECTORY);
            types.setNullSelectionAllowed(false);
            layout.addComponent(repositoryType);
            layout.addComponent(new Label("&nbsp;", ContentMode.HTML));
            layout.addComponent(types);
            layout.newLine();

            Label repositoryName = new Label("<b>Name</b>", ContentMode.HTML);
            final TextField repositoryNameTextField = new TextField();
            repositoryNameTextField.setInputPrompt("Give a display name");
            repositoryNameTextField.setWidth("400px");
            layout.addComponent(repositoryName);
            layout.addComponent(new Label("&nbsp;", ContentMode.HTML));
            layout.addComponent(repositoryNameTextField);
            layout.newLine();

            Label repositoryURL = new Label("<b>URL</b>", ContentMode.HTML);
            final TextField repositoryURLTextField = new TextField();
            repositoryURLTextField.setInputPrompt("Repository URL");
            repositoryURLTextField.setWidth("400px");
            layout.addComponent(repositoryURL);
            layout.addComponent(new Label("&nbsp;", ContentMode.HTML));
            layout.addComponent(repositoryURLTextField);
            layout.newLine();
            layout.space();
            layout.space();
            error = new Label("&nbsp;", ContentMode.HTML);
            error.addStyleName("error");
            error.setSizeUndefined();
            error.addStyleName("light");
            error.addStyleName("v-animate-reveal");
            layout.addComponent(error);

            Button cancel = new Button("Cancel");
            cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);

            final Button ok = new Button("Add repository");
            ok.addStyleName("wide");
            ok.addStyleName("default");
            ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);

            final Window creationWindow = new DefaultWindow("Adding new repository", layout, cancel, ok);
            creationWindow.setModal(true);
            uiContext.getUI().addWindow(creationWindow);

            cancel.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    creationWindow.close();
                }
            });
            ok.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    if (validateURL(repositoryURLTextField.getValue(), (String) types.getValue())) {
                        if ("".equals(repositoryNameTextField.getValue())) {
                            error.setValue("Please give a name");
                        } else {
                            String url = getURL(repositoryURLTextField.getValue(), (String) types.getValue());
                            addRepository(url, repositoryNameTextField.getValue(), (String) types.getValue());
                            creationWindow.close();
                        }
                    }
                }
            });
        }

        private void addRepository(String url, String name, String type) {
            Task task = notifierService.createTask(String.format("Adding '%s' repository ...", name));
            tasks.put(url, task);
            repositoryManager.addRepository(url, name, type);

            URI uri = null;
            try {
                uri = new URI(url);
            } catch (URISyntaxException e) {
                LOGGER.error("Cannot create URI for ''{0}''", url, e);
            }

            if (uri != null) {
                String entryName = name + " [Updating index ...]";
                if (RepositoryType.DIRECTORY.equals(type) && directoryContainer != null) {
                    DeployableEntry deployableEntry = directoryContainer.getDeployable(uri);
                    if (deployableEntry == null) {
                        deployableEntry = new DeployableEntry(uri, entryName, DeployableSource.FILE, directoryContainer, null);
                        deployableEntry.setDeployable(false);
                        directoryContainer.addItemToContainer(deployableEntry, directoryContainer.getContainerProperties(deployableEntry), false);
                    }
                } else if (RepositoryType.MAVEN.equals(type) && mavenContainer != null) {
                    DeployableEntry deployableEntry = mavenContainer.getDeployable(uri);
                    if (deployableEntry == null) {
                        MavenArtifactInfo mavenArtifactInfo = new MavenArtifactInfo(url, null, null, null, null, REPOSITORY);
                        deployableEntry = new MavenDeployableEntry(uri, entryName, DeployableSource.MAVEN, mavenContainer, null, mavenArtifactInfo);
                        deployableEntry.setDeployable(false);
                        mavenContainer.addItemToContainer(deployableEntry, mavenContainer.getContainerProperties(deployableEntry), false);
                    }
                }
            }
        }

        private boolean validateURL(String url, String type) {
            if (type == null || "".equals(type)) {
                error.setValue("Please choose a type");
                return false;
            }

            switch (type) {
                case RepositoryType.DIRECTORY:
                    File file = new File(url);
                    if (file.exists()) {
                        return true;
                    } else {
                        error.setValue("This directory does not exists");
                        return false;
                    }
                case RepositoryType.MAVEN:
                    if (url.startsWith("http:")) {
                        return true;
                    } else {
                        error.setValue("Wrong maven URL");
                        return false;
                    }
                default:
                    return false;
            }
        }

        private String getURL(String url, String type) {
            String tmp = url;
            if (RepositoryType.DIRECTORY.equals(type)) {
                tmp = new File(url).toURI().toString();
            }

            if (tmp.charAt(tmp.length() - 1) != '/') {
                tmp += '/';
            }
            return tmp;
        }
    }
}

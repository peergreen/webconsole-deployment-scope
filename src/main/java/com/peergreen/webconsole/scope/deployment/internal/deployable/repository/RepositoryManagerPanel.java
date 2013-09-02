package com.peergreen.webconsole.scope.deployment.internal.deployable.repository;

import com.peergreen.deployment.repository.RepositoryManager;
import com.peergreen.deployment.repository.RepositoryType;
import com.peergreen.deployment.repository.view.Repository;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Ready;
import com.peergreen.webconsole.UIContext;
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

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.deployment.internal.deployable.DeployablePanel.manager")
@Deployable("manager")
public class RepositoryManagerPanel extends Panel {

    private VerticalLayout contentLayout = new VerticalLayout();
    private List<RepositoryEntry> repositories = new CopyOnWriteArrayList<>();

    @Inject
    private RepositoryManager repositoryManager;
    @Inject
    private UIContext uiContext;

    @Ready
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

    @Override
    public void attach() {
        super.attach();
        updateRepositories();
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
                        } else if (RepositoryType.DIRECTORY.equals(types.getValue()) && !new File(repositoryURLTextField.getValue()).exists()) {
                            error.setValue("This directory does not exist");
                        } else {
                            repositoryManager.addRepository(repositoryURLTextField.getValue(), repositoryNameTextField.getValue(), (String) types.getValue());
                            creationWindow.close();
                        }
                    }
                }
            });
        }

        private boolean validateURL(String url, String type) {
            if (type == null || "".equals(type)) {
                error.setValue("Please choose a type");
                return false;
            }

            switch (type) {
                case RepositoryType.DIRECTORY:
                    if (url.startsWith("file:")) {
                        return true;
                    } else {
                        error.setValue("Wrong directory URL");
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
    }
}

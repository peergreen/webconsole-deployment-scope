package com.peergreen.webconsole.scope.deployment.internal.actions;

import com.peergreen.webconsole.scope.deployment.internal.deployable.entry.DeployableEntry;
import com.peergreen.webconsole.scope.deployment.internal.manager.DeploymentViewManager;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Table;

import java.util.Collection;

/**
 * @author Mohammed Boukada
 */
public class DeleteFileShortcutListener extends ShortcutListener {

    private DeploymentViewManager deploymentViewManager;
    private Table source;

    public DeleteFileShortcutListener(DeploymentViewManager deploymentViewManager, Table source, String caption, int keyCode, int... modifierKeys) {
        super(caption, keyCode, modifierKeys);
        this.deploymentViewManager = deploymentViewManager;
          this.source = source;
    }

    @Override
    public void handleAction(Object sender, Object target) {
        Table table = (Table) target;
        if (source.equals(table)) {
            Collection<DeployableEntry> deployableEntries = (Collection<DeployableEntry>) table.getValue();
            for (DeployableEntry deployableEntry : deployableEntries) {
                deploymentViewManager.delete(deployableEntry);
            }
        }
    }
}

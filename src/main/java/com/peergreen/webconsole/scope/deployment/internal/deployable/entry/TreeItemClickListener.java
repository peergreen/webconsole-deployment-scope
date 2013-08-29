package com.peergreen.webconsole.scope.deployment.internal.deployable.entry;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.TreeTable;

/**
 * @author Mohammed Boukada
 */
public class TreeItemClickListener implements ItemClickEvent.ItemClickListener {
    @Override
    public void itemClick(ItemClickEvent event) {
        DeployableEntry item = (DeployableEntry) event.getItemId();
        if (!item.isDeployable()) {
            TreeTable treeTable = (TreeTable) event.getSource();
            treeTable.setCollapsed(event.getItemId(), !treeTable.isCollapsed(event.getItemId()));
        }
    }
}

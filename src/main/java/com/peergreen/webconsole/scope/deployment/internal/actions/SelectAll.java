package com.peergreen.webconsole.scope.deployment.internal.actions;

import com.peergreen.webconsole.scope.deployment.internal.container.entry.DeployableEntry;
import com.vaadin.data.Property;
import com.vaadin.ui.AbstractSelect;

import java.util.Collection;

/**
 * @author Mohammed Boukada
 */
public class SelectAll implements Property.ValueChangeListener {

    private AbstractSelect container;

    public SelectAll(AbstractSelect container) {
        this.container = container;
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Boolean state = (Boolean) event.getProperty().getValue();
        Collection<DeployableEntry> deployableEntries = (Collection<DeployableEntry>) container.getItemIds();
        if (state) {
            for (DeployableEntry deployableEntry : deployableEntries) {
                container.select(deployableEntry);
            }
        } else {
            for (DeployableEntry deployableEntry : deployableEntries) {
                container.unselect(deployableEntry);
            }
        }
    }
}

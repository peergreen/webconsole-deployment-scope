package com.peergreen.webconsole.scope.deployment.internal.container.entry;

import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;

/**
 * @author Mohammed Boukada
 */
public class ItemDescription implements AbstractSelect.ItemDescriptionGenerator {
    @Override
    public String generateDescription(Component source, Object itemId, Object propertyId) {
        DeployableEntry deployableEntry = (DeployableEntry) itemId;
        if (deployableEntry.isDeployable()) {
            return "Double click to show more details";
        } else {
            return "";
        }
    }
}

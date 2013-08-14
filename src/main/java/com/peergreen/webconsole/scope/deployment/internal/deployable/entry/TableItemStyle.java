package com.peergreen.webconsole.scope.deployment.internal.deployable.entry;

import com.peergreen.webconsole.scope.deployment.internal.deployable.DeployableContainerType;
import com.vaadin.ui.Table;

/**
 * @author Mohammed Boukada
 */
public class TableItemStyle implements Table.CellStyleGenerator {

    private DeployableContainerType containerType;

    public TableItemStyle(DeployableContainerType containerType) {
        this.containerType = containerType;
    }

    @Override
    public String getStyle(Table source, Object itemId, Object propertyId) {
        switch (containerType) {
            case DEPLOYABLE:
                return "";
            case DEPLOYED:
                return "deployed-entry";
            case DEPLOYMENT_PLAN:
                return "";
        }
        return "";
    }
}

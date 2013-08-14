package com.peergreen.webconsole.scope.deployment.internal.actions;

import com.vaadin.data.Container;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents;

/**
 * @author Mohammed Boukada
 */
public class FilterFiles implements FieldEvents.TextChangeListener {

    private Container.Filterable container;
    private String itemId;
    private Container.Filter filter;

    public FilterFiles(Container.Filterable container, String itemId) {
        this.container = container;
        this.itemId = itemId;
    }

    @Override
    public void textChange(FieldEvents.TextChangeEvent event) {
        if (filter != null) {
            container.removeContainerFilter(filter);
        }
        filter = new SimpleStringFilter(itemId, event.getText().trim(), true, false);
        container.addContainerFilter(filter);
    }

}

package com.peergreen.webconsole.scope.deployment.internal.container;

/**
 * @author Mohammed Boukada
 */
public enum DeployableContainerType {

    DEPLOYABLE("Deployables"),

    DEPLOYED("Deployed"),

    DEPLOYMENT_PLAN("Deployment plan");

    private DeployableContainerType(String property) {
        this.property = property;
    }

    private String property;


    public String attribute() {
        return property;
    }

}

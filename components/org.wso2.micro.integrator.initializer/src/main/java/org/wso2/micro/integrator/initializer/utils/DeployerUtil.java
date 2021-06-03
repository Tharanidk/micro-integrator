package org.wso2.micro.integrator.initializer.utils;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.api.API;
import org.apache.synapse.api.version.VersionStrategy;
import org.apache.synapse.config.xml.rest.VersionStrategyFactory;

import javax.xml.namespace.QName;

public class DeployerUtil {

    /**
     * Partially build a synapse API for deployment purposes.
     * @param apiElement OMElement of API configuration.
     * @return API
     */
    public static API partiallyBuildAPI(OMElement apiElement) {
        OMAttribute nameAtt = apiElement.getAttribute(new QName("name"));
        OMAttribute contextAtt = apiElement.getAttribute(new QName("context"));
        API api = new API(nameAtt.getAttributeValue(), contextAtt.getAttributeValue());
        VersionStrategy vStrategy = VersionStrategyFactory.createVersioningStrategy(api, apiElement);
        api.setVersionStrategy(vStrategy);
        return api;
    }
}

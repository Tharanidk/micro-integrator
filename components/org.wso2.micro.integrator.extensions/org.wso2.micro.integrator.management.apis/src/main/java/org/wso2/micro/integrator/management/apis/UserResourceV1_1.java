/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.management.apis;

import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;

/**
 * Resource for a retrieving and deleting a single user with the userId provided.
 * <p>
 * Handles resources in the form "management/users/{userId}"
 * Supports API version 1.1 (URL encoded userIDs)
 */
public class UserResourceV1_1 extends UserResourceBase implements MiApiResource {

    @Override
    public boolean invoke(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext,
                          SynapseConfiguration synapseConfiguration) {
        return super.invoke(messageContext, axis2MessageContext, synapseConfiguration, true);
    }
}
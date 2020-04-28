/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.micro.integrator.management.apis.security.handler;

import org.apache.synapse.MessageContext;

public class TestSecurityHandler extends SecurityHandlerAdapter {

    private boolean isHandled = false;

    public TestSecurityHandler(String context) {
        this.context = context;
    }

    @Override
    public Boolean invoke(MessageContext messageContext) {
        this.messageContext = messageContext;
        if (needsHandling()) {
            this.isHandled = true;
        } else {
            this.isHandled = false;
        }
        return true;
    }

    @Override
    protected Boolean handle(MessageContext messageContext) {
        return true;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {

    }

    public boolean isHandleTriggered(){
        return this.isHandled;
    }
}
/*
 * Copyright (c) 2023, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.esb.car.deployment.test;

import org.awaitility.Awaitility;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.ServerConfigurationManager;
import org.wso2.esb.integration.common.utils.common.TestConfigurationProvider;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.wso2.carbon.esb.util.Utils;

/**
 * Test case to ensure the swagger definitions are getting removed upon undeployment of a faulty CApp.
 */
public class FaultyCAppSwaggerRemovalTestCase extends ESBIntegrationTest {

    private static final int SERVICE_DEPLOYMENT_DELAY = TestConfigurationProvider.getServiceDeploymentDelay();
    private ServerConfigurationManager serverConfigurationManager;
    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    private void initialize() throws Exception {

        super.init();
        carbonLogReader = new CarbonLogReader();
        serverConfigurationManager = new ServerConfigurationManager(new AutomationContext());
        carbonLogReader.start();
    }

    @Test(groups = {
            "wso2.esb"}, description = "Test whether the swagger definitions are getting removed "
            + "upon undeployment of a faulty CApp")
    public void faultyCAppSwaggerRemovalTest() throws Exception {

        // Deploy a faulty CApp which contains a Swagger definition
        Utils.deployCarbonApplication("FaultyCAppWithSwagger_1.0.0.car",false);
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isCAPPUnDeploymentLogWritten());

        // Undeploy the faulty CApp
        Utils.undeployCarbonApplication("FaultyCAppWithSwagger_1.0.0.car",false);
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isFaultyCAPPUnDeploymentLogWritten());

        // Deploy a CApp which contains the same Swagger definition
        Utils.deployCarbonApplication("CAppWithSwagger_1.0.0.car",false);
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isCAPPDeploymentLogWritten());

        // Undeploy the CApp
        Utils.undeployCarbonApplication("CAppWithSwagger_1.0.0.car",false);
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isCAPPUnDeploymentLogWritten());
    }

    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment() throws Exception {

        carbonLogReader.stop();
        serverConfigurationManager.restoreToLastMIConfiguration();
    }

    private Callable<Boolean> isCAPPDeploymentLogWritten() {

        return checkCAppLogs("Successfully Deployed Carbon Application : "
                + "FaultyCAppWithSwagger_1.0.0");
    }

    private Callable<Boolean> isCAPPUnDeploymentLogWritten() {

        return checkCAppLogs("Successfully undeployed Carbon Application : "
                + "FaultyCAppWithSwagger_1.0.0");
    }

    private Callable<Boolean> isFaultyCAPPUnDeploymentLogWritten() {

        return checkCAppLogs("Undeploying Faulty Carbon Application");
    }

    private Callable<Boolean> checkCAppLogs(String expectedLogMessage) {

        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                return carbonLogReader
                        .checkForLog(expectedLogMessage, DEFAULT_TIMEOUT);
            }
        };
    }
}


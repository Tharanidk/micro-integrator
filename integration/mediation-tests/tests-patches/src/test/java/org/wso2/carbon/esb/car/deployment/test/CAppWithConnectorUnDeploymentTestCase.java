/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.car.deployment.test;

import org.awaitility.Awaitility;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.esb.util.Utils;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.TestConfigurationProvider;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CAppWithConnectorUnDeploymentTestCase extends ESBIntegrationTest {

    private static int SERVICE_DEPLOYMENT_DELAY = TestConfigurationProvider.getServiceDeploymentDelay();
    private final String carFileName = "FileConnectorCompositeExporter";

    @BeforeClass(alwaysRun = true)
    protected void initialize() throws Exception {

        super.init();
    }

    @Test(groups = {"wso2.esb"}, description = "CApp with connector deployment", priority = 1)
    public void cAppWithConnectorDeploymentTest() throws Exception {

        Utils.deployCarbonApplication("FileConnectorCompositeExporter.car", false);
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isCAppHotDeployed(carFileName));
        assertTrue(checkCarbonAppExistence(carFileName), "FileConnectorCompositeExporter deployment failed");
    }

    @Test(groups = {"wso2.esb"}, description = "CApp with connector un deployment", priority = 2)
    public void cAppWithConnectorUnDeploymentTest() throws Exception {

        Utils.undeployCarbonApplication("FileConnectorCompositeExporter.car", false);
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isCAppUnDeployed(carFileName));
        assertFalse(checkCarbonAppExistence(carFileName), "FileConnectorCompositeExporter un deployment failed");
    }

    @Test(groups = {"wso2.esb"}, description = "CApp with connector re deployment", priority = 3)
    public void cAppWithConnectorReDeploymentTest() throws Exception {

        Utils.deployCarbonApplication("FileConnectorCompositeExporter.car", false);
        Awaitility.await().pollInterval(500, TimeUnit.MILLISECONDS).atMost(SERVICE_DEPLOYMENT_DELAY,
                TimeUnit.MILLISECONDS).until(isCAppHotDeployed(carFileName));
        assertTrue(checkCarbonAppExistence(carFileName), "FileConnectorCompositeExporter re deployment failed");
    }

    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment() throws Exception {

        super.cleanup();
    }

    private Callable<Boolean> isCAppHotDeployed(String cAppName) {

        return new Callable<Boolean>() {
            public Boolean call() throws Exception {

                return checkCarbonAppExistence(cAppName);
            }
        };
    }

    private Callable<Boolean> isCAppUnDeployed(String cAppName) {

        return new Callable<Boolean>() {
            public Boolean call() throws Exception {

                return !checkCarbonAppExistence(cAppName);
            }
        };
    }
}

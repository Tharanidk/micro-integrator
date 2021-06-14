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

package org.wso2.carbon.esb.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.esb.integration.common.extensions.carbonserver.CarbonServerExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class Utils {

    private static Log log = LogFactory.getLog(Utils.class);

    /**
     * Un-deploy a carbon application from the server artifacts location and restart if needed.
     *
     * @param artifactName  CAPP name ( must include the extension Ex:- app1.car )
     * @param restartServer Server restart required
     */
    public static void undeployCarbonApplication(String artifactName, boolean restartServer) {

        String pathString =
                System.getProperty("carbon.home") + File.separator + "repository" + File.separator + "deployment"
                        + File.separator + "server" + File.separator + "carbonapps" + File.separator + artifactName;
        Path path = FileSystems.getDefault().getPath(pathString);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Error while deleting the file", e);
        }
        if (restartServer) {
            CarbonServerExtension.restartServer();
        }
    }

    /**
     * Deploy a carbon application and restart if needed.
     *
     * @param artifactName  CAPP name ( must include the extension Ex:- app1.car )
     * @param restartServer Server restart required
     */
    public static void deployCarbonApplication(String artifactName, boolean restartServer) {

        File src = new File(FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "ESB"
                + File.separator + "car" + File.separator + artifactName);

        String destPath =
                System.getProperty("carbon.home") + File.separator + "repository" + File.separator + "deployment"
                        + File.separator + "server" + File.separator + "carbonapps" + File.separator + artifactName;

        try {
            FileUtils.copyFile(src, new File(destPath));
        } catch (IOException e) {
            log.error("Error while copying the file", e);
        }
        if (restartServer) {
            CarbonServerExtension.restartServer();
        }
    }
}

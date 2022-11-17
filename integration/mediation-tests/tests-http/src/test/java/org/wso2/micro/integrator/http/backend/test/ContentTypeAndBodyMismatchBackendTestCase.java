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

package org.wso2.micro.integrator.http.backend.test;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.testng.annotations.Test;
import org.wso2.micro.integrator.http.utils.BackendServer;
import org.wso2.micro.integrator.http.utils.Constants;
import org.wso2.micro.integrator.http.utils.HTTPRequestWithBackendResponse;
import org.wso2.micro.integrator.http.utils.PayloadSize;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.wso2.micro.integrator.http.utils.Constants.CRLF;
import static org.wso2.micro.integrator.http.utils.Constants.HTTP_VERSION;

/**
 * Test case for MI behaviour(specifically CPU usage) when a content type and body mismatch HTTPS response is
 * received.
 * In this case, The backends sends Content-Type: application/xml header with a JSON payload
 */
public class ContentTypeAndBodyMismatchBackendTestCase extends HTTPCoreBackendTest {

    @Test(groups = {"wso2.esb"}, description =
            "Test for MI behaviour when the backend response body differ from content type.",
            dataProvider = "httpRequestResponse", dataProviderClass = Constants.class)
    public void testContentTypeAndBodyMismatchResponse(HTTPRequestWithBackendResponse httpRequestWithBackendResponse)
            throws Exception {

        invokeHTTPCoreBETestAPI(httpRequestWithBackendResponse);
    }

    @Override
    protected boolean validateResponse(CloseableHttpResponse response,
                                       HTTPRequestWithBackendResponse httpRequestWithBackendResponse) throws Exception {

        assertEquals(response.getStatusLine().getStatusCode(), getExpectedHTTPSC(httpRequestWithBackendResponse),
                "Invalid HTTP Status code received");
        return true;
    }

    @Override
    protected List<BackendServer> getBackEndServers() throws Exception {

        List<BackendServer> serverList = new ArrayList<>();
        serverList.add(new ContentTypeAndBodyMismatchBackend(getServerSocket(true)));
        serverList.add(new ContentTypeAndBodyMismatchBackend(getServerSocket(false)));

        return serverList;
    }

    private static class ContentTypeAndBodyMismatchBackend extends BackendServer {

        public ContentTypeAndBodyMismatchBackend(ServerSocket serverSocket) {

            super(serverSocket);
        }

        @Override
        protected void writeOutput(Socket socket) throws Exception {

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            out.write(HTTP_VERSION + " 200 OK" + CRLF);
            out.write("Content-Type: application/xml" + CRLF);
            if (StringUtils.isNotBlank(payload)) {
                out.write("Content-Length:  " + payload.getBytes().length + CRLF);
            }
            out.write("Connection: keep-alive" + CRLF);
            out.write(CRLF);
            if (StringUtils.isNotBlank(payload)) {
                out.write(payload);
            }
            out.flush();
            socket.close();
        }
    }

    private int getExpectedHTTPSC(HTTPRequestWithBackendResponse httpRequestWithBackendResponse) {

        return (PayloadSize.EMPTY.equals(httpRequestWithBackendResponse.getBackendResponse().getBackendPayloadSize())) ?
                200 : 500;
    }
}

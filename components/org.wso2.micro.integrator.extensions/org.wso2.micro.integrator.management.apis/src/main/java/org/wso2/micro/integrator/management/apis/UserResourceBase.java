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

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.SynapseConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.micro.core.util.StringUtils;
import org.wso2.micro.integrator.management.apis.security.handler.SecurityUtils;
import org.wso2.micro.integrator.security.user.api.UserStoreException;
import org.wso2.micro.integrator.security.user.api.UserStoreManager;
import org.wso2.micro.integrator.security.user.core.multiplecredentials.UserAlreadyExistsException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.wso2.micro.integrator.management.apis.Constants.BAD_REQUEST;
import static org.wso2.micro.integrator.management.apis.Constants.DOMAIN;
import static org.wso2.micro.integrator.management.apis.Constants.INTERNAL_SERVER_ERROR;
import static org.wso2.micro.integrator.management.apis.Constants.IS_ADMIN;
import static org.wso2.micro.integrator.management.apis.Constants.NOT_FOUND;
import static org.wso2.micro.integrator.management.apis.Constants.ROLES;
import static org.wso2.micro.integrator.management.apis.Constants.STATUS;
import static org.wso2.micro.integrator.management.apis.Constants.USERNAME_PROPERTY;
import static org.wso2.micro.integrator.management.apis.Constants.USER_ID;
import static org.wso2.micro.integrator.management.apis.Constants.NEW_PASSWORD;
import static org.wso2.micro.integrator.management.apis.Constants.CONFIRM_PASSWORD;
import static org.wso2.micro.integrator.management.apis.Constants.OLD_PASSWORD;


/**
 * Resource for a retrieving and deleting a single user with the userId provided.
 * <p>
 * Handles resources in the form "management/users/{userId}"
 */
public class UserResourceBase {

    private static final Log LOG = LogFactory.getLog(UserResource.class);

    // HTTP method types supported by the resource
    protected Set<String> methods;

    public UserResourceBase() {
        methods = new HashSet<>();
        methods.add(Constants.HTTP_GET);
        methods.add(Constants.HTTP_DELETE);
        methods.add(Constants.HTTP_METHOD_PATCH);
    }


    public Set<String> getMethods() {
        return methods;
    }

    public boolean invoke(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext,
                          SynapseConfiguration synapseConfiguration, Boolean isEncoded) {
        String httpMethod = axis2MessageContext.getProperty(Constants.HTTP_METHOD_PROPERTY).toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling " + httpMethod + "request.");
        }

        if (Boolean.TRUE.equals(SecurityUtils.isFileBasedUserStoreEnabled())) {
            Utils.setInvalidUserStoreResponse(axis2MessageContext);
            return true;
        }

        JSONObject response;
        try {
            switch (httpMethod) {
                case Constants.HTTP_GET: {
                    response = handleGet(messageContext, isEncoded);
                    break;
                }
                case Constants.HTTP_DELETE: {
                    response = handleDelete(messageContext, isEncoded);
                    break;
                }
                case Constants.HTTP_METHOD_PATCH:{
                    response = handlePatch(messageContext, axis2MessageContext, isEncoded);
                    break;
                }
                default: {
                    response = Utils.createJsonError("Unsupported HTTP method, " + httpMethod + ". Only GET ," +
                                    " DELETE and PATCH methods are supported",
                            axis2MessageContext, BAD_REQUEST);
                    break;
                }
            }
        } catch (UserStoreException e) {
            response = Utils.createJsonError("Error initializing the user store. Please try again later", e,
                    axis2MessageContext, INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            response = Utils.createJsonError("Error processing the request. ", e, axis2MessageContext, BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            response = Utils.createJsonError("Requested resource not found. ", e, axis2MessageContext, NOT_FOUND);
        }
        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        Utils.setJsonPayLoad(axis2MessageContext, response);
        return true;
    }

    protected JSONObject handleGet(MessageContext messageContext, boolean isEncoded)
            throws UserStoreException, ResourceNotFoundException {
        String domain = Utils.getQueryParameter(messageContext, DOMAIN);
        if (isEncoded && !StringUtils.isEmpty(domain)) {
            domain = urlDecode(domain);
        }
        String user = getUserFromPathParam(messageContext, domain, isEncoded);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Requested details for the user: " + user);
        }
        String[] roles = Utils.getUserStore(domain).getRoleListOfUser(user);
        if (!StringUtils.isEmpty(domain)) {
            user = Utils.addDomainToName(user, domain);
        }
        JSONObject userObject = new JSONObject();
        userObject.put(USER_ID, user);
        userObject.put(IS_ADMIN, SecurityUtils.isAdmin(user));
        JSONArray list = new JSONArray(roles);
        userObject.put(ROLES, list);
        return userObject;
    }

    protected JSONObject handleDelete(MessageContext messageContext, boolean isEncoded)
            throws UserStoreException, IOException, ResourceNotFoundException {
        String domain = Utils.getQueryParameter(messageContext, DOMAIN);
        if (isEncoded && !StringUtils.isEmpty(domain)) {
            domain = urlDecode(domain);
        }
        String user = getUserFromPathParam(messageContext, domain, isEncoded);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Request received to delete the user: " + user);
        }
        String userName = Utils.getStringPropertyFromMessageContext(messageContext, USERNAME_PROPERTY);
        if (Objects.isNull(userName)) {
            LOG.warn("Deleting a user without authenticating/authorizing the request sender. Adding "
                    + "authentication and authorization handlers is recommended.");
        } else {
            if (userName.equals(user)) {
                throw new IOException("Attempt to delete the logged in user. Operation not allowed. Please login "
                        + "from another user.");
            }
        }
        UserStoreManager userStoreManager = Utils.getUserStore(domain);
        userStoreManager.deleteUser(user);
        JSONObject jsonBody = new JSONObject();
        jsonBody.put(USER_ID, user);
        jsonBody.put(STATUS, "Deleted");
        return jsonBody;
    }

    protected JSONObject handlePatch(MessageContext messageContext,
            org.apache.axis2.context.MessageContext axis2MessageContext, Boolean isEncoded)
            throws UserStoreException, IOException, ResourceNotFoundException {
        String domain = Utils.getQueryParameter(messageContext, DOMAIN);
        if (isEncoded && !StringUtils.isEmpty(domain)) {
            domain = urlDecode(domain);
        }
        String user = getUserFromPathParam(messageContext, domain, isEncoded);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Request received to update user credentials: " + user);
        }
        String userName = Utils.getStringPropertyFromMessageContext(messageContext, USERNAME_PROPERTY);
        if (Objects.isNull(userName)) {
            LOG.warn(
                    "Update a user without authenticating/authorizing the request sender. Adding "
                            + "authentication and authorization handlers is recommended.");
        }
        if (!JsonUtil.hasAJsonPayload(axis2MessageContext)) {
            return Utils.createJsonErrorObject("JSON payload is missing");
        }
        JsonObject payload = Utils.getJsonPayload(axis2MessageContext);

        if (payload.has(NEW_PASSWORD) && payload.has(CONFIRM_PASSWORD)) {
            String NewPassword = payload.get(NEW_PASSWORD).getAsString();
            String ConfirmPassword = payload.get(CONFIRM_PASSWORD).getAsString();
            String OldPassword = payload.get(OLD_PASSWORD).getAsString();
            if (NewPassword.equals(ConfirmPassword)) {
                UserStoreManager userStoreManager = Utils.getUserStore(domain);
                String[] roles = userStoreManager.getRoleListOfUser(user);
                try {
                    synchronized (this) {
                        if (userName.equals(user)) {
                            userStoreManager.updateCredential(user, NewPassword, OldPassword);
                        } else {
                            if (userName.equals("admin")) {
                                userStoreManager.updateCredentialByAdmin(user, NewPassword);
                            } else {
                                if (!Arrays.asList(roles).contains("admin")) {
                                    userStoreManager.updateCredentialByAdmin(user, NewPassword);
                                } else {
                                    throw new IOException(
                                            "Only super admin user can update the credentials of other admins");
                                }
                            }
                        }
                    }
                } catch (UserStoreException e) {
                    throw new UserStoreException("Error occurred while updating the credentials of the user : "
                            + user, e);
                }
            } else {
                throw new IOException(NEW_PASSWORD + " and " + CONFIRM_PASSWORD + " does not matches " + "payload.");
            }
        } else {
            throw new IOException(
                    "Missing one or more of the fields, '" + NEW_PASSWORD + "', '"
                            + CONFIRM_PASSWORD + "' in the " + "payload.");
        }
        JSONObject jsonBody = new JSONObject();
        jsonBody.put(USER_ID, user);
        jsonBody.put(STATUS, "Password updated");
        return jsonBody;
    }

    private String getUserFromPathParam(MessageContext messageContext, String domain, boolean isEncoded)
            throws UserStoreException, ResourceNotFoundException {
        String userId = Utils.getPathParameter(messageContext, USER_ID);
        if (isEncoded && !StringUtils.isEmpty(userId)) {
            userId = urlDecode(userId);
        }
        String domainAwareUserName = Utils.addDomainToName(userId, domain);
        if (Objects.isNull(userId)) {
            throw new AssertionError("Incorrect path parameter used: " + USER_ID);
        }
        UserStoreManager userStoreManager = Utils.getUserStore(domain);
        String[] users = userStoreManager.listUsers(userId, -1);
        if (null == users || 0 == users.length) {
            throw new ResourceNotFoundException("User: " + userId + " cannot be found.");
        }
        for (String user : users) {
            if (domainAwareUserName.equals(user)) {
                return userId;
            }
        }
        throw new ResourceNotFoundException("User: " + userId + " cannot be found.");
    }

    /**
     * Decodes the given value.
     *
     * @param value Value to be decoded.
     * @return Decoded value.
     */
    private static String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error while decoding the value: " + value, e);
        }
    }
}

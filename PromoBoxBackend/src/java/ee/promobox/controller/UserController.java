/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.controller;

import ee.promobox.entity.AdCampaigns;
import ee.promobox.entity.Clients;
import ee.promobox.entity.Devices;
import ee.promobox.entity.Permissions;
import ee.promobox.entity.Users;
import ee.promobox.entity.UsersCampaignsPermissions;
import ee.promobox.entity.UsersDevicesPermissions;
import ee.promobox.entity.Versions;
import ee.promobox.service.Session;
import ee.promobox.service.SessionService;
import ee.promobox.service.UserService;
import ee.promobox.util.RequestUtils;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Maxim
 */
@Controller
public class UserController {

    private final static Logger log = LoggerFactory.getLogger(
            UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    @RequestMapping("/version")
    public @ResponseBody
    String applicationVersion(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        resp.put("response", RequestUtils.ERROR);

        Versions version = userService.findCurrentVersion();
        if (version != null) {
            resp.put("version", version.getVersion());
            resp.put("response", RequestUtils.OK);
        }

        return resp.toString();
    }

    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    public @ResponseBody
    String userRegisterHandler(
            @RequestBody String json,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        json = URLDecoder.decode(json, "UTF-8");

        JSONObject objectGiven = new JSONObject(json);
        JSONObject resp = RequestUtils.getErrorResponse();
        resp.put("response", RequestUtils.ERROR);

        Users user = new Users();
        if (checkUser(user, objectGiven, resp, "")) {
            Date createdDt = new Date();

            Clients client = new Clients();
            client.setCompanyName(objectGiven.getString("companyName"));
            client.setCreatedDt(createdDt);
            client.setUpdatedDt(createdDt);
            userService.addClient(client);

            user.setPassword(genereateRandomPass());
            user.setClientId(client.getId());
            user.setCreatedDt(createdDt);
            user.setActive(false);
            user.setAdmin(true);
            userService.addUser(user);

            response.setStatus(HttpServletResponse.SC_OK);
            resp.put("response", RequestUtils.OK);
        }

        return resp.toString();
    }

    private boolean checkUser(Users user, JSONObject objectGiven, JSONObject resp, String excludeEmail) throws JSONException {
        String email = objectGiven.getString("email");
        if (!EmailValidator.getInstance().isValid(email)) {
            resp.put("reason", "invalidEmail");

            return false;
        } else if (userService.findUserByEmail(email, excludeEmail) != null) {
            resp.put("reason", "emailExist");

            return false;
        } else {
            user.setFirstname(objectGiven.getString("firstname"));
            user.setSurname(objectGiven.getString("surname"));
            user.setEmail(email);

            if (objectGiven.has("password")) {
                String password = objectGiven.getString("password");
                if (StringUtils.trimToNull(password) != null) {
                    user.setPassword(password);
                }
            }

            return true;
        }
    }

    @RequestMapping(value = "token/{token}/users", method = RequestMethod.GET)
    public @ResponseBody
    String listUsers(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
            if (session.isAdmin()) {

                JSONArray usersArray = new JSONArray();
                for (Users u : userService.findUsersByClientId(session.getClientId())) {
                    usersArray.put(userToJson(u));
                }

                resp.put("users", usersArray);
                response.setStatus(HttpServletResponse.SC_OK);
                resp.put("response", RequestUtils.OK);
            }
        }

        return resp.toString();
    }

    @RequestMapping(value = "token/{token}/users", method = RequestMethod.POST)
    public @ResponseBody
    String createUser(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
            if (session.isAdmin()) {
                int clientId = session.getClientId();

                Users user = new Users();
                user.setFirstname("");
                user.setSurname("");
                user.setEmail("");
                user.setPassword(genereateRandomPass());
                user.setClientId(clientId);
                user.setCreatedDt(new Date());
                user.setActive(false);
                user.setAdmin(false);
                userService.addUser(user);

                resp.put("user", userToJson(user));

                response.setStatus(HttpServletResponse.SC_OK);
                resp.put("response", RequestUtils.OK);
            }

        } else {
            RequestUtils.sendUnauthorized(response);
        }

        return resp.toString();
    }

    @RequestMapping(value = "token/{token}/users/{id}", method = RequestMethod.PUT)
    public @ResponseBody
    String updateUser(
            @PathVariable("id") int userId,
            @PathVariable("token") String token,
            @RequestBody String json,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            if (session.isAdmin()) {

                JSONObject objectGiven = new JSONObject(json);

                resp.put("response", RequestUtils.ERROR);

                Users user = userService.findUserById(userId);
                if (checkUser(user, objectGiven, resp, user.getEmail())) {
                    int clientId = session.getClientId();

                    if (session.isAdmin() && objectGiven.has("companyName")) {
                        Clients client = userService.findClientById(clientId);
                        client.setCompanyName(StringUtils.trimToEmpty(objectGiven.getString("companyName")));
                        client.setUpdatedDt(new Date());
                        userService.updateClient(client);
                    }

                    user.setActive(true);
                    userService.updateUser(user);

                    response.setStatus(HttpServletResponse.SC_OK);
                    resp.put("response", RequestUtils.OK);
                }
            }

        } else {
            RequestUtils.sendUnauthorized(response);
        }

        return resp.toString();
    }

    @RequestMapping(value = "token/{token}/users/{id}", method = RequestMethod.DELETE)
    public @ResponseBody
    String deleteUser(
            @PathVariable("id") int userId,
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            if (session.isAdmin()) {
                Users user = userService.findUserById(userId);

                user.setActive(false);

                userService.updateUser(user);

                response.setStatus(HttpServletResponse.SC_OK);
                resp.put("response", RequestUtils.OK);
            }
        } else {
            RequestUtils.sendUnauthorized(response);
        }

        return resp.toString();
    }

    @RequestMapping(value = "token/{token}/device/permissions", method = RequestMethod.GET)
    public @ResponseBody
    String listClientDevicesPermissions(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            if (session.isAdmin()) {
                List<Users> users = userService.findUsersByClientId(session.getClientId());
                List<Devices> devices = userService.findUserDevieces(session.getClientId());
                List<UsersDevicesPermissions> permissions = userService.findUsersDevicesPermissionsByClientId(session.getClientId());

                JSONArray devicesArray = new JSONArray();

                for (Devices d : devices) {
                    JSONObject deviceJson = new JSONObject();
                    deviceJson.put("entityId", d.getId());
                    deviceJson.put("name", d.getUuid());

                    List<Permissions> devicePermissionsList = new ArrayList<>();
                    for (UsersDevicesPermissions p : permissions) {
                        if (p.getDeviceId() == d.getId()) {
                            devicePermissionsList.add(p);
                        }
                    }

                    JSONArray userArray = new JSONArray();
                    for (Users u : users) {
                        JSONObject userJson = userToJson(u);

                        Permissions p = findPermissionsInList(u.getId(), devicePermissionsList);
                        userJson.put("permissionRead", p != null ? p.isPermissionRead() : false);
                        userJson.put("permissionWrite", p != null ? p.isPermissionWrite() : false);
                        userJson.put("entityId", d.getId());

                        userArray.put(userJson);
                    }
                    deviceJson.put("userPermissions", userArray);

                    devicesArray.put(deviceJson);
                }

                resp.put("entities", devicesArray);

                response.setStatus(HttpServletResponse.SC_OK);
                resp.put("response", RequestUtils.OK);
            }
        }

        return resp.toString();
    }

    @RequestMapping(value = "token/{token}/campaign/permissions", method = RequestMethod.GET)
    public @ResponseBody
    String listClientCampaignsPermissions(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            if (session.isAdmin()) {
                List<Users> users = userService.findUsersByClientId(session.getClientId());
                List<AdCampaigns> campaigns = userService.findUserAdCompaigns(session.getClientId());
                List<UsersCampaignsPermissions> permissions = userService.findUsersCampaignsPermissionsByClientId(session.getClientId());

                JSONArray campaignsArray = new JSONArray();

                for (AdCampaigns c : campaigns) {
                    JSONObject campaignJson = new JSONObject();
                    campaignJson.put("entityId", c.getId());
                    campaignJson.put("name", c.getName());

                    List<Permissions> campaignPermissionsList = new ArrayList<>();
                    for (UsersCampaignsPermissions p : permissions) {
                        if (Objects.equals(p.getCampaignId(), c.getId())) {
                            campaignPermissionsList.add(p);
                        }
                    }

                    JSONArray userArray = new JSONArray();
                    for (Users u : users) {
                        JSONObject userJson = userToJson(u);

                        Permissions p = findPermissionsInList(u.getId(), campaignPermissionsList);
                        userJson.put("permissionRead", p != null ? p.isPermissionRead() : false);
                        userJson.put("permissionWrite", p != null ? p.isPermissionWrite() : false);
                        userJson.put("entityId", c.getId());

                        userArray.put(userJson);
                    }
                    campaignJson.put("userPermissions", userArray);

                    campaignsArray.put(campaignJson);
                }

                resp.put("entities", campaignsArray);

                response.setStatus(HttpServletResponse.SC_OK);
                resp.put("response", RequestUtils.OK);
            }
        }

        return resp.toString();
    }

    private Users findUserInList(int userId, List<Users> users) {
        for (Users u : users) {
            if (u.getId() == userId) {
                return u;
            }
        }

        return null;
    }

    private Permissions findPermissionsInList(int userId, List<Permissions> permissions) {
        for (Permissions p : permissions) {
            if (p.getUserId() == userId) {
                return p;
            }
        }

        return null;
    }

    private JSONObject userToJson(Users user) throws JSONException {

        JSONObject userJson = new JSONObject();
        userJson.put("id", user.getId());
        userJson.put("firstname", user.getFirstname());
        userJson.put("surname", user.getSurname());
        userJson.put("email", user.getEmail());
        userJson.put("active", user.getActive());
        userJson.put("admin", user.getAdmin());

        return userJson;

    }

    @RequestMapping(value = "token/{token}/users/{id}/permissions/device/{deviceId}", method = RequestMethod.PUT)
    public @ResponseBody
    String updateUserDevicesPermissions(
            @PathVariable("id") int userId,
            @PathVariable("deviceId") int deviceId,
            @PathVariable("token") String token,
            @RequestBody String json,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            if (session.isAdmin()) {
                int clientId = session.getClientId();
                JSONObject objectGiven = new JSONObject(json);

                boolean permissionRead = objectGiven.getBoolean("permissionRead");
                boolean permissionWrite = objectGiven.getBoolean("permissionWrite");

                UsersDevicesPermissions permissions = userService.findUsersDevicesPermissions(userId, deviceId);
                if (permissions != null) {
                    permissions.setUpdatedDt(new Date());
                    permissions.setPermissionRead(permissionRead);
                    permissions.setPermissionWrite(permissionWrite);

                    userService.updateUsersDevicesPermissions(permissions);
                } else {
                    Date createdDt = new Date();
                    permissions = new UsersDevicesPermissions();
                    permissions.setClientId(clientId);
                    permissions.setUserId(userId);
                    permissions.setDeviceId(deviceId);
                    permissions.setPermissionRead(permissionRead);
                    permissions.setPermissionWrite(permissionWrite);
                    permissions.setCreatedDt(createdDt);
                    permissions.setUpdatedDt(createdDt);

                    userService.addUsersDevicesPermissions(permissions);
                }

                response.setStatus(HttpServletResponse.SC_OK);
                resp.put("response", RequestUtils.OK);
            }

        } else {
            RequestUtils.sendUnauthorized(response);
        }

        return resp.toString();
    }

    @RequestMapping(value = "token/{token}/users/{id}/permissions/campaign/{campaignId}", method = RequestMethod.PUT)
    public @ResponseBody
    String updateUserCampaignPermissions(
            @PathVariable("id") int userId,
            @PathVariable("campaignId") int campaignId,
            @PathVariable("token") String token,
            @RequestBody String json,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            if (session.isAdmin()) {
                int clientId = session.getClientId();
                JSONObject objectGiven = new JSONObject(json);

                boolean permissionRead = objectGiven.getBoolean("permissionRead");
                boolean permissionWrite = objectGiven.getBoolean("permissionWrite");

                UsersCampaignsPermissions permissions = userService.findUsersCampaignsPermissions(userId, campaignId);
                if (permissions != null) {
                    permissions.setUpdatedDt(new Date());
                    permissions.setPermissionRead(permissionRead);
                    permissions.setPermissionWrite(permissionWrite);

                    userService.updateUsersCampaignsPermissions(permissions);
                } else {
                    Date createdDt = new Date();
                    permissions = new UsersCampaignsPermissions();
                    permissions.setClientId(clientId);
                    permissions.setUserId(userId);
                    permissions.setCampaignId(campaignId);
                    permissions.setPermissionRead(permissionRead);
                    permissions.setPermissionWrite(permissionWrite);
                    permissions.setCreatedDt(createdDt);
                    permissions.setUpdatedDt(createdDt);

                    userService.addUsersCampaignsPermissions(permissions);
                }

                response.setStatus(HttpServletResponse.SC_OK);
                resp.put("response", RequestUtils.OK);
            }

        } else {
            RequestUtils.sendUnauthorized(response);
        }

        return resp.toString();
    }

    @RequestMapping(value = "token/{token}/users/{id}/permissions/device/{deviceId}", method = RequestMethod.DELETE)
    public @ResponseBody
    String deleteUserDevicesPermissions(
            @PathVariable("id") int userId,
            @PathVariable("deviceId") int deviceId,
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            if (session.isAdmin()) {
                userService.deleteUsersDevicesPermissions(userId, deviceId);

                response.setStatus(HttpServletResponse.SC_OK);
                resp.put("response", RequestUtils.OK);
            }
        } else {
            RequestUtils.sendUnauthorized(response);
        }

        return resp.toString();
    }

    @RequestMapping(value = "token/{token}/users/{id}/permissions/campaign/{campaignId}", method = RequestMethod.DELETE)
    public @ResponseBody
    String deleteUserCampaignPermissions(
            @PathVariable("id") int userId,
            @PathVariable("campaignId") int campaignId,
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        JSONObject resp = RequestUtils.getErrorResponse();
        Session session = sessionService.findSession(token);

        if (session != null) {
            if (session.isAdmin()) {
                userService.deleteUsersCampaignsPermissions(userId, campaignId);

                response.setStatus(HttpServletResponse.SC_OK);
                resp.put("response", RequestUtils.OK);
            }
        } else {
            RequestUtils.sendUnauthorized(response);
        }

        return resp.toString();
    }

    @RequestMapping("/user/login")
    public @ResponseBody
    String userLoginHandler(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = RequestUtils.getErrorResponse();

        Users user = userService.findUserByEmailAndPassword(email, password);

        if (user != null) {
            resp.put("response", RequestUtils.OK);

            Session session = new Session();

            session.setIp(request.getRemoteAddr());
            session.setUserId(user.getId());
            session.setAdmin(user.getAdmin());
            session.setClientId(user.getClientId());
            session.setCreatedDate(new Date());
            session.setUuid(UUID.randomUUID().toString().replaceAll("-", ""));

            sessionService.addSession(session);

            resp.put("token", session.getUuid());
        }

        return resp.toString();

    }

    @RequestMapping("/user/data/{token}")
    public @ResponseBody
    String userDataHandler(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
            resp.put("response", RequestUtils.OK);

            Clients client = userService.findClientById(session.getClientId());
            if (client != null) {
                resp.put("compName", client.getCompanyName());
            }

            Users user = userService.findUserById(session.getUserId());
            if (user != null) {
                resp.put("userId", user.getId());
                resp.put("firstname", user.getFirstname());
                resp.put("surname", user.getSurname());
                resp.put("email", user.getEmail());
                resp.put("admin", user.getAdmin());
            }

            List<AdCampaigns> campaigns = userService.findUserAdCompaigns(session.getClientId());
            List<Devices> devices = userService.findUserDevieces(session.getClientId());

            JSONArray arCamp = new JSONArray();

            for (AdCampaigns a : campaigns) {
                JSONObject obj = new JSONObject();

                obj.put("id", a.getId());
                obj.put("name", a.getName());

                arCamp.put(obj);
            }

            resp.put("campaigns", arCamp);

            JSONArray devs = new JSONArray();

            for (Devices d : devices) {
                JSONObject obj = new JSONObject();

                obj.put("id", d.getId());
                obj.put("status", d.getStatus());
                obj.put("uuid", d.getUuid());

                devs.put(obj);
            }

            resp.put("devices", devs);

            response.setStatus(HttpServletResponse.SC_OK);

            return resp.toString();

        } else {
            RequestUtils.sendUnauthorized(response);
        }

        return null;
    }

    private static String genereateRandomPass() {
        return RandomStringUtils.random(8, true, true);
    }

}

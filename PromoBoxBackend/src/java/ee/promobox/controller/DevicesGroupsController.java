/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.controller;

import ee.promobox.entity.Devices;
import ee.promobox.entity.DevicesGroup;
import ee.promobox.entity.DevicesGroupDevices;
import ee.promobox.service.DevicesGroupService;
import ee.promobox.service.Session;
import ee.promobox.service.SessionService;
import ee.promobox.service.UserService;
import ee.promobox.util.RequestUtils;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author ilja
 */
@Controller
public class DevicesGroupsController {

    private final static Logger log = LoggerFactory.getLogger(DevicesGroupsController.class);

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserService userService;

    @Autowired
    private DevicesGroupService groupService;

    @RequestMapping(value = "token/{token}/groups", method = RequestMethod.GET)
    public @ResponseBody
    String showAllDeviceGroups(
            @PathVariable("token") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null) {
            int clientId = session.getClientId();
            List<DevicesGroup> groups = null;
            if (session.isAdmin()) {
                groups = groupService.findGroupsByClientId(clientId);
            }
            
            List<Devices> userDevices = userService.findUserDevieces(clientId);

            JSONArray groupsArray = new JSONArray();
            if (groups != null && !groups.isEmpty()) {
            
                for (DevicesGroup group : groups) {
                    List<DevicesGroupDevices> groupDevices = groupService.findDevicesByGroupId(group.getId());

                    JSONObject jsonGroup = new JSONObject();
                    jsonGroup.put("id", group.getId());
                    jsonGroup.put("name", group.getName());

                    JSONArray devicesJSON = new JSONArray();
                    
                    for (Devices userDevice : userDevices) {
                        JSONObject deviceJSON = new JSONObject();
                        deviceJSON.put("id", userDevice.getId());
                        deviceJSON.put("name", userDevice.getUuid());
                        deviceJSON.put("contains", groupDeviceListContainsUserDevice(groupDevices, userDevice));
                        
                        devicesJSON.put(deviceJSON);
                    }
                    
                    jsonGroup.put("devices", devicesJSON);

                    groupsArray.put(jsonGroup);
                }
            }
            resp.put("groups", groupsArray);
            
            JSONArray devicesArray = new JSONArray();
            for (Devices device : userDevices) {
                JSONObject deviceJSON = new JSONObject();
                deviceJSON.put("id", device.getId());
                deviceJSON.put("name", device.getUuid());
                deviceJSON.put("contains",false);
                devicesArray.put(deviceJSON);
            }
            
            resp.put("devices", devicesArray);

            response.setStatus(HttpServletResponse.SC_OK);
            return resp.toString();
        } else {
            RequestUtils.sendUnauthorized(response);

            return null;
        }

    }

    @RequestMapping(value = "token/{token}/groups", method = RequestMethod.POST)
    public @ResponseBody
    String createGroup(
            @PathVariable("token") String token,
            @RequestParam String name,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null && session.isAdmin()) {
            int clientId = session.getClientId();
            DevicesGroup devicesGroup = new DevicesGroup(clientId, name != null ? name : "Devices group");
            groupService.createDeviceGroup(devicesGroup);

            response.setStatus(HttpServletResponse.SC_OK);
            resp.put("id", devicesGroup.getId());

            return resp.toString();
        } else {
            RequestUtils.sendUnauthorized(response);

            return null;
        }

    }

    @RequestMapping(value = "token/{token}/groups/{groupId}", method = RequestMethod.DELETE)
    public @ResponseBody
    String deleteGroup(
            @PathVariable("token") String token,
            @PathVariable("groupId") int groupId,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        JSONObject resp = new JSONObject();
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        Session session = sessionService.findSession(token);

        if (session != null && session.isAdmin()) {
            int clientId = session.getClientId();
            DevicesGroup devicesGroup = groupService.findGroupByClientAndGroupId(clientId, groupId);
            if (devicesGroup != null) {
                groupService.deleteEntity(devicesGroup);
                response.setStatus(HttpServletResponse.SC_OK);
            }

            return resp.toString();
        } else {
            RequestUtils.sendUnauthorized(response);

            return null;
        }

    }

    @RequestMapping(value = "token/{token}/groups/{groupId}/devices/{deviceId}", method = RequestMethod.POST)
    public @ResponseBody
    String addDeviceToGroup(
            @PathVariable("token") String token,
            @PathVariable("groupId") int groupId,
            @PathVariable("deviceId") int deviceId,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            JSONObject resp = new JSONObject();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Session session = sessionService.findSession(token);

            if (session != null && session.isAdmin()) {
                int clientId = session.getClientId();
                DevicesGroup devicesGroup = groupService.findGroupByClientAndGroupId(clientId, groupId);
                if (devicesGroup != null) {
                    response.setStatus(HttpServletResponse.SC_OK);

                    Devices device = userService.findDeviceByIdAndClientId(deviceId, clientId);
                    if (device != null) {
                        DevicesGroupDevices groupDevice = new DevicesGroupDevices(groupId, device.getId(), device.getUuid());
                        groupService.addDeviceToDeviceGroup(groupDevice);
                        resp.put(RequestUtils.RESULT, RequestUtils.OK);
                    } else {
                        log.error("Tried to add others device Client id = " + clientId + "deviceId = " + deviceId + " groupId = " + groupId);
                    }
                }
                return resp.toString();
            } else {
                RequestUtils.sendUnauthorized(response);

                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "token/{token}/groups/{groupId}/devices/{deviceId}", method = RequestMethod.DELETE)
    public @ResponseBody
    String removeDeviceFromGroup(
            @PathVariable("token") String token,
            @PathVariable("groupId") int groupId,
            @PathVariable("deviceId") int deviceId,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            JSONObject resp = new JSONObject();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Session session = sessionService.findSession(token);

            if (session != null && session.isAdmin()) {
                int clientId = session.getClientId();
                DevicesGroup devicesGroup = groupService.findGroupByClientAndGroupId(clientId, groupId);
                if (devicesGroup != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    groupService.removeDeviceFromDeviceGroup(deviceId, groupId);
                    resp.put(RequestUtils.RESULT, RequestUtils.OK);
                }
                return resp.toString();
            } else {
                RequestUtils.sendUnauthorized(response);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @RequestMapping(value = "token/{token}/groups/{groupId}/devices", method = RequestMethod.POST)
    public @ResponseBody
    String addDevicesToGroup(
            @PathVariable("token") String token,
            @PathVariable("groupId") int groupId,
            @RequestParam(required = true) String deviceIdsArray,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            JSONObject resp = new JSONObject();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Session session = sessionService.findSession(token);

            if (session != null && session.isAdmin()) {
                int clientId = session.getClientId();
                DevicesGroup devicesGroup = groupService.findGroupByClientAndGroupId(clientId, groupId);
                JSONArray deviceIds = new JSONArray(deviceIdsArray);
                if (devicesGroup != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    JSONArray added = new JSONArray();

                    for (int i = 0; i < deviceIds.length(); i++) {
                        int deviceId = deviceIds.getInt(i);
                        Devices device = userService.findDeviceByIdAndClientId(deviceId, clientId);
                        if (device != null) {
                            DevicesGroupDevices groupDevice = new DevicesGroupDevices(groupId, device.getId(), device.getUuid());
                            groupService.addDeviceToDeviceGroup(groupDevice);
                            added.put(device.getId());
                        } else {
                            log.error("Tried to add others device Client id = " + clientId + "deviceId = " + deviceId + " groupId = " + groupId);
                        }
                    }
                    resp.put("added", added);
                }

                return resp.toString();
            } else {
                RequestUtils.sendUnauthorized(response);

                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "token/{token}/groups/{groupId}/devices", method = RequestMethod.DELETE)
    public @ResponseBody
    String removeDevicesFromGroup(
            @PathVariable("token") String token,
            @PathVariable("groupId") int groupId,
            @RequestParam(required = true) String deviceIdsArray,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            JSONObject resp = new JSONObject();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            Session session = sessionService.findSession(token);

            if (session != null && session.isAdmin()) {
                int clientId = session.getClientId();
                DevicesGroup devicesGroup = groupService.findGroupByClientAndGroupId(clientId, groupId);
                JSONArray deviceIds = new JSONArray(deviceIdsArray);
                if (devicesGroup != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    JSONArray removed = new JSONArray();

                    for (int i = 0; i < deviceIds.length(); i++) {
                        int deviceId = deviceIds.getInt(i);
                        groupService.removeDeviceFromDeviceGroup(deviceId, groupId);
                        removed.put(deviceId);
                    }
                    resp.put("removed", removed);
                }

                return resp.toString();
            } else {
                RequestUtils.sendUnauthorized(response);

                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
    
    private boolean groupDeviceListContainsUserDevice(List<DevicesGroupDevices> groupDevices, Devices userDevice){
        for (DevicesGroupDevices groupDevice : groupDevices) {
            if (userDevice.getId() == groupDevice.getDeviceId()) {
                return true;
            }
        }
        return false;
    }

}

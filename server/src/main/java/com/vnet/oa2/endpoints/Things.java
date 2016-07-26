package com.vnet.oa2.endpoints;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
public class Things {
    @RequestMapping(value = "/things/A/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Map getThingsA(@PathVariable String id, Principal principal) {
        return getMap(id, principal.getName(), "A");
    }

    @RequestMapping(value = "/things/B/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Map getThingsB(@PathVariable String id, Principal principal) {
        return getMap(id, principal.getName(), "B");
    }

    @RequestMapping(value = "/things/C/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Map getThingsC(@PathVariable String id, Principal principal) {
        return getMap(id, principal.getName(), "C");
    }

    private Map getMap(String id, String name, String scope) {
        final Map map = new HashMap<>();
        map.put("id", id);
        map.put("scopedBy", scope);
        map.put("requestedBy", name);
        map.put("requestedAt", System.currentTimeMillis());
        map.put("class", getClass().getName());
        map.put("method", Thread.currentThread().getStackTrace()[2].getMethodName());
        return map;
    }
}



package com.huawei.hic.so.iam.web.rest;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Resource to return information about the currently running Spring profiles.
 */
@RestController
public class DemoController {

    @RequestMapping(method=RequestMethod.GET, path = "/api")
    public Principal home(Principal principal) {

        return principal;
    }
}

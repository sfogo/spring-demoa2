package com.vnet.oa2.endpoints;

import com.vnet.oa2.Utils;
import com.vnet.oa2.delegate.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@SessionAttributes("authorizationRequest")
public class Views {

    @Autowired
    private Delegate delegate;

    @RequestMapping(value="/app/admin", method = RequestMethod.GET)
    public RedirectView admin(HttpServletRequest request) {
        return new RedirectView(Utils.sameHostRedirectURL(request,"/app/admin/index.html"));
    }

    @RequestMapping("/oauth/error")
    public ModelAndView oauthError(HttpServletRequest request) {
        final Map<String, Object> model = new HashMap<>();
        model.put("_headerText", "Error");
        model.put("_message", "OAuth Error");
        return new ModelAndView("message", model);
    }

    @RequestMapping("/oauth/confirm_access")
    public ModelAndView consent(Map<String, Object> model, HttpServletRequest request) throws Exception {
        if (request.getAttribute("_csrf") != null) {
            model.put("_csrf", request.getAttribute("_csrf"));
        }

        final HttpSession session = request.getSession(false);
        if (session != null)
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

        model.put("_headerText", "User Consent");
        return new ModelAndView("consent", model);
    }

    @RequestMapping(value="/home", method = RequestMethod.GET)
    public ModelAndView home(Map<String, Object> model,
                             HttpServletRequest request) {
        model.put("_headerText", "Home");
        return new ModelAndView("message", model);
    }

    @RequestMapping(value="/test", method = RequestMethod.GET)
    public ModelAndView test(Map<String, Object> model,
                             HttpServletRequest request) throws Exception {

        if (request.getQueryString() != null &&
            request.getQueryString().startsWith("error"))
            throw new Exception("This was a Man Made Error");

        model.put("_headerText", "Test Page");
        model.put("_error", "Error OK");
        model.put("_warning", "Warning OK");
        model.put("_message", "Message OK");
        return new ModelAndView("message", model);
    }

    /**
     * Get login Form
     * @param model model
     * @param request Http Servlet Request
     * @return login view
     * @throws Exception
     */
    @RequestMapping(value = "/get_login", method = RequestMethod.GET)
    public ModelAndView getLogin(Map<String, Object> model,
                                 HttpServletRequest request) throws Exception {

        if (request.getAttribute("_csrf") != null) {
            model.put("_csrf", request.getAttribute("_csrf"));
        }

        // Test Spring Security Context for current authentication.
        // NOTE: another way of testing this would be to add a Principal
        // parameter to this login method and test its nullness.
        if (userLoggedIn()) {
            model.put("_warning", "Already logged in.");
            return new ModelAndView("message", model);
        }

        model.put("_headerText", "User Sign In");
        return new ModelAndView("get_login", model);
    }

    @RequestMapping(value="/app/manage", method = RequestMethod.GET)
    public ModelAndView manage(Map<String, Object> model,
                               Principal principal) {
        model.put("_headerText", "Manage");
        model.put("_approvals", delegate.getApprovals(principal.getName()));
        model.put("_tokens", delegate.getTokens(principal.getName()));
        return new ModelAndView("manage", model);
    }

    @RequestMapping(value="/app/manage", method = RequestMethod.POST)
    public ModelAndView manage(@RequestParam Map<String, String> parameters,
                               Map<String, Object> model,
                               Principal principal) {
        for (String key : parameters.keySet()) {
            try {
                final Utils.InputKey inputKey = new Utils.InputKey(key);
                if ("true".equals(parameters.get(key))) {
                    if (inputKey.forApproval())
                        delegate.revokeApprovals(principal.getName(), inputKey.getClientId(), inputKey.getTag());
                    else if (inputKey.forToken())
                        delegate.removeToken(principal.getName(), inputKey.getTag(), true);
                }
            } catch (Exception e) {}
        }
        // Reload same view : same as a GET
        return manage(model,principal);
    }

    /**
     * User Logged In
     * @return true if user has logged in
     */
    private static boolean userLoggedIn() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return !(authentication==null || authentication instanceof AnonymousAuthenticationToken);
    }

}

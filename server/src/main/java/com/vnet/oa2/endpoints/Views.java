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

    @RequestMapping("/oauth/confirm_access")
    public ModelAndView getConfirmAccessView(Map<String, Object> model, HttpServletRequest request) throws Exception {
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
    public ModelAndView dump(Map<String, Object> model,
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
    public ModelAndView managed(@RequestParam Map<String, String> parameters,
                                Map<String, Object> model,
                                Principal principal) {
        model.put("_headerText", "Manage");
        for (String key : parameters.keySet()) {
            try {
                final Utils.RemovalKey removalKey = new Utils.RemovalKey(key);
                if ("true".equals(parameters.get(key))) {
                    if (removalKey.isApproval())
                        delegate.revokeApprovals(principal.getName(), removalKey.getClientId(), removalKey.getTag());
                    else if (removalKey.isToken())
                        delegate.removeToken(principal.getName(), removalKey.getTag());
                }
            } catch (Exception e) {}
        }
        // Reload
        model.put("_approvals", delegate.getApprovals(principal.getName()));
        model.put("_tokens", delegate.getTokens(principal.getName()));
        return new ModelAndView("manage", model);
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

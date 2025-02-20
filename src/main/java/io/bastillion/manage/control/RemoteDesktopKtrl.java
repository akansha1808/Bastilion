package io.bastillion.manage.control;

import io.bastillion.common.util.AuthUtil;
import io.bastillion.manage.model.Auth;
import io.bastillion.manage.model.UserSessionsOutput;
import loophole.mvc.annotation.Kontrol;
import loophole.mvc.annotation.MethodType;
import loophole.mvc.base.BaseKontroller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Controller for remote desktop connections
 */
public class RemoteDesktopKtrl extends BaseKontroller {

    private static final Logger log = LoggerFactory.getLogger(RemoteDesktopKtrl.class);

    public RemoteDesktopKtrl(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    /**
     * Validates if user is admin and returns remote desktop view
     */
    @Kontrol(path = "/admin/remoteDesktop", method = MethodType.GET)
    public String remoteDesktop() {
        HttpSession session = getRequest().getSession();
        
        if (!Auth.MANAGER.equals(AuthUtil.getUserType(session))) {
            log.info("Unauthorized access attempt to remote desktop by {}", AuthUtil.getUsername(session));
            return "redirect:/admin/menu.html";
        }
        
        log.info("Remote desktop access by {}", AuthUtil.getUsername(session));
        return "/admin/remote_desktop.html";
    }

    /**
     * Disconnects the remote desktop session
     */
    @Kontrol(path = "/admin/disconnectRemoteDesktop", method = MethodType.GET)
    public String disconnectRemoteDesktop() {
        HttpSession session = getRequest().getSession();
        log.info("Remote desktop disconnected by {}", AuthUtil.getUsername(session));
        return "redirect:/admin/menu.html";
    }
} 
package com.dotmarketing.util;

import com.dotcms.api.web.HttpServletRequestThreadLocal;
import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.PermissionLevel;
import com.dotmarketing.business.web.WebAPILocator;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Usage:
 * 
 * PageMode mode = PageMode.get(request);
 * PageMode mode = PageMode.get(session);
 * 
 * mode.isAdmin ; mode.showLive ; mode.respectAnonPerms ;
 * 
 * 
 * if( PageMode.get(request).isAdmin){ doAdminStuff(); }
 * 
 * contentAPI.find("sad", user, mode.respectAnonPerms);
 * 
 * contentAPI.findByIdentifier("id", 1, mode.showLive, user, mode.respectAnonPerms);
 * 
 * PageMode.setPageMode(request, PageMode.PREVIEW_MODE);
 * 
 * 
 * 
 * @author will
 *
 */
public enum PageMode {

    LIVE(true, false), 
    ADMIN_MODE(true, true, true),
    PREVIEW_MODE(false, true),
    WORKING(false, true),
    EDIT_MODE(false, true),
    NAVIGATE_EDIT_MODE(false, true);

    private static PageMode DEFAULT_PAGE_MODE = LIVE;

    public final boolean showLive;
    public final boolean isAdmin;
    public final boolean respectAnonPerms;

    PageMode(boolean live, boolean admin) {
        this(live, admin, !admin);
    }

    PageMode(final boolean live, final boolean admin, final boolean respectAnonPerms) {
        this.showLive = live;
        this.isAdmin = admin;
        this.respectAnonPerms = respectAnonPerms;
    }


    public static PageMode get(final HttpSession ses) {

        PageMode mode = PageMode.isPageModeSet(ses)
                        ? (PageMode) ses.getAttribute(WebKeys.PAGE_MODE_SESSION)
                        : DEFAULT_PAGE_MODE;

        return mode;
    }

    public static PageMode getWithNavigateMode(final HttpServletRequest req) {
        HttpSession ses = req.getSession();
        PageMode mode = PageMode.isPageModeSet(ses)
                ? PageMode.getCurrentPageMode(ses)
                : DEFAULT_PAGE_MODE;

        return mode;
    }

    public static PageMode get(final HttpServletRequest req) {

        if (req == null || null!= req.getHeader("X-Requested-With")) {

            return DEFAULT_PAGE_MODE;
        }

        PageMode pageMode = null;

        if (null != req.getParameter(WebKeys.PAGE_MODE_PARAMETER)) {

            pageMode = PageMode.get(req.getParameter(WebKeys.PAGE_MODE_PARAMETER));
            req.setAttribute(WebKeys.PAGE_MODE_PARAMETER, pageMode);
        }

        if (null == pageMode && null != req.getAttribute(WebKeys.PAGE_MODE_PARAMETER)) {

            pageMode = (PageMode)req.getAttribute(WebKeys.PAGE_MODE_PARAMETER);
        }

        final HttpSession session = req.getSession(false);
        if (null == pageMode) {

            if (session == null) {

                return DEFAULT_PAGE_MODE;
            }

            pageMode = get(session);
        }

        if (PageMode.LIVE != pageMode) {

            final User user = PortalUtil.getUser(req);
            try {

                if (null != user && APILocator.getUserAPI().getAnonymousUser().equals(user)) {

                    final Host host = WebAPILocator.getHostWebAPI().getCurrentHost(req, pageMode);
                    if (null == host || !APILocator.getPermissionAPI().doesUserHavePermission
                            (host, PermissionLevel.READ.getType(), user)) {

                        pageMode = DEFAULT_PAGE_MODE;
                    }
                }
            } catch (Exception e) {

                Logger.debug(PageMode.class, e.getMessage(), e);
            }
        }

        return pageMode;
    }
    
    public static PageMode get(final String modeStr) {
        for(final PageMode mode : values()) {
                if(mode.name().equalsIgnoreCase(modeStr)) {
                    return mode;
                }
        }
        return DEFAULT_PAGE_MODE;
    }

    public static PageMode setPageMode(final HttpServletRequest request, boolean contentLocked, boolean canLock) {
        
        PageMode mode = PREVIEW_MODE;
        if (contentLocked && canLock) {
            mode=EDIT_MODE;
        } 
        return setPageMode(request,mode);

    }

    public static PageMode setPageMode(final HttpServletRequest request, PageMode mode) {
        request.getSession().setAttribute(WebKeys.PAGE_MODE_SESSION, mode);
        request.setAttribute(WebKeys.PAGE_MODE_SESSION, mode);
        return mode;
    }

    private static boolean isPageModeSet(final HttpSession ses) {
        return (ses != null && ses.getAttribute(com.dotmarketing.util.WebKeys.PAGE_MODE_SESSION) != null
                && ses.getAttribute("tm_date") == null);
    }

    private static PageMode getCurrentPageMode(final HttpSession ses) {
        PageMode sessionPageMode = (PageMode) ses.getAttribute(WebKeys.PAGE_MODE_SESSION);

        if (isNavigateEditMode(ses)) {
            return PageMode.NAVIGATE_EDIT_MODE;
        } else {
            return sessionPageMode;
        }
    }

    private static boolean isNavigateEditMode(final HttpSession ses) {
        PageMode sessionPageMode = (PageMode) ses.getAttribute(WebKeys.PAGE_MODE_SESSION);
        HttpServletRequest request = HttpServletRequestThreadLocal.INSTANCE.getRequest();

        return  sessionPageMode != PageMode.LIVE &&
                request != null &&
                request.getAttribute(WebKeys.PAGE_MODE_PARAMETER) == null ;
    }

}

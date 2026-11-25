package com.example.hotel_back.Common.Interceptor;
import com.example.hotel_back.Common.constant.JwtClaimsConstant;
import com.example.hotel_back.Common.context.BaseContext;
import com.example.hotel_back.Common.properties.JwtProperties;
import com.example.hotel_back.Utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        //2、校验令牌
        try {
            log.debug("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            BaseContext.setCurrentId(empId);

            // 角色权限校验：/admin/** 管理类接口仅允许管理员（roleId = 1）访问，放行员工个人资料与退出接口
            String uri = request.getRequestURI();
            Object roleClaim = claims.get(JwtClaimsConstant.ROLE_ID);
            boolean isSelfEndpoint = uri != null && (
                    uri.startsWith("/admin/common/")
                    || uri.equals("/admin/logout")
                    || uri.equals("/admin/info")
                    || uri.equals("/admin/updateInfo")
                    || uri.equals("/admin/updatePassword")
            );
            if (uri != null && uri.startsWith("/admin/") && !isSelfEndpoint) {
                Long roleId = roleClaim == null ? null : Long.valueOf(roleClaim.toString());
                if (roleId == null || roleId != 1L) {
                    log.warn("越权拦截：员工 {} (roleId={}) 尝试访问 {}", empId, roleId, uri);
                    BaseContext.removeCurrentId();
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }
            }
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            BaseContext.removeCurrentId();
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清理 ThreadLocal，防止线程池复用导致的身份串号与内存泄漏
        BaseContext.removeCurrentId();
    }
}

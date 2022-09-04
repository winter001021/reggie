package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//登录过滤
@WebFilter(filterName = "LoginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse Response = (HttpServletResponse) servletResponse;
        log.info("拦截到请求：{}", request.getRequestURI());
        String requestURI = request.getRequestURI();
        String[] urls = {"/employee/login", "/employ/logout", "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"

        };//不处理

        boolean check = check(urls, requestURI);

        if (check) {
            filterChain.doFilter(request, Response);
            return;
        }

        //判断后端用户是否登录
        if (request.getSession().getAttribute("employ") != null) {

            long id = Thread.currentThread().getId();

            Long empId = (Long) request.getSession().getAttribute("employ");

            BaseContext.setCurrentId(empId);
            filterChain.doFilter(request, Response);
            return;
        }
        //判断前端用户是否登录
        if (request.getSession().getAttribute("user") != null) {

            long id = Thread.currentThread().getId();

            Long userId = (Long) request.getSession().getAttribute("user");

            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, Response);

            return;
        }

        //返回输出流
        Response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

    }

    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}

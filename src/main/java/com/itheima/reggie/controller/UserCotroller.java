package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserCotroller {
    @Autowired
    private UserService userService;

    /**
     * 发送验证码
     *
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {

        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            System.out.println(code);
            log.info("code={}", code);
            // SMSUtils.sendMessage("瑞吉外卖","",phone,code );

            session.setAttribute(phone, code);
            return R.success("手机验证码发送成功");

        }
        return R.error("验证码发送失败");

    }

    /**
     * 登录
     * @param
     * @param
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpServletRequest request) {
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();
        //比较
        Object codeInSession =request.getSession().getAttribute(phone) ;
        if (codeInSession!=null&&codeInSession.equals(code)){
            LambdaQueryWrapper<User> qu = new LambdaQueryWrapper<>();
            qu.eq(User::getPhone,phone);
            User user = userService.getOne(qu);
            if (user==null){
                user=new User();
                user.setPhone(phone);

                userService.save(user);

            }

            request.getSession().setAttribute("user",user.getId());
            BaseContext.setCurrentId(user.getId());
            return R.success(user);

        }

        return R.error("登录失败");
    }


    /**
     * 退出登录
     * @return
     */
    @PostMapping("/loginout")
    public R<String> loginout(HttpServletRequest request){
        /*
        *  @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employ");
        return R.success("退出成功");
    }
*/
        request.getSession().removeAttribute("user");

        return R.success("退出成功");
    }


}

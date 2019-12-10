package org.corbin.oauth.server.auth.config;

import org.corbin.oauth.server.auth.handler.LoginFailureHandler;
import org.corbin.oauth.server.auth.handler.LoginSuccessHander;
import org.corbin.oauth.server.entity.Account;
import org.corbin.oauth.server.service.impl.AccountServiceImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author xiesu / Corbin
 * @version 1.0
 * @date 19-12-6
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private final AccountServiceImpl accountService;

  public WebSecurityConfig(AccountServiceImpl accountService) {
    this.accountService = accountService;
  }

  /**
   * 重写userDetailService方法,此处采用方法引用的形式,<br>
   * 实际上相当于定义一个类,实现UserDetailsService接口的loadUserByUsername(String name)方法
   */
  @Override
  protected UserDetailsService userDetailsService() {
    return // 返回的userDetails实现类中username为账户的唯一标识->accountId
    this::loadUserByUsername;
  }

  /**
   * 重写认证方式,使用BCryptPasswordEncoder加密的方式
   *
   * @param auth 认证参数
   * @author xiesu / Corbin
   * @date 19-12-10
   */
  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
  }

  /**
   * 此处可以配置认证成功/失败后的操作<br>
   * 可以定义免认证的接口<br>
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // 禁止跨域请求伪造
    http.csrf().disable();
    // 认证成功/失败后的操作
    http.formLogin().successHandler(new LoginSuccessHander());
    http.formLogin().failureHandler(new LoginFailureHandler());
    // 任何请求都要认证
    http.authorizeRequests().anyRequest().fullyAuthenticated();
  }

  /**
   * 实现UserDetailsService接口的方法<br>
   * {@link UserDetailsService#loadUserByUsername(String)}
   *
   * @param username 用户登录名,本案例中为用户的邮箱,手机号等
   */
  private UserDetails loadUserByUsername(String username) {
    Account account = accountService.findLoginAccount(username);
    if (Objects.isNull(account)) {
      throw new UsernameNotFoundException("用户不存在");
    }

    // 此处为角色的设置
    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
    // 返回的userDetails实现类中username为账户的唯一标识->accountId
    return new User(account.getAccountId(), account.getMd5Pwd(), authorities);
  }

  /**
   * 使用BCryptPasswordEncoder ,默认盐选随机数为(-1)10
   *
   * @author xiesu / Corbin
   * @date 19-12-10
   */
  private PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}

package com.pluralsight.conference;

import com.pluralsight.conference.service.ConferenceUserDetailsContextMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true
)
public class ConferenceSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ConferenceUserDetailsContextMapper ctxMapper;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                //.antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/anonymous*").anonymous() //ROLE is anonymous
                .antMatchers("/login*").permitAll() //ROLE is everyone
                .antMatchers("/account*").permitAll()
                .antMatchers("/password*").permitAll()
                .antMatchers("/assets/css/**", "assets/js/**", "/images/**").permitAll()
                .antMatchers("/index*").permitAll()
                .anyRequest().authenticated()

                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .failureUrl("/login?error=true")
                .permitAll()
                .defaultSuccessUrl("/", true) //alwaysUse -> true means,
                // whether people manually hit another page or not, on successful authentication,
                // we will take them to root page

                .and()

                //remember me functionality => Adds a cookie called "remember-me" and in backend stores a reference to that token
                .rememberMe()
                .key("superSecretKey")
                .tokenRepository(tokenRepository())

                .and()

                //logout
                .logout()
                .logoutSuccessUrl("/login?logout=true")
                //see index.jsp for /perform_logout. Doing AntPathRequestMatcher as hack, so that we can do a GET instead of POST
                .logoutRequestMatcher(new AntPathRequestMatcher("/perform_logout", "GET"))
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll();

    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl token = new JdbcTokenRepositoryImpl();
        token.setDataSource(dataSource);
        return token;
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {

        //METHOD 1
        //*in memory authentication* => Authenticate test using hardcoded username
        // and password

        //        auth.inMemoryAuthentication()
        //            .withUser("bryan")
        //            .password(passwordEncoder().encode("pass"))
        //            .roles("USER");

        //METHOD 2
        //*jdbc authentication*

                auth.jdbcAuthentication()
                    .dataSource(dataSource)
                    .passwordEncoder(passwordEncoder());

        //METHOD 3
        //*ldap authentication*


        //See test-server.ldif file and application.properties
//        auth.ldapAuthentication()
//            .userDnPatterns("uid={0},ou=people") //ou -> organization unit
//            .groupSearchBase("ou=groups")
//            .contextSource()
//            .url("ldap://localhost:8389/dc=pluralsight,dc=com")
//            .and()
//            .passwordCompare()
//            .passwordEncoder(passwordEncoder())
//            .passwordAttribute("userPassword")
//            //For custom user object. Remember, it's post authentication and basically we are only decorating
//            .and()
//            .userDetailsContextMapper(ctxMapper);


    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

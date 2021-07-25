package com.jojoldu.book.springboot.config.auth;

import com.jojoldu.book.springboot.config.auth.dto.OAuthAttributes;
import com.jojoldu.book.springboot.config.auth.dto.SessionUser;
import com.jojoldu.book.springboot.domain.user.User;
import com.jojoldu.book.springboot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        User user = saveOrUpdate(attributes);
//        User user = this.saveOrUpdate(attributes);
        httpSession.setAttribute("user", new SessionUser(user));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

//    public<U> Optional<U> map(Function<? super T, ? extends U> mapper) {
//        Objects.requireNonNull(mapper);
//        if (!isPresent())
//            return empty();
//        else {
//            return Optional.ofNullable(mapper.apply(value));
//        }
//    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                .orElse(attributes.toEntity());

//        User user = userRepository.findByEmail(attributes.getEmail()).get();
//        if (user == null) {
//            user = attributes.toEntity();
//        } else {
//            user.update(attributes.getName(), attributes.getPicture());
//        }

//                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
//                .map(inputuser -> inputuser.update(attributes.getName(), attributes.getPicture()))

//                .map(inputuser -> {
//            User outputuser = inputuser.update(attributes.getName(), attributes.getPicture());
//            return outputuser;
//                })

//        Optional<User> optional = Optional.ofNullable(new User());
//        optional
//                .map(
//                        new Function<User, User>() {
//                            @Override
//                            public User apply(User inputuser) {
//                                User outputuser = inputuser.update(attributes.getName(), attributes.getPicture());
//                                return outputuser;
//                            }
//                        }
//                )
//                .orElse(attributes.toEntity());

        return userRepository.save(user);
    }
}
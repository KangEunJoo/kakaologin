package com.example.kakao.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.kakao.model.User;
import com.example.kakao.model.oauth.KakaoProfile;
import com.example.kakao.model.oauth.OauthToken;
import com.example.kakao.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserService {
UserRepository userRepository; //(1)
    
    public OauthToken getAccessToken(String code) {

        //(2)
        RestTemplate rt = new RestTemplate();

        //HttpHeader 오브젝트 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        //HttpBody 오브젝트 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", "6d14dc79fabe2059d567d923273f3225");
        params.add("redirect_uri", "https://cozyinfo.vercel.app/main");
        params.add("code", code);
        //params.add("client_secret", "{시크릿 키}"); // 생략 가능!

        //HttpHeader와 HttpBody를 하나의 오브젝트에 담기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(params, headers);

        //Http 요청하기-Post방식으로-그리고 response변수의 응답받음
        ResponseEntity<String> accessTokenResponse = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        //Gson, Json SImple, ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        OauthToken oauthToken = null;
        try {
            oauthToken = objectMapper.readValue(accessTokenResponse.getBody(), OauthToken.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return oauthToken; //(8)
    }
    public User saveUser(String token) {

		//(1)
        KakaoProfile profile = findProfile(token);
 
		//(2)
        User user = userRepository.findByKakaoEmail(profile.getKakao_account().getEmail());
        
        //(3)
        if(user == null) {
            user = User.builder()
                    .kakaoId(profile.getId())
                     //(4)
                    .kakaoProfileImg(profile.getKakao_account().getProfile().getProfile_image_url())
                    .kakaoNickname(profile.getKakao_account().getProfile().getNickname())
                    .kakaoEmail(profile.getKakao_account().getEmail())
                     //(5)
                    .userRole("ROLE_USER").build();

            userRepository.save(user);
        }

        return user;
    }
    
    
    //(1-1)
    public KakaoProfile findProfile(String token) {
        
        //(1-2)
        RestTemplate rt = new RestTemplate();

		//(1-3)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		//(1-5)
        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest =
                new HttpEntity<>(headers);

		//(1-6)
        // Http 요청 (POST 방식) 후, response 변수에 응답을 받음
        ResponseEntity<String> kakaoProfileResponse = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

		//(1-7)
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoProfile kakaoProfile = null;
        try {
            kakaoProfile = objectMapper.readValue(kakaoProfileResponse.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        //User 오브젝트 : username, password, email
        System.out.println("카카오 아이디(번호) : "+kakaoProfile.getId());
        System.out.println("카카오 이메일 : "+kakaoProfile.getKakao_account().getEmail());
        
        return kakaoProfile;
    }

}

package com.example.kakao.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.example.kakao.model.User;
import com.example.kakao.model.oauth.OauthToken;
import com.example.kakao.service.UserService;

@RestController 
@RequestMapping("/api")
@CrossOrigin(origins="http://localhost:3000",allowCredentials = "true")
public class UserController {
	@Autowired
    private UserService userService; //(2)

    // 프론트에서 인가코드 받아오는 url
   @GetMapping("/oauth/token") // (3)
   public OauthToken getLogin(@RequestParam("code") String code) { //(4)

       // 넘어온 인가 코드를 통해 access_token 발급 //(5)
       OauthToken oauthToken = userService.getAccessToken(code);

       //발급 받은 accessToken 으로 카카오 회원 정보 DB 저장
       User User = userService.saveUser(oauthToken.getAccess_token());
       
       return oauthToken;
   }
}

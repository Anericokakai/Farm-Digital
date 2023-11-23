package com.farmdigital.nerddevs.controller;
import com.farmdigital.nerddevs.Dto.AuthenticationDto;
import com.farmdigital.nerddevs.Dto.FarmerRegistrationDto;
import com.farmdigital.nerddevs.Dto.ResetPasswordDto;
import com.farmdigital.nerddevs.service.UserRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor

@RequestMapping("/api/v1/farm_digital/super")
public class FarmerAuthenticationController {
    private  final UserRegistrationService userRegistrationService;

//    ! add user route
    @PostMapping("/user/register")
    public ResponseEntity<?> registerUse(@RequestBody @Valid FarmerRegistrationDto user) throws Exception{
        var  res= userRegistrationService.saveUser(user);
//1233
    return  ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
    @PostMapping("/user/authenticate")

    public  ResponseEntity<?> authenticateUser(@RequestBody @Valid AuthenticationDto request){

var response=userRegistrationService.authenticateauser(request);

        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }
// ! forgot password route
    @PostMapping("/user/reset_password")
    public  ResponseEntity<?> forgotPassword(@RequestBody @Valid ResetPasswordDto resetPasswordDto){
        var message= userRegistrationService.changePassword(resetPasswordDto.getEmail());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
    }
}

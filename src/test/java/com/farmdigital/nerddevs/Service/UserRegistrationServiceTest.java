package com.farmdigital.nerddevs.Service;
import com.farmdigital.nerddevs.Registration.Dto.AuthenticationDto;
import com.farmdigital.nerddevs.Registration.Dto.FarmerRegistrationDto;
import com.farmdigital.nerddevs.Registration.Exceptions.UserAlreadyExistException;
import com.farmdigital.nerddevs.Mails.Service.VerificationEmailComposer;
import com.farmdigital.nerddevs.Registration.model.Farmer;
import com.farmdigital.nerddevs.Registration.model.Roles;
import com.farmdigital.nerddevs.Registration.Repository.FarmerRepository;
import com.farmdigital.nerddevs.Registration.Repository.RolesRepository;
import com.farmdigital.nerddevs.Registration.security.JwtServices;
import com.farmdigital.nerddevs.Registration.service.UserRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = UserRegistrationServiceTest.class)
@ExtendWith(SpringExtension.class)
public class UserRegistrationServiceTest {
    @Mock
    private FarmerRepository farmerRepository;
    @Mock
    private RolesRepository rolesRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private VerificationEmailComposer verificationEmailComposer;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtServices jwtServices;
    @InjectMocks
    UserRegistrationService userRegistrationService;

    Farmer user;
    Roles roles;
    Farmer verifiedFarmer;
    FarmerRegistrationDto newFarmer;

    @BeforeEach
    void setUp() {
        Roles roles=new Roles(1,"USER");
       user=  Farmer.builder()
                .farmerId("123")
                .id(1)
                .email("anericokakai@gmail.com")
                .name("anericokakai")
                .password("123456")
                .phoneNumber("792626899")
                .registrationTime("11/17/2023 at 08:33 PM ")
                .roles(Collections.singletonList(roles))
                .typeOfAccount("FARMER")
                .verified(false)
                .build();
       verifiedFarmer= Farmer.builder()
               .farmerId("123")
               .id(1)
               .email("anericokakai@gmail.com")
               .name("anericokakai")
               .password("123456")
               .phoneNumber("792626899")
               .registrationTime("11/17/2023 at 08:33 PM ")
               .roles(Collections.singletonList(roles))
               .typeOfAccount("FARMER")
               .verified(true)
               .build();
       roles= Roles.builder()
               .id(1)
               .name("USER").build();
        newFarmer=FarmerRegistrationDto.builder()
                .name("anerico")
                .phoneNumber("792626899")
                .email("anericokakai@gmail.com")
                .password("Anericokakai@2004")
                .build();
    }


    @Test
    @Order(1)
    public void assert_will_Create_newUSer() throws Exception {
        String  email="anericokakai@gmail.com";
        Mockito.when(farmerRepository.save(user)).thenReturn(user);
        Mockito.when(rolesRepository.findByName("USER")).thenReturn(roles);
        Mockito.when(verificationEmailComposer.sendVerificationEmail(email)).thenReturn("email was sent successfully!");
      Map<String ,String > repose=  userRegistrationService.saveUser(newFarmer);
      Map<String,String> expectedRe=new HashMap<>();
      expectedRe.put("message","user created successfully, please check your email to verify  your account !");
        assertEquals(repose,expectedRe);
    }


    @Test
    @Order(2)
    public  void  assert_Will_throwException_ForExistingUser(){
        Mockito.when(farmerRepository.findByEmail(newFarmer.getEmail())).thenReturn(Optional.ofNullable(user));
        assertThrows(UserAlreadyExistException.class,()->userRegistrationService.saveUser(newFarmer));

    }
    @Test
    @Order(3)
    public  void assert_will_Generate_TimeStamp(){
        DateTimeFormatter expectedCreatedTime=DateTimeFormatter.ofPattern("MM/dd/yyy 'at' hh:mm a");
      String  expectedResult=  expectedCreatedTime.format(LocalDateTime.now());
        String  createdTime= userRegistrationService.timeCreatedAccout();
        assertEquals(createdTime,expectedResult);
    }

    @Test
    @Order(4)
    public  void assert_UniqueId_willBeCreated(){
        String  uniqueTime= LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyMMddss"));
        String  phoneNUmber= String.valueOf(newFarmer.getPhoneNumber());
        String  expectedId= "FARMER-"+phoneNUmber.substring(7)+uniqueTime;
        String  resultId= userRegistrationService.createUniqueId(newFarmer.getPhoneNumber());
        assertEquals(resultId,expectedId);

    }
@Test
    @Order(5)
    public  void assert_will_Not_Authenticate_UnVerified_User(){
        String  email="anericokakai@gmail.com";
        String  password="1234566";
    AuthenticationDto authenticationDto= AuthenticationDto
            .builder()
            .email(email)
            .password(password)
            .build();
    UsernamePasswordAuthenticationToken authenticationToken= new UsernamePasswordAuthenticationToken(
            email,
            password
    );
        Mockito.when(authenticationManager.authenticate(authenticationToken)).thenReturn(authenticationToken);
        Mockito.when(verificationEmailComposer.sendVerificationEmail(email)).thenReturn("Email sent successfully");
        Mockito.when(farmerRepository.findByEmail(email)) .thenReturn(Optional.of(user));
        Mockito.when(jwtServices.generateAToken(user)).thenReturn("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhbmVyaWNva2FrYWlAZ21haWwuY29tIiwibmFtZSI6ImFuZXJpY29rYWthaUBnbWFpbC5jb20iLCJpYXQiOjE1MTYyMzkwMjJ9.di54c7dhhSJu3nT9fFNmvQpvZncJQIy2nSTcrqoBOIk");
        String  expectedToken="you have not verified your account , please check your email to verify your account!!";
      Map<String,Object> response=  userRegistrationService.authenticateauser(authenticationDto);
      Object  errorMessage=response.get("errorMessage");
      assertEquals(errorMessage,expectedToken);

}
    @Test
    public  void assert_will_Authenticate_user_only_when_Account_is_Verified(){
        String  token="yJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmVyaW" +
                "NvbWlzaWtvQGdtYWlsLmNvbSIsImlhdCI6MTcwMDczNDAxMy" +
                "wiZXhwIjoxNzAwNzM5NDEzfQ.uSQ6UvVB-LlTArlUt4nNr4X" +
                "yHE5oBfgVY0EHMn5T8Rc";
        String email="anericokakai@gmail.com";
        String  password="Anericokakai@2004";
Mockito.when(farmerRepository.findByEmail(verifiedFarmer.getEmail())).thenReturn(Optional.ofNullable(verifiedFarmer));
AuthenticationDto userToAuthenticate= AuthenticationDto.builder()
        .password(password)
        .email(email).build();

UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken=
         new UsernamePasswordAuthenticationToken(
            email,
            password
         );
Mockito.when(authenticationManager.authenticate(usernamePasswordAuthenticationToken)).thenReturn(usernamePasswordAuthenticationToken);
String  expectedToken="yJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmVyaWNvbWlzaWtvQGdtYWl" +
        "sLmNvbSIsImlhdCI6MTcwMDczNDAxMywiZXhwIjoxNzAwNzM5NDEz" +
        "fQ.uSQ6UvVB-LlTArlUt4nNr4XyHE5oBfgVY0EHMn5T8Rc";
Mockito.when(jwtServices.generateAToken(verifiedFarmer)).thenReturn(token);
Map<String,Object> result=userRegistrationService.authenticateauser(userToAuthenticate);
assertEquals(result.get("token"),expectedToken);

    }


@Test
    public  void assert_Will_throw_error_For_Invalid_Phone(){
        String  invalidPhone="079303s30d34";
    assertThrows(NumberFormatException.class,()->userRegistrationService.verifyPhoneNumber(invalidPhone));
}




}



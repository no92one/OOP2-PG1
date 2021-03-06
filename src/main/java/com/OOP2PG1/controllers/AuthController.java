package com.OOP2PG1.controllers;


import com.OOP2PG1.application.repositories.SiteRepository;
import com.OOP2PG1.models.SiteRoles;
import com.OOP2PG1.payload.request.SiteRoleRequest;
import com.OOP2PG1.repository.SiteRoleRepository;
import com.OOP2PG1.repository.UserRepository;

import com.OOP2PG1.models.ERole;
import com.OOP2PG1.models.Role;
import com.OOP2PG1.models.User;
import com.OOP2PG1.payload.request.LoginRequest;
import com.OOP2PG1.payload.request.SignupRequest;
import com.OOP2PG1.payload.response.JwtResponse;
import com.OOP2PG1.payload.response.MessageResponse;
import com.OOP2PG1.repository.RoleRepository;
import com.OOP2PG1.security.jwt.JwtUtils;
import com.OOP2PG1.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SiteRoleRepository siteRoleRepository;
    @Autowired
    RoleRepository roleRepository;

    @Autowired
    SiteRepository siteRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin") // pass the username and password into a loginRequest object.
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // Authentication is enabled from the WebSecurityConfig class that extends WebSecurityConfigurerAdapter
        // Creates an authentication object that the username and password is passed into
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        //runs the authentication object through the SecurityContextHolder.getContext() method.
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    /* signUpRequest: takes the data from the json file (in postman, or the data of the input fields for username, email, password and roles)
    and create an object of it. (the signUpRequest will be a temporary place to hold the object before creating a new user.)
    */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        // compare userRepository username to signUpRequest username. If true then the user already exist
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }
        // compare userRepository email to signUpRequest email. If true then the email already exist
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        // signUpRequest passed both if statements.
        // Create new user's account
        // passing signUpRequest data to a user object.
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword())); // passing the signUpRequest password into encoder method before passing it to the user object.

        ArrayList<String> strRoles = signUpRequest.getRoles(); // passing signUpRequest roles to strRoles
        ArrayList<Role> roles = new ArrayList<>(); // creating a new ArrayList as a Role object

        if (strRoles == null) {  // if the strRoles is empty
            Role userRole = roleRepository.findByName(ERole.ROLE_USER) // creating a role object and pass the ROLE_USER and id into it.
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole); // Store the userRole object into the roles set
        } else {
            strRoles.forEach(role -> { // iterate the set of role and if it contains one of the strings in the case then add it to strRoles
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "editor":
                        Role modRole = roleRepository.findByName(ERole.ROLE_EDITOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles); // pass the roles that exist into the user object
        userRepository.save(user); // save the user object into the database

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

     @DeleteMapping("/signout")
    public ResponseEntity logoutUser() {
        SecurityContextHolder.getContext().setAuthentication(null);
        return ResponseEntity.ok(new MessageResponse("logout NOT REALLY successful"));
    }


    @PutMapping("/addeditor")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> addEditor(@RequestBody SiteRoleRequest roleRequest){

        if(!userRepository.existsByUsername(roleRequest.getUsername())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Can't find user " + roleRequest.getUsername() + "!"));
        }

        if (!siteRepository.existsByurlHeader(roleRequest.getUrlHeader())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Can't find site " + roleRequest.getUrlHeader() + "!"));
        }

        User temp = userRepository.findUserByUsername(roleRequest.getUsername());

        for (int i = 0; i < temp.getSiteRoles().size(); i++) {
            if(temp.getSiteRoles().get(i).getUrlHeader().equals(roleRequest.getUrlHeader())){
                if(temp.getSiteRoles().get(i).getName().equals(ERole.ROLE_EDITOR)) {
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Error: Already a editor for this site!"));
                }
            }
        }

        temp.getSiteRoles().add(new SiteRoles(ERole.ROLE_EDITOR, roleRequest.getUrlHeader()));
        userRepository.save(temp);

        return ResponseEntity.ok( new MessageResponse("It worked" + temp.getSiteRoles().get(temp.getSiteRoles().size() - 1).toString()));

    }

    @DeleteMapping("/deleteeditor")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> deleteEditor(@RequestBody SiteRoleRequest roleRequest){

        if(!userRepository.existsByUsername(roleRequest.getUsername())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Can't find user " + roleRequest.getUsername() + "!"));
        }

        if (!siteRepository.existsByurlHeader(roleRequest.getUrlHeader())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Can't find site " + roleRequest.getUrlHeader() + "!"));
        }

        User temp = userRepository.findUserByUsername(roleRequest.getUsername());

        for (int i = 0; i < temp.getSiteRoles().size(); i++) {

            if(temp.getSiteRoles().get(i).getUrlHeader().equals(roleRequest.getUrlHeader()) &&
                    temp.getSiteRoles().get(i).getName().equals(ERole.ROLE_EDITOR)) {

                temp.getSiteRoles().remove(i);
                userRepository.save(temp);
                return ResponseEntity.ok( new MessageResponse("It worked"));
            }
        }
        return ResponseEntity
                .badRequest()
                .body( new MessageResponse("Error: No match"));

    }

    @GetMapping("/geteditors/{urlHeader}") // takes this parameter
    @PreAuthorize("permitAll()")
    public List<User> geteditors(@PathVariable String urlHeader){ // pass it into this method

        List<User> allUsers = userRepository.findAll();
        ERole role = ERole.ROLE_EDITOR;
        List<User> siteEditor = new ArrayList<>();

        for (int i = 0; i < allUsers.size(); i++) {
            for (int j = 0; j < allUsers.get(i).getSiteRoles().size(); j++) {
                if (allUsers.get(i).getSiteRoles().get(j).getUrlHeader().equals(urlHeader)
                && allUsers.get(i).getSiteRoles().get(j).getName().equals(ERole.ROLE_EDITOR)){
                    siteEditor.add(userRepository.findUserByUsername(allUsers.get(i).getUsername()));
                }
            }
        }

        return siteEditor;
    }

    @PutMapping("/addadmin")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> addAdmin(@RequestBody SiteRoleRequest roleRequest){

        if(!userRepository.existsByUsername(roleRequest.getUsername())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Can't find user " + roleRequest.getUsername() + "!"));
        }

        if (!siteRepository.existsByurlHeader(roleRequest.getUrlHeader())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Can't find site " + roleRequest.getUrlHeader() + "!"));
        }

        User temp = userRepository.findUserByUsername(roleRequest.getUsername());

        for (int i = 0; i < temp.getSiteRoles().size(); i++) {
            if(temp.getSiteRoles().get(i).getUrlHeader().equals(roleRequest.getUrlHeader())){
                if(temp.getSiteRoles().get(i).getName().equals(ERole.ROLE_ADMIN)) {
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Error: Already a editor for this site!"));
                }
            }
        }

        temp.getSiteRoles().add(new SiteRoles(ERole.ROLE_ADMIN, roleRequest.getUrlHeader()));
        userRepository.save(temp);

        return ResponseEntity.ok( new MessageResponse("It worked" + temp.getSiteRoles().get(temp.getSiteRoles().size() - 1).toString()));

    }

    @DeleteMapping("/deleteadmin")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> deleteAdmin(@RequestBody SiteRoleRequest roleRequest){

        if(!userRepository.existsByUsername(roleRequest.getUsername())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Can't find user " + roleRequest.getUsername() + "!"));
        }

        if (!siteRepository.existsByurlHeader(roleRequest.getUrlHeader())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Can't find site " + roleRequest.getUrlHeader() + "!"));
        }

        User temp = userRepository.findUserByUsername(roleRequest.getUsername());

        for (int i = 0; i < temp.getSiteRoles().size(); i++) {

            if(temp.getSiteRoles().get(i).getUrlHeader().equals(roleRequest.getUrlHeader()) &&
                    temp.getSiteRoles().get(i).getName().equals(ERole.ROLE_ADMIN)) {

                temp.getSiteRoles().remove(i);
                userRepository.save(temp);
                return ResponseEntity.ok( new MessageResponse("It worked"));
            }
        }
        return ResponseEntity
                .badRequest()
                .body( new MessageResponse("Error: No match"));

    }

    @GetMapping("/getadmin/{urlHeader}") // takes this parameter
    @PreAuthorize("permitAll()")
    public List<User> getAdmin(@PathVariable String urlHeader){ // pass it into this method

        List<User> allUsers = userRepository.findAll();
        List<User> siteEditor = new ArrayList<>();

        for (int i = 0; i < allUsers.size(); i++) {
            for (int j = 0; j < allUsers.get(i).getSiteRoles().size(); j++) {
                if (allUsers.get(i).getSiteRoles().get(j).getUrlHeader().equals(urlHeader)
                        && allUsers.get(i).getSiteRoles().get(j).getName().equals(ERole.ROLE_ADMIN)){
                    siteEditor.add(userRepository.findUserByUsername(allUsers.get(i).getUsername()));
                }
            }
        }

        return siteEditor;
    }


}





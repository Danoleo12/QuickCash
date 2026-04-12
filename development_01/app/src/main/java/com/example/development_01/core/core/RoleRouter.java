package com.example.development_01.core.core;

public class RoleRouter {
    public String determineRole(String email){
        if(email.equals("employer@dal.ca")){
            return "Employer";
        } else if (email.equals("employee@dal.ca")) {
            return "Employee";
        } else if (email.trim().isEmpty()){
            return "Empty Email";
        }


        return "Invalid Email";
    }
}

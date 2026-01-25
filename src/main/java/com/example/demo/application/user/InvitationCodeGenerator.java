package com.example.demo.application.user;

import com.example.demo.domain.User;

public interface InvitationCodeGenerator {

    String generate(User user);
}

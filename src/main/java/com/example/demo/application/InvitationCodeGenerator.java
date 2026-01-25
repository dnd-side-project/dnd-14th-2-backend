package com.example.demo.application;

import com.example.demo.domain.User;

public interface InvitationCodeGenerator {

    String generate(User user);
}

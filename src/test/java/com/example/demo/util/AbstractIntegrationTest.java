package com.example.demo.util;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = NONE)
public abstract class AbstractIntegrationTest {
}

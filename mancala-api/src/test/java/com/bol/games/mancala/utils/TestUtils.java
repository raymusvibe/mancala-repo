package com.bol.games.mancala.utils;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class TestUtils {

    private TestUtils () {}

    public static Reader resourceAsInputStream (Resource resource) throws IOException {
        return new InputStreamReader(resource.getInputStream(), UTF_8);
    }
}
